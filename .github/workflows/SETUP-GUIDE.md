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

### Configurar o Angular para apontar para o backend

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
