package com.quinto.taller.service;

import com.quinto.taller.dto.RepuestoRequest;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.Repuesto;
import com.quinto.taller.repository.DetalleOrdenRepository;
import com.quinto.taller.repository.RepuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Inventario de repuestos: altas, edición, entradas de stock y movimientos por órdenes.
 */
@Service
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final AuditoriaService auditoria;

    public RepuestoService(RepuestoRepository repuestoRepository, DetalleOrdenRepository detalleOrdenRepository,
                           AuditoriaService auditoria) {
        this.repuestoRepository = repuestoRepository;
        this.detalleOrdenRepository = detalleOrdenRepository;
        this.auditoria = auditoria;
    }

    public List<Repuesto> listar() {
        return repuestoRepository.findAllByOrderByNombreAsc();
    }

    public Repuesto buscar(Integer id) {
        return repuestoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el repuesto con ID " + id));
    }

    @Transactional
    public Repuesto crear(RepuestoRequest req) {
        String codigo = req.codigo().trim().toUpperCase(Locale.ROOT);
        if (repuestoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("Ya existe un repuesto con el código " + codigo);
        }
        Repuesto r = new Repuesto();
        r.setCodigo(codigo);
        r.setStock(escala(req.stock() != null ? req.stock() : BigDecimal.ZERO));
        copiar(req, r);
        Repuesto guardado = repuestoRepository.save(r);
        auditoria.registrar("CREAR", "Repuesto", codigo, r.getNombre() + " · stock inicial " + r.getStock().toPlainString());
        return guardado;
    }

    /** Edita los datos del repuesto. El stock NO se modifica aquí (se usa una entrada de stock). */
    @Transactional
    public Repuesto actualizar(Integer id, RepuestoRequest req) {
        Repuesto r = buscar(id);
        String codigo = req.codigo().trim().toUpperCase(Locale.ROOT);
        if (!codigo.equalsIgnoreCase(r.getCodigo()) && repuestoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflictoException("Ya existe un repuesto con el código " + codigo);
        }
        r.setCodigo(codigo);
        copiar(req, r);
        auditoria.registrar("ACTUALIZAR", "Repuesto", codigo, r.getNombre() + " · precio " + r.getPrecioVenta().toPlainString());
        return repuestoRepository.save(r);
    }

    /** Entrada (cantidad positiva) o ajuste negativo de stock. */
    @Transactional
    public Repuesto ajustarStock(Integer id, BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste debe ser distinta de cero");
        }
        Repuesto r = buscar(id);
        BigDecimal nuevo = r.getStock().add(cantidad);
        if (nuevo.signum() < 0) {
            throw new ConflictoException("El ajuste dejaría el stock en negativo (disponible: "
                    + r.getStock().stripTrailingZeros().toPlainString() + ")");
        }
        r.setStock(escala(nuevo));
        auditoria.registrar("STOCK", "Repuesto", r.getCodigo(), (cantidad.signum() > 0 ? "Entrada de " : "Ajuste de ")
                + cantidad.stripTrailingZeros().toPlainString() + " " + r.getUnidad() + " · nuevo stock "
                + r.getStock().stripTrailingZeros().toPlainString());
        return repuestoRepository.save(r);
    }

    @Transactional
    public void eliminar(Integer id) {
        Repuesto r = buscar(id);
        if (detalleOrdenRepository.existsByRepuestoIdRepuesto(id)) {
            throw new ConflictoException("El repuesto ya se usó en órdenes de trabajo; no se puede eliminar");
        }
        repuestoRepository.delete(r);
        auditoria.registrar("ELIMINAR", "Repuesto", r.getCodigo(), r.getNombre());
    }

    /* ---------- Movimientos usados por OrdenService ---------- */

    /** Descuenta stock al registrar un repuesto en una orden. */
    void descontar(Repuesto r, BigDecimal cantidad) {
        if (r.getStock().compareTo(cantidad) < 0) {
            throw new ConflictoException("Stock insuficiente de " + r.getNombre() + " (disponible: "
                    + r.getStock().stripTrailingZeros().toPlainString() + " " + r.getUnidad() + ")");
        }
        r.setStock(escala(r.getStock().subtract(cantidad)));
        repuestoRepository.save(r);
    }

    /** Devuelve stock cuando se quita una línea o se elimina una orden. */
    void devolver(Repuesto r, BigDecimal cantidad) {
        r.setStock(escala(r.getStock().add(cantidad)));
        repuestoRepository.save(r);
    }

    private void copiar(RepuestoRequest req, Repuesto r) {
        r.setNombre(req.nombre().trim());
        r.setUnidad(req.unidad().trim().toLowerCase(Locale.ROOT));
        r.setStockMinimo(escala(req.stockMinimo()));
        r.setPrecioVenta(escala(req.precioVenta()));
    }

    private BigDecimal escala(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
