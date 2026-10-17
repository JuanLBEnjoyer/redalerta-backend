package redalerta_backend.modules.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import redalerta_backend.modules.alerta.dto.AlertaResponseDto;
import redalerta_backend.modules.alerta.entity.Alerta;
import redalerta_backend.modules.alerta.entity.EvidenciaAlerta;
import redalerta_backend.modules.alerta.mapper.AlertaMapper;
import redalerta_backend.modules.alerta.valueobject.EstadoAlerta;
import redalerta_backend.modules.alerta.valueobject.UbicacionAlerta;
import redalerta_backend.modules.usuario.entity.Usuario;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlertaMapperTest {

    private AlertaMapper mapper;
    private Usuario autor;
    private UbicacionAlerta ubicacion;

    @BeforeEach
    void setUp() {
        mapper = new AlertaMapper();
        autor = Usuario.builder()
                .id(1L)
                .nombre("Juan Perez")
                .build();
        ubicacion = new UbicacionAlerta("Quindio", "Armenia", "Calle 10 # 15-20");
    }

    @Test
    @DisplayName("Debe mapear a resumen limitando las evidencias a maximo 3 y calculando totalEvidencias")
    void toResumenResponseDtoLimitaEvidencias() {
        List<EvidenciaAlerta> evidencias = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            evidencias.add(EvidenciaAlerta.builder()
                    .id((long) i)
                    .url("https://s3.amazonaws.com/foto" + i + ".jpg")
                    .build());
        }

        Alerta alerta = Alerta.builder()
                .id(10L)
                .descripcion("Emergencia comunitaria con 5 evidencias")
                .ubicacion(ubicacion)
                .autor(autor)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .esAnonimo(false)
                .evidencias(evidencias)
                .build();

        AlertaResponseDto dto = mapper.toResumenResponseDto(alerta, 3);

        assertNotNull(dto);
        assertEquals(5, dto.getTotalEvidencias());
        assertEquals(3, dto.getEvidencias().size());
        assertEquals("Juan Perez", dto.getAutorNombre());
        assertEquals(1L, dto.getAutorId());
        assertFalse(dto.getEsAnonimo());
    }

    @Test
    @DisplayName("Debe ocultar el nombre e identificador del autor si la alerta es anonima")
    void toResumenResponseDtoAlertaAnonimaOcultaAutor() {
        Alerta alertaAnonima = Alerta.builder()
                .id(11L)
                .descripcion("Denuncia anonima de seguridad")
                .ubicacion(ubicacion)
                .autor(autor)
                .estado(EstadoAlerta.PENDIENTE_REVISION)
                .esAnonimo(true)
                .build();

        AlertaResponseDto dto = mapper.toResumenResponseDto(alertaAnonima, 3);

        assertNotNull(dto);
        assertTrue(dto.getEsAnonimo());
        assertEquals("Anónimo", dto.getAutorNombre());
        assertNull(dto.getAutorId());
    }

    @Test
    @DisplayName("Debe mapear a detalle completo retornando todas las evidencias sin recortar")
    void toResponseDtoDetalleCompletoRetornaTodasLasEvidencias() {
        List<EvidenciaAlerta> evidencias = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            evidencias.add(EvidenciaAlerta.builder()
                    .id((long) i)
                    .url("https://s3.amazonaws.com/foto" + i + ".jpg")
                    .build());
        }

        Alerta alerta = Alerta.builder()
                .id(12L)
                .descripcion("Detalle completo")
                .ubicacion(ubicacion)
                .autor(autor)
                .estado(EstadoAlerta.EN_GESTION)
                .esAnonimo(false)
                .evidencias(evidencias)
                .build();

        AlertaResponseDto dto = mapper.toResponseDto(alerta);

        assertNotNull(dto);
        assertEquals(4, dto.getTotalEvidencias());
        assertEquals(4, dto.getEvidencias().size());
    }
}
