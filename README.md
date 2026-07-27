# Marluse API — Demo (Portfólio)

Backend do SaaS **Marluse**, um sistema de gestão para loja de materiais de construção (vendas, locações, estoque, clientes e financeiro). Esta é a API do **ambiente de demonstração público**, com dados fictícios reiniciados automaticamente.

🔗 **Demo ao vivo:** https://maluse-demo.vercel.app · 👤 `demo@marluse.com` / `demo123`
🖥️ **Frontend:** [repositório](https://github.com/zeus-crf/maluse_frontend_demo) (Angular 22)

---

## 🧱 Stack

- **Spring Boot 4** · Java 21
- **MySQL 8** · Flyway (migrations versionadas)
- **Spring Security** + JWT: access token curto + refresh token em **cookie httpOnly**
- Lombok · Bean Validation · Spring Actuator
- Testes: JUnit 5 + H2 em memória

---

## 🗂️ Arquitetura

Organização por **domínio** (package-by-feature), cada um com `controller`, `service`, `repository`, `model`, `dto`:

```
clientes · estoque (produtos + fornecedores) · vendas (pedidos)
locacoes · financeiro (lançamentos + abatimentos) · entrega
dashboard · relatorios · security
```

Destaques:
- **JWT com refresh em cookie httpOnly** — access token de curta duração renovado automaticamente.
- **Schedulers** — marcam locações atrasadas e geram lançamentos recorrentes.
- **Flyway** — schema versionado (V1…V11).
- **Perfil `demo`** — semeia dados realistas no boot e **reinicia o banco a cada 6h** (ver abaixo).

---

## 🔒 Modo demonstração (perfil `demo`)

Ativado com `SPRING_PROFILES_ACTIVE=docker,demo`. Quando ligado:

- `DemoDataService` gera dados fictícios coerentes em todos os módulos (clientes, produtos, pedidos, locações, financeiro), com datas espalhadas para os gráficos ficarem preenchidos.
- `DemoSeedRunner` semeia no primeiro boot (se o banco estiver vazio).
- `DemoResetScheduler` **limpa e re-semeia a cada 6h**, devolvendo o demo ao estado ideal.
- Um usuário público (`demo@marluse.com`) é provisionado; cadastro público fica desativado.

Fora do perfil `demo`, nada disso sobe — é código isolado do runtime normal.

---

## 🚀 Rodar localmente

Com Docker (sobe API + MySQL e semeia os dados):

```bash
cp .env.example .env   # ajuste as variáveis
docker compose up --build
```

Sem Docker (precisa de um MySQL local e Java 21):

```bash
./mvnw spring-boot:run
```

Rodar os testes:

```bash
./mvnw test
```

Health check: `GET /actuator/health` → `{"status":"UP"}`

---

## ⚙️ Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `SPRING_PROFILES_ACTIVE` | `docker,demo` para ligar o modo demonstração |
| `DB_PASSWORD` | senha do MySQL |
| `JWT_SECRET` | segredo para assinatura do JWT (256 bits) |
| `CORS_ORIGIN` | origem permitida (URL do frontend) |
| `ADMIN_EMAIL` / `ADMIN_SENHA` | admin interno |
| `APP_DEMO_RESET_INTERVAL_MS` | intervalo de reset (opcional, default 6h) |

---

*Projeto de portfólio. O sistema original foi desenvolvido para um cliente real; este demo usa uma base de código separada, sem dados reais.*
