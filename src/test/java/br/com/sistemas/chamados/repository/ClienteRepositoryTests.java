package br.com.sistemas.chamados.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.sistemas.chamados.entity.Cliente;

@DataJpaTest
class ClienteRepositoryTests {
    @Autowired private ClienteRepository repository;

    @Test
    void persisteClienteEConsultaEmail() {
        Cliente salvo = repository.saveAndFlush(new Cliente("Ana Souza", "ana@example.com", "47999999999"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repository.existsByEmail("ana@example.com")).isTrue();
        assertThat(repository.findById(salvo.getId())).contains(salvo);
    }
}
