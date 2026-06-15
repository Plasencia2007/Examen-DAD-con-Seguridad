# 🚌 Examen DAD — Sistema de Transporte (Dominio RESERVA) con Seguridad JWT

Arquitectura de **microservicios** con **Spring Boot 3.5.15**, **Spring Cloud 2025.0.3** y **Java 21**, protegida con **JWT firmado en RS256** (clave asimétrica RSA). El sistema implementa un **Authorization Server propio** (`ms-seguridad`) que emite los tokens y publica su clave pública vía **JWKS**, y un **Resource Server** (`ms-reserva`) que valida la firma del token y aplica **control de acceso por permisos (RBAC)**.

Persistencia en **PostgreSQL 16** sobre Docker.

---

## 📑 Tabla de contenidos

- [Arquitectura](#-arquitectura)
- [Flujo de seguridad](#-flujo-de-seguridad)
- [Requisitos previos](#-requisitos-previos)
- [Base de datos (PostgreSQL en Docker)](#-base-de-datos-postgresql-en-docker)
- [Orden de arranque](#-orden-de-arranque-muy-importante)
- [Datos iniciales (seed)](#-datos-iniciales-seed)
- [Roles y permisos](#-roles--permisos)
- [Endpoints](#-endpoints-todos-vía-el-gateway)
- [Modelo Reserva](#-modelo-reserva)
- [Pruebas con Postman](#-pruebas-con-postman)
- [Cómo cambiar de usuario en Postman](#-cómo-cambiar-de-usuario-en-postman)
- [Resolución de problemas](#-resolución-de-problemas)

---

## 🏗️ Arquitectura

El sistema está compuesto por **5 módulos** Spring Boot independientes:

| Módulo | Rol | Puerto | Base de datos | Cliente Eureka |
|---|---|:---:|---|:---:|
| `ms-lib-registry-server` | **Eureka Server** (descubrimiento de servicios) | `8761` | — | (es el server) |
| `ms-lib-config-server` | **Config Server** (modo *native*, sirve ficheros **YAML** desde `classpath:/configurations/`) | `8888` | — | ✅ |
| `ms-seguridad` | **Authorization Server**: usuarios/roles/permisos, login y **emisión del JWT (RS256)**; publica la clave pública en `/oauth2/jwks` | `8081` | `db_seguridad` | ✅ |
| `ms-reserva` | **Resource Server**: CRUD de reservas; **valida el JWT vía JWKS** y aplica RBAC por permiso | `8082` | `db_reserva` | ✅ |
| `ms-lib-api-gateway` | **API Gateway** (Spring Cloud Gateway WebMvc): **único punto de entrada**, enruta y valida el JWT | `8080` | — | ✅ |

### Diagrama

```text
                              ┌──────────────────────────────────┐
                              │      ms-lib-registry-server       │
                              │         (Eureka Server)           │
                              │            :8761                  │
                              └──────────────────────────────────┘
                                     ▲   ▲   ▲   ▲   (registro / descubrimiento)
              ┌──────────────────────┘   │   │   └───────────────────────┐
              │              ┌────────────┘   └───────────┐               │
              │              │                            │               │
   ┌──────────────────┐     │                            │     ┌────────────────────┐
   │ ms-lib-config-   │     │                            │     │   (todos los demás  │
   │     server       │─────┘   (config centralizada)    └─────│   leen su config)   │
   │     :8888        │ ........................................│                     │
   └──────────────────┘                                        └────────────────────┘

                          ┌─────────────────────────────┐
        Cliente   ──────► │     ms-lib-api-gateway       │  ◄── ÚNICO punto de entrada
   (Postman / web)        │  (Spring Cloud Gateway)      │
        :8080             │            :8080             │
                          │  • Enruta a los servicios    │
                          │  • Valida la firma del JWT   │
                          └───────────────┬─────────────┘
                                          │
                  ┌───────────────────────┴────────────────────────┐
                  │                                                 │
                  ▼                                                 ▼
      ┌────────────────────────┐                       ┌────────────────────────┐
      │      ms-seguridad      │                       │       ms-reserva       │
      │ (Authorization Server) │                       │   (Resource Server)    │
      │         :8081          │                       │         :8082          │
      │                        │   1) JWKS (clave pública)                       │
      │  • Login /auth/login   │ ◄──────────────────────  • Valida firma JWT     │
      │  • Emite JWT (RS256)   │   GET /oauth2/jwks     │  • RBAC por permiso     │
      │  • /oauth2/jwks        │ ───────────────────────►  • CRUD /reservas       │
      │  • CRUD usuario/rol/   │                       │                         │
      │    permiso             │                       │                         │
      └───────────┬────────────┘                       └───────────┬────────────┘
                  │                                                 │
                  ▼                                                 ▼
         ┌─────────────────┐                              ┌─────────────────┐
         │   db_seguridad  │                              │    db_reserva   │
         └─────────────────┘                              └─────────────────┘
                  │                                                 │
                  └───────────── PostgreSQL 16 (Docker) ───────────┘
                              postgres-local @ localhost:5433
```

### ⚙️ Configuración (YAML)

Toda la configuración está escrita en **YAML**. Cada módulo usa su `application.yml`, y el **Config Server** sirve la configuración centralizada como ficheros `.yml` desde `ms-lib-config-server/src/main/resources/configurations/`:

- `application.yml` (configuración común a todos los servicios)
- `ms-seguridad.yml`
- `ms-reserva.yml`
- `ms-lib-api-gateway.yml`

---

## 🔐 Flujo de seguridad

1. El cliente hace **`POST /auth/login`** contra `ms-seguridad` (a través del **gateway**) enviando `username` y `password`.
2. `ms-seguridad` valida las credenciales y **emite un JWT firmado con RSA (RS256)** que incluye el **rol** (uno por usuario) y los **permisos** del usuario.
3. El cliente envía ese token en cada petición protegida mediante la cabecera:
   ```http
   Authorization: Bearer <accessToken>
   ```
4. El **gateway** y **`ms-reserva`** **validan la firma** del token usando la **clave pública** publicada por `ms-seguridad` en el endpoint **JWKS** (`/oauth2/jwks`). No comparten la clave privada: solo `ms-seguridad` puede firmar.
5. La autorización se resuelve **según los roles/permisos contenidos en el token** (RBAC). Cada endpoint de reservas exige un permiso concreto; si el token no lo contiene, la respuesta es **`403 Forbidden`**.

```text
Cliente ──POST /auth/login──► Gateway ──► ms-seguridad
   ◄──────── { accessToken (JWT RS256), roles, permisos } ───────

Cliente ──GET /reservas + Authorization: Bearer <JWT>──► Gateway ─(valida firma JWKS)─► ms-reserva
                                                                  ms-reserva valida firma + permiso
   ◄──────── 200 OK  /  401 (token inválido)  /  403 (sin permiso) ───────
```

---

## ✅ Requisitos previos

- **Java 21** (JDK).
- **Maven**: no es necesario instalarlo; cada módulo incluye el **wrapper** (`mvnw` / `mvnw.cmd`).
- **Docker** con un contenedor de **PostgreSQL 16**.

---

## 🐘 Base de datos (PostgreSQL en Docker)

La base de datos ya corre en un contenedor llamado **`postgres-local`**:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5433` |
| Usuario | `postgres` |
| Password | `oracle123` |
| Bases de datos | `db_seguridad`, `db_reserva` |

Las bases **ya están creadas**. Si necesitas **recrearlas**, ejecuta:

```bash
docker exec -e PGPASSWORD=oracle123 postgres-local psql -U postgres -c "CREATE DATABASE db_seguridad;"
docker exec -e PGPASSWORD=oracle123 postgres-local psql -U postgres -c "CREATE DATABASE db_reserva;"
```

> 💡 Cada microservicio crea/actualiza su esquema automáticamente al arrancar (Hibernate DDL). Solo las **bases de datos** deben existir previamente.

---

## ▶️ Orden de arranque (MUY IMPORTANTE)

> ⚠️ **Respeta el orden.** Cada servicio depende del anterior (registro en Eureka y/o configuración centralizada). Arrancar fuera de orden provoca fallos de descubrimiento o de configuración.

| # | Módulo | Puerto | Espera a que esté arriba antes de continuar |
|:--:|---|:---:|---|
| 1 | `ms-lib-registry-server` | `8761` | Abre **http://localhost:8761** y comprueba que carga el panel de Eureka |
| 2 | `ms-lib-config-server` | `8888` | Que registre en Eureka |
| 3 | `ms-seguridad` | `8081` | **Al arrancar crea los datos iniciales** (permisos, roles y usuario `admin`) |
| 4 | `ms-reserva` | `8082` | Que registre en Eureka |
| 5 | `ms-lib-api-gateway` | `8080` | Punto de entrada del sistema |

### Arranque manual (un módulo a la vez)

Desde la carpeta de **cada módulo**:

```bash
# Linux / macOS
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
.\mvnw spring-boot:run
```

### Arranque automático (recomendado)

En la **raíz del proyecto** dispones de scripts que arrancan los 5 módulos **en el orden correcto**, cada uno en su propia ventana:

```powershell
# Windows (PowerShell)
.\start-all.ps1
```

```bat
:: Windows (CMD)
start-all.bat
```

> Los scripts respetan las esperas entre servicios para que cada uno se registre en Eureka antes de levantar el siguiente.

---

## 🌱 Datos iniciales (seed)

`ms-seguridad` carga estos datos **la primera vez** que arranca (de forma idempotente):

**Permisos**

- `RESERVA_LEER`
- `RESERVA_CREAR`
- `RESERVA_ACTUALIZAR`
- `RESERVA_ELIMINAR`

**Roles**

- `ADMIN` — todos los permisos.
- `OPERADOR` — `LEER`, `CREAR`, `ACTUALIZAR`.
- `USER` — solo `LEER`.

**Usuario inicial**

| Usuario | Password | Rol |
|---|---|---|
| **`admin`** | **`admin123`** | `ADMIN` |

> 🔑 Con el usuario **`admin`** se crean el resto de usuarios, roles y permisos a través de los endpoints de gestión.
>
> ℹ️ **Cada usuario tiene un único rol.** El sistema define varios roles (`ADMIN`, `OPERADOR`, `USER`), cada uno con su conjunto de permisos, pero a cada usuario se le asigna **uno solo**.

---

## 👥 Roles → Permisos

| Rol | RESERVA_LEER | RESERVA_CREAR | RESERVA_ACTUALIZAR | RESERVA_ELIMINAR |
|---|:---:|:---:|:---:|:---:|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ |
| **OPERADOR** | ✅ | ✅ | ✅ | ❌ |
| **USER** | ✅ | ❌ | ❌ | ❌ |

---

## 🌐 Endpoints (todos vía el gateway `http://localhost:8080`)

### 🔓 Autenticación (público)

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| `POST` | `/auth/login` | `{ "username", "password" }` | `{ accessToken, tokenType, expiraEn, username, roles, permisos }` |

**Ejemplo de petición:**

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Ejemplo de respuesta:**

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiraEn": "2026-06-15T13:45:00Z",
  "username": "admin",
  "roles": ["ADMIN"],
  "permisos": ["RESERVA_LEER", "RESERVA_CREAR", "RESERVA_ACTUALIZAR", "RESERVA_ELIMINAR"]
}
```

### 🛡️ Gestión de seguridad (requiere rol `ADMIN`)

> Todas estas rutas exigen un token de un usuario con rol **`ADMIN`** en la cabecera `Authorization: Bearer <token>`.

**Usuarios** — `/seguridad/usuarios`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/seguridad/usuarios` | Lista todos los usuarios |
| `GET` | `/seguridad/usuarios/{id}` | Obtiene un usuario |
| `POST` | `/seguridad/usuarios` | Crea un usuario |
| `PUT` | `/seguridad/usuarios/{id}/rol` | Cambia el rol de un usuario |
| `DELETE` | `/seguridad/usuarios/{id}` | Elimina un usuario |

> **Crear usuario** (`POST /seguridad/usuarios`). Cada usuario tiene **un solo rol** (campo `rol` como *string*):
>
> ```json
> {
>   "username": "operador1",
>   "password": "oper123",
>   "nombreCompleto": "Operador Uno",
>   "rol": "OPERADOR"
> }
> ```
>
> **Cambiar el rol** (`PUT /seguridad/usuarios/{id}/rol`):
>
> ```json
> { "rol": "USER" }
> ```

**Roles** — `/seguridad/roles`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/seguridad/roles` | Lista todos los roles |
| `POST` | `/seguridad/roles` | Crea un rol |
| `PUT` | `/seguridad/roles/{id}/permisos` | Asigna/actualiza los permisos de un rol |
| `DELETE` | `/seguridad/roles/{id}` | Elimina un rol |

**Permisos** — `/seguridad/permisos`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/seguridad/permisos` | Lista todos los permisos |
| `POST` | `/seguridad/permisos` | Crea un permiso |
| `DELETE` | `/seguridad/permisos/{id}` | Elimina un permiso |

### 🎫 Reservas (cada operación exige un permiso)

| Método | Ruta | Permiso requerido |
|---|---|---|
| `GET` | `/reservas` | `RESERVA_LEER` |
| `GET` | `/reservas/{nro}` | `RESERVA_LEER` |
| `POST` | `/reservas` | `RESERVA_CREAR` |
| `PUT` | `/reservas/{nro}` | `RESERVA_ACTUALIZAR` |
| `DELETE` | `/reservas/{nro}` | `RESERVA_ELIMINAR` |

---

## 📦 Modelo Reserva

```json
{
  "nroReser": "00000001",
  "fechaReser": "2026-06-15",
  "horaReser": "14:30:00",
  "codCli": "CLI01",
  "idProg": 5,
  "codDest": "LIM1"
}
```

| Campo | Tipo / Formato | Restricción |
|---|---|---|
| `nroReser` | `String` | Identificador, longitud **8** |
| `fechaReser` | Fecha `yyyy-MM-dd` | — |
| `horaReser` | Hora `HH:mm:ss` | — |
| `codCli` | `String` | Código de cliente, longitud **5** |
| `idProg` | `Integer` | Id de programación |
| `codDest` | `String` | Código de destino, longitud **4** |

---

## 🧪 Pruebas con Postman

1. Importa los dos ficheros de la carpeta `postman/`:
   - `postman/Examen-DAD-Seguridad.postman_collection.json`
   - `postman/Examen-DAD-Seguridad.postman_environment.json`
2. En la esquina superior derecha de Postman, **selecciona el environment** importado.
3. Ejecuta **`1. Auth > Login`**. La petición **guarda el token automáticamente** en la variable del environment (el resto de peticiones ya lo usan en la cabecera `Authorization`).
4. Ejecuta el resto de peticiones de la colección.

### Probar el control de permisos (RBAC)

El objetivo es demostrar que cada rol solo puede hacer aquello para lo que tiene permiso. Crea (con `admin`) un usuario por rol y prueba:

| Rol | `GET /reservas` | `POST /reservas` | `PUT /reservas/{nro}` | `DELETE /reservas/{nro}` |
|---|:---:|:---:|:---:|:---:|
| **USER** | ✅ `200` | ⛔ `403` | ⛔ `403` | ⛔ `403` |
| **OPERADOR** | ✅ `200` | ✅ `201` | ✅ `200` | ⛔ `403` |
| **ADMIN** | ✅ `200` | ✅ `201` | ✅ `200` | ✅ `200/204` |

Pasos sugeridos:

1. Crea un usuario con rol **`USER`** (solo `RESERVA_LEER`), loguéate con él y comprueba que **`GET /reservas` → `200`**, pero **`POST` / `PUT` / `DELETE` → `403 Forbidden`**.
2. Repite con un usuario **`OPERADOR`**: puede **crear** y **actualizar**, pero **`DELETE` → `403`**.
3. Repite con **`ADMIN`**: **todo `200/201/204`**.

> Un **token sin el permiso** necesario devuelve `403 Forbidden`; un **token ausente o inválido** (firma incorrecta o caducado) devuelve `401 Unauthorized`.

---

## 🔄 Cómo cambiar de usuario en Postman

El token está asociado al usuario con el que hiciste login. Para **probar con otro rol/usuario**:

1. Abre el **environment** seleccionado (icono del ojo 👁️ o *Environments* en la barra lateral).
2. Edita las variables **`username`** y **`password`** con las credenciales del nuevo usuario.

   | Variable | Valor de ejemplo |
   |---|---|
   | `username` | `operador1` |
   | `password` | `clave123` |

3. **Guarda** el environment.
4. Vuelve a ejecutar **`1. Auth > Login`**: se generará un **nuevo token** (con los roles/permisos del nuevo usuario) y se **sobrescribirá** la variable del token automáticamente.
5. Ya puedes ejecutar el resto de peticiones; usarán el token del nuevo usuario.

> 💡 No hace falta copiar/pegar el token a mano: el script de la petición *Login* lo guarda en el environment por ti. Solo recuerda **volver a hacer Login** cada vez que cambies de usuario.

---

## 🩹 Resolución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `401 Unauthorized` en rutas protegidas | Token ausente, caducado o firma inválida | Vuelve a ejecutar `Login` y reintenta |
| `403 Forbidden` | El usuario no tiene el permiso requerido | Usa un usuario con el rol/permiso adecuado |
| Servicios no aparecen en Eureka | Arranque fuera de orden | Arranca en el orden indicado o usa `start-all.ps1` |
| Error de conexión a BD | El contenedor `postgres-local` no está activo o faltan las bases | Verifica Docker y recrea `db_seguridad` / `db_reserva` |
| Config no se aplica | `ms-lib-config-server` no estaba arriba al iniciar otro servicio | Asegúrate de levantar el Config Server (paso 2) antes de los servicios de negocio |
