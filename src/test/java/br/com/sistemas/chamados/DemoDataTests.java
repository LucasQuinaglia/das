package br.com.sistemas.chamados;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:demo-testdb")
@ActiveProfiles("demo")
class DemoDataTests {
    @Autowired private ClienteRepository clientes;
    @Autowired private CategoriaRepository categorias;
    @Autowired private TecnicoRepository tecnicos;
    @Autowired private ChamadoRepository chamados;

    @Test
    void carregaDadosDeDemonstracao() {
        assertThat(clientes.count()).isPositive();
        assertThat(categorias.count()).isPositive();
        assertThat(tecnicos.count()).isPositive();
        assertThat(chamados.count()).isPositive();
    }
}
