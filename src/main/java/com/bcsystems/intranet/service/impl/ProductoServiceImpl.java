package com.bcsystems.intranet.service.impl;

import com.bcsystems.intranet.domain.*;
import com.bcsystems.intranet.domain.en.AccionAuditoria;
import com.bcsystems.intranet.domain.en.TipoMovimiento;
import com.bcsystems.intranet.domain.en.TipoMultimedia;
import com.bcsystems.intranet.dto.InventarioSucursalRequest;
import com.bcsystems.intranet.dto.MovimientoStockRequest;
import com.bcsystems.intranet.dto.ProductoListaResponse;
import com.bcsystems.intranet.dto.ProductoRequest;
import com.bcsystems.intranet.dto.ProductoResponse;
import com.bcsystems.intranet.dto.ProductoVentaResponse;
import com.bcsystems.intranet.dto.TransferenciaRequest;
import com.bcsystems.intranet.exception.InvalidEntryException;
import com.bcsystems.intranet.exception.NotFoundException;
import com.bcsystems.intranet.repository.*;
import com.bcsystems.intranet.service.AuditoriaService;
import com.bcsystems.intranet.service.ProductoService;
import com.bcsystems.intranet.util.CodigoGeneratorService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository multimediaRepository;
    private final SucursalRepository sucursalRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ProductoVarianteAtributoRepository productoVarianteAtributoRepository;
    private final AtributoValorRepository atributoValorRepository;
    private final CodigoGeneratorService codigoGenerator;
    private final AuditoriaService auditoriaService;
    private final EntityManager entityManager;

    @Value("${app.upload.dir:./uploads/multimedia}")
    private String uploadDir;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ProductoMultimediaRepository multimediaRepository,
                               SucursalRepository sucursalRepository,
                               InventarioSucursalRepository inventarioSucursalRepository,
                               MovimientoStockRepository movimientoStockRepository,
                               ProductoVarianteAtributoRepository productoVarianteAtributoRepository,
                               AtributoValorRepository atributoValorRepository,
                               CodigoGeneratorService codigoGenerator,
                               AuditoriaService auditoriaService,
                               EntityManager entityManager) {
        this.productoRepository = productoRepository;
        this.multimediaRepository = multimediaRepository;
        this.sucursalRepository = sucursalRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.productoVarianteAtributoRepository = productoVarianteAtributoRepository;
        this.atributoValorRepository = atributoValorRepository;
        this.codigoGenerator = codigoGenerator;
        this.auditoriaService = auditoriaService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoListaResponse> listar(String search, Boolean activo, Integer idSucursal, Pageable pageable) {
        Page<Producto> page = productoRepository.buscarConFiltros(search, activo, idSucursal, pageable);
        List<Producto> content = page.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, page.getTotalElements());
        }
        Map<Integer, ProductoListaResponse> mapa = buildListaResponseMap(content);
        List<ProductoListaResponse> resultado = content.stream()
                .map(p -> mapa.get(p.getIdProducto()))
                .collect(Collectors.toList());
        return new PageImpl<>(resultado, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductoVentaResponse> listarParaVenta(String search, Integer idSucursal, Pageable pageable) {
        Page<Producto> page = productoRepository.buscarParaVenta(search, idSucursal, pageable);
        List<Producto> content = page.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, page.getTotalElements());
        }

        Set<Integer> ids = content.stream().map(Producto::getIdProducto).collect(Collectors.toSet());

        Map<Integer, List<ProductoVentaResponse.InventarioSucursalResponse>> inventario = productoRepository.findInventarioByProductoIdIn(ids).stream()
                .collect(Collectors.groupingBy(i -> i.getProducto().getIdProducto(),
                        Collectors.mapping(this::toVentaInventario, Collectors.toList())));

        Map<Integer, List<ProductoVentaResponse.AtributoInfo>> atributos = productoRepository.findVarianteAtributosByProductoIdIn(ids).stream()
                .collect(Collectors.groupingBy(pva -> pva.getProductoVariante().getIdProducto(),
                        Collectors.mapping(this::toVentaAtributo, Collectors.toList())));

        List<ProductoVentaResponse> resultado = content.stream()
                .map(p -> toVentaResponse(p,
                        inventario.getOrDefault(p.getIdProducto(), Collections.emptyList()),
                        atributos.getOrDefault(p.getIdProducto(), Collections.emptyList())))
                .collect(Collectors.toList());
        return new PageImpl<>(resultado, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Override
    public ProductoResponse obtenerPorId(Integer id) {
        return toResponse(buscarOExcepcion(id));
    }

    @Transactional
    @Override
    public ProductoResponse crear(ProductoRequest request) {
        String sku = request.sku();
        if (sku == null || sku.isBlank()) {
            sku = codigoGenerator.generarSku();
        } else if (productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new InvalidEntryException("Ya existe un producto con el SKU: " + sku);
        }

        ProductoRequest requestConSku = new ProductoRequest(
                sku, request.nombre(), request.descripcion(),
                request.precio1(), request.precio2(), request.precio3(), request.precio4(),
                request.costoPromedio(), request.activo(), request.tieneVariantes(),
                request.idProductoPadre(), request.variantes(), request.inventarios()
        );

        String usuario = obtenerUsuarioActual();

        if (Boolean.TRUE.equals(requestConSku.tieneVariantes())) {
            return crearProductoConVariantes(requestConSku, usuario);
        }

        return crearProductoSimple(requestConSku, usuario);
    }

    private ProductoResponse crearProductoSimple(ProductoRequest request, String usuario) {
        int stockTotal = 0;
        int minTotal = 0;
        int maxTotal = 0;
        if (request.inventarios() != null) {
            for (var invReq : request.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;
            }
        }

        Producto producto = Producto.builder()
                .sku(request.sku())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio1(request.precio1())
                .precio2(request.precio2())
                .precio3(request.precio3())
                .precio4(request.precio4())
                .costoPromedio(request.costoPromedio())
                .precioPersonalizado(false)
                .stockActual(stockTotal)
                .stockMinimo(minTotal)
                .stockMaximo(maxTotal)
                .tieneVariantes(false)
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        producto = productoRepository.save(producto);
        guardarInventarios(producto, request.inventarios(), usuario);
        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.CREACION.name(), usuario,
                "Se cre\u00f3 el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    private ProductoResponse crearProductoConVariantes(ProductoRequest request, String usuario) {
        Producto padre = Producto.builder()
                .sku(request.sku())
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .precio1(request.precio1())
                .precio2(request.precio2())
                .precio3(request.precio3())
                .precio4(request.precio4())
                .costoPromedio(request.costoPromedio())
                .precioPersonalizado(false)
                .stockActual(0)
                .stockMinimo(0)
                .stockMaximo(0)
                .tieneVariantes(true)
                .activo(request.activo() != null ? request.activo() : true)
                .build();

        padre = productoRepository.save(padre);

        if (request.variantes() != null) {
            for (var varReq : request.variantes()) {
                crearVariante(padre, varReq);
            }
        }

        padre.setStockActual(0);

        recalcularStockPadre(padre);

        auditoriaService.registrar("PRODUCTO", padre.getIdProducto(), AccionAuditoria.CREACION.name(), usuario,
                "Se cre\u00f3 el producto con variantes: " + padre.getNombre());

        return toResponse(padre);
    }

    private Producto crearVariante(Producto padre, ProductoRequest.VarianteRequest varReq) {
        String sku = varReq.sku();
        if (sku == null || sku.isBlank()) {
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(varReq.idAtributoValores());
            List<String> codigos = valores.stream()
                    .map(v -> v.getCodigoSku() != null ? v.getCodigoSku() : "")
                    .filter(c -> !c.isBlank())
                    .collect(Collectors.toList());
            sku = codigoGenerator.generarSkuVariante(padre.getSku(), codigos);
        } else if (productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new InvalidEntryException("Ya existe un producto con el SKU: " + sku);
        }

        Double precio1 = varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio1() : padre.getPrecio1();
        Double precio2 = varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio2() : padre.getPrecio2();
        Double precio3 = varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio3() : padre.getPrecio3();
        Double precio4 = varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio4() : padre.getPrecio4();

        int stockTotal = 0;
        int minTotal = 0;
        int maxTotal = 0;
        if (varReq.inventarios() != null) {
            for (var invReq : varReq.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;
            }
        }

        String nombreVariante = varReq.nombre() != null && !varReq.nombre().isBlank() ? varReq.nombre() : padre.getNombre();

        Producto variante = Producto.builder()
                .sku(sku)
                .nombre(nombreVariante)
                .descripcion(padre.getDescripcion())
                .precio1(precio1)
                .precio2(precio2)
                .precio3(precio3)
                .precio4(precio4)
                .precioPersonalizado(varReq.precioPersonalizado() != null && varReq.precioPersonalizado())
                .stockActual(stockTotal)
                .stockMinimo(minTotal)
                .stockMaximo(maxTotal)
                .tieneVariantes(false)
                .productoPadre(padre)
                .activo(true)
                .build();

        variante = productoRepository.save(variante);

        guardarInventarios(variante, varReq.inventarios(), obtenerUsuarioActual());

        if (varReq.idAtributoValores() != null) {
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(varReq.idAtributoValores());
            for (AtributoValor valor : valores) {
                ProductoVarianteAtributo pva = ProductoVarianteAtributo.builder()
                        .productoVariante(variante)
                        .atributo(valor.getAtributo())
                        .valor(valor)
                        .build();
                productoVarianteAtributoRepository.save(pva);
            }
        }

        return variante;
    }

    private void guardarInventarios(Producto producto, List<InventarioSucursalRequest> inventarios, String usuario) {
        if (inventarios == null || inventarios.isEmpty()) return;

        Map<Integer, Sucursal> sucursales = cargarSucursales(inventarios);

        for (var invReq : inventarios) {
            Sucursal sucursal = sucursales.get(invReq.idSucursal());
            if (sucursal == null) throw new NotFoundException("Sucursal no encontrada");

            InventarioSucursal inventario = InventarioSucursal.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .stock(invReq.stock() != null ? invReq.stock() : 0)
                    .stockMinimo(invReq.stockMinimo())
                    .stockMaximo(invReq.stockMaximo())
                    .build();
            inventarioSucursalRepository.save(inventario);

            if (invReq.stock() != null && invReq.stock() > 0) {
                MovimientoStock movimiento = MovimientoStock.builder()
                        .producto(producto)
                        .sucursal(sucursal)
                        .tipoMovimiento(TipoMovimiento.ENTRADA)
                        .cantidad(invReq.stock())
                        .stockAnterior(0)
                        .stockNuevo(invReq.stock())
                        .referencia("Stock inicial")
                        .usuario(usuario)
                        .observacion("Stock inicial en sucursal: " + sucursal.getNombre())
                        .build();
                movimientoStockRepository.save(movimiento);

                auditoriaService.registrarMovimiento("PRODUCTO", producto.getIdProducto(), "ENTRADA", usuario,
                        "Stock inicial de " + invReq.stock() + " unidades en " + sucursal.getNombre(),
                        "Stock inicial", invReq.stock(), 0, invReq.stock());
            }
        }
    }

    @Transactional
    @Override
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        String usuario = obtenerUsuarioActual();

        Producto existing = buscarOExcepcion(id);
        if (existing.getProductoPadre() != null && Boolean.TRUE.equals(request.tieneVariantes())) {
            throw new InvalidEntryException("Un producto variante no puede tener variantes hijas");
        }

        if (Boolean.TRUE.equals(request.tieneVariantes()) && request.variantes() != null) {
            Producto padre = buscarOExcepcion(id);

            padre.setNombre(request.nombre());
            padre.setDescripcion(request.descripcion());
            padre.setPrecio1(request.precio1());
            padre.setPrecio2(request.precio2());
            padre.setPrecio3(request.precio3());
            padre.setPrecio4(request.precio4());
            if (request.costoPromedio() != null) padre.setCostoPromedio(request.costoPromedio());
            if (request.activo() != null) padre.setActivo(request.activo());

            List<Producto> existingVariants = productoRepository.findByProductoPadreIdProducto(id);
            Map<Integer, Producto> variantMap = new HashMap<>();
            for (Producto v : existingVariants) {
                variantMap.put(v.getIdProducto(), v);
            }

            Set<Integer> remainingIds = new HashSet<>();

            for (var varReq : request.variantes()) {
                if (varReq.idVariante() != null && variantMap.containsKey(varReq.idVariante())) {
                    actualizarVariante(padre, variantMap.get(varReq.idVariante()), varReq, usuario);
                    remainingIds.add(varReq.idVariante());
                } else {
                    crearVariante(padre, varReq);
                }
            }

            for (Producto v : existingVariants) {
                if (!remainingIds.contains(v.getIdProducto())) {
                    v.setActivo(false);
                    productoRepository.save(v);
                }
            }

            recalcularStockPadre(padre);
            padre = productoRepository.save(padre);
            auditoriaService.registrar("PRODUCTO", padre.getIdProducto(), AccionAuditoria.ACTUALIZACION.name(), usuario,
                    "Se actualiz\u00f3 el producto con variantes: " + padre.getNombre());
            return toResponse(padre);
        }

        Producto producto = buscarOExcepcion(id);

        producto.setNombre(request.nombre());
        producto.setDescripcion(request.descripcion());
        producto.setPrecio1(request.precio1());
        producto.setPrecio2(request.precio2());
        producto.setPrecio3(request.precio3());
        producto.setPrecio4(request.precio4());
        if (request.costoPromedio() != null) producto.setCostoPromedio(request.costoPromedio());
        if (request.activo() != null) producto.setActivo(request.activo());

        if (request.inventarios() != null && !request.inventarios().isEmpty()) {
            int stockTotal = 0;
            int minTotal = 0;
            int maxTotal = 0;
            Map<Integer, Sucursal> sucursales = cargarSucursales(request.inventarios());
            for (var invReq : request.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;

                Sucursal sucursal = sucursales.get(invReq.idSucursal());
                if (sucursal == null) throw new NotFoundException("Sucursal no encontrada");

                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(id, invReq.idSucursal())
                        .orElse(InventarioSucursal.builder()
                                .producto(producto)
                                .sucursal(sucursal)
                                .stock(0)
                                .build());
                inv.setStock(invReq.stock() != null ? invReq.stock() : 0);
                inv.setStockMinimo(invReq.stockMinimo());
                inv.setStockMaximo(invReq.stockMaximo());
                inventarioSucursalRepository.save(inv);
            }
            producto.setStockActual(stockTotal);
            producto.setStockMinimo(minTotal);
            producto.setStockMaximo(maxTotal);
        }

        producto = productoRepository.save(producto);

        recalcularStockPadre(producto);

        auditoriaService.registrar("PRODUCTO", producto.getIdProducto(), AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se actualiz\u00f3 el producto: " + producto.getNombre());

        return toResponse(producto);
    }

    private void actualizarInventarios(Producto producto, List<InventarioSucursalRequest> inventarios, String usuario) {
        if (inventarios == null || inventarios.isEmpty()) return;

        Map<Integer, Sucursal> sucursales = cargarSucursales(inventarios);

        for (var invReq : inventarios) {
            Sucursal sucursal = sucursales.get(invReq.idSucursal());
            if (sucursal == null) throw new NotFoundException("Sucursal no encontrada");
            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(producto.getIdProducto(), invReq.idSucursal())
                    .orElse(InventarioSucursal.builder()
                            .producto(producto)
                            .sucursal(sucursal)
                            .stock(0)
                            .build());
            inv.setStock(invReq.stock() != null ? invReq.stock() : 0);
            inv.setStockMinimo(invReq.stockMinimo());
            inv.setStockMaximo(invReq.stockMaximo());
            inventarioSucursalRepository.save(inv);
        }
    }

    private void actualizarVariante(Producto padre, Producto variante, ProductoRequest.VarianteRequest varReq, String usuario) {
        String sku = varReq.sku();
        if (sku == null || sku.isBlank()) {
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(varReq.idAtributoValores());
            List<String> codigos = valores.stream()
                    .map(v -> v.getCodigoSku() != null ? v.getCodigoSku() : "")
                    .filter(c -> !c.isBlank())
                    .collect(Collectors.toList());
            sku = codigoGenerator.generarSkuVariante(padre.getSku(), codigos);
        } else if (!sku.equalsIgnoreCase(variante.getSku()) && productoRepository.existsBySkuIgnoreCase(sku)) {
            throw new InvalidEntryException("Ya existe un producto con el SKU: " + sku);
        }

        String nombreVariante = varReq.nombre() != null && !varReq.nombre().isBlank() ? varReq.nombre() : padre.getNombre();

        variante.setSku(sku);
        variante.setNombre(nombreVariante);
        variante.setDescripcion(padre.getDescripcion());
        variante.setPrecio1(varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio1() : padre.getPrecio1());
        variante.setPrecio2(varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio2() : padre.getPrecio2());
        variante.setPrecio3(varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio3() : padre.getPrecio3());
        variante.setPrecio4(varReq.precioPersonalizado() != null && varReq.precioPersonalizado() ? varReq.precio4() : padre.getPrecio4());
        variante.setPrecioPersonalizado(varReq.precioPersonalizado() != null && varReq.precioPersonalizado());
        if (varReq.costoPromedio() != null) variante.setCostoPromedio(varReq.costoPromedio());

        int stockTotal = 0;
        int minTotal = 0;
        int maxTotal = 0;
        if (varReq.inventarios() != null) {
            for (var invReq : varReq.inventarios()) {
                stockTotal += invReq.stock() != null ? invReq.stock() : 0;
                minTotal += invReq.stockMinimo() != null ? invReq.stockMinimo() : 0;
                maxTotal += invReq.stockMaximo() != null ? invReq.stockMaximo() : 0;
            }
        }
        variante.setStockActual(stockTotal);
        variante.setStockMinimo(minTotal);
        variante.setStockMaximo(maxTotal);
        variante.setActivo(true);
        productoRepository.save(variante);

        actualizarInventarios(variante, varReq.inventarios(), usuario);
        actualizarAtributosVariante(variante, varReq.idAtributoValores());
    }

    private void actualizarAtributosVariante(Producto variante, List<Integer> idAtributoValores) {
        productoVarianteAtributoRepository.deleteByProductoVarianteIdProducto(variante.getIdProducto());
        entityManager.flush();
        if (idAtributoValores != null) {
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(idAtributoValores);
            for (AtributoValor valor : valores) {
                ProductoVarianteAtributo pva = ProductoVarianteAtributo.builder()
                        .productoVariante(variante)
                        .atributo(valor.getAtributo())
                        .valor(valor)
                        .build();
                productoVarianteAtributoRepository.save(pva);
            }
        }
    }

    @Transactional
    @Override
    public void eliminar(Integer id) {
        Producto producto = buscarOExcepcion(id);

        if (producto.getStockActual() != null && producto.getStockActual() > 0) {
            throw new InvalidEntryException("No se puede desactivar un producto con stock mayor a 0");
        }

        List<InventarioSucursal> inventarios = inventarioSucursalRepository.findByProductoIdProducto(id);
        for (InventarioSucursal inv : inventarios) {
            if (inv.getStock() != null && inv.getStock() > 0) {
                throw new InvalidEntryException("No se puede desactivar: stock en sucursal " +
                        (inv.getSucursal() != null ? inv.getSucursal().getNombre() : "") + " es " + inv.getStock());
            }
        }

        producto.setActivo(false);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", id, AccionAuditoria.ELIMINACION.name(), usuario,
                "Se elimin\u00f3 (desactiv\u00f3) el producto: " + producto.getNombre());
    }

    @Transactional
    @Override
    public void reactivar(Integer id) {
        Producto producto = buscarOExcepcion(id);
        producto.setActivo(true);
        productoRepository.save(producto);

        String usuario = obtenerUsuarioActual();
        auditoriaService.registrar("PRODUCTO", id, AccionAuditoria.ACTUALIZACION.name(), usuario,
                "Se reactiv\u00f3 el producto: " + producto.getNombre());
    }

    @Transactional
    @Override
    public ProductoResponse agregarMultimedia(Integer idProducto, MultipartFile archivo, Boolean esPrincipal) {
        Producto producto = buscarOExcepcion(idProducto);

        if (archivo.isEmpty()) {
            throw new InvalidEntryException("El archivo est\u00e1 vac\u00edo");
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

        recalcularStockPadre(producto);

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

        if (request.tipoMovimiento() == TipoMovimiento.AJUSTE) {
            throw new InvalidEntryException("El tipo de movimiento Ajuste ya no está disponible. Usa Entrada, Salida o Traslado");
        }

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

        recalcularStockPadre(producto);

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

    @Transactional
    @Override
    public ProductoResponse transferirStock(Integer idProducto, TransferenciaRequest request) {
        Producto producto = buscarOExcepcion(idProducto);
        Sucursal origen = sucursalRepository.findById(request.idSucursalOrigen())
                .orElseThrow(() -> new NotFoundException("Sucursal origen no encontrada"));
        Sucursal destino = sucursalRepository.findById(request.idSucursalDestino())
                .orElseThrow(() -> new NotFoundException("Sucursal destino no encontrada"));

        if (origen.getIdSucursal().equals(destino.getIdSucursal())) {
            throw new InvalidEntryException("La sucursal origen y destino deben ser diferentes");
        }

        int cantidad = request.cantidad();

        InventarioSucursal invOrigen = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursalOrigen())
                .orElseThrow(() -> new InvalidEntryException("Producto sin inventario en sucursal origen"));

        if (invOrigen.getStock() < cantidad) {
            throw new InvalidEntryException("Stock insuficiente en sucursal origen. Actual: " + invOrigen.getStock() +
                    ", solicitado: " + cantidad);
        }

        InventarioSucursal invDestino = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, request.idSucursalDestino())
                .orElse(InventarioSucursal.builder()
                        .producto(producto)
                        .sucursal(destino)
                        .stock(0)
                        .build());

        int stockOrigenAntes = invOrigen.getStock();
        int stockDestinoAntes = invDestino.getStock();

        invOrigen.setStock(stockOrigenAntes - cantidad);
        invDestino.setStock(stockDestinoAntes + cantidad);

        inventarioSucursalRepository.save(invOrigen);
        inventarioSucursalRepository.save(invDestino);

        String usuario = obtenerUsuarioActual();
        String referencia = request.referencia();
        String observacion = request.observacion();

        MovimientoStock movOrigen = MovimientoStock.builder()
                .producto(producto)
                .sucursal(origen)
                .tipoMovimiento(TipoMovimiento.TRANSFERENCIA)
                .cantidad(cantidad)
                .stockAnterior(stockOrigenAntes)
                .stockNuevo(invOrigen.getStock())
                .referencia(referencia)
                .usuario(usuario)
                .observacion(observacion != null ? observacion : "Transferido a " + destino.getNombre())
                .build();
        movimientoStockRepository.save(movOrigen);

        MovimientoStock movDestino = MovimientoStock.builder()
                .producto(producto)
                .sucursal(destino)
                .tipoMovimiento(TipoMovimiento.TRANSFERENCIA)
                .cantidad(cantidad)
                .stockAnterior(stockDestinoAntes)
                .stockNuevo(invDestino.getStock())
                .referencia(referencia)
                .usuario(usuario)
                .observacion(observacion != null ? observacion : "Transferido desde " + origen.getNombre())
                .build();
        movimientoStockRepository.save(movDestino);

        int stockGlobal = producto.getStockActual();
        auditoriaService.registrarMovimiento("PRODUCTO", idProducto, TipoMovimiento.TRANSFERENCIA.name(), usuario,
                "Transferencia de " + cantidad + " unidades de " + origen.getNombre() + " a " + destino.getNombre(),
                referencia, cantidad, stockGlobal, stockGlobal);

        return toResponse(producto);
    }

    @Override
    public ProductoStats obtenerStats() {
        List<Object[]> rows = productoRepository.resumenStats();
        Object[] r = rows.isEmpty() ? new Object[]{0L, 0L, 0L, 0L} : rows.get(0);
        long total = r != null && r[0] != null ? ((Number) r[0]).longValue() : 0L;
        long activos = r != null && r[1] != null ? ((Number) r[1]).longValue() : 0L;
        long stockGlobal = r != null && r[2] != null ? ((Number) r[2]).longValue() : 0L;
        long stockMinimo = r != null && r[3] != null ? ((Number) r[3]).longValue() : 0L;
        Double costoTotal = productoRepository.sumCostoTotalInventario();
        return new ProductoStats(total, activos, stockGlobal, stockMinimo,
                costoTotal != null ? costoTotal : 0);
    }

    @Override
    public List<Map<String, Object>> costoPorSucursal() {
        List<Object[]> rows = inventarioSucursalRepository.sumCostoPorSucursal();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("sucursal", row[0]);
            map.put("costo", row[1]);
            result.add(map);
        }
        return result;
    }

    private Producto buscarOExcepcion(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }

    private void recalcularStockPadre(Producto producto) {
        Producto padre = producto.getProductoPadre();
        if (padre != null) {
            Integer totalStock = productoRepository.sumStockByProductoPadreId(padre.getIdProducto());
            padre.setStockActual(totalStock);
            productoRepository.save(padre);
        } else if (Boolean.TRUE.equals(producto.getTieneVariantes())) {
            Integer totalStock = productoRepository.sumStockByProductoPadreId(producto.getIdProducto());
            producto.setStockActual(totalStock);
            productoRepository.save(producto);
        }
    }

    private ProductoVentaResponse toVentaResponse(Producto p,
            List<ProductoVentaResponse.InventarioSucursalResponse> inventario,
            List<ProductoVentaResponse.AtributoInfo> atributos) {
        return new ProductoVentaResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(),
                p.getPrecio1(), p.getPrecio2(), p.getPrecio3(), p.getPrecio4(),
                p.getStockActual(), p.getCostoPromedio(), p.getTieneVariantes(),
                p.getProductoPadre() != null ? p.getProductoPadre().getIdProducto() : null,
                p.getActivo(), inventario, atributos);
    }

    private ProductoVentaResponse.InventarioSucursalResponse toVentaInventario(InventarioSucursal i) {
        return new ProductoVentaResponse.InventarioSucursalResponse(
                i.getId(), i.getSucursal().getIdSucursal(),
                i.getSucursal().getNombre(), i.getStock(),
                i.getStockMinimo(), i.getStockMaximo());
    }

    private ProductoVentaResponse.AtributoInfo toVentaAtributo(ProductoVarianteAtributo pva) {
        return new ProductoVentaResponse.AtributoInfo(
                pva.getAtributo().getNombre(), pva.getValor().getValor());
    }

    private Map<Integer, Sucursal> cargarSucursales(List<InventarioSucursalRequest> inventarios) {
        List<Integer> ids = inventarios.stream()
                .map(InventarioSucursalRequest::idSucursal)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyMap();
        return sucursalRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Sucursal::getIdSucursal, s -> s));
    }

    private Map<Integer, ProductoListaResponse> buildListaResponseMap(List<Producto> padres) {
        if (padres.isEmpty()) return Collections.emptyMap();

        Set<Integer> ids = padres.stream().map(Producto::getIdProducto).collect(Collectors.toSet());
        List<Producto> hijos = productoRepository.findByProductoPadreIdProductoIn(ids);

        Set<Integer> todos = new HashSet<>(ids);
        hijos.forEach(h -> todos.add(h.getIdProducto()));

        Map<Integer, List<ProductoListaResponse.MultimediaResponse>> multimedia = productoRepository.findMultimediaByProductoIdIn(todos).stream()
                .collect(Collectors.groupingBy(m -> m.getProducto().getIdProducto(),
                        Collectors.mapping(this::toListaMultimedia, Collectors.toList())));

        Map<Integer, List<ProductoListaResponse.InventarioSucursalResponse>> inventario = productoRepository.findInventarioByProductoIdIn(todos).stream()
                .collect(Collectors.groupingBy(i -> i.getProducto().getIdProducto(),
                        Collectors.mapping(this::toListaInventario, Collectors.toList())));

        Map<Integer, List<ProductoListaResponse.VarianteAtributoResponse>> atributos = productoRepository.findVarianteAtributosByProductoIdIn(todos).stream()
                .collect(Collectors.groupingBy(pva -> pva.getProductoVariante().getIdProducto(),
                        Collectors.mapping(this::toListaAtributo, Collectors.toList())));

        Map<Integer, List<ProductoListaResponse>> hijosPorPadre = new HashMap<>();
        for (Producto h : hijos) {
            Integer padreId = h.getProductoPadre().getIdProducto();
            hijosPorPadre.computeIfAbsent(padreId, k -> new ArrayList<>())
                    .add(toListaInterno(h, multimedia, inventario, atributos, null));
        }

        Map<Integer, ProductoListaResponse> resultado = new HashMap<>();
        for (Producto p : padres) {
            List<ProductoListaResponse> variantes = hijosPorPadre.getOrDefault(p.getIdProducto(), Collections.emptyList());
            resultado.put(p.getIdProducto(), toListaInterno(p, multimedia, inventario, atributos, variantes));
        }
        return resultado;
    }

    private ProductoListaResponse toListaInterno(Producto p,
            Map<Integer, List<ProductoListaResponse.MultimediaResponse>> multimedia,
            Map<Integer, List<ProductoListaResponse.InventarioSucursalResponse>> inventario,
            Map<Integer, List<ProductoListaResponse.VarianteAtributoResponse>> atributos,
            List<ProductoListaResponse> variantes) {
        return new ProductoListaResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecio1(), p.getPrecio2(), p.getPrecio3(), p.getPrecio4(),
                p.getPrecioPersonalizado(),
                p.getStockActual(), p.getStockMinimo(), p.getStockMaximo(),
                p.getCostoPromedio(), p.getTieneVariantes(),
                p.getProductoPadre() != null ? p.getProductoPadre().getIdProducto() : null,
                p.getActivo(), p.getFechaCreacion(), p.getFechaActualizacion(),
                multimedia.getOrDefault(p.getIdProducto(), Collections.emptyList()),
                inventario.getOrDefault(p.getIdProducto(), Collections.emptyList()),
                variantes,
                atributos.getOrDefault(p.getIdProducto(), Collections.emptyList()));
    }

    private ProductoListaResponse.MultimediaResponse toListaMultimedia(ProductoMultimedia m) {
        return new ProductoListaResponse.MultimediaResponse(
                m.getIdMultimedia(),
                m.getTipo() != null ? m.getTipo().name() : null,
                m.getUrl(), m.getNombreArchivo(), m.getEsPrincipal());
    }

    private ProductoListaResponse.InventarioSucursalResponse toListaInventario(InventarioSucursal i) {
        return new ProductoListaResponse.InventarioSucursalResponse(
                i.getId(), i.getSucursal().getIdSucursal(),
                i.getSucursal().getNombre(), i.getStock(),
                i.getStockMinimo(), i.getStockMaximo());
    }

    private ProductoListaResponse.VarianteAtributoResponse toListaAtributo(ProductoVarianteAtributo pva) {
        return new ProductoListaResponse.VarianteAtributoResponse(
                pva.getAtributo().getIdAtributo(),
                pva.getAtributo().getNombre(),
                pva.getValor().getIdValor(),
                pva.getValor().getValor(),
                pva.getValor().getCodigoSku());
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
                        i.getSucursal().getNombre(), i.getStock(),
                        i.getStockMinimo(), i.getStockMaximo()))
                .collect(Collectors.toList());

        List<ProductoResponse.VarianteAtributoResponse> atributos = p.getVarianteAtributos().stream()
                .map(pva -> new ProductoResponse.VarianteAtributoResponse(
                        pva.getAtributo().getIdAtributo(),
                        pva.getAtributo().getNombre(),
                        pva.getValor().getIdValor(),
                        pva.getValor().getValor(),
                        pva.getValor().getCodigoSku()))
                .collect(Collectors.toList());

        List<ProductoResponse> variantes = null;
        if (Boolean.TRUE.equals(p.getTieneVariantes())) {
            variantes = productoRepository.findByProductoPadreIdProducto(p.getIdProducto()).stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return new ProductoResponse(
                p.getIdProducto(), p.getSku(), p.getNombre(), p.getDescripcion(),
                p.getPrecio1(), p.getPrecio2(), p.getPrecio3(), p.getPrecio4(),
                p.getPrecioPersonalizado(),
                p.getStockActual(), p.getStockMinimo(), p.getStockMaximo(),
                p.getCostoPromedio(), p.getTieneVariantes(),
                p.getProductoPadre() != null ? p.getProductoPadre().getIdProducto() : null,
                p.getActivo(), p.getFechaCreacion(), p.getFechaActualizacion(),
                multimedia, inventario, variantes, atributos);
    }
}
