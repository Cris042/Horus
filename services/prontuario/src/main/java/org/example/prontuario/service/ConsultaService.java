package org.example.prontuario.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.prontuario.domain.Consulta;
import org.example.prontuario.domain.Prontuario;
import org.example.prontuario.domain.StatusConsulta;

import java.time.OffsetDateTime;
import java.util.List;

/** Casos de uso de consulta (RF-008..010). */
@ApplicationScoped
public class ConsultaService {

    @Inject
    ProntuarioService prontuarios;

    /** Registra uma consulta vinculada a um prontuário (RF-008). */
    @Transactional
    public Consulta registrar(Long prontuarioId, String descricao) {
        Prontuario prontuario = prontuarios.buscar(prontuarioId);
        Consulta c = new Consulta();
        c.prontuario = prontuario;
        c.descricao = descricao;
        c.persist();
        return c;
    }

    /** Atualiza a descrição de uma consulta em andamento (RF-009). */
    @Transactional
    public Consulta atualizar(Long consultaId, String descricao) {
        Consulta c = buscar(consultaId);
        if (c.finalizada()) {
            throw new ConflitoConsultaException("Consulta " + consultaId + " já finalizada; não pode ser atualizada");
        }
        c.descricao = descricao;
        c.atualizadoEm = OffsetDateTime.now();
        return c;
    }

    /** Finaliza uma consulta, tornando-a imutável (RF-010). */
    @Transactional
    public Consulta finalizar(Long consultaId) {
        Consulta c = buscar(consultaId);
        if (c.finalizada()) {
            throw new ConflitoConsultaException("Consulta " + consultaId + " já está finalizada");
        }
        c.status = StatusConsulta.FINALIZADA;
        c.finalizadoEm = OffsetDateTime.now();
        return c;
    }

    public Consulta buscar(Long consultaId) {
        Consulta c = Consulta.findById(consultaId);
        if (c == null) {
            throw new NotFoundException("Consulta " + consultaId + " não encontrada");
        }
        return c;
    }

    public List<Consulta> listarDoProntuario(Long prontuarioId) {
        prontuarios.buscar(prontuarioId); // 404 se o prontuário não existir
        return Consulta.list("prontuario.id", prontuarioId);
    }
}
