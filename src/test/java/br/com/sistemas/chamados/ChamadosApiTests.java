package br.com.sistemas.chamados;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.repository.CategoriaRepository;
import br.com.sistemas.chamados.repository.ClienteRepository;
import br.com.sistemas.chamados.repository.TecnicoRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ChamadosApiTests {
    @Autowired private MockMvc mvc;
    @Autowired private ClienteRepository clientes;
    @Autowired private CategoriaRepository categorias;
    @Autowired private TecnicoRepository tecnicos;

    @Test
    void clientesExecutamCrudERetornamErrosEsperados() throws Exception {
        String location = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Souza","email":"ana@example.com","telefone":"47999999999"}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getHeader("Location");
        long clienteId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mvc.perform(get("/clientes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(clienteId));
        mvc.perform(get("/clientes/{id}", clienteId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
        mvc.perform(put("/clientes/{id}", clienteId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Ana Atualizada","email":"ana@example.com","telefone":"47988888888"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana Atualizada"));

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Outra Ana","email":"ana@example.com","telefone":"47988888888"}
                        """))
                .andExpect(status().isConflict());

        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"","email":"inválido","telefone":"123"}
                        """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detalhes").isArray());

        mvc.perform(delete("/clientes/{id}", clienteId)).andExpect(status().isNoContent());
        mvc.perform(get("/clientes/{id}", clienteId)).andExpect(status().isNotFound());
        mvc.perform(delete("/clientes/999999")).andExpect(status().isNotFound());
    }

    @Test
    void categoriasETecnicosImpedemDuplicidade() throws Exception {
        String categoriaLocation = mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Rede","descricao":"Conectividade"}
                        """))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        long categoriaId = Long.parseLong(categoriaLocation.substring(categoriaLocation.lastIndexOf('/') + 1));
        mvc.perform(get("/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(categoriaId));
        mvc.perform(get("/categorias/{id}", categoriaId)).andExpect(status().isOk());
        mvc.perform(put("/categorias/{id}", categoriaId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Rede cabeada","descricao":"Conectividade local"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Rede cabeada"));
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"REDE CABEADA","descricao":"Duplicada"}
                        """))
                .andExpect(status().isConflict());

        String tecnicoLocation = mvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"João","email":"joao@example.com","perfil":"TECNICO"}
                        """))
                .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        long tecnicoId = Long.parseLong(tecnicoLocation.substring(tecnicoLocation.lastIndexOf('/') + 1));
        mvc.perform(get("/tecnicos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(tecnicoId));
        mvc.perform(get("/tecnicos/{id}", tecnicoId)).andExpect(status().isOk());
        mvc.perform(put("/tecnicos/{id}", tecnicoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"João Silva","email":"joao@example.com","perfil":"ADMIN"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("ADMIN"));
        mvc.perform(post("/tecnicos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"João 2","email":"JOAO@example.com","perfil":"TECNICO"}
                        """))
                .andExpect(status().isConflict());
        mvc.perform(delete("/categorias/{id}", categoriaId)).andExpect(status().isNoContent());
        mvc.perform(delete("/tecnicos/{id}", tecnicoId)).andExpect(status().isNoContent());
    }

    @Test
    void chamadoPermiteAtribuicaoStatusComentariosFiltrosEPaginacao() throws Exception {
        Cliente cliente = clientes.save(new Cliente("Maria", "maria@example.com", "47999999999"));
        Categoria categoria = categorias.save(new Categoria("Acesso", "Contas"));
        Tecnico tecnico = tecnicos.save(new Tecnico("Caio", "caio@example.com", Perfil.TECNICO));

        String payload = """
                {"titulo":"Acesso bloqueado","descricao":"Não consigo entrar","clienteId":%d,"categoriaId":%d,"prioridade":"ALTA"}
                """
                .formatted(cliente.getId(), categoria.getId());
        String location = mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn().getResponse().getHeader("Location");
        long chamadoId = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));

        mvc.perform(get("/chamados").param("prioridade", "ALTA").param("clienteId", cliente.getId().toString())
                .param("page", "0").param("size", "5").param("sort", "titulo,asc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(chamadoId))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/clientes/{clienteId}/chamados", cliente.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(chamadoId));
        mvc.perform(get("/dashboard")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.porStatus.ABERTO").value(1))
                .andExpect(jsonPath("$.porPrioridade.ALTA").value(1));

        mvc.perform(patch("/chamados/{id}/atribuir", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"tecnicoId":%d}
                        """.formatted(tecnico.getId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tecnicoId").value(tecnico.getId()));
        mvc.perform(post("/chamados/{id}/comentarios", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"texto":"Iniciei a análise","autor":"Caio"}
                        """))
                .andExpect(status().isCreated());
        mvc.perform(get("/chamados/{id}", chamadoId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios[0].texto").value("Iniciei a análise"));

        mvc.perform(patch("/chamados/{id}/status", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"EM_ATENDIMENTO"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("EM_ATENDIMENTO"));
        mvc.perform(patch("/chamados/{id}/status", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"FECHADO"}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dataFechamento").isNotEmpty());
        mvc.perform(patch("/chamados/{id}/status", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"ABERTO"}
                        """))
                .andExpect(status().isConflict());
        mvc.perform(patch("/chamados/{id}/atribuir", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"tecnicoId":%d}
                        """.formatted(tecnico.getId())))
                .andExpect(status().isConflict());
        mvc.perform(put("/chamados/{id}", chamadoId).contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());
        mvc.perform(post("/chamados/{id}/comentarios", chamadoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"texto":"Após fechar","autor":"Caio"}
                        """))
                .andExpect(status().isConflict());
        mvc.perform(delete("/chamados/{id}", chamadoId)).andExpect(status().isNoContent());
        mvc.perform(get("/chamados/{id}", chamadoId)).andExpect(status().isNotFound());
    }
}
