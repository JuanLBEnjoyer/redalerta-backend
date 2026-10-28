package redalerta_backend.common.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistException extends CustomException {
    public EmailAlreadyExistException(String email) {
        super(String.format("El correo electrónico '%s' ya se encuentra registrado", email), HttpStatus.CONFLICT);
    }
}
