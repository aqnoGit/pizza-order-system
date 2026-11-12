# pizza-order-system
Sistema de gerenciamento de comandas para uma pizzaria de pequeno porte. O sistema permite que clientes façam pedidos, garçons gerenciem mesas e comandas, e a cozinha acompanhe e gerencie a preparação dos pedidos. O sistema suporta tanto pedidos presenciais (via garçom) quanto pedidos para entrega (via cliente).

## Estrutura

```
/backend   -> API Java 17 + Spring Boot 3 (Maven wrapper)
/frontend  -> app Angular (standalone, SCSS, Vitest)
/.github   -> CI/CD (GitHub Actions) e Checkstyle
```

## Ambiente com Docker

Requer apenas Docker (com Compose v2). Copie as variaveis de exemplo antes de subir:

```bash
cp .env.example .env
docker compose up -d --build   # postgres + backend (:8080) + frontend (:4200)
docker compose up -d postgres  # so o banco (127.0.0.1:5432)
docker compose down            # derruba (dados ficam no volume postgres_data)
```

As imagens vêm de `backend/Dockerfile` (JAR no `eclipse-temurin:17-jre-alpine`) e `frontend/Dockerfile` (build Angular servido pelo nginx, com fallback de SPA em `frontend/nginx.conf`).

Testes sem instalar Java/Node (servicos do profile `tools`):

```bash
docker compose run --rm mvn verify
docker compose run --rm web npm ci
docker compose run --rm web npx ng test --watch=false
```

O `node_modules` do servico `web` fica no volume `web_node_modules`, separado do `node_modules` local.

## Como rodar

**Backend** (requer PostgreSQL: suba com `docker compose up -d postgres`; para outro endereco, configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`)
```bash
cd backend
./mvnw spring-boot:run
./mvnw -B verify          # testes
```

**Frontend** (Node 24 LTS)
```bash
cd frontend
npm ci
npm start                 # http://localhost:4200
npx ng test --watch=false
npx ng build
```
