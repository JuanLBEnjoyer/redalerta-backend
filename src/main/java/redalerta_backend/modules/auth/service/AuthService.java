package redalerta_backend.modules.auth.service;

import redalerta_backend.modules.auth.dto.LoginRequestDto;
import redalerta_backend.modules.auth.dto.LoginResponseDto;
import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

public interface AuthService {

    UsuarioResponseDto registrar(RegistroRequestDto request);

    LoginResponseDto login(LoginRequestDto request);
}
