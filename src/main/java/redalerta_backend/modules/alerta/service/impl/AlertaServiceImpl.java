package redalerta_backend.modules.alerta.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import redalerta_backend.common.exception.BadRequestException;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.common.storage.StorageService;

import java.util.List;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CancelarDireccionamientoRequestDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.DireccionarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.FiltroAlertaDto;
import redalerta_backend.modules.alerta.dto.ModificarAlertaRequestDto;
import redalerta_backend.modules.alerta.dto.OrganizacionResponseDto;
import redalerta_backend.modules.alerta.dto.UploadEvidenciaResponseDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.repository.AlertaRepository;
import redalerta_backend.modules.alerta.repository.AlertaSpecification;
import redalerta_backend.modules.alerta.service.AlertaService;
import redalerta_backend.modules.alerta.service.MensajeAlertaHelper;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Rol;

/**
 * Implementacion del servicio para la publicacion y consulta de alertas por
 * parte de ciudadanos.
 * Aplica principios Zero Trust y control de acceso contra vulnerabilidades
 * IDOR.
 */
@Service
public class AlertaServiceImpl implements AlertaService {

    private static final int MAX_EVIDENCIAS_PERMITIDAS = 5;

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaMapper alertaMapper;
    private final StorageService storageService;

    public AlertaServiceImpl(
            AlertaRepository alertaRepository,
            UsuarioRepository usuarioRepository,
            AlertaMapper alertaMapper,
            StorageService storageService) {
        super();
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaMapper = alertaMapper;
        this.storageService = storageService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto crearAlerta(CrearAlertaRequestDto request, String emailUsuario) {
        Usuario autor = buscarUsuarioPorEmail(emailUsuario);

        if (request.getEvidenciasUrls() != null && request.getEvidenciasUrls().size() > MAX_EVIDENCIAS_PERMITIDAS) {
            throw new BadRequestException("No se pueden adjuntar mas de " + MAX_EVIDENCIAS_PERMITIDAS + " evidencias por alerta");
        }

        UbicacionAlerta ubicacion = alertaMapper.toUbicacionAlerta(request.getUbicacion());
        Boolean esAnonimo = Boolean.TRUE.equals(request.getEsAnonimo());

        Alerta alerta = Alerta.builder()
                .descripcion(request.getDescripcion().trim())
                .ubicacion(ubicacion)
                .autor(autor)
                .esAnonimo(esAnonimo)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .build();

        alerta.registrarPasoHistorial(
                MensajeAlertaHelper.mensajeReportada(alerta.getDescripcion()),
                EstadoAlerta.PENDIENTE_REVISION,
                autor
        );

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

        // El filtro de departamento es un valor fijo ligado obligatoriamente a la ubicacion del usuario autenticado
        if (usuario.getUbicacion() != null && usuario.getRol() != Rol.ROLE_ADMIN) {
            filtro.setDepartamento(usuario.getUbicacion().departamento());
        }

        // Si el municipio viene en blanco, se establece en null para consultar todos los municipios del departamento
        if (filtro.getMunicipio() != null && filtro.getMunicipio().isBlank()) {
            filtro.setMunicipio(null);
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

        if (request.getEvidenciasUrls() != null && request.getEvidenciasUrls().size() > MAX_EVIDENCIAS_PERMITIDAS) {
            throw new BadRequestException("No se pueden adjuntar mas de " + MAX_EVIDENCIAS_PERMITIDAS + " evidencias por alerta");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public UploadEvidenciaResponseDto subirEvidencia(MultipartFile archivo, String emailUsuario) {
        buscarUsuarioPorEmail(emailUsuario);
        return storageService.subirEvidencia(archivo, "alertas/evidencias");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrganizacionResponseDto> listarOrganizacionesElegibles(String emailLider) {
        Usuario lider = buscarUsuarioPorEmail(emailLider);
        if (lider.getRol() != Rol.ROLE_LIDER && lider.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Acceso restringido a líderes comunitarios y administradores");
        }

        String departamento = lider.getUbicacion() != null ? lider.getUbicacion().departamento() : null;
        String municipio = lider.getUbicacion() != null ? lider.getUbicacion().municipio() : null;

        List<Usuario> organizaciones = usuarioRepository.findByRolAndUbicacionDepartamentoAndUbicacionMunicipioAndActivoTrue(
                Rol.ROLE_ORGANIZACION,
                departamento,
                municipio
        );

        return organizaciones.stream()
                .map(alertaMapper::toOrganizacionResponseDto)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto direccionarAlerta(
            Long alertaId,
            DireccionarAlertaRequestDto request,
            String emailLider
    ) {
        Usuario lider = buscarUsuarioPorEmail(emailLider);
        if (lider.getRol() != Rol.ROLE_LIDER && lider.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Solo un líder comunitario o administrador puede direccionar alertas");
        }

        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + alertaId));

        if (alerta.getEstado() != EstadoAlerta.PENDIENTE_REVISION) {
            throw new BadRequestException("Solo se pueden direccionar alertas en estado PENDIENTE_REVISION");
        }

        // Validacion de jurisdiccion territorial para lider comunitario
        if (lider.getRol() == Rol.ROLE_LIDER) {
            if (lider.getUbicacion() == null || alerta.getUbicacion() == null) {
                throw new BadRequestException("No es posible verificar la jurisdicción territorial de la alerta");
            }
            boolean mismoDepartamento = lider.getUbicacion().departamento()
                    .equalsIgnoreCase(alerta.getUbicacion().departamento());
            boolean mismoMunicipio = lider.getUbicacion().municipio()
                    .equalsIgnoreCase(alerta.getUbicacion().municipio());

            if (!mismoDepartamento || !mismoMunicipio) {
                throw new UnauthorizedException("No tiene jurisdicción territorial sobre alertas de este municipio");
            }
        }

        Usuario organizacion = usuarioRepository.findById(request.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada con id: " + request.getOrganizacionId()));

        if (organizacion.getRol() != Rol.ROLE_ORGANIZACION) {
            throw new BadRequestException("El usuario seleccionado no tiene rol de ORGANIZACION");
        }
        if (Boolean.FALSE.equals(organizacion.getActivo())) {
            throw new BadRequestException("La organización seleccionada se encuentra inactiva");
        }

        if (organizacion.getUbicacion() != null && alerta.getUbicacion() != null) {
            boolean mismoDeptoOrg = organizacion.getUbicacion().departamento()
                    .equalsIgnoreCase(alerta.getUbicacion().departamento());
            if (!mismoDeptoOrg) {
                throw new BadRequestException("La organización seleccionada no opera en el departamento de la alerta");
            }
        }

        alerta.direccionar(
                request.getCategoria(),
                request.getPrioridad(),
                organizacion,
                lider,
                request.getObservacionTecnica()
        );

        Alerta alertaGuardada = alertaRepository.save(alerta);
        return alertaMapper.toResponseDto(alertaGuardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponseDto> listarAlertasDireccionadasPorLider(String emailLider, Pageable pageable) {
        Usuario lider = buscarUsuarioPorEmail(emailLider);
        if (lider.getRol() != Rol.ROLE_LIDER && lider.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Acceso restringido a líderes comunitarios");
        }

        return alertaRepository.findByLiderComunitarioIdOrderByFechaCreacionDesc(lider.getId(), pageable)
                .map(alerta -> alertaMapper.toResumenResponseDto(alerta, 3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AlertaResponseDto> listarBandejaOrganizacion(String emailOrganizacion, Pageable pageable) {
        Usuario organizacion = buscarUsuarioPorEmail(emailOrganizacion);
        if (organizacion.getRol() != Rol.ROLE_ORGANIZACION && organizacion.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Acceso restringido a organizaciones de atención");
        }

        List<EstadoAlerta> estadosBandeja = List.of(EstadoAlerta.DIRECCIONADA, EstadoAlerta.EN_GESTION);
        return alertaRepository.findByOrganizacionAsignadaIdAndEstadoInOrderByFechaCreacionDesc(
                organizacion.getId(),
                estadosBandeja,
                pageable
        ).map(alerta -> alertaMapper.toResumenResponseDto(alerta, 3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto modificarDireccionamiento(
            Long alertaId,
            DireccionarAlertaRequestDto request,
            String emailLider
    ) {
        Usuario lider = buscarUsuarioPorEmail(emailLider);
        if (lider.getRol() != Rol.ROLE_LIDER && lider.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Solo un líder comunitario o administrador puede modificar el direccionamiento");
        }

        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + alertaId));

        if (alerta.getEstado() != EstadoAlerta.DIRECCIONADA) {
            throw new BadRequestException("Solo se puede modificar el direccionamiento de alertas en estado DIRECCIONADA");
        }

        if (lider.getRol() == Rol.ROLE_LIDER) {
            if (alerta.getLiderComunitario() != null && !alerta.getLiderComunitario().getId().equals(lider.getId())) {
                throw new UnauthorizedException("Solo el líder comunitario que direccionó la alerta puede modificar su direccionamiento");
            }
        }

        Usuario organizacion = usuarioRepository.findById(request.getOrganizacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Organización no encontrada con id: " + request.getOrganizacionId()));

        if (organizacion.getRol() != Rol.ROLE_ORGANIZACION) {
            throw new BadRequestException("El usuario seleccionado no tiene rol de ORGANIZACION");
        }
        if (Boolean.FALSE.equals(organizacion.getActivo())) {
            throw new BadRequestException("La organización seleccionada se encuentra inactiva");
        }

        if (organizacion.getUbicacion() != null && alerta.getUbicacion() != null) {
            boolean mismoDeptoOrg = organizacion.getUbicacion().departamento()
                    .equalsIgnoreCase(alerta.getUbicacion().departamento());
            if (!mismoDeptoOrg) {
                throw new BadRequestException("La organización seleccionada no opera en el departamento de la alerta");
            }
        }

        alerta.modificarDireccionamiento(
                request.getCategoria(),
                request.getPrioridad(),
                organizacion,
                lider,
                request.getObservacionTecnica()
        );

        Alerta alertaGuardada = alertaRepository.save(alerta);
        return alertaMapper.toResponseDto(alertaGuardada);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AlertaResponseDto cancelarDireccionamiento(
            Long alertaId,
            CancelarDireccionamientoRequestDto request,
            String emailLider
    ) {
        Usuario lider = buscarUsuarioPorEmail(emailLider);
        if (lider.getRol() != Rol.ROLE_LIDER && lider.getRol() != Rol.ROLE_ADMIN) {
            throw new UnauthorizedException("Solo un líder comunitario o administrador puede cancelar el direccionamiento");
        }

        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada con id: " + alertaId));

        if (alerta.getEstado() != EstadoAlerta.DIRECCIONADA) {
            throw new BadRequestException("Solo se puede cancelar el direccionamiento de alertas en estado DIRECCIONADA");
        }

        if (lider.getRol() == Rol.ROLE_LIDER) {
            if (alerta.getLiderComunitario() != null && !alerta.getLiderComunitario().getId().equals(lider.getId())) {
                throw new UnauthorizedException("Solo el líder comunitario que direccionó la alerta puede cancelar su direccionamiento");
            }
        }

        alerta.cancelarDireccionamiento(request.getMotivo(), lider);

        Alerta alertaGuardada = alertaRepository.save(alerta);
        return alertaMapper.toResponseDto(alertaGuardada);
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof redalerta_backend.config.security.UsuarioPrincipal principal) {
            Usuario usuario = principal.usuario();
            if (usuario != null && usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(email)) {
                if (Boolean.FALSE.equals(usuario.getActivo())) {
                    throw new UnauthorizedException("El usuario se encuentra inactivo");
                }
                return usuario;
            }
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        return usuario;
    }
}
