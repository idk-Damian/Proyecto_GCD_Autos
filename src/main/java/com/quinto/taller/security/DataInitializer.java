package com.quinto.taller.security;

import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.Repuesto;
import com.quinto.taller.model.Usuario;
import com.quinto.taller.repository.ClienteRepository;
import com.quinto.taller.repository.RepuestoRepository;
import com.quinto.taller.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RepuestoRepository repuestoRepository;
    private final ClienteRepository clienteRepository;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           RepuestoRepository repuestoRepository, ClienteRepository clienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.repuestoRepository = repuestoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "admin123", Catalogos.ROL_ADMIN, "Administrador");
        crearSiNoExiste("mecanico", "mecanico123", Catalogos.ROL_MECANICO, "Mecánico General");
        cargarInventarioInicial();
        crearClienteDemo();
    }

    /**
     * Cuenta de demostración del portal: usuario "cliente" / "cliente123",
     * vinculada al primer cliente registrado (solo si aún no existe).
     */
    private void crearClienteDemo() {
        if (usuarioRepository.findByUsername("cliente").isPresent()) return;
        clienteRepository.findAll().stream()
                .filter(c -> !usuarioRepository.existsByClienteCedula(c.getCedula()))
                .min(java.util.Comparator.comparing(com.quinto.taller.model.Cliente::getCedula))
                .ifPresent(c -> {
                    Usuario u = new Usuario();
                    u.setUsername("cliente");
                    u.setPassword(passwordEncoder.encode("cliente123"));
                    u.setRol(Catalogos.ROL_CLIENTE);
                    u.setNombreCompleto(c.getNombre() + " " + c.getApellido());
                    u.setCliente(c);
                    usuarioRepository.save(u);
                    System.out.println(">>> Cuenta demo de cliente 'cliente' vinculada a " + u.getNombreCompleto());
                });
    }

    /** Repuestos de ejemplo solo si el inventario está vacío. */
    private void cargarInventarioInicial() {
        if (repuestoRepository.count() > 0) return;
        Object[][] datos = {
                {"ACE-1040", "Aceite de motor 10W-40 (galón)", "galón", "12", "4", "28.50"},
                {"ACE-5W30", "Aceite sintético 5W-30 (galón)", "galón", "8", "3", "42.00"},
                {"FIL-ACE01", "Filtro de aceite", "unidad", "20", "5", "8.50"},
                {"FIL-AIR01", "Filtro de aire", "unidad", "10", "3", "14.00"},
                {"FIL-COMB1", "Filtro de combustible", "unidad", "6", "2", "18.00"},
                {"FRE-PDEL", "Pastillas de freno delanteras", "juego", "6", "2", "45.00"},
                {"FRE-LIQ", "Líquido de frenos DOT 4", "litro", "5", "2", "9.75"},
                {"BUJ-STD", "Bujía estándar", "unidad", "16", "8", "6.50"},
                {"REF-1GL", "Refrigerante (galón)", "galón", "3", "3", "16.00"},
                {"EMB-KIT", "Kit de embrague", "kit", "1", "1", "210.00"}
        };
        for (Object[] d : datos) {
            Repuesto r = new Repuesto();
            r.setCodigo((String) d[0]);
            r.setNombre((String) d[1]);
            r.setUnidad((String) d[2]);
            r.setStock(new BigDecimal((String) d[3]));
            r.setStockMinimo(new BigDecimal((String) d[4]));
            r.setPrecioVenta(new BigDecimal((String) d[5]));
            repuestoRepository.save(r);
        }
        System.out.println(">>> Inventario de ejemplo cargado.");
    }

    private void crearSiNoExiste(String username, String password, String rol, String nombre) {
        usuarioRepository.findByUsername(username).ifPresentOrElse(u -> {
            // Completa el nombre en usuarios creados antes de que existiera la columna
            if (u.getNombreCompleto() == null) {
                u.setNombreCompleto(nombre);
                usuarioRepository.save(u);
            }
        }, () -> {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRol(rol);
            u.setNombreCompleto(nombre);
            usuarioRepository.save(u);
            System.out.println(">>> Usuario por defecto '" + username + "' creado.");
        });
    }
}
