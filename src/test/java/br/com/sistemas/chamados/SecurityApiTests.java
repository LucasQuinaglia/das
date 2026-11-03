package br.com.sistemas.chamados;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityApiTests {
    @Autowired private MockMvc mvc;

    @Test
    void protegeApiEAplicaPermissoesPorPerfil() throws Exception {
        mvc.perform(get("/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/clientes").header("Authorization", basic("tecnico", "tecnico1234")))
                .andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());

        String categoria = """
                {"nome":"Infraestrutura","descricao":"Equipamentos e rede"}
                """;
        mvc.perform(post("/categorias").header("Authorization", basic("tecnico", "tecnico1234"))
                .contentType(MediaType.APPLICATION_JSON).content(categoria))
                .andExpect(status().isForbidden());
        mvc.perform(post("/categorias").header("Authorization", basic("admin", "admin1234"))
                .contentType(MediaType.APPLICATION_JSON).content(categoria))
                .andExpect(status().isCreated());
    }

    private String basic(String username, String password) {
        String value = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
