package com.quinto.taller.service;

import com.quinto.taller.dto.SolicitudRequest;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.Auto;
import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.model.SolicitudServicio;
import com.quinto.taller.repository.SolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Solicitudes de servicio (citas) creadas por los clientes desde el portal. */
@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final AuditoriaService auditoria;

    public SolicitudService(SolicitudRepository solicitudRepository, AuditoriaService auditoria) {
        this.solicitudRepository = solicitudRepository;
        this.auditoria = auditoria;
    }

    public List<SolicitudServicio> listar() {
        return solicitudRepository.findAllByOrderByFechaSolicitudDesc();
    }

    public List<SolicitudServicio> listarDeCliente(String cedula) {
        return solicitudRepository.findByClienteCedulaOrderByFechaSolicitudDesc(cedula);
    }

    public SolicitudServicio buscar(Integer id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud " + id));
    }

    @Transactional
    public SolicitudServicio crear(Cliente cliente, Auto auto, SolicitudRequest req) {
        if (req.fechaPreferida().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha preferida no puede ser anterior a hoy");
        }
        if (!List.of("Mañana", "Tarde").contains(req.franja())) {
            throw new IllegalArgumentException("Franja horaria no válida");
        }
        if (!Catalogos.TIPOS_MANTENIMIENTO.contains(req.tipoMantenimiento())) {
            throw new IllegalArgumentException("Tipo de servicio no válido");
        }
        boolean yaPendiente = listarDeCliente(cliente.getCedula()).stream()
                .anyMatch(s -> s.getAuto().getIdAuto().equals(auto.getIdAuto()) && Catalogos.SOL_PENDIENTE.equals(s.getEstado()));
        if (yaPendiente) {
            throw new ConflictoException("Ya tiene una solicitud pendiente para este vehículo");
        }
        SolicitudServicio s = new SolicitudServicio();
        s.setAuto(auto);
        s.setCliente(cliente);
        s.setFechaSolicitud(LocalDateTime.now());
        s.setFechaPreferida(req.fechaPreferida());
        s.setFranja(req.franja());
        s.setTipoMantenimiento(req.tipoMantenimiento());
        s.setDescripcion(req.descripcion().trim());
        s.setEstado(Catalogos.SOL_PENDIENTE);
        SolicitudServicio guardada = solicitudRepository.save(s);
        auditoria.registrar("CREAR", "Solicitud", guardada.getIdSolicitud(), auto.getPlaca() + " · "
                + req.tipoMantenimiento() + " · " + req.fechaPreferida() + " " + req.franja());
        return guardada;
    }

    @Transactional
    public SolicitudServicio cancelar(SolicitudServicio s) {
        if (!Catalogos.SOL_PENDIENTE.equals(s.getEstado())) {
            throw new ConflictoException("Solo se pueden cancelar solicitudes pendientes");
        }
        s.setEstado(Catalogos.SOL_CANCELADA);
        auditoria.registrar("ESTADO", "Solicitud", s.getIdSolicitud(), "Cancelada por el cliente");
        return solicitudRepository.save(s);
    }

    @Transactional
    public SolicitudServicio rechazar(Integer id, String motivo) {
        SolicitudServicio s = buscar(id);
        if (!Catalogos.SOL_PENDIENTE.equals(s.getEstado())) {
            throw new ConflictoException("La solicitud ya fue procesada");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Indique el motivo para el cliente");
        }
        s.setEstado(Catalogos.SOL_RECHAZADA);
        s.setRespuesta(motivo.trim().length() > 200 ? motivo.trim().substring(0, 200) : motivo.trim());
        auditoria.registrar("ESTADO", "Solicitud", id, "Rechazada: " + s.getRespuesta());
        return solicitudRepository.save(s);
    }
}
