package com.bcsystems.intranet.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.List;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProducto;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio1;
    private Double precio2;
    private Double precio3;
    private Double precio4;

    @Column(nullable = false)
    private Integer stockActual;

    private Integer stockMinimo;
    private Integer stockMaximo;

    @Column(length = 100)
    private String material;

    @Column(name = "numero_molde", length = 50)
    private String numeroMolde;

    @Column(length = 20)
    private String talla;

    @Column(name = "accesorio_1", length = 100)
    private String accesorio1;

    @Column(name = "accesorio_2", length = 100)
    private String accesorio2;

    @Column(nullable = false)
    private Boolean activo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductoMultimedia> multimedia = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventarioSucursal> inventarioSucursales = new java.util.ArrayList<>();
}
