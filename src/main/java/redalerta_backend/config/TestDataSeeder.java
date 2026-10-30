package redalerta_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import redalerta_backend.modules.usuario.entity.Usuario;
import redalerta_backend.modules.usuario.repository.UsuarioRepository;
import redalerta_backend.modules.usuario.valueobject.Documento;
import redalerta_backend.modules.usuario.valueobject.Rol;
import redalerta_backend.modules.usuario.valueobject.TipoDeDocumento;
import redalerta_backend.modules.usuario.valueobject.Ubicacion;

/**
 * Sembrador de datos iniciales para pruebas y desarrollo.
 * Registra líderes comunitarios y comités o brigadas comunitarias
 * con competencia territorial predefinida si no existen en la base de datos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsuarioSiNoExiste(
                "lider.armenia@redalerta.org",
                "Líder Comunitario Armenia",
                "1094900001",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Armenia",
                "Barrio Granada Carrera 14",
                "3101234567",
                Rol.ROLE_LIDER
        );

        seedUsuarioSiNoExiste(
                "lider.calarca@redalerta.org",
                "Líder Comunitario Calarcá",
                "1094900002",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Calarcá",
                "Plaza de Bolívar",
                "3101234568",
                Rol.ROLE_LIDER
        );

        seedUsuarioSiNoExiste(
                "brigada.emergencias.armenia@redalerta.org",
                "Brigada Comunitaria de Emergencias Armenia",
                "890000001",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Armenia",
                "Sede Comunal Barrio Granada Calle 20",
                "3109990001",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "comite.salud.armenia@redalerta.org",
                "Comité Comunitario de Salud y Primeros Auxilios",
                "890000002",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Armenia",
                "Caseta Comunal Manuela Beltrán",
                "3109990002",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "red.humanitaria.armenia@redalerta.org",
                "Comité Comunitario de Solidaridad y Apoyo Humanitario",
                "890000003",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Armenia",
                "Centro Comunitario La Fachada",
                "3109990003",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "comite.convivencia.armenia@redalerta.org",
                "Comité Barrial de Convivencia y Prevención",
                "890000004",
                TipoDeDocumento.CEDULA,
                "Quindio",
                "Armenia",
                "Casa de la Cultura Comuna 2",
                "3109990004",
                Rol.ROLE_ORGANIZACION
        );
    }

    private void seedUsuarioSiNoExiste(
            String email,
            String nombre,
            String documentoNumero,
            TipoDeDocumento documentoTipo,
            String departamento,
            String municipio,
            String direccion,
            String telefono,
            Rol rol
    ) {
        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario usuario = Usuario.builder()
                    .email(email)
                    .nombre(nombre)
                    .documento(new Documento(documentoNumero, documentoTipo))
                    .ubicacion(new Ubicacion(departamento, municipio, direccion))
                    .telefono(telefono)
                    .password(passwordEncoder.encode("Password123*"))
                    .rol(rol)
                    .activo(true)
                    .build();

            usuarioRepository.save(usuario);
            log.info("Usuario de prueba sembrado exitosamente: {} ({})", email, rol);
        }
    }
}
