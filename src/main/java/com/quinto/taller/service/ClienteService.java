package com.quinto.taller.service;

import com.quinto.taller.dto.ClienteRequest;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.repository.ClienteRepository;
import com.quinto.taller.repository.SolicitudRepository;
import com.quinto.taller.repository.UsuarioRepository;
import com.quinto.taller.util.ValidadorEcuador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudRepository solicitudRepository;
    private final AuditoriaService auditoria;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository,
                          SolicitudRepository solicitudRepository, AuditoriaService auditoria) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.solicitudRepository = solicitudRepository;
        this.auditoria = auditoria;
    }

    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    public Cliente buscar(String cedula) {
        return clienteRepository.findById(cedula)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con cédula " + cedula));
    }

    /** Crea un cliente nuevo. Si la cédula ya existe NO lo sobrescribe: devuelve 409. */
    @Transactional
    public Cliente crear(ClienteRequest req) {
        String cedula = req.cedula().trim();
        if (!ValidadorEcuador.cedulaValida(cedula)) {
            throw new IllegalArgumentException("La cédula " + cedula + " no es una cédula ecuatoriana válida");
        }
        if (clienteRepository.existsById(cedula)) {
            throw new ConflictoException("Ya existe un cliente registrado con la cédula " + cedula);
        }
        Cliente c = new Cliente();
        c.setCedula(cedula);
        copiarDatos(req, c);
        Cliente guardado = clienteRepository.save(c);
        auditoria.registrar("CREAR", "Cliente", cedula, "Cliente " + c.getNombre() + " " + c.getApellido());
        return guardado;
    }

    /** Actualiza solo los datos de contacto; nunca toca la lista de vehículos. */
    @Transactional
    public Cliente actualizar(String cedula, ClienteRequest req) {
        Cliente c = buscar(cedula);
        copiarDatos(req, c);
        auditoria.registrar("ACTUALIZAR", "Cliente", cedula, "Datos de " + c.getNombre() + " " + c.getApellido());
        return clienteRepository.save(c);
    }

    /** Usado por el portal: el cliente solo puede cambiar su teléfono y correo. */
    @Transactional
    public Cliente actualizarContacto(String cedula, String telefono, String correo) {
        Cliente c = buscar(cedula);
        c.setTelefono(vacioANull(telefono));
        c.setCorreo(vacioANull(correo));
        auditoria.registrar("ACTUALIZAR", "Cliente", cedula, "El cliente actualizó sus datos de contacto");
        return clienteRepository.save(c);
    }

    @Transactional
    public void eliminar(String cedula) {
        Cliente c = buscar(cedula);
        // Primero lo que referencia al cliente: solicitudes del portal y su cuenta de acceso
        solicitudRepository.deleteByClienteCedula(cedula);
        usuarioRepository.deleteByClienteCedula(cedula);
        clienteRepository.delete(c);
        auditoria.registrar("ELIMINAR", "Cliente", cedula, "Cliente " + c.getNombre() + " " + c.getApellido()
                + " (con sus vehículos e historial)");
    }

    private void copiarDatos(ClienteRequest req, Cliente c) {
        c.setNombre(req.nombre().trim());
        c.setApellido(req.apellido().trim());
        c.setTelefono(vacioANull(req.telefono()));
        c.setCorreo(vacioANull(req.correo()));
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
