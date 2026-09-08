package com.bcsystems.intranet.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;

@Audited
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean esSistema = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_permiso",
        joinColumns = @JoinColumn(name = "id_rol"),
        inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
}
