package redalerta_backend.modules.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.admin.dto.AprobarSolicitudRequestDto;
import redalerta_backend.modules.admin.dto.RechazarSolicitudRequestDto;
import redalerta_backend.modules.admin.dto.SolicitudRegistroResponseDto;
import redalerta_backend.modules.admin.entity.SolicitudRegistro;
import redalerta_backend.modules.admin.mapper.SolicitudRegistroMapper;
import redalerta_backend.modules.admin.repository.SolicitudRegistroRepository;
import redalerta_backend.modules.admin.service.AdminSolicitudService;
import redalerta_backend.modules.admin.valueobject.EstadoSolicitud;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

import java.time.LocalDateTime;

/**
 * Implementacion del servicio de administracion para la auditoria y bandeja de solicitudes de registro.
 * Aplica principios Zero Trust y validacion estricta de privilegios de administrador.
 */
@Service
@RequiredArgsConstructor
public class AdminSolicitudServiceImpl implements AdminSolicitudService {

    private final SolicitudRegistroRepository solicitudRegistroRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudRegistroMapper solicitudRegistroMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudRegistroResponseDto> listarSolicitudes(EstadoSolicitud estado, Rol rol, String emailAdmin, Pageable pageable) {
        validarAdministrador(emailAdmin);

        if (rol != null && rol != Rol.ROLE_LIDER && rol != Rol.ROLE_ORGANIZACION) {
            throw new BadRequestException("El filtro de rol solo permite consultar solicitudes de ROLE_LIDER o ROLE_ORGANIZACION");
        }

        EstadoSolicitud estadoConsulta = (estado != null) ? estado : EstadoSolicitud.PENDIENTE;

        Page<SolicitudRegistro> paginaSolicitudes;
        if (rol != null) {
            paginaSolicitudes = solicitudRegistroRepository.findByEstadoAndUsuarioRol(estadoConsulta, rol, pageable);
        } else {
            paginaSolicitudes = solicitudRegistroRepository.findByEstado(estadoConsulta, pageable);
        }

        return paginaSolicitudes.map(solicitudRegistroMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public SolicitudRegistroResponseDto obtenerSolicitudPorId(Long id, String emailAdmin) {
        validarAdministrador(emailAdmin);

        SolicitudRegistro solicitud = solicitudRegistroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de registro no encontrada con id: " + id));

        return solicitudRegistroMapper.toResponseDto(solicitud);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SolicitudRegistroResponseDto aprobarSolicitud(Long id, AprobarSolicitudRequestDto request, String emailAdmin) {
        Usuario admin = validarAdministrador(emailAdmin);

        SolicitudRegistro solicitud = solicitudRegistroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de registro no encontrada con id: " + id));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("Solo se pueden aprobar solicitudes en estado PENDIENTE. Estado actual: " + solicitud.getEstado());
        }

        Usuario solicitante = solicitud.getUsuario();
        if (solicitante == null) {
            throw new BadRequestException("La solicitud no tiene un usuario solicitante asociado");
        }

        // Activacion de la cuenta del usuario en el sistema
        solicitante.setActivo(true);
        usuarioRepository.save(solicitante);

        // Actualizacion del estado y registro de auditoria del administrador
        solicitud.setEstado(EstadoSolicitud.APROBADA);
        if (request != null && request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            solicitud.setObservacionesAdmin(request.getObservaciones().trim());
        }
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitud.setRevisadoPor(admin);

        SolicitudRegistro guardada = solicitudRegistroRepository.save(solicitud);
        return solicitudRegistroMapper.toResponseDto(guardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SolicitudRegistroResponseDto rechazarSolicitud(Long id, RechazarSolicitudRequestDto request, String emailAdmin) {
        Usuario admin = validarAdministrador(emailAdmin);

        SolicitudRegistro solicitud = solicitudRegistroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de registro no encontrada con id: " + id));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BadRequestException("Solo se pueden rechazar solicitudes en estado PENDIENTE. Estado actual: " + solicitud.getEstado());
        }

        Usuario solicitante = solicitud.getUsuario();
        if (solicitante != null) {
            solicitante.setActivo(false);
            usuarioRepository.save(solicitante);
        }

        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setObservacionesAdmin(request.getMotivo().trim());
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitud.setRevisadoPor(admin);

        SolicitudRegistro guardada = solicitudRegistroRepository.save(solicitud);
        return solicitudRegistroMapper.toResponseDto(guardada);
    }

    private Usuario validarAdministrador(String emailAdmin) {
        Usuario admin = usuarioRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + emailAdmin));

        if (Boolean.FALSE.equals(admin.getActivo())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        if (admin.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("No tiene permisos administrativos para acceder a este recurso");
        }

        return admin;
    }
}
