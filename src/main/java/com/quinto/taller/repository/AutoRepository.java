package com.quinto.taller.repository;

import com.quinto.taller.model.Auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoRepository extends JpaRepository<Auto, Integer> {
    Optional<Auto> findByPlacaIgnoreCase(String placa);
    boolean existsByPlacaIgnoreCase(String placa);
    List<Auto> findByClienteCedula(String cedula);
    List<Auto> findByEstadoTaller(String estadoTaller);
}
