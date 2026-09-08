package com.bcsystems.intranet.config;

import com.bcsystems.intranet.domain.Permiso;
import com.bcsystems.intranet.domain.Persona;
import com.bcsystems.intranet.domain.Rol;
import com.bcsystems.intranet.repository.PermisoRepository;
import com.bcsystems.intranet.repository.PersonaRepository;
import com.bcsystems.intranet.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataSeedRunner implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final PersonaRepository personaRepository;

    public DataSeedRunner(PermisoRepository permisoRepository,
                          RolRepository rolRepository,
                          PersonaRepository personaRepository) {
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedPermisosYRoles();
        migrarPersonasRol();
    }

    private void seedPermisosYRoles() {
        if (permisoRepository.count() > 0) return;

        Map<String, List<String[]>> permisosPorModulo = buildCatalogo();

        Map<String, Permiso> permisoMap = new HashMap<>();
        for (var entry : permisosPorModulo.entrySet()) {
            String modulo = entry.getKey();
            for (String[] perm : entry.getValue()) {
                Permiso p = Permiso.builder()
                        .clave(perm[0])
                        .nombre(perm[1])
                        .descripcion(perm[1] + " en " + modulo)
                        .modulo(modulo)
                        .activo(true)
                        .build();
                p = permisoRepository.save(p);
                permisoMap.put(p.getClave(), p);
            }
        }

        Set<Permiso> todosLosPermisos = new HashSet<>(permisoMap.values());

        Rol admin = Rol.builder()
                .nombre("ADMINISTRADOR")
                .descripcion("Administrador del sistema - acceso total")
                .esSistema(true)
                .activo(true)
                .permisos(todosLosPermisos)
                .build();
        rolRepository.save(admin);

        Rol sistemas = Rol.builder()
                .nombre("SISTEMAS")
                .descripcion("Soporte tecnico - acceso total")
                .esSistema(true)
                .activo(true)
                .permisos(todosLosPermisos)
                .build();
        rolRepository.save(sistemas);

        Set<String> auditoriaModulos = new HashSet<>(
                List.of("KARDEX", "AUDITORIAS", "HISTORIAL_VENTAS", "CORTES", "GASTOS"));
        Set<Permiso> auditoriaPermisos = permisosPorModulo.entrySet().stream()
                .filter(e -> auditoriaModulos.contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .map(p -> permisoMap.get(p[0]))
                .collect(Collectors.toSet());

        Rol auditoria = Rol.builder()
                .nombre("AUDITORIAS")
                .descripcion("Auditoria - solo lectura en areas financieras")
                .esSistema(true)
                .activo(true)
                .permisos(auditoriaPermisos)
                .build();
        rolRepository.save(auditoria);

        Set<Permiso> usuarioPermisos = new HashSet<>();
        for (String clave : List.of(
                "VENTAS_VER", "VENTAS_CREAR",
                "CLIENTES_VER", "CLIENTES_CREAR",
                "COTIZACIONES_VER", "COTIZACIONES_CREAR",
                "GASTOS_VER",
                "CREDITOS_VER", "CREDITOS_ABONAR",
                "CAJAS_VER", "CAJAS_APERTURA", "CAJAS_CIERRE", "CAJAS_CORTE",
                "HISTORIAL_VENTAS_VER",
                "PRODUCTOS_VER",
                "SUCURSALES_VER",
                "TIPOS_PAGO_VER")) {
            usuarioPermisos.add(permisoMap.get(clave));
        }

        Rol usuario = Rol.builder()
                .nombre("USUARIO")
                .descripcion("Usuario estandar - ventas y cajas")
                .esSistema(true)
                .activo(true)
                .permisos(usuarioPermisos)
                .build();
        rolRepository.save(usuario);
    }

    private void migrarPersonasRol() {
        List<Persona> sinRol = personaRepository.findByRolIsNull();
        if (sinRol.isEmpty()) return;

        Map<String, Rol> rolesPorNombre = new HashMap<>();
        rolRepository.findAll().forEach(r -> rolesPorNombre.put(r.getNombre(), r));

        for (Persona persona : sinRol) {
            Optional<String> legacy = personaRepository.findLegacyRolColumn(persona.getIdPersona());
            String nombreLegacy = legacy.map(v -> v == null ? "USUARIO" : v.toUpperCase()).orElse("USUARIO");
            Rol rol = rolesPorNombre.getOrDefault(nombreLegacy,
                    rolesPorNombre.getOrDefault("USUARIO", null));
            if (rol == null) continue;
            persona.setRol(rol);
            personaRepository.save(persona);
        }
    }

    private Map<String, List<String[]>> buildCatalogo() {
        Map<String, List<String[]>> map = new LinkedHashMap<>();
        map.put("VENTAS", List.of(
                new String[]{"VENTAS_VER", "Ver Ventas"},
                new String[]{"VENTAS_CREAR", "Crear Ventas"},
                new String[]{"VENTAS_CANCELAR", "Cancelar Ventas"}
        ));
        map.put("HISTORIAL_VENTAS", List.<String[]>of(
                new String[]{"HISTORIAL_VENTAS_VER", "Ver Historial de Ventas"}
        ));
        map.put("GASTOS", List.of(
                new String[]{"GASTOS_VER", "Ver Gastos"},
                new String[]{"GASTOS_AUTORIZAR", "Autorizar Gastos"}
        ));
        map.put("COTIZACIONES", List.of(
                new String[]{"COTIZACIONES_VER", "Ver Cotizaciones"},
                new String[]{"COTIZACIONES_CREAR", "Crear Cotizaciones"},
                new String[]{"COTIZACIONES_CANCELAR", "Cancelar Cotizaciones"}
        ));
        map.put("CORTES", List.of(
                new String[]{"CORTES_VER", "Ver Cortes"},
                new String[]{"CORTES_EDITAR", "Editar Cortes"}
        ));
        map.put("CREDITOS", List.of(
                new String[]{"CREDITOS_VER", "Ver Creditos"},
                new String[]{"CREDITOS_ABONAR", "Registrar Abonos"}
        ));
        map.put("PRODUCTOS", List.of(
                new String[]{"PRODUCTOS_VER", "Ver Productos"},
                new String[]{"PRODUCTOS_CREAR", "Crear Productos"},
                new String[]{"PRODUCTOS_EDITAR", "Editar Productos"},
                new String[]{"PRODUCTOS_ELIMINAR", "Eliminar Productos"},
                new String[]{"PRODUCTOS_STOCK", "Gestionar Stock"}
        ));
        map.put("PEDIDOS", List.of(
                new String[]{"PEDIDOS_VER", "Ver Pedidos"},
                new String[]{"PEDIDOS_CREAR", "Crear Pedidos"},
                new String[]{"PEDIDOS_RECEBIR", "Recibir Pedidos"},
                new String[]{"PEDIDOS_CANCELAR", "Cancelar Pedidos"}
        ));
        map.put("CLIENTES", List.of(
                new String[]{"CLIENTES_VER", "Ver Clientes"},
                new String[]{"CLIENTES_CREAR", "Crear Clientes"},
                new String[]{"CLIENTES_EDITAR", "Editar Clientes"},
                new String[]{"CLIENTES_ELIMINAR", "Eliminar Clientes"}
        ));
        map.put("SUCURSALES", List.of(
                new String[]{"SUCURSALES_VER", "Ver Sucursales"},
                new String[]{"SUCURSALES_CREAR", "Crear Sucursales"},
                new String[]{"SUCURSALES_EDITAR", "Editar Sucursales"},
                new String[]{"SUCURSALES_ELIMINAR", "Eliminar Sucursales"}
        ));
        map.put("TIPOS_PAGO", List.of(
                new String[]{"TIPOS_PAGO_VER", "Ver Tipos de Pago"},
                new String[]{"TIPOS_PAGO_CREAR", "Crear Tipos de Pago"},
                new String[]{"TIPOS_PAGO_EDITAR", "Editar Tipos de Pago"},
                new String[]{"TIPOS_PAGO_ELIMINAR", "Eliminar Tipos de Pago"}
        ));
        map.put("ATRIBUTOS", List.of(
                new String[]{"ATRIBUTOS_VER", "Ver Atributos"},
                new String[]{"ATRIBUTOS_CREAR", "Crear Atributos"},
                new String[]{"ATRIBUTOS_EDITAR", "Editar Atributos"},
                new String[]{"ATRIBUTOS_ELIMINAR", "Eliminar Atributos"}
        ));
        map.put("PROVEEDORES", List.of(
                new String[]{"PROVEEDORES_VER", "Ver Proveedores"},
                new String[]{"PROVEEDORES_CREAR", "Crear Proveedores"},
                new String[]{"PROVEEDORES_EDITAR", "Editar Proveedores"},
                new String[]{"PROVEEDORES_ELIMINAR", "Eliminar Proveedores"}
        ));
        map.put("KARDEX", List.<String[]>of(
                new String[]{"KARDEX_VER", "Ver Kardex"}
        ));
        map.put("AUDITORIAS", List.<String[]>of(
                new String[]{"AUDITORIAS_VER", "Ver Auditorias"}
        ));
        map.put("PERSONAS", List.of(
                new String[]{"PERSONAS_VER", "Ver Usuarios"},
                new String[]{"PERSONAS_CREAR", "Crear Usuarios"},
                new String[]{"PERSONAS_EDITAR", "Editar Usuarios"},
                new String[]{"PERSONAS_ELIMINAR", "Eliminar Usuarios"}
        ));
        map.put("CAJAS", List.of(
                new String[]{"CAJAS_VER", "Ver Cajas"},
                new String[]{"CAJAS_CREAR", "Crear Cajas"},
                new String[]{"CAJAS_EDITAR", "Editar Cajas"},
                new String[]{"CAJAS_ELIMINAR", "Eliminar Cajas"},
                new String[]{"CAJAS_APERTURA", "Abrir Cajas"},
                new String[]{"CAJAS_CIERRE", "Cerrar Cajas"},
                new String[]{"CAJAS_CORTE", "Realizar Cortes de Caja"}
        ));
        map.put("PROMOCIONES", List.of(
                new String[]{"PROMOCIONES_VER", "Ver Promociones"},
                new String[]{"PROMOCIONES_CREAR", "Crear Promociones"},
                new String[]{"PROMOCIONES_EDITAR", "Editar Promociones"},
                new String[]{"PROMOCIONES_ELIMINAR", "Eliminar Promociones"}
        ));
        map.put("ROLES", List.of(
                new String[]{"ROLES_VER", "Ver Roles"},
                new String[]{"ROLES_CREAR", "Crear Roles"},
                new String[]{"ROLES_EDITAR", "Editar Roles"},
                new String[]{"ROLES_ELIMINAR", "Eliminar Roles"}
        ));
        return map;
    }
}
