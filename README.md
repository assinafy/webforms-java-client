# SDK Java Webforms Client da Assinafy

*Português · [Read in English](README.en.md)*

SDK cliente Java para a [API Assinafy](https://api.assinafy.com.br/v1/docs) — plataforma brasileira
de assinatura eletrônica.

Cobre as 93 operações do contrato oficial da API: contas, usuários, autenticação, OAuth, documentos,
signatários, assignments, campos, templates, tags, webhooks e os fluxos de assinatura do signatário.

> **Dois clientes Java para a mesma API.** A Assinafy publica dois. O `com.assinafy:assinafy-sdk` é o
> atual e é onde uma integração **nova** deve começar. Este artefato é o cliente mais antigo, mantido
> para integrações que já o utilizam. Ambos expõem `com.assinafy.sdk.AssinafyClient` e ambos cobrem
> todas as operações documentadas, mas **não** são equivalentes drop-in:
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

Este documento acompanha **um documento do começo ao fim**: da instalação até o PDF assinado e baixado.
Cada seção é o passo seguinte dessa jornada, então ler de cima a baixo dá a integração inteira, e pular
para um título dá uma etapa dela. A [referência completa da API](docs/API_REFERENCE.md) é a tabela de
consulta por operação, os [exemplos](docs/EXAMPLES.md) trazem programas executáveis mais longos, e o
[docs/INSTALLATION.md](docs/INSTALLATION.md) detalha a configuração de build.

---

## 1. Requisitos e instalação

- Java 25+ (o SDK é compilado e verificado no Java 25 LTS atual)
- Maven 3.9.16 ou 3.x mais recente (o wrapper fixa 3.9.16); para Gradle, use um release que suporte
  JDK 25

**Maven**

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.5.0</version>
</dependency>
```

**Gradle**

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.5.0'
```

O artefato é publicado no GitHub Packages, então o repositório precisa ser declarado uma vez no seu
build. Veja [docs/INSTALLATION.md](docs/INSTALLATION.md) para isso e para os jars de sources e
Javadoc.

---

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

A seção 10 cobre obter um token e gerenciar chaves de API; a seção 11 cobre OAuth, que é como uma
aplicação age no workspace **de outras pessoas**.

---

## 3. O que toda chamada devolve

Respostas JSON vêm embrulhadas em `{ "status": <int>, "message": "<string>", "data": <payload> }`. O SDK
desembrulha o `data` no modelo tipado, então seu código nunca lida com o envelope. Ele lança
`ApiException` para um status HTTP de erro **ou** para `status >= 400` dentro do envelope — a API às
vezes devolve um envelope de erro sob HTTP 200, e o SDK trata isso como o erro que é.

Disso decorrem três formatos de resposta:

- **Modelos tipados** para os endpoints JSON comuns — `DocumentDetails`, `Assignment`, `Signer`, e assim
  por diante.
- **`PaginatedResult<T>`** para endpoints de listagem. `getData()` é o array; `getMeta()` traz
  `currentPage`, `perPage`, `total` e `lastPage`, lidos dos headers `X-Pagination-*`. Mapas de query
  aceitam `per-page`, `per_page` ou `perPage`; o SDK sempre envia `per-page`.
- **`byte[]`** para downloads binários (logos, artefatos de documento, imagens de página, thumbnails,
  imagens de assinatura). Esses não são envelopes; quando o servidor responde um deles com um envelope
  de erro, o SDK detecta o corpo JSON e lança `ApiException` em vez de te entregar o erro como se fosse
  um PDF.

Os endpoints OAuth da seção 11 são a exceção documentada: a RFC 6749, o OpenID Connect e a RFC 8615
exigem um objeto plano, então eles respondem com o objeto em vez do envelope. O SDK lê esse objeto
diretamente e continua lançando `ApiException` em caso de falha.

Retries são opt-in e deliberadamente estreitos. Com `maxRetries` acima de zero, o cliente repete apenas
`GET`, `HEAD` e `OPTIONS` que receberem 429 ou 503. Ele honra um `Retry-After` ou `X-Rate-Limit-Reset`
numérico, limita a espera em 30 segundos e preserva a interrupção da thread. Ele nunca repete um upload,
criação, atualização, exclusão, notificação ou assinatura.

---

## 4. Preparando o workspace

Tudo abaixo tem escopo de conta. Os métodos aceitam um `accountId` final opcional que sobrepõe o padrão
do cliente, então um cliente pode atender vários workspaces.

```java
List<WorkspaceAccount> workspaces = client.accounts.list();
WorkspaceAccount workspace = client.accounts.get();
WorkspaceAccount created = client.accounts.create(new AccountPayload("Jurídico")
    .setNotificationSenderType("Account"));
client.accounts.update(new AccountPayload().setName("Jurídico e Contratos"));

AccountTheme theme = client.accounts.getTheme();
byte[] logo = client.accounts.downloadLogo();
client.accounts.uploadLogo(pngBytes, "logo.png");
client.accounts.deleteLogo();

User me = client.users.getSelf();
NotificationPreferences preferences = client.users.getNotificationPreferences();
client.users.updateNotificationPreferences(
    new NotificationPreferences().setDocumentCompleted(true).setSignerDeclined(true));

// Permanente; force=true também cancela uma assinatura paga ativa.
client.accounts.delete(false, created.getId());
```

**Tags** organizam documentos, e **definições de campo** descrevem as entradas que um assignment
`collect` pode posicionar em uma página. Ambas são de nível de workspace e normalmente são criadas uma
única vez, antes de existir qualquer documento.

```java
Tag contratos = client.tags.create(new CreateTagPayload("Contratos").setColor("ff8800"));
Tag renomeada = client.tags.update(contratos.getId(),
    new UpdateTagPayload().setName("Contratos de Venda").clearColor());
PaginatedResult<Tag> tags = client.tags.list(Map.of("search", "contrato"));
boolean removida = client.tags.delete(renomeada.getId(), true); // force desanexa dos documentos antes

FieldDefinition referencia = client.fields.create(
    new CreateFieldPayload("text", "Referência").setRequired(true));
PaginatedResult<FieldDefinition> campos = client.fields.list(Map.of("include_standard", "true"));
client.fields.update(referencia.getId(), new UpdateFieldPayload().setName("Referência interna"));
List<FieldTypeInfo> tipos = client.fields.listTypes();
client.fields.delete(referencia.getId());

// Valida um valor contra o tipo/regex do campo antes de submetê-lo.
FieldValidationResult check = client.fields.validate(referencia.getId(), "ABC-123");
List<FieldValidationResult> checks = client.fields.validateMultiple(List.of(
    new FieldValidationPayload(referencia.getId(), "ABC-123")));
```

---

## 5. Criando o documento

Um documento nasce de um PDF enviado ou de um template. Uploads são `multipart/form-data` com uma parte
`file`, de no máximo 25 MB e 2.000 páginas.

```java
// De um arquivo, ou de bytes já em memória.
DocumentDetails doc = client.documents.upload(new File("contrato.pdf"));
DocumentDetails deBytes = client.documents.upload(pdfBytes, "contrato.pdf");

// Renomear só é permitido enquanto não existe assignment.
DocumentDetails renomeado = client.documents.rename(doc.getId(), "Contrato assinado.pdf");

// Listagem completa, e uma busca leve para typeahead (sem assignment/páginas expandidos).
PaginatedResult<DocumentListItem> pagina = client.documents.list(Map.of("page", "1", "per_page", "20"));
PaginatedResult<DocumentListItem> achados = client.documents.search(Map.of("search", "nota"));

// Tags deste documento.
List<Tag> anexadas = client.documents.listTags(doc.getId());
client.documents.appendTags(doc.getId(), List.of(urgenteTagId));
client.documents.replaceTags(doc.getId(), List.of(contratoTagId, trimestreTagId));
boolean desanexada = client.documents.detachTag(doc.getId(), tagId);
```

Templates produzem um documento e seu assignment em uma única chamada. Forneça uma entrada de signatário
por papel do template; os signatários já precisam existir na conta.

```java
PaginatedResult<TemplateListItem> templates = client.templates.list(Map.of("search", "NDA"));
TemplateDetails template = client.templates.get(templateId);

CostEstimate custoTemplate = client.documents.estimateCostFromTemplate(templateId,
    List.of(new TemplateSigner(template.getRoles().get(0).getId()).setVerificationMethod("Email")));

DocumentDetails gerado = client.documents.createFromTemplate(
    templateId,
    List.of(new TemplateSigner(template.getRoles().get(0).getId(), signerId)
        .setVerificationMethod("Email")
        .setNotificationMethods(List.of("Email"))
        .setStep(1)),
    new CreateDocumentFromTemplateOptions().setTags(List.of("Gerado")));
```

Um PDF enviado não fica imediatamente pronto para assignment: a API extrai os metadados das páginas
antes. `waitUntilReady` consulta `GET /documents/{id}` até o status chegar a `metadata_ready`,
`pending_signature` ou `certificated`, lançando `ValidationException` se o documento falhar, expirar,
for recusado ou se o orçamento de espera se esgotar.

```java
DocumentDetails pronto = client.documents.waitUntilReady(doc.getId());          // 30s de orçamento, 2s de intervalo
DocumentDetails paciente = client.documents.waitUntilReady(doc.getId(), 120_000, 5_000);
```

Um assignment `virtual` pode ser criado enquanto os metadados ainda processam; um `collect` não, porque
as posições dos seus campos referenciam IDs de páginas específicas.

---

## 6. Identificando os signatários

Signatários vivem no nível da conta e são reutilizados entre documentos, então a mesma pessoa é um único
registro, não importa quantos contratos ela assine.

```java
// Criação estrita: sempre envia POST e reporta e-mail duplicado como ApiException.
Signer signer = client.signers.create(
    new CreateSignerPayload("João da Silva", "joao@example.com")
        .setWhatsappPhoneNumber("+5548999990000"));

// Política de reuso explícita: busca por e-mail exato (case-insensitive) e só faz POST se não existir.
// Não atualiza os campos de um signatário existente.
Signer reutilizavel = client.signers.findOrCreate(
    new CreateSignerPayload("João da Silva", "joao@example.com"));

Signer existente = client.signers.findByEmail("joao@example.com");
Signer buscado = client.signers.get(signer.getId());
PaginatedResult<Signer> lista = client.signers.list(Map.of("search", "joão"));
client.signers.update(signer.getId(), new UpdateSignerPayload()
    .setFullName("João Pedro da Silva")
    .setGovernmentId("39053344705"));
client.signers.delete(signer.getId());
```

Use `create` quando o signatário é genuinamente novo e uma duplicata deve ser erro; use `findOrCreate`
quando o que você quer dizer é "esta pessoa, tendo a gente já a conhecido ou não". Alterar o e-mail ou o
WhatsApp de um signatário é recusado enquanto aquele canal estiver verificado em um documento em
andamento, e rotaciona os códigos de acesso das solicitações não verificadas em andamento — reenvie a
notificação depois de uma alteração dessas.

---

## 7. Precificando e solicitando assinaturas

Estime primeiro. O endpoint de estimativa aceita o mesmo formato de payload da criação, não cobra nada, e
diz se a conta consegue custear a solicitação.

```java
CreateAssignmentPayload solicitacao = new CreateAssignmentPayload()
    .setMethod("virtual")
    .setSignerStrings(signer.getId())
    .setMessage("Por favor, revise e assine")
    .setExpiresAt("2030-12-31T23:59:00Z");

CostEstimate estimativa = client.assignments.estimateCost(doc.getId(), solicitacao);
if (!Boolean.TRUE.equals(estimativa.getHasSufficientResources())) {
    throw new IllegalStateException("Assignment sem recursos: " + estimativa.getBlockingReason());
}

Assignment assignment = client.assignments.create(doc.getId(), solicitacao);
```

`blocking_reason` é `PendingPayment`, `InsufficientDocuments` ou `InsufficientCredits`; `getBreakdown()`
detalha o que compôs o número.

**`virtual` versus `collect`.** Um assignment virtual pede uma assinatura e nada mais. Um assignment
collect, além disso, posiciona campos de entrada em coordenadas de páginas específicas, então exige um
documento em `metadata_ready` e uma entrada por página:

```java
CreateAssignmentPayload collect = new CreateAssignmentPayload()
    .setMethod("collect")
    .setSignerStrings(signer.getId())
    .setCollectEntries(List.of(new CollectAssignmentEntry(pageId, List.of(
        new CollectFieldPlacement(signer.getId(), fieldId,
            new DisplaySettings(100, 100, 240, 40, 12))))));
```

**Ordem de assinatura.** `step` sequencia os signatários: todos que compartilham um passo assinam em
paralelo, e o passo seguinte só é notificado quando o anterior termina. Se você usar, todo signatário
precisa de um, e os valores precisam ser contíguos a partir de 1.

Uma vez que o assignment existe, você pode listar, renotificar e reagendar:

```java
PaginatedResult<Assignment> assignments = client.assignments.list(Map.of("page", "1", "per-page", "20"));

ResendResult reenviado = client.assignments.resendNotification(doc.getId(), assignment.getId(), signer.getId());
ResendCostEstimate custoReenvio = client.assignments.estimateResendCost(
    doc.getId(), assignment.getId(), signer.getId());

client.assignments.resetExpiration(doc.getId(), assignment.getId(), "2027-06-30T00:00:00Z");
client.assignments.clearExpiration(doc.getId(), assignment.getId()); // envia expires_at: null

List<WhatsappNotification> whatsapp =
    client.assignments.whatsappNotifications(doc.getId(), assignment.getId());
```

### Métodos de verificação e notificação

Definidos por signatário ao criar o assignment, em `signers[].verification_method` e
`signers[].notification_methods` — no SDK, `SignerRef.setVerificationMethod(...)` e
`setNotificationMethods(...)`. O método de verificação e o de notificação são **acoplados**: envie um, os
dois ou nenhum — o lado que faltar é inferido. Sem nenhum dos dois, ambos assumem `Email`.

| Método | Como funciona | Custo por signatário |
| --- | --- | --- |
| `Email` *(padrão)* | Código de uso único (OTP) por e-mail, exigido antes de assinar | Gratuito |
| `Whatsapp` | Código de uso único (OTP) por WhatsApp | Verificação gratuita; a notificação que ela exige custa 0,45 crédito, só em planos pagos |
| `DigitalCertificate` | O signatário assina com o **próprio certificado ICP-Brasil (A1 ou A3)**, pela extensão de navegador Web PKI, gerando uma assinatura **PAdES qualificada** | 2 créditos, além do custo da notificação |

Combinações permitidas — uma combinação inválida devolve HTTP 400:

| Verificação | Notificações aceitas |
| --- | --- |
| `Email` | `Email` |
| `Whatsapp` | `Whatsapp` |
| `DigitalCertificate` | `Email` **ou** `Whatsapp` |

Apenas um método de notificação por signatário. Nenhum método de verificação tem preço próprio: o que é
cobrado é a **notificação** com que ele viaja — mais, no caso do certificado digital, a própria
assinatura. Por isso 2 signatários notificados por e-mail custam 0 créditos e 2 por WhatsApp custam 0,9.

```java
CreateAssignmentPayload porWhatsapp = new CreateAssignmentPayload()
    .setMethod("virtual")
    .setSigners(List.of(SignerRef.of(signer.getId())
        .setVerificationMethod("Whatsapp")
        .setNotificationMethods(List.of("Whatsapp"))
        .setStep(1)));
```

#### Certificado digital ICP-Brasil (A1 e A3)

Exige o recurso **Certificado Digital** na conta (planos Standard e Pro), CPF ou CNPJ em `government_id`
do signatário, e exatamente **um signatário por certificado naquele passo**. Um CPF exige o certificado
daquela pessoa (e-CPF, ou e-CNPJ que a nomeie como representante legal); um CNPJ exige um e-CNPJ da
empresa. Tanto o A1 (arquivo) quanto o A3 (token ou cartão) funcionam — a diferença fica na mídia em que
a chave do signatário está guardada, e ambos são acessados pela mesma extensão Web PKI.

Estime o custo antes: a assinatura por certificado custa 2 créditos por signatário, aparecendo na
estimativa sob o código `SignatureDigitalCertificate`, além do custo da notificação escolhida.

Antes de abrir o assignment, o signatário precisa confirmar os dados de identidade e aceitar os termos. O
endpoint comum de assinatura **rejeita** signatários por certificado — a assinatura deles é produzida por
um handshake de dois passos com a extensão Web PKI:

```
POST /v1/signers/certificate/start     → data.token   (token da operação Web PKI)
        ↓  o navegador assina o token com o certificado do signatário
POST /v1/signers/certificate/complete  → data.signerName
```

> Essas duas rotas são extensões implantadas **somente em produção**: o sandbox não as expõe e elas não
> constam do documento OpenAPI publicado, então este SDK não as embrulha. Concluído o fluxo, baixar o
> artefato `pades` devolve a assinatura PAdES qualificada.

### O atalho de uma chamada

Para o caminho virtual simples, `uploadAndRequestSignatures` compõe upload, espera opcional por
processamento, `findOrCreate` por signatário e criação do assignment, devolvendo o documento, o
assignment e os IDs dos signatários.

```java
UploadAndRequestSignaturesResult resultado = client.uploadAndRequestSignatures(
    new UploadAndRequestSignaturesOptions(new File("contrato.pdf"), List.of(
            new UploadAndRequestSignaturesSigner("João da Silva", "joao@example.com")))
        .setMessage("Por favor, revise e assine"));
```

A API não tem transação abrangendo essas chamadas. Se uma etapa posterior falhar, o helper tenta, em
melhor esforço, apagar o documento enviado, e anexa qualquer falha de limpeza à exceção original como
exceção suprimida. Signatários de conta nunca são apagados automaticamente, porque outro fluxo pode já
referenciá-los.

---

## 8. O lado do signatário

Esses endpoints são autorizados por um `signer-access-code` de vida curta enviado como parâmetro de
query, não pela chave de API da conta. Normalmente são chamados de uma página de assinatura, não do seu
back-end, e o SDK os expõe para que você possa construir essa página ou simular o fluxo em testes.

```java
// O documento para o qual o signatário foi convidado. Devolve HTTP 409 enquanto ele ainda está sendo
// preparado — exposto como ApiException com getStatusCode() == 409; repita com backoff.
DocumentDetails visaoDeAssinatura = client.signerSelf.getSign(signerAccessCode);

// Identidade: perfil, termos, código de uso único e dados pessoais confirmados.
Signer eu = client.signerSelf.getSelf(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);
client.signerSelf.verifyEmail("123456", signerAccessCode);
Signer confirmado = client.signerSelf.confirmSignerData(doc.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setFullName("João da Silva").setEmail("joao@example.com")
        .setGovernmentId("15774136604"));

// Imagem da assinatura. PNG é o tipo publicado; o SDK também detecta bytes JPEG.
// reuse=true permite reutilizar a assinatura salva entre documentos (define is_signature_reusable).
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature");
client.signerSelf.uploadSignature(signerAccessCode, signatureBytes, "signature", true);
byte[] salva = client.signerSelf.downloadSignature(signerAccessCode, "signature");
```

Um signatário por certificado digital precisa confirmar os dados *e* aceitar os termos antes que
`getSign` devolva o documento; caso contrário a resposta é HTTP 400. Um signatário em assignment virtual
precisa confirmar os dados antes de assinar, ou a chamada de assinatura responde HTTP 400.

Assinar e recusar são mutuamente exclusivos:

```java
client.assignments.signEntries(doc.getId(), assignment.getId(), signerAccessCode, List.of(
    new AssignmentSignEntry("item-1", "field-1", "page-1", "João da Silva")));

// Alternativa ao acima, não um passo seguinte:
// client.assignments.decline(doc.getId(), assignment.getId(), signerAccessCode, "Cláusula 3 inaceitável");
```

Um signatário com vários documentos pendentes pode resolvê-los em lote, e pode navegar e baixar as
próprias cópias:

```java
DocumentDetails atual = client.signerSelf.getCurrentDocument(signerId, signerAccessCode);
PaginatedResult<DocumentDetails> meus = client.signerSelf.listDocuments(
    signerId, signerAccessCode, Map.of("page", "1", "per_page", "20"));
PaginatedResult<DocumentDetails> achados =
    client.signerSelf.searchDocuments(signerId, signerAccessCode, "nota");

// A rota de artefato é pública; uma sobrecarga também envia o código de acesso onde o ambiente exigir.
byte[] copiaSignatario = client.signerSelf.downloadDocument(signerId, doc.getId(), "pades");
byte[] copiaAutorizada = client.signerSelf.downloadDocument(
    signerId, doc.getId(), "original", signerAccessCode);

client.signerSelf.signMultiple(signerAccessCode, List.of(doc1.getId(), doc2.getId()));
// Alternativa ao acima para os mesmos documentos, não um passo seguinte:
// client.signerSelf.declineMultiple(signerAccessCode, List.of(doc1.getId()), "Sem interesse");
```

Dois endpoints públicos sustentam uma página de assinatura antes de existir qualquer código de acesso —
uma visão não autenticada do documento e um pedido de reenvio do token de acesso de uso único:

```java
AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
DocumentDetails infoPublica = publicClient.documents.getPublic(doc.getId());

// O documento precisa estar em pending_signature. channel é "email" ou "whatsapp".
publicClient.documents.sendToken(doc.getId(), "signatario@example.com", "email");
```

---

## 9. Acompanhando o progresso e recolhendo o resultado

Consulte o estado, ou — melhor — assine webhooks e busque o estado quando um chegar.

```java
DocumentDetails estadoAtual = client.documents.details(doc.getId());
SigningProgress progresso = client.documents.getSigningProgress(doc.getId());
boolean concluido = client.documents.isFullySigned(doc.getId());
List<DocumentActivity> atividades = client.documents.activities(doc.getId());
List<DocumentStatsRow> statsConta = client.accounts.stats(Map.of("granularity", "monthly"));
List<DocumentStatsRow> statsUsuario = client.users.stats(Map.of("granularity", "monthly"));
```

As atividades de um documento devolvem todos os eventos registrados, cada um com um snapshot do `payload`
do evento e a `origin` da requisição (`ip`, `user-agent`).

O workspace tem uma única assinatura de webhook, atualizada com um `PUT` de criar-ou-substituir. Não
existe endpoint de exclusão definitiva; `inactivate()` para as entregas e mantém a configuração.

```java
WebhookSubscription sub = client.webhooks.register(
    new RegisterWebhookPayload("https://example.com/webhooks", "admin@example.com")
        .setEvents(List.of("document_ready", "signer_signed_document"))
        .setActive(true));

client.webhooks.getSubscription();
client.webhooks.update(new RegisterWebhookPayload(sub.getUrl(), sub.getEmail())
    .setEvents(sub.getEvents()).setActive(sub.isActive()));
client.webhooks.inactivate();

List<WebhookEventTypeInfo> tiposDeEvento = client.webhooks.listEventTypes();
PaginatedResult<WebhookDispatch> entregas = client.webhooks.listDispatches(
    new ListDispatchesParams().setEvent("document_ready").setDelivered(false));
client.webhooks.retryDispatch(dispatchId);
```

Quando o documento chega a `certificated`, seus artefatos ficam disponíveis:

| Artefato | Conteúdo |
| --- | --- |
| `original` | O PDF enviado, como recebido |
| `certificated` | O documento assinado, com a certificação da plataforma |
| `certificate-page` | Apenas a página de certificação |
| `pades` | Assinaturas ICP-Brasil dos signatários + caixa de certificação — só existe em documentos que tiveram signatários por certificado digital |
| `bundle` | Zip com `original`, `certificated` e `certificate-page`, mais o `pades` quando houver |

```java
byte[] pdfAssinado = client.documents.download(doc.getId());               // usa "certificated"
byte[] original = client.documents.download(doc.getId(), "original");
byte[] bundle = client.documents.download(doc.getId(), "bundle");
byte[] thumbnail = client.documents.thumbnail(doc.getId());
byte[] imagemPagina = client.documents.downloadPage(doc.getId(), pageId);

// Verificação pública e não autenticada, pelo hash impresso no documento assinado.
DocumentVerification verificacao = client.documents.verify(signatureHash);
boolean valido = Boolean.TRUE.equals(verificacao.getIsValid());
```

Apague apenas o que é seu e apenas quando o status permitir — `documents.statuses()` informa quais
status são deletáveis.

```java
boolean deletavel = client.documents.statuses().stream()
    .anyMatch(status -> estadoAtual.getStatus().equals(status.getCode())
        && Boolean.TRUE.equals(status.getDeletable()));
if (deletavel) {
    client.documents.delete(doc.getId());
}
```

---

## 10. Sessões, senhas e chaves de API

O recurso `auth` cobre o ciclo de vida da credencial em si. As rotas de redefinição de senha são
públicas; o resto precisa de um token bearer ou de uma chave de API.

```java
AuthenticationResult sessao = client.auth.login("user@example.com", "senha");
String accessToken = sessao.getAccessToken();

AuthenticationResult sessaoGoogle = client.auth.socialLogin(
    new SocialLoginPayload("google", googleToken, true));

// Rotacione chaves por uma sessão bearer, para o cliente não reter uma chave que acabou de revogar.
AssinafyClient tokenClient = new AssinafyClient(new AssinafyClientOptions().setToken(accessToken));
ApiKeyResponse mascarada = tokenClient.auth.getApiKey();     // mascarada; a chave completa não é recuperável
ApiKeyResponse criada = tokenClient.auth.createApiKey("senha"); // substitui qualquer chave anterior
tokenClient.auth.deleteApiKey();

tokenClient.auth.linkSocialLogin("google", googleToken);
tokenClient.auth.changePassword("user@example.com", "senha-antiga", "senha-nova");

AssinafyClient publicClient = new AssinafyClient(new AssinafyClientOptions());
publicClient.auth.requestPasswordReset("user@example.com");
publicClient.auth.resetPassword("user@example.com", resetToken, "senha-nova");
```

---

## 11. OAuth — agindo no workspace de outra pessoa

Tudo acima pressupõe que o workspace é seu. OAuth é o outro caso: uma aplicação que **outros** clientes
Assinafy conectam ao workspace **deles**. Eles aprovam uma vez, e você recebe tokens limitados às
permissões concedidas e ao único workspace escolhido — nunca a senha nem a chave de API deles, e eles
podem desligar a qualquer momento. Se você automatiza a própria conta, fique com a chave de API e pule
esta seção.

Registre a aplicação no app da Assinafy em **Configurações → Aplicações OAuth**. Você recebe um
`client_id` e, se ela for *confidencial* (roda em um servidor seu), um `client_secret`. PKCE é
obrigatório para toda aplicação, inclusive as confidenciais.

**1. Iniciar uma conexão.** Gere um verifier e um state por tentativa e guarde os dois na sessão do
usuário.

```java
String verifier = OAuthResource.generateCodeVerifier();
String state = OAuthResource.generateState();

String authorizeUrl = client.oauth.authorizationUrl(
    new OAuthAuthorizationRequest("seu-client-id", "https://meuapp.example/oauth/callback")
        .setScopes(List.of("documents:read", "documents:write", "offline_access"))
        .setState(state)
        .setCodeVerifier(verifier));
```

Leve o navegador até lá com uma navegação de página inteira, não por AJAX. `response_type=code` e
`code_challenge_method=S256` são fixos, o challenge é derivado do verifier, e `resource` assume por
padrão a origem da base URL do cliente. A redirect URI precisa ser HTTPS, não ter fragmento, e bater
caractere por caractere com uma URI registrada — `…/callback` e `…/callback/` são URIs diferentes. Se o
client ID ou a redirect URI estiverem errados, o usuário **não** volta para você: o servidor de
autorização mostra o erro na própria página dele.

| Escopo | Permite ao seu app |
|---|---|
| `documents:read` | Ler documentos, signatários, assignments e atividades |
| `documents:write` | Criar documentos e enviá-los para assinatura |
| `templates:read` / `templates:write` | Ler, e criar ou alterar, templates |
| `account:read` | Ler o perfil, o tema e o logo do workspace |
| `webhooks:write` | Configurar e desativar a assinatura de webhooks do workspace |
| `openid` / `profile` / `email` | Identificar o usuário e ler nome e e-mail |
| `offline_access` | Receber um refresh token, para seguir funcionando na ausência do usuário |

Peça o mínimo: o usuário aprova tudo ou nada, e cada permissão é mais uma linha que ele lê. Cobrança,
ciclo de vida da conta, credenciais e administração nunca são alcançáveis por um token OAuth.

**2. Tratar o retorno.** Confira `state` contra o valor guardado e `iss` contra
`https://auth.assinafy.com.br` antes de qualquer coisa. Depois troque o código a partir do seu servidor —
ele é de uso único e expira 60 segundos após a aprovação.

```java
OAuthTokens tokens = client.oauth.exchangeAuthorizationCode(
    "seu-client-id", "seu-client-secret",   // secret nulo para uma aplicação pública
    code, "https://meuapp.example/oauth/callback", verifierGuardado);
```

Leia `tokens.getScope()` para saber o que você realmente recebeu, em vez de presumir. `getRefreshToken()`
só vem preenchido quando `offline_access` foi concedido, e `getIdToken()` só quando `openid` foi.

**3. Chamar a API como o usuário.** Um token pertence a exatamente um workspace, e a lista de workspaces
devolve justamente ele; guarde o ID ao lado dos tokens.

```java
AssinafyClient comoUsuario = new AssinafyClient(
    new AssinafyClientOptions().setToken(tokens.getAccessToken()));
String workspaceId = comoUsuario.accounts.list().get(0).getId();
```

Chamar qualquer outro workspace responde 403, mesmo um a que o próprio usuário pertença. Se um cliente
usa vários, conecte cada um separadamente e guarde tokens por workspace.

**4. Manter viva.** Access tokens duram uma hora; uma conexão dura 30 dias a partir da aprovação, e
renovar não estende esse prazo, então planeje que os usuários reconectem mensalmente.

```java
OAuthTokens renovados = client.oauth.refreshToken("seu-client-id", "seu-client-secret", refreshGuardado);
store.save(renovados.getRefreshToken());   // antes de usar qualquer outra coisa da resposta
```

Cada refresh emite um novo refresh token e aposenta o anterior. Um refresh token reutilizado não pode ser
distinguido de um roubado sendo replicado, então ele encerra a conexão inteira: trate um timeout como
"talvez tenha funcionado", releia o token que você salvou em vez de repetir às cegas, e faça um refresh
de cada vez por conexão.

**5. Tratar as duas falhas de OAuth.** Uma permissão faltando responde 403 com um desafio que a nomeia; o
SDK expõe as duas partes.

```java
catch (ApiException e) {
    if ("insufficient_scope".equals(e.getOAuthError())) {
        reconectarPedindo(e.getRequiredScope());   // reconectar, não repetir
    } else if (e.getStatusCode() == 401) {
        // Expirado ou revogado: renove e, se falhar, peça ao usuário para conectar de novo.
    }
}
```

Um 403 **sem** esse código tem outra causa: workspace diferente, o papel do próprio usuário, ou uma área
que tokens OAuth nunca alcançam.

**6. Identificar e desconectar.**

```java
OAuthUserInfo quem = comoUsuario.oauth.userInfo();   // exige openid; name exige profile, email exige email

// Ao desconectar, revogue em vez de apenas esquecer o token. Todo desfecho responde 200.
client.oauth.revoke("seu-client-id", "seu-client-secret", refreshGuardado, "refresh_token");
```

`client.oauth.protectedResourceMetadata()` lê o documento RFC 9728 na raiz do host da API, que nomeia o
identificador canônico do recurso e o servidor de autorização. A maioria das bibliotecas OAuth só precisa
do issuer, `https://auth.assinafy.com.br`, e lê o resto do `/.well-known/oauth-authorization-server`
dele — servido pelo servidor de autorização, não por esta API.

Antes de ir para produção: um verifier e um state novos por tentativa; `state` e `iss` conferidos; o
secret só no seu servidor; o novo refresh token salvo antes de usar; 401 tratado; o ID do workspace
guardado por conexão; toda redirect URI de produção registrada; só as permissões necessárias; tokens
revogados ao desconectar. Uma aplicação nova é não verificada e conecta a no máximo 25 workspaces até a
Assinafy revisá-la.

---

## 12. Erros

Tudo que o SDK lança descende de `AssinafyException`, então um único catch serve de rede de segurança,
enquanto os três subtipos separam "minha entrada estava errada" de "a API disse não" de "a rede falhou".

```java
import com.assinafy.sdk.exceptions.*;

try {
    client.documents.upload(new File("contrato.pdf"));
} catch (ValidationException e) {
    // Capturado antes de qualquer requisição: IDs faltando, e-mail inválido, arquivo grande demais.
    System.err.println("Validação: " + e.getMessage() + " " + e.getErrors());
} catch (ApiException e) {
    // A API recusou. getResponseBody() guarda o JSON de erro completo.
    System.err.println("Erro de API " + e.getStatusCode() + ": " + e.getMessage());
    Integer backoff = e.getRetryAfterSeconds(); // preenchido só em 429/503 repetíveis
    String oauthError = e.getOAuthError();      // código RFC 6749 em falha de OAuth; veja a seção 11
} catch (NetworkException e) {
    // Falha de transporte, ou um corpo de resposta que não pôde ser lido.
    System.err.println("Rede: " + e.getMessage());
} catch (AssinafyException e) {
    System.err.println("Erro do SDK: " + e.getMessage());
}
```

O corpo de erro padrão é `{ "status": integer, "message": string, "data": object|null }`. Trate 400 e 422
como falhas de validação. Uma exclusão de conta bloqueada acrescenta um array `restrictions` nomeando
cada bloqueio.

---

## 13. Ambientes

| | |
| --- | --- |
| Produção | `https://api.assinafy.com.br/v1` (padrão) |
| Sandbox | `https://sandbox.assinafy.com.br/v1` — defina via `setBaseUrl(...)`; este artefato não expõe constante de sandbox |

O sandbox acompanha a produção com atraso. Uma rota que responde 404 no roteador do sandbox mas funciona
em `api.assinafy.com.br` é atraso de implantação, não rota inexistente — hoje é o caso dos endpoints
OAuth e das duas rotas de certificado digital.

---

## 14. Desenvolvimento

```bash
# Rodar os testes no Docker (recomendado)
docker compose run --rm test

# Ou a verificação local completa com o Maven Wrapper (exige JDK 25+)
./mvnw verify

# O portão de release: doclint do Javadoc mais os jars de sources e javadoc. O CI roda isso como um
# passo separado, porque avisos de doclint falham aqui e não no `verify`.
./mvnw -DskipTests -Prelease package

# Testes de fumaça ao vivo contra o sandbox (pulados sem credenciais; usa a base URL do sandbox)
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ./mvnw test -Dtest=LiveSmokeTest

# Opt-in explícito para os testes que disparam e-mail real de solicitação de assinatura
ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... ASSINAFY_LIVE_EMAILS=true \
  ASSINAFY_TEST_EMAIL=... ASSINAFY_SECOND_TEST_EMAIL=... ./mvnw test -Dtest=LiveSmokeTest
```

`LiveSmokeTest` recusa qualquer base URL que não seja a do sandbox. Mantenha credenciais de teste em
variáveis de ambiente ou segredos de CI, nunca em propriedades do Maven ou em arquivos do repositório.

O CI roda `./mvnw verify` no JDK 25 LTS atual. O GitLab é a fonte da verdade e espelha para o GitHub,
onde rodam os workflows equivalentes do Actions. Releases publicam no GitHub Packages a partir de uma tag
`v*`, pelo profile `release`.

---

## Documentação

- [docs/API_REFERENCE.md](docs/API_REFERENCE.md) — referência por operação
- [docs/EXAMPLES.md](docs/EXAMPLES.md) — programas executáveis mais longos
- [docs/INSTALLATION.md](docs/INSTALLATION.md) — setup de build
- [README.en.md](README.en.md) — este mesmo guia, em inglês
- [Documentação da API](https://api.assinafy.com.br/v1/docs)

## Licença

Distribuído sob a licença [MIT](LICENSE).
