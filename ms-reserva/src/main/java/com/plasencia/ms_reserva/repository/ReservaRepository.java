package com.plasencia.ms_reserva.repository;

import com.plasencia.ms_reserva.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA de Reserva. La PK es String (nro_reser).
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {
}
