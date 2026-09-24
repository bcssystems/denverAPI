package com.bcsystems.intranet.domain;

import com.bcsystems.intranet.domain.en.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "solicitud_cambio_precio")
public class SolicitudCambioPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private Double precioActual;

    @Column(nullable = false)
    private Double nuevoPrecio;

    @Column(nullable = false, length = 300)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitante", nullable = false)
    private Persona solicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoSolicitud estado;

    @Column(length = 4)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_genero_codigo")
    private Persona generadoPor;

    private LocalDateTime fechaGeneracionCodigo;

    @Column(updatable = false)
    private LocalDateTime fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autorizador")
    private Persona autorizador;

    private LocalDateTime fechaAutorizacion;
}