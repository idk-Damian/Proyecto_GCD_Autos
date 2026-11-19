package com.quinto.taller.repository;

import com.quinto.taller.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findTop1000ByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);
}
