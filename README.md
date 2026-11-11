# SDK Java Webforms Client da Assinafy

*Português · [Read in English](README.en.md)*

Cliente Java 25 para as 147 operações da [API Assinafy](https://api.assinafy.com.br/v1/docs). O nome
`webforms` é histórico; novas integrações também devem considerar `com.assinafy:assinafy-sdk`.

A documentação detalhada de operações, requests e responses está em
[API_REFERENCE.md](docs/API_REFERENCE.md), com exemplos executáveis em [EXAMPLES.md](docs/EXAMPLES.md) e
configuração de repositórios em [INSTALLATION.md](docs/INSTALLATION.md).

## Instalação

O SDK exige Java 25+, Maven 3.9.16+ quando usado com Maven e TLS 1.2+.

```xml
<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>webforms-java-client-sdk</artifactId>
    <version>2.10.0</version>
</dependency>
```

```groovy
implementation 'com.assinafy:webforms-java-client-sdk:2.10.0'
```

O Maven Central não exige credenciais de repositório. O guia de instalação cobre GitHub Packages e Gradle
Kotlin DSL.

## Configuração do cliente

Crie um cliente thread-safe por credencial/conta e reutilize-o. Guarde credenciais em variáveis de ambiente ou
armazenamento de segredos; o SDK não lê arquivos `.env`.

```java
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions()
    .setApiKey(System.getenv("ASSINAFY_API_KEY"))
    .setAccountId(System.getenv("ASSINAFY_ACCOUNT_ID"))
    .setTimeoutMs(30_000)
    .setMaxRetries(2));
```

Quando ambos existem, a chave de API tem preferência sobre o bearer token. `maxRetries` vale apenas para
leituras seguras que recebem 429/503; uploads e mutações são enviados uma única vez. A URL de produção é
`https://api.assinafy.com.br/v1`. Endpoints binários devolvem `byte[]`; listagens devolvem
`PaginatedResult<T>`; os demais endpoints JSON desembalam o envelope da API em modelos tipados.
`ApiException` expõe detalhes da API, OAuth, retry e rate limit.

## Fluxo completo de assinatura

Envie um PDF (máximo de 25 MB/2.000 páginas), aguarde os metadados e, opcionalmente, salve os termos contratuais.

```java
DocumentDetails document = client.documents.upload(new File("contrato.pdf"));
document = client.documents.waitUntilReady(document.getId(), 60_000, 2_000);

DocumentContractResult terms = client.contracts.set(document.getId(), new DocumentContractInput()
    .setStatus("active").setCounterpartyName("Empresa Exemplo")
    .setStartDate("2026-11-01").setEndDate("2027-10-31"));
```

Crie ou reutilize um signatário da conta e estime o assignment exato antes de criá-lo.

```java
Signer signer = client.signers.findOrCreate(
    new CreateSignerPayload("Signatário Exemplo", "signer@example.com")
        .setWhatsappPhoneNumber("+5548999990000")
        .setGovernmentId("39053344705"));

CreateAssignmentPayload request = new CreateAssignmentPayload().setMethod("virtual").setSigners(List.of(
    SignerRef.of(signer.getId()).setVerificationMethod("Email")
        .setNotificationMethods(List.of("Email")).setStep(1)));
CostEstimate estimate = client.assignments.estimateCost(document.getId(), request);
if (!Boolean.TRUE.equals(estimate.getHasSufficientResources())) {
    throw new IllegalStateException(estimate.getBlockingReason());
}
Assignment assignment = client.assignments.create(document.getId(), request);
```

As combinações de verificação/notificação suportadas são:

| Verificação | Notificação | Requisitos |
|---|---|---|
| `Email` | `Email` | Padrão, código de uso único |
| `Whatsapp` | `Whatsapp` | Canal de notificação de plano pago |
| `IdDocumentFaceMatchBasic` | `Email` ou `Whatsapp` | Recurso ativo, CPF e consentimento biométrico explícito |
| `DigitalCertificate` | `Email` ou `Whatsapp` | Recurso ativo, CPF/CNPJ, `virtual`, signatário sozinho na etapa |

DigitalCertificate usa o certificado ICP-Brasil A1/A3 do signatário no fluxo Web PKI do navegador e produz
PAdES qualificado. Na biometria, `InReview` permite assinar, mas segura a certificação até o workspace aprovar
ou rejeitar a evidência privada. Estime primeiro porque recursos do plano e créditos variam.

Chamadas do signatário usam somente `signer-access-code`; o cliente não envia credenciais do workspace.

```java
DocumentDetails signingView = client.signerSelf.getSign(signerAccessCode);
client.signerSelf.acceptTerms(signerAccessCode);
client.signerSelf.verifyEmail("123456", signerAccessCode); // Whatsapp usa verifyWhatsapp
client.signerSelf.confirmSignerData(document.getId(), signerAccessCode,
    new ConfirmSignerDataPayload().setEmail("signer@example.com").setHasAcceptedTerms(true));

// Somente para IdDocumentFaceMatchBasic, após consentimento explícito do signatário:
if (biometriaObrigatoria && signatarioConsentiu) {
    IdentityVerificationStatus identity = client.signerSelf.getIdentityVerification(signerAccessCode);
    if (identity.canStart()) redirecionarSignatario(
        client.signerSelf.startIdentityVerification(signerAccessCode, true).url());
}

client.signerSelf.uploadSignature(signerAccessCode, assinaturaPng, "signature", true);
// Envie os AssignmentSignEntry não vazios representados por signingView.getAssignment().getItems().
```

Para certificados, confirme os dados e aceite os termos antes de chamar `getSign`.
Conclua assignments comuns com `assignments.signEntries`; cada item leva os IDs de item, campo e página
devolvidos, além do valor em string. A assinatura com certificado termina no navegador; o SDK não fabrica
material de certificado. Consulte o status ou processe webhooks idempotentemente e baixe o artefato final.

```java
DocumentDetails current = client.documents.details(document.getId());
byte[] signedPdf = client.documents.download(document.getId(), "certificated");
byte[] qualifiedPdf = client.documents.download(document.getId(), "pades"); // só DigitalCertificate
```

## Envelopes e assinatura integrada

`client.envelopes` envia até 20 documentos a até 20 signatários com um convite por signatário. Estime primeiro,
defina `max_total_credits` e use uma chave de idempotência estável para recuperação. Cada documento tem seu certificado.
Para assinar no seu app, cadastre origens HTTPS exatas em `client.embed`, defina `embedded=true` no signatário
e crie uma sessão de iframe de uso único, válida por cinco minutos. Não registre a URL nem troque o token no backend.
Após o encerramento do acesso, `requestCertifiedCopy` envia um novo link ao contato original da assinatura.
Veja [payloads e fluxos completos](docs/EXAMPLES.md#envelopes-and-embedded-signing).

## Webhooks assinados

URLs novas ou alteradas precisam usar HTTPS; URLs HTTP legadas continuam disponíveis para consulta/exclusão
exatas. Guarde o segredo da criação imediatamente, pois respostas de listagem/consulta/atualização não o trazem.

```java
WebhookEndpoint endpoint = client.webhooks.createEndpoint(new WebhookEndpointPayload()
    .setUrl("https://example.com/webhooks/assinafy").setEmail("ops@example.com")
    .setEvents(List.of("document_ready", "signer_signed_document")).setSigningEnabled(true));
WebhookVerifier verifier = new WebhookVerifier(endpoint.secret());
WebhookEvent event = verifier.verify(webhookId, webhookTimestamp, webhookSignature, rawBody);
```

Verifique o corpo cru antes de confirmar, deduplique por `webhook-id`, responda 2xx rapidamente e processe
depois. Chaves de API/sessões de usuário podem consultar o segredo. Apps OAuth podem rotacioná-lo com
`webhooks:write`; `rotateEndpointSecret(id, true)` mantém a chave anterior por 24 horas, com ambas as assinaturas.

## Conexões OAuth 2.1 de marketplace

Use chaves de API no workspace próprio. Para workspaces de clientes, registre callbacks HTTPS exatos e use
state com PKCE S256. Valide o `state` e o issuer do callback antes de trocar o código de uso único.

```java
String verifier = OAuthResource.generateCodeVerifier();
String state = OAuthResource.generateState();
String authorizeUrl = client.oauth.authorizationUrl(new OAuthAuthorizationRequest(
    "client-id", "https://example.com/oauth/callback")
    .setScopes(List.of("documents:read", "documents:write", "offline_access"))
    .setState(state).setCodeVerifier(verifier));

OAuthTokens tokens = client.oauth.exchangeAuthorizationCode(
    "client-id", clientSecret, code, "https://example.com/oauth/callback", verifier);
AssinafyClient asUser = new AssinafyClient(
    new AssinafyClientOptions().setToken(tokens.getAccessToken()));
```

Guarde tokens atomicamente por workspace. Serialize os refreshes: cada sucesso rotaciona o refresh token e dá
novos 30 dias. Salve o substituto antes de usá-lo; nunca repita um token após resultado incerto. Revogue o
token mais recente ao desconectar. Tokens OAuth ficam limitados aos escopos aprovados e a um workspace.

```java
OAuthTokens renewed = client.oauth.refreshToken("client-id", clientSecret, storedRefreshToken);
tokenStore.replaceAtomically(renewed);
client.oauth.revoke("client-id", clientSecret, renewed.getRefreshToken(), "refresh_token");
```

## Build e suporte

```bash
./mvnw verify
docker compose run --rm test
```

O build exige Java 25, trata warnings do compilador como erros, roda testes unitários e valida o Javadoc no
perfil de release. Testes live usam as variáveis de [Instalação](docs/INSTALLATION.md#building-and-sandbox-tests)
e nunca miram produção.

- [Referência da API](docs/API_REFERENCE.md) — todas as operações e o catálogo completo de payloads
- [Exemplos](docs/EXAMPLES.md) — Java e JSON por endpoint
- [Instalação](docs/INSTALLATION.md) — repositórios e ferramentas de build
- [Changelog](CHANGELOG.md) — comportamento publicado
- [Licença](LICENSE) — MIT
