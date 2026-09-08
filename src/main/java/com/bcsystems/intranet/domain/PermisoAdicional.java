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
@Table(name = "persona_permiso_adicional")
public class PermisoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPermisoAdicional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_permiso", nullable = false)
    private Permiso permiso;
}
