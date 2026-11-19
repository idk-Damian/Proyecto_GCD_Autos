package com.quinto.taller.service;

import com.quinto.taller.dto.AutoRequest;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.Auto;
import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.repository.AutoRepository;
import com.quinto.taller.repository.SolicitudRepository;
import com.quinto.taller.util.ValidadorEcuador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AutoService {

    private final AutoRepository autoRepository;
    private final ClienteService clienteService;
    private final SolicitudRepository solicitudRepository;
    private final AuditoriaService auditoria;

    public AutoService(AutoRepository autoRepository, ClienteService clienteService,
                       SolicitudRepository solicitudRepository, AuditoriaService auditoria) {
        this.autoRepository = autoRepository;
        this.clienteService = clienteService;
        this.solicitudRepository = solicitudRepository;
        this.auditoria = auditoria;
    }

    public List<Auto> listar() {
        return autoRepository.findAll();
    }

    public Auto buscar(Integer idAuto) {
        return autoRepository.findById(idAuto)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el vehículo con ID " + idAuto));
    }

    public Auto buscarPorPlaca(String placa) {
        String p = ValidadorEcuador.normalizarPlaca(placa);
        return autoRepository.findByPlacaIgnoreCase(p)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró un vehículo con la placa " + p));
    }

    public List<Auto> buscarPorCliente(String cedula) {
        return autoRepository.findByClienteCedula(cedula);
    }

    public List<Auto> buscarPorEstado(String estado) {
        return autoRepository.findByEstadoTaller(estado);
    }

    @Transactional
    public Auto crear(AutoRequest req) {
        String placa = validarPlaca(req.placa());
        if (autoRepository.existsByPlacaIgnoreCase(placa)) {
            throw new ConflictoException("Ya existe un vehículo registrado con la placa " + placa);
        }
        Auto a = new Auto();
        a.setPlaca(placa);
        a.setCliente(clienteService.buscar(req.clienteCedula()));
        copiarDatos(req, a);
        if (a.getEstadoTaller() == null) a.setEstadoTaller(Catalogos.AUTO_INGRESADO);
        Auto guardado = autoRepository.save(a);
        auditoria.registrar("CREAR", "Vehiculo", placa, a.getMarca() + " " + a.getModelo()
                + " de " + a.getCliente().getNombre() + " " + a.getCliente().getApellido());
        return guardado;
    }

    /**
     * Actualiza la ficha cargando la entidad existente (no se pierde el historial).
     * Solo el administrador puede cambiar el propietario o la placa.
     */
    @Transactional
    public Auto actualizar(Integer idAuto, AutoRequest req, boolean esAdmin) {
        Auto a = buscar(idAuto);

        if (esAdmin) {
            String placa = validarPlaca(req.placa());
            if (!placa.equalsIgnoreCase(a.getPlaca()) && autoRepository.existsByPlacaIgnoreCase(placa)) {
                throw new ConflictoException("Ya existe un vehículo registrado con la placa " + placa);
            }
            a.setPlaca(placa);
            if (!req.clienteCedula().equals(a.getCliente() != null ? a.getCliente().getCedula() : null)) {
                Cliente nuevo = clienteService.buscar(req.clienteCedula());
                a.setCliente(nuevo);
            }
        }
        copiarDatos(req, a);
        auditoria.registrar("ACTUALIZAR", "Vehiculo", a.getPlaca(), "Ficha del vehículo " + a.getMarca() + " " + a.getModelo());
        return autoRepository.save(a);
    }

    @Transactional
    public Auto actualizarEstado(Integer idAuto, String estado, Integer kilometraje) {
        Auto a = buscar(idAuto);
        if (estado != null && !estado.isBlank()) {
            a.setEstadoTaller(validarEstado(estado));
        }
        if (kilometraje != null) {
            if (a.getKilometraje() != null && kilometraje < a.getKilometraje()) {
                throw new IllegalArgumentException("El kilometraje no puede ser menor al registrado ("
                        + a.getKilometraje() + " km)");
            }
            a.setKilometraje(kilometraje);
        }
        auditoria.registrar("ESTADO", "Vehiculo", a.getPlaca(), "Estado: " + a.getEstadoTaller()
                + (kilometraje != null ? " · km: " + kilometraje : ""));
        return autoRepository.save(a);
    }

    @Transactional
    public void eliminar(Integer idAuto) {
        Auto a = buscar(idAuto);
        solicitudRepository.deleteByAutoIdAuto(idAuto);
        autoRepository.delete(a);
        auditoria.registrar("ELIMINAR", "Vehiculo", a.getPlaca(), a.getMarca() + " " + a.getModelo() + " (con su historial)");
    }

    private void copiarDatos(AutoRequest req, Auto a) {
        a.setMarca(req.marca().trim());
        a.setModelo(req.modelo().trim());
        a.setAnio(req.anio());
        a.setColor(req.color() == null || req.color().isBlank() ? null : req.color().trim());
        a.setKilometraje(req.kilometraje());
        if (req.estadoTaller() != null && !req.estadoTaller().isBlank()) {
            a.setEstadoTaller(validarEstado(req.estadoTaller()));
        }
    }

    private String validarPlaca(String placa) {
        String p = ValidadorEcuador.normalizarPlaca(placa);
        if (!ValidadorEcuador.placaValida(p)) {
            throw new IllegalArgumentException("La placa " + placa + " no tiene un formato válido (ej. ABC-1234 o AB123C)");
        }
        return p;
    }

    private String validarEstado(String estado) {
        if (!Catalogos.ESTADOS_AUTO.contains(estado)) {
            throw new IllegalArgumentException("Estado de vehículo no válido: " + estado);
        }
        return estado;
    }
}
