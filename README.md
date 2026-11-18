# DTAComp
![status](https://img.shields.io/badge/Status-Em_Desenvolvimento-yellow)
![versão](https://img.shields.io/badge/Versão-Nenhuma-red)
![license](https://img.shields.io/badge/Licença-MIT-green)

Plataforma de Divulgação de Trabalhos Acadêmicos do curso de Ciência da Computação - UFMA.

Projeto realizado como requisito para conclusão da disciplina Laboratório de Programação, 
sob a supervisão do docente Geraldo Braz Júnior.

## Arquitetura

| Camada | Local (Docker Compose) | Produção |
|---|---|---|
| Front (React + Vite) | container `front` (Nginx) | Vercel |
| API (Spring Boot) | container `api` | Render |
| Banco (PostgreSQL) | container `postgres` | Supabase |
| Arquivos (imagens e PDFs) | container `rustfs` (compatível com S3) | Cloudflare R2 |

O schema do banco é versionado com Flyway em `api/src/main/resources/db/migration`.
Para mudar o banco, crie uma nova migration (`V2__descricao.sql`, ...); nunca edite uma já aplicada.

## Rodando localmente

Pré-requisitos: Docker com Compose, Java 21 e Node 22.
Se o Docker pedir `sudo`, adicione seu usuário ao grupo e faça logout/login:

```bash
sudo usermod -aG docker $USER
```

Na pasta `DTAComp/`:

```bash
cp .env.example .env
```

### Opção 1: stack completa em containers (igual à produção)

```bash
docker compose up -d --build
```

- App: http://localhost:8081
- Console do armazenamento (RustFS): http://localhost:9001 (usuário e senha = `S3_ACCESS_KEY` / `S3_SECRET_KEY`)
- Com `SPRING_PROFILES_ACTIVE=prod,dev` no `.env`, são criados `admin@dta.dev` e `revisor@dta.dev` (senha `dev12345`).

Os dados ficam nos volumes `pgdata` e `objetos` e sobrevivem a `docker compose down`.
Apenas `docker compose down -v` apaga os volumes.

### Opção 2: desenvolvimento com hot reload

Suba só a infraestrutura e rode API e front fora do Docker:

```bash
docker compose up -d postgres rustfs bucket-init

cd api && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
cd front && npm install && npm run dev      # http://localhost:3000
```

No perfil `dev`, os arquivos vão para `api/storage/`. Para usar o RustFS, rode a API com `STORAGE_TIPO=s3`.

### Testes

```bash
cd api && ./mvnw test
```

Os testes usam um PostgreSQL real via Testcontainers, então o Docker precisa estar rodando.

## Deploy

### Banco: Supabase
1. Crie o projeto e copie a connection string do **Session pooler** (compatível com IPv4) em *Connect*.
2. Formato JDBC: `jdbc:postgresql://aws-0-<regiao>.pooler.supabase.com:5432/postgres?sslmode=require`,
   usuário `postgres.<project-ref>`.
3. As tabelas são criadas pelo Flyway na primeira subida da API.

### Arquivos: Cloudflare R2
1. Crie um bucket (privado; os arquivos são servidos pela API).
2. Em *R2 > Manage API tokens*, crie um token com permissão *Object Read & Write* nesse bucket.
3. Endpoint: `https://<account-id>.r2.cloudflarestorage.com`.

### API: Render
Use o blueprint `render.yaml` (*New > Blueprint*) ou crie um *Web Service* Docker com *Root Directory* `api`.
Variáveis de ambiente:

| Variável | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | dados do Supabase |
| `JWT_SECRET` | segredo longo e aleatório |
| `CORS_ORIGINS` | domínio do front, ex.: `https://dtacomp.vercel.app` (aceita `https://*.vercel.app`) |
| `S3_ENDPOINT`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY` | dados do R2 |

### Front: Vercel
Importe o repositório com *Root Directory* `front` (framework Vite) e defina
`VITE_API_URL=https://<sua-api>.onrender.com`. O `vercel.json` redireciona as rotas da SPA para o `index.html`.
