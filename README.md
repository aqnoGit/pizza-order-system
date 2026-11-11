# pizza-order-system
Sistema de gerenciamento de comandas para uma pizzaria de pequeno porte. O sistema permite que clientes façam pedidos, garçons gerenciem mesas e comandas, e a cozinha acompanhe e gerencie a preparação dos pedidos. O sistema suporta tanto pedidos presenciais (via garçom) quanto pedidos para entrega (via cliente).

## Estrutura

```
/backend   -> API Java 17 + Spring Boot 3 (Maven wrapper)
/frontend  -> app Angular (standalone, SCSS, Vitest)
/.github   -> CI/CD (GitHub Actions) e Checkstyle
```

## Como rodar

**Backend** (requer PostgreSQL; configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`)
```bash
cd backend
./mvnw spring-boot:run
./mvnw -B verify          # testes (perfil "test" usa H2 em modo PostgreSQL)
```

**Frontend** (Node 24 LTS)
```bash
cd frontend
npm ci
npm start                 # http://localhost:4200
npx ng test --watch=false
npx ng build
```
