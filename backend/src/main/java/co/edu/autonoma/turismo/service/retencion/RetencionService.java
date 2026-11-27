package co.edu.autonoma.turismo.service.retencion;

import co.edu.autonoma.turismo.domain.Turista;
import co.edu.autonoma.turismo.repository.TuristaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RNF-02 / Ley 1581: anonimiza las cuentas sin actividad durante el periodo
 * de retencion (12 meses por defecto). Conserva la fila para no romper las
 * rutas guardadas, pero elimina los datos personales.
 */
@Service
public class RetencionService {

    private static final Logger log = LoggerFactory.getLogger(RetencionService.class);

    private final TuristaRepository turistas;
    private final int mesesRetencion;

    public RetencionService(
            TuristaRepository turistas,
            @Value("${RETENCION_MESES:12}") int mesesRetencion) {

        this.turistas = turistas;
        this.mesesRetencion = mesesRetencion;
    }

    @Scheduled(cron = "${RETENCION_CRON:0 0 3 * * *}")
    public void purgarInactivos() {
        int total = anonimizarInactivos(LocalDateTime.now());
        if (total > 0) {
            log.info("Retencion Ley 1581: {} cuentas anonimizadas", total);
        }
    }

    @Transactional
    public int anonimizarInactivos(LocalDateTime momento) {

        LocalDateTime limite = momento.minusMonths(mesesRetencion);
        List<Turista> inactivos = turistas.buscarParaAnonimizar(limite);

        for (Turista turista : inactivos) {
            turista.anonimizar(momento);
        }

        turistas.saveAll(inactivos);

        return inactivos.size();
    }
}
