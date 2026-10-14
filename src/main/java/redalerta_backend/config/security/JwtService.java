package redalerta_backend.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import redalerta_backend.modules.usuario.entity.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio criptografico responsable de la generacion, firma y validacion de JSON Web Tokens (JWT).
 * Utiliza el algoritmo HMAC-SHA256 con claves simetricas parametrizadas por entorno.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpiration;

    /**
     * Genera un token JWT para la entidad Usuario, inyectando claims personalizados
     * (userId, rol y nombre).
     *
     * @param usuario Entidad del usuario autenticado.
     * @return Cadena compacta del token JWT firmado.
     */
    public String generateToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", usuario.getId());
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("nombre", usuario.getNombre());
        return buildToken(extraClaims, usuario.getEmail().valor(), jwtExpiration);
    }

    /**
     * Genera un token JWT a partir de un objeto UserDetails y un mapa de claims adicionales.
     *
     * @param extraClaims Metadatos adicionales para incluir en el payload.
     * @param userDetails Detalles del usuario reconocidos por Spring Security.
     * @return Token JWT firmado.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String username,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Valida si un token pertenece al usuario y no ha expirado.
     *
     * @param token       Cadena JWT a validar.
     * @param userDetails Detalles del usuario para cotejar el subject.
     * @return true si el token es valido y vigente.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Valida de forma autonoma si un token no ha expirado.
     *
     * @param token Cadena JWT.
     * @return true si la fecha de expiracion es posterior a la fecha actual.
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae el sujeto (subject / email) contenido en el token.
     *
     * @param token Cadena JWT.
     * @return Email o username del sujeto.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim especifico aplicando una funcion extractora sobre el payload.
     *
     * @param token          Cadena JWT.
     * @param claimsResolver Funcion de transformacion/extraccion.
     * @param <T>            Tipo de dato del claim esperado.
     * @return Valor del claim resuelto.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parsea y valida criptograficamente la firma del token para obtener todos los claims.
     *
     * @param token Cadena JWT.
     * @return Objeto Claims con el cuerpo decodificado.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (Exception e) {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
