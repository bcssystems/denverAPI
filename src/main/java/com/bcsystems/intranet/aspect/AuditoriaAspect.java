package com.bcsystems.intranet.aspect;

import com.bcsystems.intranet.service.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditoriaAspect {

    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @AfterReturning(value = "execution(* com.bcsystems.intranet.service.impl.SucursalServiceImpl.crear(..))", returning = "result")
    public void auditarCrearSucursal(JoinPoint jp, Object result) {
        // Already handled in service impl
    }

    private String obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SISTEMA";
    }
}
