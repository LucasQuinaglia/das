package br.com.sistemas.chamados.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ChamadoSpecifications;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Service
public class ChamadoService {
    private final ChamadoRepository repository;
    private final ClienteRepository clienteRepository;
    private final CategoriaRepository categoriaRepository;
    private final TecnicoRepository tecnicoRepository;

    public ChamadoService(ChamadoRepository repository, ClienteRepository clienteRepository,
            CategoriaRepository categoriaRepository, TecnicoRepository tecnicoRepository) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.categoriaRepository = categoriaRepository;
        this.tecnicoRepository = tecnicoRepository;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest request) {
        Cliente cliente = buscarCliente(request.clienteId());
        Categoria categoria = buscarCategoria(request.categoriaId());
        Chamado chamado = new Chamado(request.titulo().trim(), request.descricao().trim(), request.prioridade(), cliente, categoria);
        return ChamadoResponse.de(repository.save(chamado));
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade, Long clienteId, Pageable pageable) {
        if (clienteId != null) buscarCliente(clienteId);
        Specification<Chamado> filtro = ChamadoSpecifications.filtrar(status, prioridade, clienteId);
        return repository.findAll(filtro, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, StatusChamado status, Prioridade prioridade,
            Pageable pageable) {
        buscarCliente(clienteId);
        Specification<Chamado> filtro = ChamadoSpecifications.filtrar(status, prioridade, clienteId);
        return repository.findAll(filtro, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public ChamadoResponse buscar(Long id) { return ChamadoResponse.de(buscarEntidade(id)); }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = buscarEntidade(id);
        exigirAberto(chamado, "alterar");
        chamado.setTitulo(request.titulo().trim());
        chamado.setDescricao(request.descricao().trim());
        chamado.setPrioridade(request.prioridade());
        chamado.setCliente(buscarCliente(request.clienteId()));
        chamado.setCategoria(buscarCategoria(request.categoriaId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) { repository.delete(buscarEntidade(id)); }

    @Transactional
    public ChamadoResponse atribuir(Long id, Long tecnicoId) {
        Chamado chamado = buscarEntidade(id);
        exigirAberto(chamado, "atribuir técnico ao");
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Técnico " + tecnicoId + " não encontrado"));
        if (tecnico.getPerfil() != Perfil.TECNICO) {
            throw new RegraNegocioException("Somente usuários com perfil TECNICO podem receber chamados");
        }
        chamado.setTecnico(tecnico);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, StatusChamado novoStatus) {
        Chamado chamado = buscarEntidade(id);
        StatusChamado atual = chamado.getStatus();
        if (!transicaoPermitida(atual, novoStatus)) {
            throw new RegraNegocioException("Transição de status inválida: " + atual + " para " + novoStatus);
        }
        chamado.setStatus(novoStatus);
        chamado.setDataFechamento(novoStatus == StatusChamado.FECHADO ? LocalDateTime.now() : null);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ComentarioResponse comentar(Long id, ComentarioRequest request) {
        Chamado chamado = buscarEntidade(id);
        exigirAberto(chamado, "comentar no");
        Comentario comentario = new Comentario(request.texto().trim(), request.autor().trim());
        chamado.adicionarComentario(comentario);
        return ComentarioResponse.de(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarComentarios(Long id) {
        return buscarEntidade(id).getComentarios().stream().map(ComentarioResponse::de).toList();
    }

    private boolean transicaoPermitida(StatusChamado atual, StatusChamado novoStatus) {
        if (atual == StatusChamado.FECHADO || atual == novoStatus) return false;
        return switch (atual) {
            case ABERTO -> novoStatus == StatusChamado.EM_ATENDIMENTO || novoStatus == StatusChamado.FECHADO;
            case EM_ATENDIMENTO -> novoStatus == StatusChamado.AGUARDANDO_CLIENTE || novoStatus == StatusChamado.FECHADO;
            case AGUARDANDO_CLIENTE -> novoStatus == StatusChamado.EM_ATENDIMENTO || novoStatus == StatusChamado.FECHADO;
            case FECHADO -> false;
        };
    }

    private void exigirAberto(Chamado chamado, String acao) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            throw new RegraNegocioException("Não é possível " + acao + " chamado fechado");
        }
    }

    private Chamado buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado " + id + " não encontrado"));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente " + id + " não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada"));
    }
}
