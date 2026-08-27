# Guia de Configuração da Pipeline CI/CD

## O que a pipeline faz

```
Push/PR → Build Backend (Maven + Testes) → Build Frontend (Angular + Testes)
                                                    ↓
                                    Se testes passam + push em main:
                                                    ↓
                               Deploy Frontend → Cloudflare Pages
                               Deploy Backend  → Render.com
```

## Pré-requisitos

### 1. Criar conta no Cloudflare (Frontend)

1. Acesse https://dash.cloudflare.com/sign-up
2. Crie uma conta gratuita
3. Vá em **Workers & Pages** → **Create** → **Pages** → **Connect to Git**
4. Conecte seu repositório GitHub
5. Configure:
   - Build command: (deixe vazio, a pipeline faz o build)
   - Build output directory: (deixe vazio)
6. Pegue seu **Account ID** (aparece na URL ou em Overview)
7. Crie um **API Token**: 
   - Vá em https://dash.cloudflare.com/profile/api-tokens
   - Create Token → Custom token
   - Permissions: `Cloudflare Pages: Edit`
   - Copie o token gerado

### 2. Criar conta no Render (Backend)

1. Acesse https://render.com e crie conta com GitHub
2. Clique **New** → **Web Service**
3. Conecte o repositório `pizza-order-system`
4. Configure:
   - Name: `pizza-order-system-api`
   - Region: Oregon (mais perto do Brasil)
   - Branch: `main`
   - Root Directory: `backend`
   - Runtime: Docker
   - Instance Type: **Free**
5. Em **Environment Variables**, adicione:
   - `SPRING_DATASOURCE_URL` = (URL do Neon.tech, passo 3)
   - `SPRING_DATASOURCE_USERNAME` = (seu user do Neon)
   - `SPRING_DATASOURCE_PASSWORD` = (sua senha do Neon)
   - `JWT_SECRET` = (gere uma string aleatória longa)
   - `SPRING_PROFILES_ACTIVE` = `prod`
6. Vá em **Settings** → copie o **Deploy Hook URL**

### 3. Criar banco no Neon.tech (PostgreSQL)

1. Acesse https://neon.tech e crie conta
2. Clique **New Project**
3. Nome: `pizzaria-db`
4. Region: São Paulo (se disponível) ou US East
5. Copie a **Connection String** que aparece:
   ```
   postgresql://user:password@ep-xxx.us-east-2.aws.neon.tech/pizzaria_db?sslmode=require
   ```
6. Use essa URL no Render (passo 2, variável SPRING_DATASOURCE_URL)

### 4. Configurar Secrets no GitHub

1. No repositório GitHub, vá em **Settings** → **Secrets and variables** → **Actions**
2. Clique **New repository secret** para cada um:

| Secret Name | Valor | De onde pegar |
|---|---|---|
| `CLOUDFLARE_API_TOKEN` | Token da API | Cloudflare (passo 1) |
| `CLOUDFLARE_ACCOUNT_ID` | Account ID | URL do dashboard Cloudflare |
| `RENDER_DEPLOY_HOOK_URL` | Deploy Hook URL | Render Settings (passo 2) |

### 5. Configurar o Angular para apontar para o backend

No arquivo `frontend/src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://pizza-order-system-api.onrender.com',
  wsUrl: 'wss://pizza-order-system-api.onrender.com/ws'
};
```

## Como funciona o fluxo

### Em desenvolvimento (local)
```
docker-compose up → PostgreSQL local + Backend + Frontend
Acessa: http://localhost:80
```

### Pull Request (CI)
```
Abre PR → Pipeline roda → Build + Testes
Se falhar: PR bloqueado (optional: branch protection)
Se passar: ✅ pronto para merge
```

### Deploy em produção
```
Merge PR na main → Pipeline roda → Build + Testes → Deploy automático
Frontend: https://pizza-order-system.pages.dev
Backend: https://pizza-order-system-api.onrender.com
```

## URLs finais (após deploy)

| O que | URL |
|---|---|
| App (cliente, garçom, cozinha) | `https://pizza-order-system.pages.dev` |
| API Backend | `https://pizza-order-system-api.onrender.com` |
| Banco de dados | Neon.tech (acesso só pelo backend) |

## Notas importantes

- **Render free tier**: O backend "dorme" após 15 minutos sem acesso. A primeira requisição após dormir leva ~30s para acordar. Para produção real com clientes, considere o plano $7/mês que mantém o serviço ativo.
- **Cloudflare Pages**: Sem limitação de "dormir". O frontend fica sempre disponível.
- **Neon.tech**: O banco fica ativo por 5 minutos de inatividade (free tier). Para uso contínuo, considere manter um health check.
- **Domínio personalizado**: Pode ser adicionado depois tanto no Cloudflare Pages quanto no Render sem custo extra.
