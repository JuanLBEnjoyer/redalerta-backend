package redalerta_backend.modules.alerta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.repository.AlertaRepository;
import redalerta_backend.modules.alerta.repository.AlertaSpecification;
import redalerta_backend.modules.alerta.service.AlertaService;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;

/**
 * Implementacion del servicio para la publicacion y consulta de alertas por
 * parte de ciudadanos.
 * Aplica principios Zero Trust y control de acceso contra vulnerabilidades
 * IDOR.
 */
@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaMapper alertaMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto crearAlerta(CrearAlertaRequestDto request, String emailUsuario) {
        Usuario autor = buscarUsuarioPorEmail(emailUsuario);

        UbicacionAlerta ubicacion = alertaMapper.toUbicacionAlerta(request.getUbicacion());
        Boolean esAnonimo = Boolean.TRUE.equals(request.getEsAnonimo());

        Alerta alerta = Alerta.builder()
                .descripcion(request.getDescripcion().trim())
                .ubicacion(ubicacion)
                .autor(autor)
                .esAnonimo(esAnonimo)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .build();

        if (request.getEvidenciasUrls() != null && !request.getEvidenciasUrls().isEmpty()) {
            for (String url : request.getEvidenciasUrls()) {
                if (url != null && !url.isBlank()) {
                    EvidenciaAlerta evidencia = EvidenciaAlerta.builder()
                            .url(url.trim())
                            .build();
                    alerta.agregarEvidencia(evidencia);
                }
            }
        }

        Alerta alertaGuardada = alertaRepository.save(alerta);
        return alertaMapper.toResponseDto(alertaGuardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponseDto> listarMisAlertas(String emailUsuario, Pageable pageable) {
        Usuario autor = buscarUsuarioPorEmail(emailUsuario);
        return alertaRepository.findByAutorIdOrderByFechaCreacionDesc(autor.getId(), pageable)
                .map(alerta -> alertaMapper.toResumenResponseDto(alerta, 3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponseDto> listarAlertas(FiltroAlertaDto filtro, String emailUsuario, Pageable pageable) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);

        if (filtro == null) {
            filtro = new FiltroAlertaDto();
        }

        // Si no se proporcionaron filtros geograficos, por defecto se filtran por la ubicacion del usuario
        if ((filtro.getDepartamento() == null || filtro.getDepartamento().isBlank()) &&
                (filtro.getMunicipio() == null || filtro.getMunicipio().isBlank())) {
            if (usuario.getUbicacion() != null) {
                filtro.setDepartamento(usuario.getUbicacion().departamento());
                filtro.setMunicipio(usuario.getUbicacion().municipio());
            }
        }

        if (filtro.getFechaInicio() != null && filtro.getFechaFin() != null) {
            if (filtro.getFechaInicio().isAfter(filtro.getFechaFin())) {
                throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
            }
        }

        Specification<Alerta> spec = AlertaSpecification.conFiltros(filtro);
        Page<Alerta> alertasPage = alertaRepository.findAll(spec, pageable);

        return alertasPage.map(alerta -> alertaMapper.toResumenResponseDto(alerta, 3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AlertaResponseDto obtenerAlertaPorId(Long id, String emailUsuario) {
        buscarUsuarioPorEmail(emailUsuario);

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        return alertaMapper.toResponseDto(alerta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void eliminarAlerta(Long id, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        // Prevencion IDOR: Solo el autor puede eliminar su alerta
        if (!alerta.getAutor().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No tiene permisos para eliminar esta alerta");
        }

        // Regla de negocio: Solo se pueden eliminar alertas en estado
        // PENDIENTE_REVISION
        if (alerta.getEstado() != EstadoAlerta.PENDIENTE_REVISION) {
            throw new BadRequestException("Solo se pueden eliminar alertas en estado PENDIENTE_REVISION");
        }

        alertaRepository.delete(alerta);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto modificarAlerta(Long id, ModificarAlertaRequestDto request, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);

        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + id));

        // Prevencion IDOR: Solo el autor puede modificar su alerta
        if (!alerta.getAutor().getId().equals(usuario.getId())) {
            throw new UnauthorizedException("No tiene permisos para modificar esta alerta");
        }

        // Regla de negocio: Solo se pueden modificar alertas en estado
        // PENDIENTE_REVISION
        if (alerta.getEstado() != EstadoAlerta.PENDIENTE_REVISION) {
            throw new BadRequestException("Solo se pueden modificar alertas en estado PENDIENTE_REVISION");
        }

        alerta.setDescripcion(request.getDescripcion().trim());
        alerta.setUbicacion(alertaMapper.toUbicacionAlerta(request.getUbicacion()));

        if (request.getEvidenciasUrls() != null) {
            alerta.getEvidencias().clear();
            for (String url : request.getEvidenciasUrls()) {
                if (url != null && !url.isBlank()) {
                    EvidenciaAlerta evidencia = EvidenciaAlerta.builder()
                            .url(url.trim())
                            .build();
                    alerta.agregarEvidencia(evidencia);
                }
            }
        }

        if (request.getEsAnonimo() != null) {
            alerta.setEsAnonimo(request.getEsAnonimo());
        }

        Alerta alertaActualizada = alertaRepository.save(alerta);
        return alertaMapper.toResponseDto(alertaActualizada);
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        return usuario;
    }
}
