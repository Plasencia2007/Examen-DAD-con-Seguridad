package com.plasencia.ms_lib_api_gateway.config;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Rutas del API Gateway (Spring Cloud Gateway Server WebMvc / variante SERVLET).
 *
 * MECANISMO ELEGIDO: enfoque FUNCIONAL con RouterFunction<ServerResponse>.
 *
 * Verificado inspeccionando el jar real (Spring Cloud 2025.0.3 -> Gateway 4.3.4):
 *  - El artefacto 'spring-cloud-gateway-server-webmvc' es solo un jar de relocacion
 *    (contiene unicamente un Marker.class) y depende de
 *    'spring-cloud-gateway-server-mvc', que es donde viven las clases reales.
 *    Paquete base real: org.springframework.cloud.gateway.server.mvc.*
 *  - org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions
 *      public static RouterFunctions.Builder route(String)
 *      public static <T extends ServerResponse> RouterFunction<T> route(RequestPredicate, HandlerFunction<T>)
 *  - org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions
 *      public static HandlerFunction<ServerResponse> http(String)   <- acepta la URI directa
 *      public static HandlerFunction<ServerResponse> http(java.net.URI)
 *    => No hace falta un filtro 'before' para fijar la URI: http(String) ya la acepta.
 *  - org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates
 *      public static RequestPredicate path(String)
 *      public static RequestPredicate path(String...)  <- varargs: casa con cualquiera
 *
 * Se usan URIs DIRECTAS (http://localhost:8081 y http://localhost:8082) en lugar de
 * 'lb://' para no depender de Eureka en el enrutado y que sea mas robusto.
 */
@Configuration
public class RoutesConfig {

    /**
     * Ruta hacia ms-seguridad: /auth/** y /seguridad/** -> http://localhost:8081
     */
    @Bean
    public RouterFunction<ServerResponse> rutaMsSeguridad() {
        return route("ms-seguridad")
                .route(path("/auth/**", "/seguridad/**"), http("http://localhost:8081"))
                .build();
    }

    /**
     * Ruta hacia ms-reserva: /reservas/** -> http://localhost:8082
     */
    @Bean
    public RouterFunction<ServerResponse> rutaMsReserva() {
        return route("ms-reserva")
                .route(path("/reservas/**"), http("http://localhost:8082"))
                .build();
    }
}
