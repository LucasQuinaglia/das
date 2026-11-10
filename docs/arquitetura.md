# Arquitetura e domínio

O código está em `br.com.sistemas.chamados`, com pacotes `controller`, `dto`, `entity`, `exception`, `repository` e `service`. Controllers traduzem HTTP, Services aplicam regras e transações, Repositories encapsulam JPA, Entities representam persistência e DTOs definem o contrato JSON.

## Casos de uso

```mermaid
flowchart LR
  Usuario[Usuário do sistema] --> UC1((Cadastrar cliente))
  Usuario --> UC2((Consultar clientes))
  Usuario --> UC3((Abrir chamado))
  Usuario --> UC4((Acompanhar chamado))
  Tecnico[Técnico] --> UC4
  Tecnico --> UC5((Registrar comentário))
  Tecnico --> UC6((Alterar status))
  Admin[Administrador] --> UC7((Visualizar dashboard))
  Admin --> UC8((Atribuir técnico))
  Admin --> UC9((Manter categorias e técnicos))
```

## Sequência: abertura de chamado

```mermaid
sequenceDiagram
  actor Cliente HTTP
  participant Controller
  participant Service
  participant Repository
  participant Banco H2
  Cliente HTTP->>Controller: POST /chamados (JSON)
  Controller->>Service: criar(ChamadoRequest)
  Service->>Repository: buscar cliente e categoria
  Repository->>Banco H2: SELECT
  Banco H2-->>Repository: entidades relacionadas
  Service->>Repository: salvar chamado ABERTO
  Repository->>Banco H2: INSERT
  Banco H2-->>Repository: id gerado
  Repository-->>Service: chamado persistido
  Service-->>Controller: ChamadoResponse
  Controller-->>Cliente HTTP: 201 Created + JSON
```

## Classes e camadas

```mermaid
classDiagram
  class ClienteController
  class CategoriaController
  class TecnicoController
  class ChamadoController
  class ComentarioController
  class ClienteService
  class CategoriaService
  class TecnicoService
  class ChamadoService
  class ClienteRepository
  class CategoriaRepository
  class TecnicoRepository
  class ChamadoRepository
  ClienteController --> ClienteService
  CategoriaController --> CategoriaService
  TecnicoController --> TecnicoService
  ChamadoController --> ChamadoService
  ComentarioController --> ChamadoService
  ClienteService --> ClienteRepository
  CategoriaService --> CategoriaRepository
  TecnicoService --> TecnicoRepository
  ChamadoService --> ChamadoRepository
```

## Entidade-relacionamento

```mermaid
erDiagram
  CLIENTES ||--o{ CHAMADOS : abre
  CATEGORIAS ||--o{ CHAMADOS : classifica
  TECNICOS o|--o{ CHAMADOS : atende
  CHAMADOS ||--o{ COMENTARIOS : registra
  CLIENTES {
    bigint id PK
    varchar nome
    varchar email UK
    varchar telefone
  }
  CATEGORIAS {
    bigint id PK
    varchar nome UK
    varchar descricao
  }
  TECNICOS {
    bigint id PK
    varchar nome
    varchar email UK
    varchar perfil
  }
  CHAMADOS {
    bigint id PK
    varchar titulo
    varchar descricao
    datetime data_abertura
    datetime data_fechamento
    varchar prioridade
    varchar status
    bigint cliente_id FK
    bigint categoria_id FK
    bigint tecnico_id FK
  }
  COMENTARIOS {
    bigint id PK
    varchar texto
    varchar autor
    datetime data
    bigint chamado_id FK
  }
```

## Mapa de telas planejado

```mermaid
flowchart TD
  Login[Login - futuro] --> Dashboard[Dashboard - futuro]
  Dashboard --> Clientes[Clientes: lista, novo, editar, detalhe]
  Dashboard --> Categorias[Categorias: lista e cadastro]
  Dashboard --> Tecnicos[Técnicos: lista e cadastro]
  Dashboard --> Chamados[Chamados: lista, filtros e novo]
  Clientes -. chamados do cliente .-> Chamados
  Chamados --> Detalhe[Detalhe: status, técnico e comentários]
```

O mapa de telas descreve uma evolução futura; esta entrega implementa a API REST, autenticação HTTP Basic, documentação OpenAPI e endpoint de dashboard, sem interface web.
