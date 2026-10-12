package redalerta_backend.modules.alerta.controller;

import redalerta_backend.common.dto.ApiResponse;
import redalerta_backend.common.exception.ResourceNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alertas")
public class AlertaController {

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("Modulo de Alertas activo", "Operacion exitosa"));
    }

    @GetMapping("/prueba-error/{id}")
    public ResponseEntity<ApiResponse<String>> pruebaError(@PathVariable Long id) {
        if (id == 0) {
            throw new ResourceNotFoundException("Alerta", "id", id);
        }
        return ResponseEntity.ok(ApiResponse.success("Alerta " + id + " encontrada"));
    }

}