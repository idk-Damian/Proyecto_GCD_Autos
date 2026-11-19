package com.quinto.taller.repository;

import com.quinto.taller.model.Mantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Integer> {
    List<Mantenimiento> findAllByOrderByFechaIngresoDescIdMantenimientoDesc();
    List<Mantenimiento> findByAutoPlacaIgnoreCaseOrderByFechaIngresoDesc(String placa);
    List<Mantenimiento> findByAutoIdAutoOrderByFechaIngresoDesc(Integer idAuto);
    boolean existsByMecanicoIdUsuario(Integer idUsuario);
}
