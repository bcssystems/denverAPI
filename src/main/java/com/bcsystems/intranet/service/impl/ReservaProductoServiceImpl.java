package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.Caja;
import com.bcsystems.intranet.domain.InventarioSucursal;
import com.bcsystems.intranet.domain.ReservaProducto;
import com.bcsystems.intranet.dto.ReservaProductoResponse;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.CajaRepository;
import com.bcsystems.intranet.repository.InventarioSucursalRepository;
import com.bcsystems.intranet.repository.ReservaProductoRepository;
import com.bcsystems.intranet.service.ReservaProductoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaProductoServiceImpl implements ReservaProductoService {

    private final ReservaProductoRepository reservaProductoRepository;
    private final CajaRepository cajaRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;

    public ReservaProductoServiceImpl(ReservaProductoRepository reservaProductoRepository,
                                      CajaRepository cajaRepository,
                                      InventarioSucursalRepository inventarioSucursalRepository) {
        this.reservaProductoRepository = reservaProductoRepository;
        this.cajaRepository = cajaRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
    }

    @Override
    @Transactional
    public void reservar(Integer idCaja, Integer idProducto, Integer cantidad) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));

        validarStockDisponible(idProducto, caja.getSucursal().getIdSucursal(), idCaja, cantidad);

        ReservaProducto reserva = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .orElse(ReservaProducto.builder()
                        .caja(caja)
                        .idProducto(idProducto)
                        .build());

        reserva.setCantidad(cantidad);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setExpiraEn(LocalDateTime.now().plusMinutes(30));
        reservaProductoRepository.save(reserva);
    }

    @Override
    @Transactional
    public void quitarReserva(Integer idCaja, Integer idProducto) {
        reservaProductoRepository.findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .ifPresent(reservaProductoRepository::delete);
    }

    @Override
    @Transactional
    public void actualizarCantidad(Integer idCaja, Integer idProducto, Integer cantidad) {
        ReservaProducto reserva = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCaja, idProducto)
                .orElseThrow(() -> new NotFoundException("No hay reserva activa para este producto"));

        Caja caja = reserva.getCaja();
        validarStockDisponible(idProducto, caja.getSucursal().getIdSucursal(), idCaja, cantidad);

        reserva.setCantidad(cantidad);
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setExpiraEn(LocalDateTime.now().plusMinutes(30));
        reservaProductoRepository.save(reserva);
    }

    @Override
    @Transactional
    public void limpiarReservas(Integer idCaja) {
        reservaProductoRepository.deleteByCajaIdCaja(idCaja);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaProductoResponse> obtenerReservasPorSucursal(Integer idSucursal) {
        return reservaProductoRepository.findByCajaSucursalIdSucursal(idSucursal)
                .stream()
                .map(r -> new ReservaProductoResponse(r.getIdReserva(), r.getCaja().getIdCaja(),
                        r.getIdProducto(), r.getCantidad()))
                .toList();
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void limpiarExpiradas() {
        List<ReservaProducto> expiradas = reservaProductoRepository
                .findByExpiraEnBefore(LocalDateTime.now());
        if (!expiradas.isEmpty()) {
            reservaProductoRepository.deleteAll(expiradas);
        }
    }

    private void validarStockDisponible(Integer idProducto, Integer idSucursal,
                                         Integer idCajaExcluir, Integer nuevaCantidad) {
        Integer reservadoPorOtros = reservaProductoRepository
                .sumCantidadReservada(idProducto, idSucursal, LocalDateTime.now());

        Integer reservadoPorEstaCaja = reservaProductoRepository
                .findByCajaIdCajaAndIdProducto(idCajaExcluir, idProducto)
                .map(ReservaProducto::getCantidad)
                .orElse(0);

        Integer totalReservado = reservadoPorOtros - reservadoPorEstaCaja + nuevaCantidad;

        InventarioSucursal inv = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursal)
                .orElse(null);

        int stockDisponible = inv != null ? inv.getStock() : 0;

        if (totalReservado > stockDisponible) {
            throw new InvalidEntryException("Stock insuficiente en la sucursal"
                    + " (disponible: " + stockDisponible
                    + ", solicitado: " + totalReservado + ")");
        }
    }
}
