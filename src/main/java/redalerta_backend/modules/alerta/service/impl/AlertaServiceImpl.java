package redalerta_backend.modules.alerta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redalerta_backend.common.exception.ResourceNotFoundException;
import redalerta_backend.common.exception.UnauthorizedException;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.dto.CrearAlertaRequestDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.repository.AlertaRepository;
import redalerta_backend.modules.alerta.service.AlertaService;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;

/**
 * Implementacion del servicio enfocado exclusivamente en la publicacion de alertas por ciudadanos.
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
        Usuario autor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + emailUsuario));

        if (Boolean.FALSE.equals(autor.getActivo())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        UbicacionAlerta ubicacion = alertaMapper.toUbicacionAlerta(request.getUbicacion());

        Alerta alerta = Alerta.builder()
                .descripcion(request.getDescripcion().trim())
                .ubicacion(ubicacion)
                .autor(autor)
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
}
