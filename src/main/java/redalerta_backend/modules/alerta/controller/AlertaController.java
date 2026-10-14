package redalerta_backend.modules.alerta.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST encargado de las operaciones del modulo de alertas
 * comunitarias.
 */
@Tag(name = "Alertas", description = "Endpoints para gestion y monitoreo de alertas comunitarias")
@RestController
@RequestMapping("/alertas")
public class AlertaController {

}