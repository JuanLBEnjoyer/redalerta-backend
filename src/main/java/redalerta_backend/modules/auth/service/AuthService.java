package redalerta_backend.modules.auth.service;

import redalerta_backend.modules.auth.dto.RegistroRequestDto;
import redalerta_backend.modules.usuario.dto.UsuarioResponseDto;

public interface AuthService {

    UsuarioResponseDto registrar(RegistroRequestDto request);
}
