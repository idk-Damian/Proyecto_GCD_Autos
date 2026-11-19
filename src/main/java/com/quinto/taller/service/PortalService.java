package com.quinto.taller.service;

import com.quinto.taller.dto.*;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.*;
import com.quinto.taller.repository.AutoRepository;
import com.quinto.taller.repository.MantenimientoRepository;
import com.quinto.taller.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Operaciones del portal del cliente. Todo se filtra por el cliente dueño de la sesión:
 * un cliente nunca puede ver ni tocar vehículos u órdenes de otro.
 */
@Service
public class PortalService {

    private final UsuarioRepository usuarioRepository;
    private final AutoRepository autoRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final RecordatorioService recordatorioService;
    private final SolicitudService solicitudService;
    private final OrdenService ordenService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;
    private final AutoService autoService;

    public PortalService(UsuarioRepository usuarioRepository, AutoRepository autoRepository,
                         MantenimientoRepository mantenimientoRepository, RecordatorioService recordatorioService,
                         SolicitudService solicitudService, OrdenService ordenService,
                         ClienteService clienteService, UsuarioService usuarioService,
                         AutoService autoService) {
        this.autoService = autoService;
        this.usuarioRepository = usuarioRepository;
        this.autoRepository = autoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
        this.recordatorioService = recordatorioService;
        this.solicitudService = solicitudService;
        this.ordenService = ordenService;
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    public Cliente clienteActual(String username) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (u.getCliente() == null) {
            throw new AccessDeniedException("La cuenta no está vinculada a un cliente");
        }
        return u.getCliente();
    }

    @Transactional(readOnly = true)
    public PortalResumen resumen(String username) {
        Cliente c = clienteActual(username);
        List<Auto> autos = autoRepository.findByClienteCedula(c.getCedula());
        Set<Integer> ids = autos.stream().map(Auto::getIdAuto).collect(Collectors.toSet());

        List<Mantenimiento> ordenes = mantenimientoRepository.findAll().stream()
                .filter(m -> m.getAuto() != null && ids.contains(m.getAuto().getIdAuto()))
                .sorted(Comparator.comparing(Mantenimiento::getFechaIngreso, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Mantenimiento::getIdMantenimiento, Comparator.reverseOrder()))
                .toList();
        // Forzar la carga del detalle dentro de la transacción
        ordenes.forEach(m -> m.getDetalles().size());

        RecordatoriosResponse rec = recordatorioService.calcular();
        List<RecordatorioDto> misRecordatorios = rec.items().stream().filter(r -> ids.contains(r.idAuto())).toList();

        return new PortalResumen(username, c, autos, ordenes, misRecordatorios,
                solicitudService.listarDeCliente(c.getCedula()), rec.intervaloKm(), rec.intervaloMeses());
    }

    @Transactional
    public Mantenimiento aprobarPresupuesto(String username, Integer idOrden) {
        Cliente c = clienteActual(username);
        Mantenimiento m = ordenService.buscar(idOrden);
        verificarDueno(c, m.getAuto());
        return ordenService.aprobarPresupuesto(idOrden);
    }

    @Transactional
    public SolicitudServicio crearSolicitud(String username, SolicitudRequest req) {
        Cliente c = clienteActual(username);
        Auto a = autoRepository.findById(req.autoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado"));
        verificarDueno(c, a);
        return solicitudService.crear(c, a, req);
    }

    @Transactional
    public SolicitudServicio cancelarSolicitud(String username, Integer idSolicitud) {
        Cliente c = clienteActual(username);
        SolicitudServicio s = solicitudService.buscar(idSolicitud);
        if (!s.getCliente().getCedula().equals(c.getCedula())) {
            throw new AccessDeniedException("La solicitud no le pertenece");
        }
        return solicitudService.cancelar(s);
    }

    /** El cliente registra un vehículo propio. Queda como "Entregado" (fuera del taller) hasta que lo lleve. */
    @Transactional
    public Auto registrarVehiculo(String username, VehiculoPortalRequest req) {
        Cliente c = clienteActual(username);
        String placa = com.quinto.taller.util.ValidadorEcuador.normalizarPlaca(req.placa());
        if (autoRepository.existsByPlacaIgnoreCase(placa)) {
            throw new com.quinto.taller.exception.ConflictoException("La placa " + placa
                    + " ya está registrada en el taller. Si el vehículo es suyo, comuníquese con el taller para vincularlo.");
        }
        return autoService.crear(new AutoRequest(placa, req.marca(), req.modelo(), req.anio(), req.color(),
                req.kilometraje(), Catalogos.AUTO_ENTREGADO, c.getCedula()));
    }

    @Transactional
    public Cliente actualizarPerfil(String username, PerfilRequest req) {
        Cliente c = clienteActual(username);
        return clienteService.actualizarContacto(c.getCedula(), req.telefono(), req.correo());
    }

    public void cambiarPassword(String username, CambioPasswordRequest req) {
        usuarioService.cambiarPassword(username, req.actual(), req.nueva());
    }

    private void verificarDueno(Cliente c, Auto a) {
        if (a == null || a.getCliente() == null || !a.getCliente().getCedula().equals(c.getCedula())) {
            throw new AccessDeniedException("El vehículo no le pertenece");
        }
    }
}
