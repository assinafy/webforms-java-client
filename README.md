# SDK Java Webforms Client da Assinafy

*Português · [Read in English](README.en.md)*

SDK cliente Java para a [API Assinafy](https://api.assinafy.com.br/v1/docs) — plataforma brasileira
de assinatura eletrônica.

Cobre as 89 operações do contrato oficial da API: contas, usuários, autenticação, documentos,
signatários, assignments, campos, templates, tags, webhooks e os fluxos de assinatura do signatário.

> **Dois clientes Java para a mesma API.** A Assinafy publica dois. O `com.assinafy:assinafy-sdk` é o
> atual e é onde uma integração **nova** deve começar. Este artefato é o cliente mais antigo, mantido
> para integrações que já o utilizam. Ambos expõem `com.assinafy.sdk.AssinafyClient` e ambos cobrem
> as 89 operações, mas **não** são equivalentes drop-in:
>
> | | `assinafy-sdk` | `webforms-java-client-sdk` (este) |
> |---|---|---|
> | Configuração | `AssinafyClientOptions.builder().apiKey(...)` | `new AssinafyClientOptions().setApiKey(...)` |
> | `timeoutMs` | `long` | `int` |
> | `Logger` plugável | sim | não |
> | Constante `SANDBOX_BASE_URL` | sim | não |
> | Retry automático em 429/503 | não | sim, via `maxRetries` |
>
> Portar para o `assinafy-sdk` significa, portanto, reescrever a construção do cliente e
> re-implementar o backoff por conta própria, se você depende de `maxRetries`.
>
> O `webforms` no nome do artefato é histórico. Ele não carrega nenhuma API específica de webforms, e
> não tem relação com Oracle Forms — para o qual "WebForms Java client" é o significado mais comum da
> expressão.

> **Referência completa em inglês.** Este documento cobre requisitos, instalação, construção do
> cliente e autenticação. O guia completo, que acompanha um documento da instalação até o PDF
> assinado e baixado, está em **[README.en.md](README.en.md)**.

## 1. Requisitos e instalação

- Java 25+ (o SDK é compilado e verificado no Java 25 LTS atual)
- Maven 3.9.16 ou 3.x mais recente (o wrapper fixa 3.9.16); para Gradle, use um release que suporte
  JDK 25

**Maven**

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.2.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.2.0'
```

O artefato é publicado no GitHub Packages, então o repositório precisa ser declarado uma vez no seu
build. Veja [docs/INSTALLATION.md](docs/INSTALLATION.md) para isso e para os jars de sources e
Javadoc.

## 2. Construindo o cliente

Um `AssinafyClient` por credencial e conta. Ele é thread-safe, mantém um pool de conexões
compartilhado, e foi feito para ser criado uma vez e reutilizado por toda a vida da aplicação.

Mantenha credenciais em variáveis de ambiente ou em um gerenciador de segredos de deploy. O SDK
**não** lê arquivos `.env`, então as aplicações passam os valores secretos explicitamente. Nunca
coloque credenciais no código-fonte, em logs, em mensagens de exceção, em propriedades do Maven ou em
código de navegador.

```bash
export ASSINAFY_API_KEY=sua_chave_de_api
export ASSINAFY_ACCOUNT_ID=seu_account_id
```

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;

AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID"))
    .setTimeoutMs(30_000)
    .setMaxRetries(2)); // só leituras seguras; mutações nunca são repetidas
```

| Opção | Tipo | Padrão | Descrição |
|-------|------|--------|-----------|
| `apiKey` | String | — | Credencial preferida, enviada no header `X-Api-Key` |
| `token` | String | — | Token de acesso bearer, usado só quando não há chave de API |
| `accountId` | String | — | Workspace padrão para todo método com escopo de conta |
| `baseUrl` | String | `https://api.assinafy.com.br/v1` | Raiz HTTPS da API; HTTP em loopback só é aceito em testes locais |
| `timeoutMs` | int | `30000` | Timeout de conexão, leitura e escrita; precisa ser positivo |
| `maxRetries` | int | `0` | Tentativas extras para leituras seguras em HTTP 429/503; requisições de mutação nunca são repetidas |

Existem dois atalhos para os casos comuns:

```java
// Fábrica posicional com um customizador opcional.
AssinafyClient configured = AssinafyClient.create("chave-de-api", "account-id",
    opts -> opts.setTimeoutMs(60_000));

// A partir de configuração em string (chaves snake_case ou camelCase).
AssinafyClient fromMap = AssinafyClient.fromConfig(Map.of(
    "api_key", System.getenv("ASSINAFY_API_KEY"),
    "account_id", System.getenv("ASSINAFY_ACCOUNT_ID")
));
```

Uma chave de API é a credencial certa para uma integração de back-end. Um token bearer também
funciona, e um cliente **sem credencial alguma** é válido — é o que você usa para os endpoints
públicos (login, redefinição de senha, verificação de documento e as visões públicas de documento).

```java
// Sessão bearer: Authorization: Bearer <token>
new AssinafyClient(new AssinafyClientOptions().setToken("jwt_xxx").setAccountId("acc_xxx"));

// Não autenticado: apenas login e fluxos públicos do signatário.
new AssinafyClient(new AssinafyClientOptions());
```

## Métodos de verificação do signatário

Definidos por signatário ao criar o assignment. O método de verificação e o de notificação são
**acoplados**: envie um, os dois ou nenhum — o lado que faltar é inferido. Sem nenhum dos dois, ambos
assumem `Email`.

| Método | Como funciona | Custo por signatário |
| --- | --- | --- |
| `Email` *(padrão)* | Código de uso único (OTP) por e-mail, exigido antes de assinar | Gratuito |
| `Whatsapp` | Código de uso único (OTP) por WhatsApp | Verificação gratuita; notificação 0,45 crédito, só em planos pagos |
| `DigitalCertificate` | O signatário assina com o **próprio certificado ICP-Brasil (A1/A3)**, pela extensão de navegador Web PKI, gerando uma assinatura **PAdES qualificada** | 2 créditos |

Combinações permitidas: `Email` → notifica por `Email`; `Whatsapp` → notifica por `Whatsapp`;
`DigitalCertificate` → notifica por `Email` **ou** `Whatsapp`. Apenas um método de notificação por
signatário.

### Certificado digital ICP-Brasil

Exige o recurso **Certificado Digital** na conta (planos Standard e Pro), CPF ou CNPJ em
`government_id` do signatário, e exatamente **um signatário por certificado naquele passo**. Um CPF
exige o certificado daquela pessoa (e-CPF, ou e-CNPJ que a nomeie como representante legal); um CNPJ
exige um e-CNPJ da empresa.

Estime o custo antes: a assinatura por certificado custa 2 créditos por signatário, além do custo da
notificação escolhida.

Antes de abrir o assignment, o signatário precisa confirmar os dados de identidade e aceitar os
termos. O endpoint comum de assinatura **rejeita** signatários por certificado — a assinatura deles é
produzida por um handshake de dois passos com a extensão Web PKI:

```
POST /v1/signers/certificate/start     → data.token   (token da operação Web PKI)
        ↓  o navegador assina o token com o certificado do signatário
POST /v1/signers/certificate/complete  → data.signerName
```

> Essas duas rotas são extensões implantadas **somente em produção**: o sandbox não as expõe e elas
> não constam do documento OpenAPI publicado.

Concluído o fluxo, baixar o artefato `pades` devolve a assinatura PAdES qualificada.

## Trilha de atividades e artefatos

As atividades de um documento devolvem todos os eventos registrados, cada um com um snapshot do
`payload` do evento e a `origin` da requisição (`ip`, `user-agent`).

| Artefato | Conteúdo |
| --- | --- |
| `original` | O PDF enviado, como recebido |
| `certificated` | O documento assinado, com a certificação da plataforma |
| `certificate-page` | Apenas a página de certificação |
| `pades` | Assinaturas ICP-Brasil dos signatários + caixa de certificação — só existe em documentos que tiveram signatários por certificado digital |
| `bundle` | Zip com `original`, `certificated` e `certificate-page`, mais o `pades` quando houver |

A verificação pública confere um documento assinado pelo hash da assinatura, sem autenticação.

## Ambientes

| | |
| --- | --- |
| Produção | `https://api.assinafy.com.br/v1` (padrão) |
| Sandbox | `https://sandbox.assinafy.com.br/v1` — defina via `setBaseUrl(...)`; este artefato não expõe constante de sandbox |

## Documentação

- **[README.en.md](README.en.md)** — guia completo, em inglês
- [docs/API_REFERENCE.md](docs/API_REFERENCE.md) — referência por operação
- [docs/EXAMPLES.md](docs/EXAMPLES.md) — programas executáveis mais longos
- [docs/INSTALLATION.md](docs/INSTALLATION.md) — setup de build
- [Documentação da API](https://api.assinafy.com.br/v1/docs)

## Licença

Distribuído sob a licença [MIT](LICENSE).
