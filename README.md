# Sistema de Chamados

API REST didática para gerenciamento de clientes, categorias, técnicos, chamados e comentários, organizada em camadas com Spring Boot, Spring Data JPA, Bean Validation e H2.

## Executar

Requisitos: Java 21 e Maven Wrapper.

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080`. O H2 Console fica em `http://localhost:8080/h2-console` com JDBC URL `jdbc:h2:mem:chamadosdb`, usuário `sa` e senha vazia. O banco é em memória e perde os dados ao reiniciar a aplicação.

## Rotas

| Recurso | Rotas |
| --- | --- |
| Clientes | `POST/GET /clientes`, `GET/PUT/DELETE /clientes/{id}` |
| Categorias | `POST/GET /categorias`, `GET/PUT/DELETE /categorias/{id}` |
| Técnicos | `POST/GET /tecnicos`, `GET/PUT/DELETE /tecnicos/{id}` |
| Chamados | `POST/GET /chamados`, `GET/PUT/DELETE /chamados/{id}` |
| Ações do chamado | `PATCH /chamados/{id}/atribuir`, `PATCH /chamados/{id}/status` |
| Comentários | `POST/GET /chamados/{id}/comentarios` |
| Chamados do cliente | `GET /clientes/{id}/chamados` |
| Dashboard | `GET /dashboard` (contagem por status e prioridade) |

Chamados aceitam filtros opcionais `status`, `prioridade` e `clienteId`, paginação `page` e `size` (padrão 20, máximo 100) e ordenação `sort`, por exemplo `GET /chamados?status=ABERTO&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc`.

## Exemplos rápidos

```powershell
curl.exe -X POST http://localhost:8080/clientes -H "Content-Type: application/json" -d '{"nome":"Ana Souza","email":"ana@example.com","telefone":"47999999999"}'
curl.exe -X POST http://localhost:8080/categorias -H "Content-Type: application/json" -d '{"nome":"Acesso","descricao":"Contas e permissões"}'
curl.exe -X POST http://localhost:8080/tecnicos -H "Content-Type: application/json" -d '{"nome":"Caio","email":"caio@example.com","perfil":"TECNICO"}'
curl.exe -X POST http://localhost:8080/chamados -H "Content-Type: application/json" -d '{"titulo":"Acesso bloqueado","descricao":"Não consigo entrar","clienteId":1,"categoriaId":1,"prioridade":"ALTA"}'
```

O chamado nasce em `ABERTO` com data de abertura preenchida. Atribuição exige técnico com perfil `TECNICO`. Transições aceitas: `ABERTO` para `EM_ATENDIMENTO` ou `FECHADO`; `EM_ATENDIMENTO` para `AGUARDANDO_CLIENTE` ou `FECHADO`; `AGUARDANDO_CLIENTE` para `EM_ATENDIMENTO` ou `FECHADO`. Um chamado fechado não pode ser alterado, reatribuído ou receber comentários.

Erros seguem o formato `{ momento, status, erro, detalhes }`: validação retorna 400, recurso ausente 404 e conflito de regra ou integridade 409.

## Autenticação e documentação da API

As rotas exigem HTTP Basic. O usuário `admin` tem perfil `ADMIN`; `tecnico` tem perfil `TECNICO`. Para desenvolvimento local, as senhas padrão são `admin1234` e `tecnico1234`; configure `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `TECHNICIAN_USERNAME` e `TECHNICIAN_PASSWORD` antes de usar outros ambientes. O administrador mantém clientes, categorias e técnicos e atribui chamados. Técnicos podem consultar a API, alterar status e registrar comentários.

Swagger UI: `http://localhost:8080/swagger-ui.html`. Especificação OpenAPI: `http://localhost:8080/v3/api-docs`. A página de documentação é pública; use o botão **Authorize** para enviar credenciais ao testar as rotas protegidas.

## Perfis opcionais

Para carregar registros de demonstração no H2, inicie com `--spring.profiles.active=demo`. A carga é idempotente e cria cliente, categoria, técnico, chamado e comentário quando não existem registros.

Para PostgreSQL, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` e execute:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

O perfil `postgres` usa `jdbc:postgresql://localhost:5432/chamados` como URL padrão e desativa o console H2.

## Verificar

```powershell
.\mvnw.cmd clean test
```

Os diagramas de caso de uso, sequência, classes, entidade-relacionamento e mapa de telas estão em [`docs/arquitetura.md`](docs/arquitetura.md).
