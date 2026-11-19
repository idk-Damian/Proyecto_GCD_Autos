package com.quinto.taller.repository;

import com.quinto.taller.model.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Integer> {
    boolean existsByCodigoIgnoreCase(String codigo);
    List<Repuesto> findAllByOrderByNombreAsc();
}
