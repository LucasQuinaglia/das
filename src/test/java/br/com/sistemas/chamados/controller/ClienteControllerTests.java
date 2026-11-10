package br.com.sistemas.chamados.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.service.ClienteService;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTests {
    @Autowired private MockMvc mvc;
    @MockitoBean private ClienteService service;

    @Test
    void listaClientesComStatusOk() throws Exception {
        given(service.listar()).willReturn(List.of(new ClienteResponse(1L, "Ana", "ana@example.com", null)));

        mvc.perform(get("/clientes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void criaClienteComStatusCreated() throws Exception {
        given(service.criar(any(ClienteRequest.class)))
                .willReturn(new ClienteResponse(7L, "Ana", "ana@example.com", null));

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana","email":"ana@example.com","telefone":"47999999999"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }
}
