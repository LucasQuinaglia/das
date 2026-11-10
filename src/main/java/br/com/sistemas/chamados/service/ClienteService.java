package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ClienteRepository;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest dto) {
        String email = normalizarEmail(dto.email());
        if (repository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe um cliente com o e-mail " + email);
        }
        Cliente salvo = repository.save(new Cliente(dto.nome().trim(), email, dto.telefone()));
        return ClienteResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntity(id));
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest dto) {
        Cliente cliente = buscarEntity(id);
        String email = normalizarEmail(dto.email());
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new RegraNegocioException("Já existe um cliente com o e-mail " + email);
        }

        cliente.setNome(dto.nome().trim());
        cliente.setEmail(email);
        cliente.setTelefone(dto.telefone());
        return ClienteResponse.de(cliente);

    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Cliente buscarEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado"));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
