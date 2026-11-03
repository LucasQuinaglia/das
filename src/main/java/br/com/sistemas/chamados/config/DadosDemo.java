package br.com.sistemas.chamados.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@Configuration
@Profile("demo")
public class DadosDemo {
    @Bean
    ApplicationRunner carregarDadosDemo(ClienteRepository clientes, CategoriaRepository categorias,
            TecnicoRepository tecnicos, ChamadoRepository chamados) {
        return args -> {
            Cliente cliente = clientes.findAll().stream().findFirst()
                    .orElseGet(() -> clientes.save(new Cliente("Ana Souza", "ana@example.com", "47999999999")));
            Categoria categoria = categorias.findAll().stream().findFirst()
                    .orElseGet(() -> categorias.save(new Categoria("Acesso", "Contas e permissões")));
            Tecnico tecnico = tecnicos.findAll().stream().findFirst()
                    .orElseGet(() -> tecnicos.save(new Tecnico("Caio Silva", "caio@example.com", Perfil.TECNICO)));

            if (chamados.count() == 0) {
                Chamado chamado = new Chamado("Acesso bloqueado", "Não consigo entrar no sistema",
                        Prioridade.ALTA, cliente, categoria);
                chamado.setTecnico(tecnico);
                chamado.adicionarComentario(new Comentario("Chamado criado para demonstração", tecnico.getNome()));
                chamados.save(chamado);
            }
        };
    }
}
