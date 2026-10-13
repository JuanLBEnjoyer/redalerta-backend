package redalerta_backend.modules.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.auth.service.AuthService;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> registrar(
            @Valid @RequestBody RegistroRequestDto request) {
        UsuarioResponseDto nuevoUsuario = authService.registrar(request);
        return new ResponseEntity<>(
                ApiResponse.success(nuevoUsuario, "Usuario registrado exitosamente"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto loginResponse = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success(loginResponse, "Inicio de sesión exitoso")
        );
    }
}
