package com.quinto.taller.repository;

import com.quinto.taller.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findByRolOrderByUsernameAsc(String rol);
    boolean existsByClienteCedula(String cedula);
    List<Usuario> findByClienteCedula(String cedula);
    void deleteByClienteCedula(String cedula);
}
