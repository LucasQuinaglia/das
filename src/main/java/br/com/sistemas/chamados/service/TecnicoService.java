package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) { this.repository = repository; }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest request) {
        validarEmailUnico(request.email(), null);
        return TecnicoResponse.de(repository.save(new Tecnico(request.nome().trim(), request.email().trim().toLowerCase(), request.perfil())));
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return repository.findAll(Sort.by("nome")).stream().map(TecnicoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscar(Long id) { return TecnicoResponse.de(buscarEntidade(id)); }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscarEntidade(id);
        validarEmailUnico(request.email(), id);
        tecnico.setNome(request.nome().trim());
        tecnico.setEmail(request.email().trim().toLowerCase());
        tecnico.setPerfil(request.perfil());
        return TecnicoResponse.de(tecnico);
    }

    @Transactional
    public void excluir(Long id) { repository.delete(buscarEntidade(id)); }

    private Tecnico buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Técnico " + id + " não encontrado"));
    }

    private void validarEmailUnico(String email, Long id) {
        String normalizado = email.trim().toLowerCase();
        boolean duplicado = id == null ? repository.existsByEmailIgnoreCase(normalizado)
                : repository.existsByEmailIgnoreCaseAndIdNot(normalizado, id);
        if (duplicado) throw new RegraNegocioException("Já existe um técnico com o e-mail " + normalizado);
    }
}
