package com.quinto.taller.service;

import com.quinto.taller.dto.DetalleRequest;
import com.quinto.taller.dto.OrdenRequest;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.*;
import com.quinto.taller.repository.AutoRepository;
import com.quinto.taller.repository.MantenimientoRepository;
import com.quinto.taller.repository.SolicitudRepository;
import com.quinto.taller.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de las órdenes de trabajo (mantenimientos).
 * Al cambiar el estado de una orden se sincroniza el estado del vehículo.
 */
@Service
public class OrdenService {

    private final MantenimientoRepository mantenimientoRepository;
    private final AutoRepository autoRepository;
    private final AutoService autoService;
    private final UsuarioRepository usuarioRepository;
    private final RepuestoService repuestoService;
    private final SolicitudRepository solicitudRepository;
    private final AuditoriaService auditoria;

    public OrdenService(MantenimientoRepository mantenimientoRepository, AutoRepository autoRepository,
                        AutoService autoService, UsuarioRepository usuarioRepository,
                        RepuestoService repuestoService, SolicitudRepository solicitudRepository,
                        AuditoriaService auditoria) {
        this.solicitudRepository = solicitudRepository;
        this.auditoria = auditoria;
        this.mantenimientoRepository = mantenimientoRepository;
        this.autoRepository = autoRepository;
        this.autoService = autoService;
        this.usuarioRepository = usuarioRepository;
        this.repuestoService = repuestoService;
    }

    public List<Mantenimiento> listar() {
        return mantenimientoRepository.findAllByOrderByFechaIngresoDescIdMantenimientoDesc();
    }

    public Mantenimiento buscar(Integer id) {
        return mantenimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la orden OT-" + id));
    }

    public List<Mantenimiento> buscarPorPlaca(String placa) {
        return mantenimientoRepository.findByAutoPlacaIgnoreCaseOrderByFechaIngresoDesc(placa.trim());
    }

    public List<Mantenimiento> buscarPorAuto(Integer idAuto) {
        return mantenimientoRepository.findByAutoIdAutoOrderByFechaIngresoDesc(idAuto);
    }

    @Transactional
    public Mantenimiento crear(OrdenRequest req, String usernameActual, boolean esAdmin) {
        Mantenimiento m = new Mantenimiento();
        aplicarDatos(m, req, usernameActual, esAdmin);
        Mantenimiento guardado = mantenimientoRepository.save(m);
        sincronizarEstadoAuto(guardado);

        // Si nace de una solicitud del portal, la solicitud queda atendida con esta orden
        if (req.solicitudId() != null) {
            solicitudRepository.findById(req.solicitudId()).ifPresent(s -> {
                if (Catalogos.SOL_PENDIENTE.equals(s.getEstado())) {
                    s.setEstado(Catalogos.SOL_ATENDIDA);
                    s.setIdOrden(guardado.getIdMantenimiento());
                    s.setRespuesta("Se generó la orden OT-" + String.format("%04d", guardado.getIdMantenimiento()));
                    solicitudRepository.save(s);
                }
            });
        }
        auditoria.registrar("CREAR", "Orden", codigo(guardado), guardado.getTipoMantenimiento() + " · "
                + guardado.getAuto().getPlaca() + " · " + guardado.getEstado() + " · $" + guardado.getCosto().toPlainString());
        return guardado;
    }

    @Transactional
    public Mantenimiento actualizar(Integer id, OrdenRequest req, String usernameActual, boolean esAdmin) {
        Mantenimiento m = buscar(id);
        if (Catalogos.ORDEN_FINALIZADO.equals(m.getEstado()) && !esAdmin) {
            throw new ConflictoException("La orden ya está finalizada; solo el administrador puede modificarla");
        }
        BigDecimal costoAnterior = m.getCosto();
        aplicarDatos(m, req, usernameActual, esAdmin);
        // Si cambia el total, la aprobación previa del cliente deja de valer
        if (Boolean.TRUE.equals(m.getAprobadoCliente()) && costoAnterior != null
                && costoAnterior.compareTo(m.getCosto()) != 0) {
            m.setAprobadoCliente(null);
            m.setFechaAprobacion(null);
        }
        Mantenimiento guardado = mantenimientoRepository.save(m);
        sincronizarEstadoAuto(guardado);
        auditoria.registrar("ACTUALIZAR", "Orden", codigo(guardado), guardado.getAuto().getPlaca() + " · "
                + guardado.getEstado() + " · total $" + guardado.getCosto().toPlainString());
        return guardado;
    }

    @Transactional
    public Mantenimiento cambiarEstado(Integer id, String estado, String usernameActual, boolean esAdmin) {
        Mantenimiento m = buscar(id);
        if (Catalogos.ORDEN_FINALIZADO.equals(m.getEstado()) && !esAdmin) {
            throw new ConflictoException("La orden ya está finalizada; solo el administrador puede reabrirla");
        }
        m.setEstado(validarEstado(estado));
        actualizarFechaFinalizacion(m);
        if (m.getMecanico() == null && !esAdmin) {
            usuarioRepository.findByUsername(usernameActual).ifPresent(m::setMecanico);
        }
        Mantenimiento guardado = mantenimientoRepository.save(m);
        sincronizarEstadoAuto(guardado);
        auditoria.registrar("ESTADO", "Orden", codigo(guardado), guardado.getAuto().getPlaca() + " → " + guardado.getEstado());
        return guardado;
    }

    /** El cliente aprueba el presupuesto desde el portal. */
    @Transactional
    public Mantenimiento aprobarPresupuesto(Integer id) {
        Mantenimiento m = buscar(id);
        if (Catalogos.ORDEN_FINALIZADO.equals(m.getEstado())) {
            throw new ConflictoException("La orden ya está finalizada");
        }
        if (Boolean.TRUE.equals(m.getAprobadoCliente())) {
            throw new ConflictoException("El presupuesto ya fue aprobado");
        }
        if (m.getCosto() == null || m.getCosto().signum() <= 0) {
            throw new ConflictoException("La orden aún no tiene un presupuesto para aprobar");
        }
        m.setAprobadoCliente(true);
        m.setFechaAprobacion(java.time.LocalDateTime.now());
        auditoria.registrar("APROBAR", "Orden", codigo(m), "Cliente aprobó el presupuesto de $" + m.getCosto().toPlainString());
        return mantenimientoRepository.save(m);
    }

    @Transactional
    public void eliminar(Integer id) {
        Mantenimiento m = buscar(id);
        devolverStock(m);
        mantenimientoRepository.delete(m);
        auditoria.registrar("ELIMINAR", "Orden", codigo(m), m.getAuto().getPlaca() + " · $" + m.getCosto());
    }

    // ------------------------------------------------------------------

    private void aplicarDatos(Mantenimiento m, OrdenRequest req, String usernameActual, boolean esAdmin) {
        Auto auto = autoService.buscar(req.autoId());
        m.setAuto(auto);
        m.setFechaIngreso(req.fechaIngreso() != null ? req.fechaIngreso()
                : (m.getFechaIngreso() != null ? m.getFechaIngreso() : LocalDate.now()));

        if (!Catalogos.TIPOS_MANTENIMIENTO.contains(req.tipoMantenimiento())) {
            throw new IllegalArgumentException("Tipo de mantenimiento no válido: " + req.tipoMantenimiento());
        }
        m.setTipoMantenimiento(req.tipoMantenimiento());
        m.setEstado(validarEstado(req.estado()));
        actualizarFechaFinalizacion(m);

        // Kilometraje de ingreso: si es mayor al registrado, actualiza la ficha del vehículo
        Integer km = req.kilometraje() != null ? req.kilometraje()
                : (m.getKilometraje() != null ? m.getKilometraje() : auto.getKilometraje());
        m.setKilometraje(km);
        if (km != null && (auto.getKilometraje() == null || km > auto.getKilometraje())) {
            auto.setKilometraje(km);
        }

        m.setDiagnostico(textoONull(req.diagnostico()));
        m.setTrabajosRealizados(textoONull(req.trabajosRealizados()));

        // Mecánico responsable
        if (req.mecanicoId() != null) {
            Usuario mec = usuarioRepository.findById(req.mecanicoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el mecánico seleccionado"));
            if (!Catalogos.ROL_MECANICO.equals(mec.getRol())) {
                throw new IllegalArgumentException("El usuario seleccionado no es un mecánico");
            }
            m.setMecanico(mec);
        } else if (!esAdmin) {
            // Si un mecánico registra la orden sin asignar a nadie, queda a su nombre
            usuarioRepository.findByUsername(usernameActual).ifPresent(m::setMecanico);
        } else {
            m.setMecanico(null);
        }

        // Líneas de detalle (repuestos y mano de obra).
        // Primero se devuelve al inventario lo que la orden tenía, luego se descuenta lo nuevo.
        devolverStock(m);
        m.getDetalles().clear();
        if (req.detalles() != null) {
            for (DetalleRequest d : req.detalles()) {
                if (!Catalogos.TIPOS_DETALLE.contains(d.tipo())) {
                    throw new IllegalArgumentException("Tipo de línea no válido: " + d.tipo());
                }
                DetalleOrden det = new DetalleOrden();
                det.setTipo(d.tipo());
                det.setDescripcion(d.descripcion().trim());
                det.setCantidad(d.cantidad());
                det.setPrecioUnitario(d.precioUnitario().setScale(2, RoundingMode.HALF_UP));
                if (d.repuestoId() != null) {
                    if (!Catalogos.DETALLE_REPUESTO.equals(d.tipo())) {
                        throw new IllegalArgumentException("Solo las líneas de tipo repuesto pueden vincularse al inventario");
                    }
                    Repuesto rep = repuestoService.buscar(d.repuestoId());
                    repuestoService.descontar(rep, d.cantidad());
                    det.setRepuesto(rep);
                }
                det.setMantenimiento(m);
                m.getDetalles().add(det);
            }
        }

        // Total: suma de líneas; si no hay líneas se usa el costo manual
        if (!m.getDetalles().isEmpty()) {
            BigDecimal total = m.getDetalles().stream()
                    .map(DetalleOrden::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            m.setCosto(total.setScale(2, RoundingMode.HALF_UP));
        } else {
            m.setCosto(req.costo() != null ? req.costo().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        }
    }

    /**
     * Regla de negocio: el estado de la orden mueve el estado del vehículo.
     * Pendiente   -> el auto vuelve a "Ingresado" si estaba Listo/Entregado (reingreso)
     * En Proceso  -> "En Reparación"
     * Finalizado  -> "Listo" (si aún no fue entregado)
     */
    private void sincronizarEstadoAuto(Mantenimiento m) {
        Auto auto = m.getAuto();
        String actual = auto.getEstadoTaller();
        switch (m.getEstado()) {
            case Catalogos.ORDEN_PENDIENTE -> {
                if (actual == null || Catalogos.AUTO_LISTO.equals(actual) || Catalogos.AUTO_ENTREGADO.equals(actual)) {
                    auto.setEstadoTaller(Catalogos.AUTO_INGRESADO);
                }
            }
            case Catalogos.ORDEN_EN_PROCESO -> auto.setEstadoTaller(Catalogos.AUTO_REPARACION);
            case Catalogos.ORDEN_FINALIZADO -> {
                if (!Catalogos.AUTO_ENTREGADO.equals(actual)) {
                    auto.setEstadoTaller(Catalogos.AUTO_LISTO);
                }
            }
            default -> { }
        }
        autoRepository.save(auto);
    }

    private String codigo(Mantenimiento m) {
        return "OT-" + String.format("%04d", m.getIdMantenimiento());
    }

    /** Registra (o limpia) la fecha en que la orden se finalizó. */
    private void actualizarFechaFinalizacion(Mantenimiento m) {
        if (Catalogos.ORDEN_FINALIZADO.equals(m.getEstado())) {
            if (m.getFechaFinalizacion() == null) m.setFechaFinalizacion(LocalDate.now());
        } else {
            m.setFechaFinalizacion(null);
        }
    }

    /** Devuelve al inventario los repuestos consumidos por la orden. */
    private void devolverStock(Mantenimiento m) {
        for (DetalleOrden d : m.getDetalles()) {
            if (d.getRepuesto() != null) {
                repuestoService.devolver(d.getRepuesto(), d.getCantidad());
            }
        }
    }

    private String validarEstado(String estado) {
        if (!Catalogos.ESTADOS_ORDEN.contains(estado)) {
            throw new IllegalArgumentException("Estado de orden no válido: " + estado);
        }
        return estado;
    }

    private String textoONull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
