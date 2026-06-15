package com.plasencia.ms_reserva.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @Column(name = "nro_reser", length = 8)
    private String nroReser;

    @Column(name = "fecha_reser")
    private LocalDate fechaReser;

    @Column(name = "hora_reser")
    private LocalTime horaReser;

    // CHAR(5) lógico
    @Column(name = "cod_cli", length = 5)
    private String codCli;

    @Column(name = "id_prog")
    private Integer idProg;

    // CHAR(4) lógico
    @Column(name = "cod_dest", length = 4)
    private String codDest;
}
