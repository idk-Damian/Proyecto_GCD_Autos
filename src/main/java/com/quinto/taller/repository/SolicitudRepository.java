package com.quinto.taller.repository;

import com.quinto.taller.model.SolicitudServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<SolicitudServicio, Integer> {
    List<SolicitudServicio> findAllByOrderByFechaSolicitudDesc();
    List<SolicitudServicio> findByClienteCedulaOrderByFechaSolicitudDesc(String cedula);
    void deleteByAutoIdAuto(Integer idAuto);
    void deleteByClienteCedula(String cedula);
}
