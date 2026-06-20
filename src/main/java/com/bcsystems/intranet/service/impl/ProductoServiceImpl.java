package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.*;
import com.bcsystems.intranet.domain.en.AccionAuditoria;
import com.bcsystems.intranet.domain.en.TipoMovimiento;
import com.bcsystems.intranet.domain.en.TipoMultimedia;
import com.bcsystems.intranet.dto.MovimientoStockRequest;
import com.bcsystems.intranet.dto.ProductoRequest;
import com.bcsystems.intranet.dto.ProductoResponse;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.*;
import com.bcsystems.intranet.service.AuditoriaService;
import com.bcsystems.intranet.service.ProductoService;
import com.bcsystems.intranet.util.CodigoGeneratorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository multimediaRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final CodigoGeneratorService codigoGenerator;
    private final AuditoriaService auditoriaService;

    @Value("${app.upload.dir:./uploads/multimedia}")
    private String uploadDir;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                                ProductoMultimediaRepository multimediaRepository,
                                SucursalRepository sucursalRepository,
                                InventarioSucursalRepository inventarioSucursalRepository,
                                MovimientoStockRepository movimientoStockRepository,
                                CodigoGeneratorService codigoGenerator,
                                AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.multimediaRepository = multimediaRepository;
        this.sucursalRepository = sucursalRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.codigoGenerator = codigoGenerator;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public Page<ProductoResponse> listar(String search, Boolean activo, Integer idSucursal, Pageable pageable) {
        return productoRepository.buscarConFiltros(search, activo, idSucursal, pageable)
                .map(this::toResponse);
    }

    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Transactional
    @Override
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new InvalidEntryException("Ya existe un producto con el SKU: " + request.sku());
        }

        Producto producto = Producto.builder()
                .sku(request.sku())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio1(request.precio1())
                .precio2(request.precio2())
                .precio3(request.precio3())
                .precio4(request.precio4())
                .stockActual(request.stockActual() != null ? request.stockActual() : 0)
                .stockMinimo(request.stockMinimo())
                .stockMaximo(request.stockMaximo())
                .material(request.material())
                .numeroMolde(request.numeroMolde())
                .talla(request.talla())
                .accesorio1(request.accesorio1())
                .accesorio2(request.accesorio2())
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        producto = productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();

        if (request.idSucursal() != null) {
            Sucursal sucursal = sucursalRepository.findById(request.idSucursal())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

            InventarioSucursal inventario = InventarioSucursal.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .stock(producto.getStockActual())
                    .build();
            inventarioSucursalRepository.save(inventario);

            MovimientoStock movimiento = MovimientoStock.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .tipoMovimiento(TipoMovimiento.ENTRADA)
                    .cantidad(producto.getStockActual())
                    .stockAnterior(0)
                    .stockNuevo(producto.getStockActual())
                    .referencia("Stock inicial")
                    .usuario(usuario)
                    .observacion("Stock inicial en sucursal: " + sucursal.getNombre())
                    .build();
            movimientoStockRepository.save(movimiento);

            auditoriaService.registrarMovimiento("PRODUCTO", producto.getIdProducto(), "ENTRADA", usuario,
                    "Stock inicial de " + producto.getStockActual() + " unidades en " + sucursal.getNombre(),
                    "Stock inicial", producto.getStockActual(), 0, producto.getStockActual());
        }

        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.CREACION.name(), usuario,
                "Se creó el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    @Transactional
    @Override
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = buscarOExcepcion(id);

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio1(request.precio1());
        producto.setPrecio2(request.precio2());
        producto.setPrecio3(request.precio3());
        producto.setPrecio4(request.precio4());
        producto.setStockActual(request.stockActual() != null ? request.stockActual() : producto.getStockActual());
        producto.setStockMinimo(request.stockMinimo());
        producto.setStockMaximo(request.stockMaximo());
        producto.setMaterial(request.material());
        producto.setNumeroMolde(request.numeroMolde());
        producto.setTalla(request.talla());
        producto.setAccesorio1(request.accesorio1());
        producto.setAccesorio2(request.accesorio2());
        if (request.activo() != null) producto.setActivo(request.activo());

        producto = productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualizó el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Producto producto = buscarOExcepcion(id);
        producto.setActivo(false);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se eliminó (desactivó) el producto: " + producto.getNombre());
    }

    @Transactional
    @Override
    public ProductoResponse agregarMultimedia(Integer idProducto, MultipartFile archivo, Boolean esPrincipal) {
        Producto producto = buscarOExcepcion(idProducto);

        if (archivo.isEmpty()) {
            throw new InvalidEntryException("El archivo está vacío");
        }

        String contentType = archivo.getContentType();
        TipoMultimedia tipo;
        if (contentType != null && contentType.startsWith("video")) {
            tipo = TipoMultimedia.VIDEO;
        } else if (contentType != null && contentType.startsWith("image")) {
            tipo = TipoMultimedia.IMAGEN;
        } else {
            throw new InvalidEntryException("El archivo debe ser una imagen o video");
        }

        try {
            String extension = Objects.requireNonNull(archivo.getOriginalFilename())
                    .substring(archivo.getOriginalFilename().lastIndexOf("."));
            String nombreArchivo = UUID.randomUUID() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            if (Boolean.TRUE.equals(esPrincipal)) {
                multimediaRepository.findByProductoIdProductoAndEsPrincipalTrue(idProducto)
                        .ifPresent(m -> {
                            m.setEsPrincipal(false);
                            multimediaRepository.save(m);
                        });
            }

            ProductoMultimedia multimedia = ProductoMultimedia.builder()
                    .producto(producto)
                    .tipo(tipo)
                    .url("/uploads/" + nombreArchivo)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .esPrincipal(esPrincipal != null && esPrincipal)
                    .build();

            multimediaRepository.save(multimedia);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage());
        }

        return toResponse(productoRepository.findById(idProducto).orElseThrow());
    }

    @Transactional
    @Override
    public void eliminarMultimedia(Integer idMultimedia) {
        ProductoMultimedia multimedia = multimediaRepository.findById(idMultimedia)
                .orElseThrow(() -> new NotFoundException("Multimedia no encontrada"));

        try {
            Path filePath = Paths.get(uploadDir).resolve(
                    multimedia.getUrl().replace("/uploads/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // ignore
        }

        multimediaRepository.delete(multimedia);
    }

    @Transactional
    @Override
    public ProductoResponse marcarMultimediaPrincipal(Integer idProducto, Integer idMultimedia) {
        multimediaRepository.findByProductoIdProductoAndEsPrincipalTrue(idProducto)
                .ifPresent(m -> {
                    m.setEsPrincipal(false);
                    multimediaRepository.save(m);
                });

        ProductoMultimedia multimedia = multimediaRepository.findById(idMultimedia)
                .orElseThrow(() -> new NotFoundException("Multimedia no encontrada"));
        multimedia.setEsPrincipal(true);
        multimediaRepository.save(multimedia);

        return toResponse(buscarOExcepcion(idProducto));
    }

    @Transactional
    @Override
    public ProductoResponse actualizarStockSucursal(Integer idProducto, Integer idSucursal, Integer nuevoStock) {
        Producto producto = buscarOExcepcion(idProducto);
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioSucursal inventario = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursal)
                .orElse(InventarioSucursal.builder()
                        .producto(producto)
                        .sucursal(sucursal)
                        .stock(0)
                        .build());

        int diferencia = nuevoStock - inventario.getStock();
        inventario.setStock(nuevoStock);
        inventarioSucursalRepository.save(inventario);

        int stockAnteriorGlobal = producto.getStockActual();
        producto.setStockActual(stockAnteriorGlobal + diferencia);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        TipoMovimiento tipo = diferencia >= 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(tipo)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnteriorGlobal)
                .stockNuevo(producto.getStockActual())
                .referencia("Ajuste manual")
                .usuario(usuario)
                .observacion("Ajuste de stock en sucursal: " + sucursal.getNombre())
                .build();
        movimientoStockRepository.save(movimiento);

        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, tipo.name(), usuario,
                "Ajuste de stock en " + sucursal.getNombre() + ": " + diferencia,
                "Ajuste manual", Math.abs(diferencia), stockAnteriorGlobal, producto.getStockActual());

        return toResponse(productoRepository.findById(idProducto).orElseThrow());
    }

    @Transactional
    @Override
    public ProductoResponse registrarMovimientoStock(Integer idProducto, MovimientoStockRequest request) {
        Producto producto = buscarOExcepcion(idProducto);

        Sucursal sucursal = null;
        if (request.idSucursal() != null) {
            sucursal = sucursalRepository.findById(request.idSucursal())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        int stockAnteriorGlobal = producto.getStockActual();
        int nuevoStockGlobal;
        int cantidad = request.cantidad();
        int stockAnteriorSuc = 0;
        int nuevoStockSuc = 0;
        boolean tieneSucursal = sucursal != null;

        if (tieneSucursal) {
            InventarioSucursal inventario = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursal())
                    .orElse(InventarioSucursal.builder()
                            .producto(producto)
                            .sucursal(sucursal)
                            .stock(0)
                            .build());
            stockAnteriorSuc = inventario.getStock();

            switch (request.tipoMovimiento()) {
                case ENTRADA -> {
                    nuevoStockSuc = stockAnteriorSuc + cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal + cantidad;
                }
                case SALIDA -> {
                    if (stockAnteriorSuc < cantidad) {
                        throw new InvalidEntryException("Stock insuficiente en sucursal. Actual: " + stockAnteriorSuc +
                                ", solicitado: " + cantidad);
                    }
                    nuevoStockSuc = stockAnteriorSuc - cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal - cantidad;
                }
                case AJUSTE -> {
                    int diff = cantidad - stockAnteriorSuc;
                    nuevoStockSuc = cantidad;
                    nuevoStockGlobal = stockAnteriorGlobal + diff;
                }
                default -> nuevoStockGlobal = stockAnteriorGlobal;
            }

            inventario.setStock(nuevoStockSuc);
            inventarioSucursalRepository.save(inventario);
        } else {
            switch (request.tipoMovimiento()) {
                case ENTRADA -> nuevoStockGlobal = stockAnteriorGlobal + cantidad;
                case SALIDA -> {
                    if (stockAnteriorGlobal < cantidad) {
                        throw new InvalidEntryException("Stock insuficiente. Actual: " + stockAnteriorGlobal +
                                ", solicitado: " + cantidad);
                    }
                    nuevoStockGlobal = stockAnteriorGlobal - cantidad;
                }
                case AJUSTE -> nuevoStockGlobal = cantidad;
                default -> nuevoStockGlobal = stockAnteriorGlobal;
            }
        }

        producto.setStockActual(nuevoStockGlobal);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();

        MovimientoStock movimiento = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(request.tipoMovimiento())
                .cantidad(cantidad)
                .stockAnterior(tieneSucursal ? stockAnteriorSuc : stockAnteriorGlobal)
                .stockNuevo(tieneSucursal ? nuevoStockSuc : nuevoStockGlobal)
                .referencia(request.referencia())
                .usuario(usuario)
                .observacion(request.observacion())
                .build();
        movimientoStockRepository.save(movimiento);

        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, request.tipoMovimiento().name(), usuario,
                request.tipoMovimiento() + " de " + cantidad + " unidades" +
                        (sucursal != null ? " en " + sucursal.getNombre() : ""),
                request.referencia(), cantidad, stockAnteriorGlobal, nuevoStockGlobal);

        return toResponse(producto);
    }

    @Override
    public ProductoStats obtenerStats() {
        long total = productoRepository.count();
        long activos = productoRepository.countByActivoTrue();
        Integer stockGlobal = productoRepository.sumStockActual();
        Integer stockMinimo = productoRepository.sumStockMinimo();
        return new ProductoStats(total, activos,
                stockGlobal != null ? stockGlobal : 0,
                stockMinimo != null ? stockMinimo : 0);
    }

    private Producto buscarOExcepcion(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private ProductoResponse toResponse(Producto p) {
        List<ProductoResponse.MultimediaResponse> multimedia = p.getMultimedia().stream()
                .map(m -> new ProductoResponse.MultimediaResponse(
                        m.getIdMultimedia(), m.getTipo().name(), m.getUrl(),
                        m.getNombreArchivo(), m.getEsPrincipal()))
                .collect(Collectors.toList());

        List<ProductoResponse.InventarioSucursalResponse> inventario = p.getInventarioSucursales().stream()
                .map(i -> new ProductoResponse.InventarioSucursalResponse(
                        i.getId(), i.getSucursal().getIdSucursal(),
                        i.getSucursal().getNombre(), i.getStock()))
                .collect(Collectors.toList());

        return new ProductoResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecio1(), p.getPrecio2(), p.getPrecio3(), p.getPrecio4(),
                p.getStockActual(), p.getStockMinimo(), p.getStockMaximo(),
                p.getMaterial(), p.getNumeroMolde(), p.getTalla(),
                p.getAccesorio1(), p.getAccesorio2(),
                p.getActivo(), p.getFechaCreacion(), p.getFechaActualizacion(),
                multimedia, inventario);
    }
}
