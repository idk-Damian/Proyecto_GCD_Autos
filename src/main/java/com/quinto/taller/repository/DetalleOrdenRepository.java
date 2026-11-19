package com.quinto.taller.repository;

import com.quinto.taller.model.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Integer> {
    boolean existsByRepuestoIdRepuesto(Integer idRepuesto);
}
