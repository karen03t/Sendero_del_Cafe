package co.edu.autonoma.turismo.api;

import co.edu.autonoma.turismo.api.dto.InteresResponse;
import co.edu.autonoma.turismo.repository.InteresRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intereses")
public class InteresController {

    private final InteresRepository intereses;

    public InteresController(InteresRepository intereses) {
        this.intereses = intereses;
    }

    @GetMapping
    public List<InteresResponse> listar() {
        return intereses.findByActivoTrueOrderByNombreAsc().stream()
                .map(i -> new InteresResponse(i.getId(), i.getNombre()))
                .toList();
    }
}
