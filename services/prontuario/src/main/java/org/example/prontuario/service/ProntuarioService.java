package org.example.prontuario.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.prontuario.domain.Prontuario;

import java.util.List;

/** Casos de uso de prontuário (RF-006/007). Persiste apenas em {@code prontuario_db} (RF-025). */
@ApplicationScoped
public class ProntuarioService {

    @Transactional
    public Prontuario criar(String pacienteId) {
        Prontuario p = new Prontuario();
        p.pacienteId = pacienteId;
        p.persist();
        return p;
    }

    public Prontuario buscar(Long id) {
        Prontuario p = Prontuario.findById(id);
        if (p == null) {
            throw new NotFoundException("Prontuário " + id + " não encontrado");
        }
        return p;
    }

    public List<Prontuario> listar() {
        return Prontuario.listAll();
    }
}
