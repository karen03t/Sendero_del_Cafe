package co.edu.autonoma.turismo.service.retencion;

import co.edu.autonoma.turismo.domain.Turista;
import co.edu.autonoma.turismo.repository.TuristaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetencionServiceTest {

    private TuristaRepository turistas;
    private RetencionService servicio;

    @BeforeEach
    void setUp() {
        turistas = mock(TuristaRepository.class);
        servicio = new RetencionService(turistas, 12);
    }

    @Test
    void anonimizaLasCuentasVencidasSinBorrarLaFila() {

        Turista viejo = new Turista(
                "id-1", "Ana Perez", "ana@test.co", "hash",
                Turista.Proveedor.LOCAL, null, true);

        when(turistas.buscarParaAnonimizar(any())).thenReturn(List.of(viejo));

        LocalDateTime momento = LocalDateTime.of(2027, 1, 1, 3, 0);
        int total = servicio.anonimizarInactivos(momento);

        assertThat(total).isEqualTo(1);
        assertThat(viejo.getEstado()).isEqualTo(Turista.EstadoTurista.INACTIVO);
        assertThat(viejo.getNombreCompleto()).isEqualTo("Cuenta eliminada");
        assertThat(viejo.getCorreo()).contains("eliminado+id-1@anonimo.local");
        assertThat(viejo.getPasswordHash()).isNull();
        assertThat(viejo.getFechaEliminacion()).isEqualTo(momento);

        verify(turistas).saveAll(List.of(viejo));
    }

    @Test
    void usaElPeriodoDeRetencionConfigurado() {

        servicio.anonimizarInactivos(LocalDateTime.of(2026, 11, 27, 3, 0));

        ArgumentCaptor<LocalDateTime> limite = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(turistas).buscarParaAnonimizar(limite.capture());

        assertThat(limite.getValue()).isEqualTo(LocalDateTime.of(2025, 11, 27, 3, 0));
    }
}
