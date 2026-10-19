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
 * Registra lideres comunitarios y organizaciones de socorro/gubernamentales
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
                "lider.armenia@redalerta.gov.co",
                "Líder Comunitario Armenia",
                "1094900001",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Armenia",
                "Barrio Granada Carrera 14",
                "3101234567",
                Rol.ROLE_LIDER
        );

        seedUsuarioSiNoExiste(
                "lider.calarca@redalerta.gov.co",
                "Líder Comunitario Calarcá",
                "1094900002",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Calarcá",
                "Plaza de Bolívar",
                "3101234568",
                Rol.ROLE_LIDER
        );

        seedUsuarioSiNoExiste(
                "bomberos.armenia@redalerta.gov.co",
                "Cuerpo Oficial de Bomberos Armenia",
                "890000001",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Armenia",
                "Estación Central Calle 20",
                "3109990001",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "policia.armenia@redalerta.gov.co",
                "Policía Nacional Distrito Armenia",
                "890000002",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Armenia",
                "Comando Central Avenida Centenario",
                "3109990002",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "cruzroja.quindio@redalerta.gov.co",
                "Cruz Roja Colombiana Seccional Quindío",
                "890000003",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Armenia",
                "Avenida Bolívar # 10 Norte",
                "3109990003",
                Rol.ROLE_ORGANIZACION
        );

        seedUsuarioSiNoExiste(
                "epa.armenia@redalerta.gov.co",
                "Empresas Públicas de Armenia E.S.P.",
                "890000004",
                TipoDeDocumento.CEDULA,
                "Quindío",
                "Armenia",
                "Centro Administrativo Municipal CAM",
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
