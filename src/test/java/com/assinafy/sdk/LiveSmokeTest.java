package com.assinafy.sdk;

import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.AccountPayload;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.CreateFieldPayload;
import com.assinafy.sdk.models.DocumentVerification;
import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.CreateTagPayload;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldTypeInfo;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.NotificationPreferences;
import com.assinafy.sdk.models.RegisterWebhookPayload;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.models.TemplateDetails;
import com.assinafy.sdk.models.TemplateSigner;
import com.assinafy.sdk.models.CreateDocumentFromTemplateOptions;
import com.assinafy.sdk.models.CollectAssignmentEntry;
import com.assinafy.sdk.models.CollectFieldPlacement;
import com.assinafy.sdk.models.DisplaySettings;
import com.assinafy.sdk.models.UpdateFieldPayload;
import com.assinafy.sdk.models.UpdateSignerPayload;
import com.assinafy.sdk.models.UpdateTagPayload;
import com.assinafy.sdk.models.UploadAndRequestSignaturesOptions;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.models.UploadAndRequestSignaturesSigner;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import com.assinafy.sdk.exceptions.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Smoke test that exercises the SDK against the live Assinafy API.
 *
 * <p>This test is skipped unless both {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID}
 * environment variables are set. It only runs against the exact Assinafy sandbox base URL; the optional
 * {@code ASSINAFY_BASE_URL} value may only add/remove trailing slashes. Run with:</p>
 *
 * <pre>{@code
 * ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... mvn test -Dtest=LiveSmokeTest
 * }</pre>
 *
 * <p>The test exercises read-only endpoints plus isolated create/update/delete round-trips. Because it performs
 * destructive operations, a hard runtime guard rejects every non-sandbox URL.</p>
 */
@EnabledIfEnvironmentVariable(named = "ASSINAFY_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ASSINAFY_ACCOUNT_ID", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveSmokeTest {

    private static final String API_KEY = System.getenv("ASSINAFY_API_KEY");
    private static final String ACCOUNT_ID = System.getenv("ASSINAFY_ACCOUNT_ID");
    private static final String DEFAULT_SANDBOX_BASE_URL = "https://sandbox.assinafy.com.br/v1";
    private static final String INSUFFICIENT_RESOURCES_MESSAGE =
            "A conta não possui recursos suficientes. Você precisa de documentos ou créditos para continuar.";

    @BeforeEach
    void paceSandboxRequests() throws InterruptedException {
        Thread.sleep(1_000);
    }

    private static AssinafyClient client() {
        String baseUrl = System.getenv().getOrDefault("ASSINAFY_BASE_URL", DEFAULT_SANDBOX_BASE_URL);
        if (!DEFAULT_SANDBOX_BASE_URL.equals(baseUrl.replaceFirst("/+$", ""))) {
            throw new IllegalStateException("LiveSmokeTest only runs against the Assinafy sandbox");
        }
        return AssinafyClient.create(API_KEY, ACCOUNT_ID, opts -> opts.setBaseUrl(baseUrl));
    }

    @Test
    @Order(1)
    @DisplayName("Document statuses are returned by the public catalogue endpoint")
    void documentStatuses() {
        List<DocumentStatus> statuses = client().documents.statuses();
        assertThat(statuses).isNotEmpty();
        assertThat(statuses.stream().map(DocumentStatus::getCode))
                .contains("uploaded", "metadata_ready", "certificated");
    }

    @Test
    @Order(2)
    @DisplayName("Field types catalogue is reachable")
    void fieldTypes() {
        List<FieldTypeInfo> types = client().fields.listTypes();
        assertThat(types).isNotEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("Webhook event-types catalogue is reachable")
    void webhookEventTypes() {
        List<WebhookEventTypeInfo> events = client().webhooks.listEventTypes();
        assertThat(events).isNotEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Account-scoped templates list parses with pagination headers")
    void listTemplates() {
        PaginatedResult<TemplateListItem> page = client().templates.list();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(5)
    @DisplayName("Account-scoped documents list parses, including pages and artifacts")
    void listDocuments() {
        PaginatedResult<DocumentListItem> page = client().documents.list();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
        for (DocumentListItem doc : page.getData()) {
            assertThat(doc.getId()).isNotBlank();
            if (doc.getArtifacts() != null) {
                assertThat(doc.getArtifacts().getOriginal()).startsWith("https://");
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("Existing field definitions list with pre-defined entries")
    void listFields() {
        PaginatedResult<FieldDefinition> page = client().fields.list();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(7)
    @DisplayName("Webhook subscription endpoint returns the current configuration object")
    void getWebhookSubscription() {
        WebhookSubscription sub = client().webhooks.getSubscription();
        assertThat(sub).isNotNull();
        assertThat(sub.getEvents()).isNotNull();
    }

    @Test
    @Order(8)
    @DisplayName("Account-scoped webhook dispatches list returns paginated result")
    void listDispatches() {
        var page = client().webhooks.listDispatches();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("Workspace tags list parses")
    void listTags() {
        PaginatedResult<Tag> page = client().tags.list();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(10)
    @DisplayName("Tag create -> update -> delete round-trip")
    void tagRoundTrip() {
        AssinafyClient client = client();
        String tagName = "sdk-smoke-" + UUID.randomUUID().toString().substring(0, 8);

        Tag created = client.tags.create(new CreateTagPayload(tagName).setColor("112233"));
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getName()).isEqualTo(tagName);

            Tag updated = client.tags.update(created.getId(),
                    new UpdateTagPayload().setName(tagName + "-updated").clearColor());
            assertThat(updated.getName()).isEqualTo(tagName + "-updated");
        } finally {
            assertThat(client.tags.delete(created.getId(), true)).isTrue();
        }
    }

    @Test
    @Order(11)
    @DisplayName("Signer create → findByEmail → update → delete round-trip")
    void signerRoundTrip() {
        AssinafyClient client = client();
        String email = "sdk-smoke+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        Signer created = client.signers.create(
                new CreateSignerPayload("SDK Smoke Test", email)
                        .setWhatsappPhoneNumber("+5511999990000"));
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getEmail()).isEqualToIgnoringCase(email);

            Signer found = client.signers.findByEmail(email);
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(created.getId());

            Signer updated = client.signers.update(created.getId(),
                    new UpdateSignerPayload().setFullName("SDK Smoke Test (updated)"));
            assertThat(updated.getFullName()).isEqualTo("SDK Smoke Test (updated)");

            Signer fetched = client.signers.get(created.getId());
            assertThat(fetched.getFullName()).isEqualTo("SDK Smoke Test (updated)");
        } finally {
            client.signers.delete(created.getId());
        }
    }

    @Test
    @Order(12)
    @DisplayName("Public document endpoint returns 404 envelope (parsed as ApiException)")
    void publicDocument404() {
        AssinafyClient client = client();
        try {
            client.documents.getPublic("000000000000000000000000");
            throw new AssertionError("Expected ApiException");
        } catch (com.assinafy.sdk.exceptions.ApiException e) {
            assertThat(e.getStatusCode()).isEqualTo(404);
        }
    }

    @Test
    @Order(13)
    @DisplayName("Document lifecycle: upload -> details -> pages -> download original/thumbnail/page -> delete")
    void documentLifecycle() throws Exception {
        AssinafyClient client = client();
        byte[] pdf = samplePdf();

        DocumentDetails uploaded = client.documents.upload(pdf, "sdk-smoke-" + shortId() + ".pdf");
        try {
            assertThat(uploaded.getId()).isNotBlank();

            DocumentDetails ready = client.documents.waitUntilReady(uploaded.getId(), 60_000, 2_000);
            assertThat(ready.getPages()).isNotEmpty();
            assertThat(ready.getArtifacts().getOriginal()).startsWith("https://");

            byte[] original = client.documents.download(uploaded.getId(), "original");
            assertThat(original).isNotEmpty();

            byte[] thumbnail = client.documents.thumbnail(uploaded.getId());
            assertThat(thumbnail).isNotEmpty();

            String pageId = ready.getPages().get(0).getId();
            byte[] page = client.documents.downloadPage(uploaded.getId(), pageId);
            assertThat(page).isNotEmpty();
            assertThat(client.documents.activities(uploaded.getId())).isNotEmpty();
            assertThat(client.documents.isFullySigned(uploaded.getId())).isFalse();
            assertThat(client.documents.getSigningProgress(uploaded.getId()).getTotal()).isZero();

            // download() defaults to the certificated artifact, which is unavailable for an unsigned document:
            // the binary path must now surface the server's error message, not a generic status string.
            assertThatThrownBy(() -> client.documents.download(uploaded.getId()))
                    .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class);
        } finally {
            client.documents.delete(uploaded.getId());
        }
    }

    @Test
    @Order(14)
    @DisplayName("Field lifecycle: create -> get -> validate -> validateMultiple -> update -> delete")
    void fieldLifecycle() {
        AssinafyClient client = client();

        FieldDefinition created = client.fields.create(
                new CreateFieldPayload("text", "SDK Smoke " + shortId()).setRequired(true));
        try {
            assertThat(created.getId()).isNotBlank();
            assertThat(client.fields.get(created.getId()).getName()).isEqualTo(created.getName());

            FieldValidationResult validation = client.fields.validate(created.getId(), "hello");
            assertThat(validation.getSuccess()).isTrue();

            List<FieldValidationResult> multi = client.fields.validateMultiple(List.of(
                    new com.assinafy.sdk.models.FieldValidationPayload(created.getId(), "world")));
            assertThat(multi).hasSize(1);

            FieldDefinition updated = client.fields.update(created.getId(),
                    new UpdateFieldPayload().setName("SDK Smoke Updated " + shortId()));
            assertThat(updated.getName()).startsWith("SDK Smoke Updated");
        } finally {
            client.fields.delete(created.getId());
        }
    }

    @Test
    @Order(15)
    @DisplayName("Assignment estimate cost succeeds for a freshly uploaded document (no notifications sent)")
    void assignmentEstimateCost() throws Exception {
        AssinafyClient client = client();
        Signer signer = ensureTestSigner(client);
        DocumentDetails doc = client.documents.upload(samplePdf(), "sdk-smoke-asg-" + shortId() + ".pdf");
        try {
            client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
            CostEstimate cost = client.assignments.estimateCost(doc.getId(),
                    new CreateAssignmentPayload().setSigners(List.of(
                            new SignerRef().setId(signer.getId()).setVerificationMethod("Email")
                                    .setNotificationMethods(List.of("Email")))));
            assertThat(cost.getHasSufficientResources()).isNotNull();
        } finally {
            client.documents.delete(doc.getId());
        }
    }

    /**
     * Opt-in (sets {@code ASSINAFY_LIVE_EMAILS=true}) because creating an assignment dispatches a real
     * notification email to the signer. Exercises create -> resetExpiration(date) -> clearExpiration(null).
     */
    @Test
    @Order(16)
    @EnabledIfEnvironmentVariable(named = "ASSINAFY_LIVE_EMAILS", matches = "(?i)true")
    @DisplayName("Assignment create + reset/clear expiration round-trip (sends an email)")
    void assignmentExpirationRoundTrip() throws Exception {
        AssinafyClient client = client();
        Signer signer = ensureTestSigner(client);
        DocumentDetails doc = client.documents.upload(samplePdf(), "sdk-smoke-exp-" + shortId() + ".pdf");
        try {
            client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
            Assignment assignment;
            try {
                assignment = client.assignments.create(doc.getId(), new CreateAssignmentPayload()
                        .setMethod("virtual")
                        .setSigners(List.of(new SignerRef().setId(signer.getId())
                                .setVerificationMethod("Email").setNotificationMethods(List.of("Email"))))
                        .setExpiresAt("2027-12-31T23:59:00Z"));
            } catch (ApiException e) {
                skipIfInsufficientResources(e);
                throw e;
            }
            assertThat(assignment.getId()).isNotBlank();

            Assignment reset = client.assignments.resetExpiration(doc.getId(), assignment.getId(),
                    "2028-01-15T12:00:00Z");
            assertThat(reset.getExpiresAt()).isEqualTo("2028-01-15T12:00:00Z");

            Assignment cleared = client.assignments.clearExpiration(doc.getId(), assignment.getId());
            assertThat(cleared.getExpiresAt()).isNull();

            assertThat(client.assignments.estimateResendCost(
                    doc.getId(), assignment.getId(), signer.getId()).getTotal()).isNotNull();
            assertThat(client.assignments.whatsappNotifications(doc.getId(), assignment.getId())).isEmpty();
            assertThat(client.assignments.resendNotification(
                    doc.getId(), assignment.getId(), signer.getId()).getSent()).isTrue();
            assertThat(client.documents.getPublic(doc.getId()).getId()).isEqualTo(doc.getId());
            client.documents.sendToken(doc.getId(), signer.getEmail(), "email");
            assertThat(client.documents.activities(doc.getId())).isNotEmpty();
        } finally {
            client.documents.delete(doc.getId());
        }
    }

    @Test
    @Order(17)
    @DisplayName("Document rename: upload → PATCH rename → details reflect new name → delete")
    void documentRename() throws Exception {
        AssinafyClient client = client();
        DocumentDetails doc = client.documents.upload(samplePdf(), "sdk-smoke-rename-" + shortId() + ".pdf");
        try {
            // Wait for metadata processing to finish before renaming/deleting (a document still in
            // metadata_processing cannot be deleted).
            client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
            String newName = "sdk-smoke-renamed-" + shortId() + ".pdf";
            DocumentDetails renamed = client.documents.rename(doc.getId(), newName);
            assertThat(renamed.getName()).isEqualTo(newName);
            assertThat(client.documents.details(doc.getId()).getName()).isEqualTo(newName);
        } finally {
            client.documents.delete(doc.getId());
        }
    }

    @Test
    @Order(18)
    @DisplayName("Lightweight document search returns a paginated result")
    void documentSearch() {
        PaginatedResult<DocumentListItem> page = client().documents.search(Map.of("per_page", "5"));
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(19)
    @DisplayName("Assignments list (camelCase accountId query) parses with pagination metadata")
    void assignmentsList() {
        PaginatedResult<Assignment> page = client().assignments.list();
        assertThat(page).isNotNull();
        assertThat(page.getData()).isNotNull();
    }

    @Test
    @Order(20)
    @DisplayName("Public document verify returns a typed result (invalid hash → isValid false)")
    void verifyInvalidHash() {
        DocumentVerification result = client().documents.verify("ZZSDKDUMMYHASH000");
        assertThat(result).isNotNull();
        assertThat(result.getIsValid()).isFalse();
        assertThat(result.getMessage()).isNotBlank();
    }

    @Test
    @Order(21)
    @DisplayName("Accounts, current user, and API-key read endpoints return typed payloads")
    void accountAndUserReads() {
        AssinafyClient client = client();

        assertThat(client.accounts.list()).anyMatch(account -> ACCOUNT_ID.equals(account.getId()));
        assertThat(client.accounts.get().getId()).isEqualTo(ACCOUNT_ID);
        assertThat(client.accounts.getTheme().getAccountName()).isNotBlank();
        assertThat(client.users.getSelf().getEmail()).isNotBlank();
        assertThat(client.auth.getApiKey()).isNotNull();
    }

    @Test
    @Order(22)
    @DisplayName("Published account-stats route is exercised when deployed to the sandbox")
    void accountStats() {
        assertThat(publishedSandboxRoute(() -> client().accounts.stats())).isNotNull();
    }

    @Test
    @Order(23)
    @DisplayName("Published user-stats route is exercised when deployed to the sandbox")
    void userStats() {
        assertThat(publishedSandboxRoute(() -> client().users.stats())).isNotNull();
    }

    @Test
    @Order(24)
    @DisplayName("Both approved test-email signers can be created or reused without notification")
    void approvedTestEmails() {
        AssinafyClient client = client();
        for (String email : List.of(testEmail(), secondTestEmail())) {
            Signer signer = client.signers.findOrCreate(new CreateSignerPayload("Assinafy SDK Test", email));
            assertThat(signer.getId()).isNotBlank();
            assertThat(signer.getEmail()).isEqualToIgnoringCase(email);
        }
        assertThatThrownBy(() -> client.signers.create(
                new CreateSignerPayload("Assinafy SDK Duplicate Test", testEmail())))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getStatusCode()).isIn(400, 409));
    }

    @Test
    @Order(25)
    @DisplayName("Notification preferences update and restore against the sandbox")
    void notificationPreferenceRoundTrip() {
        AssinafyClient client = client();
        NotificationPreferences original = publishedSandboxRoute(client.users::getNotificationPreferences);
        Boolean originalValue = original.getDocumentCompleted();
        assertThat(originalValue).isNotNull();

        try {
            NotificationPreferences updated = client.users.updateNotificationPreferences(
                    new NotificationPreferences().setDocumentCompleted(!originalValue));
            assertThat(updated.getDocumentCompleted()).isEqualTo(!originalValue);
        } finally {
            client.users.updateNotificationPreferences(
                    new NotificationPreferences().setDocumentCompleted(originalValue));
        }
    }

    @Test
    @Order(26)
    @DisplayName("Disposable account covers account, logo, and webhook mutations")
    void disposableAccountRoundTrip() {
        AssinafyClient client = client();
        String accountId = null;
        try {
            // The sandbox deployment currently predates the documented optional notification_sender_type
            // create field; its exact wire contract remains covered by AccountResourceTest.
            var created = client.accounts.create(new AccountPayload("SDK Smoke " + shortId()));
            accountId = created.getId();
            assertThat(accountId).isNotBlank();

            var updated = client.accounts.update(new AccountPayload().setName("SDK Smoke Updated " + shortId()),
                    accountId);
            assertThat(updated.getName()).startsWith("SDK Smoke Updated");
            assertThat(client.accounts.get(accountId).getId()).isEqualTo(accountId);
            assertThat(client.accounts.getTheme(accountId).getAccountName()).isNotBlank();
            byte[] logo = Base64.getDecoder().decode(
                    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
            client.accounts.uploadLogo(logo, "sdk-smoke.png", accountId);
            assertThat(client.accounts.downloadLogo(accountId)).isNotEmpty();
            client.accounts.deleteLogo(accountId);

            String event = client.webhooks.listEventTypes().get(0).getId();
            RegisterWebhookPayload webhook = new RegisterWebhookPayload(
                    "https://example.com/assinafy-sdk-smoke", testEmail())
                    .setEvents(List.of(event)).setActive(true);
            assertThat(client.webhooks.register(webhook, accountId).isActive()).isTrue();
            assertThat(client.webhooks.update(webhook, accountId).getUrl())
                    .isEqualTo("https://example.com/assinafy-sdk-smoke");
            assertThat(client.webhooks.getSubscription(accountId).getEvents()).contains(event);
            assertThat(client.webhooks.listDispatches(null, accountId).getData()).isNotNull();
            assertThat(client.webhooks.inactivate(accountId).isActive()).isFalse();
        } finally {
            if (accountId != null) {
                client.accounts.delete(false, accountId);
            }
        }
    }

    @Test
    @Order(27)
    @DisplayName("Document tag replace, append, list, detach, and cleanup round-trip")
    void documentTagRoundTrip() throws Exception {
        AssinafyClient client = client();
        Tag first = null;
        Tag second = null;
        DocumentDetails doc = null;
        try {
            first = client.tags.create(new CreateTagPayload("sdk-doc-tag-a-" + shortId()));
            second = client.tags.create(new CreateTagPayload("sdk-doc-tag-b-" + shortId()));
            doc = client.documents.upload(samplePdf(), "sdk-smoke-tags-" + shortId() + ".pdf");
            client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);

            assertThat(client.documents.replaceTags(doc.getId(), List.of(first.getName())))
                    .extracting(Tag::getId).containsExactly(first.getId());
            assertThat(client.documents.appendTags(doc.getId(), List.of(second.getName())))
                    .extracting(Tag::getId).contains(first.getId(), second.getId());
            assertThat(client.documents.listTags(doc.getId()))
                    .extracting(Tag::getId).contains(first.getId(), second.getId());
            assertThat(client.documents.detachTag(doc.getId(), first.getId())).isTrue();
            assertThat(client.documents.replaceTags(doc.getId(), List.of())).isEmpty();
        } finally {
            try {
                if (doc != null) client.documents.delete(doc.getId());
            } finally {
                try {
                    if (first != null) assertThat(client.tags.delete(first.getId(), true)).isTrue();
                } finally {
                    if (second != null) assertThat(client.tags.delete(second.getId(), true)).isTrue();
                }
            }
        }
    }

    @Test
    @Order(28)
    @EnabledIfEnvironmentVariable(named = "ASSINAFY_LIVE_EMAILS", matches = "(?i)true")
    @DisplayName("Template detail, estimate, and document creation round-trip")
    void templateDocumentRoundTrip() {
        AssinafyClient client = client();
        // Templates are authored in the web application; the API exposes no template-creation route, so a
        // workspace without one cannot exercise this flow. Treat that as an unmet precondition, not a failure.
        Optional<TemplateListItem> candidate = client.templates.list().getData().stream()
                .filter(item -> item.getRoles() != null && !item.getRoles().isEmpty())
                .findFirst();
        Assumptions.assumeTrue(candidate.isPresent(),
                "The sandbox account has no template with roles to create a document from");
        TemplateListItem template = candidate.get();
        TemplateDetails details = client.templates.get(template.getId());
        assertThat(details.getRoles()).isNotEmpty();

        List<TemplateSigner> estimateSigners = details.getRoles().stream()
                .map(role -> new TemplateSigner(role.getId()))
                .toList();
        assertThat(client.documents.estimateCostFromTemplate(template.getId(), estimateSigners))
                .isNotNull();

        Signer signer = ensureTestSigner(client);
        List<TemplateSigner> createSigners = details.getRoles().stream()
                .map(role -> new TemplateSigner(role.getId(), signer.getId())
                        .setVerificationMethod("Email").setNotificationMethods(List.of("Email")))
                .toList();
        DocumentDetails created;
        try {
            created = client.documents.createFromTemplate(template.getId(), createSigners,
                    new CreateDocumentFromTemplateOptions().setName("sdk-template-" + shortId() + ".pdf"));
        } catch (ApiException e) {
            skipIfInsufficientResources(e);
            throw e;
        }
        try {
            assertThat(client.documents.waitUntilReady(created.getId(), 60_000, 2_000).getId())
                    .isEqualTo(created.getId());
        } finally {
            client.documents.delete(created.getId());
        }
    }

    @Test
    @Order(29)
    @DisplayName("Published signer government_id update is exercised when deployed to the sandbox")
    void signerGovernmentIdUpdate() {
        AssinafyClient client = client();
        String email = "sdk-government-id+" + shortId() + "@example.com";
        Signer signer = client.signers.create(new CreateSignerPayload("SDK Government ID", email));
        try {
            Signer updated = client.signers.update(signer.getId(),
                    new UpdateSignerPayload().setGovernmentId("52998224725"));
            if (updated.getGovernmentId() == null) {
                Assumptions.assumeTrue(false,
                        "Published signer government_id update is not deployed to the current sandbox version");
            }
            assertThat(updated.getGovernmentId()).isEqualTo("52998224725");
        } finally {
            client.signers.delete(signer.getId());
        }
    }

    @Test
    @Order(30)
    @EnabledIfEnvironmentVariable(named = "ASSINAFY_LIVE_EMAILS", matches = "(?i)true")
    @DisplayName("Password-reset request returns the approved test email")
    void passwordResetRequest() {
        String email = secondTestEmail();
        try {
            assertThat(client().auth.requestPasswordReset(email).getEmail()).isEqualToIgnoringCase(email);
        } catch (ApiException e) {
            if (e.getStatusCode() == 404 && "Usuário não localizado.".equals(e.getMessage())) {
                Assumptions.assumeTrue(false, "The approved password-reset address is not a sandbox user");
            }
            throw e;
        }
    }

    @Test
    @Order(31)
    @EnabledIfEnvironmentVariable(named = "ASSINAFY_LIVE_EMAILS", matches = "(?i)true")
    @DisplayName("Collect assignment estimate and create with typed entries")
    void collectAssignmentRoundTrip() throws Exception {
        AssinafyClient client = client();
        String accountId = null;
        try {
            accountId = client.accounts.create(new AccountPayload("SDK Collect Account " + shortId())).getId();
            FieldDefinition field = client.fields.create(
                    new CreateFieldPayload("text", "SDK Collect " + shortId()).setRequired(true), accountId);
            Signer signer = client.signers.create(
                    new CreateSignerPayload("SDK Collect Signer", testEmail()), accountId);
            DocumentDetails doc = client.documents.upload(
                    samplePdf(), "sdk-collect-" + shortId() + ".pdf", accountId);
            DocumentDetails ready = client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
            CollectAssignmentEntry entry = new CollectAssignmentEntry(ready.getPages().get(0).getId(), List.of(
                    new CollectFieldPlacement(signer.getId(), field.getId(),
                            new DisplaySettings(10, 10, 100, 30, 12))));

            CostEstimate estimate = client.assignments.estimateCost(doc.getId(),
                    new CreateAssignmentPayload().setMethod("collect")
                            .setSignerStrings(signer.getId())
                            .setCollectEntries(List.of(entry)));
            assertThat(estimate.getHasSufficientResources()).isNotNull();

            Assignment created;
            try {
                created = client.assignments.create(doc.getId(), new CreateAssignmentPayload()
                        .setMethod("collect")
                        .setSignerStrings(signer.getId())
                        .setCollectEntries(List.of(entry)));
            } catch (ApiException e) {
                skipIfInsufficientResources(e);
                throw e;
            }
            assertThat(created.getId()).isNotBlank();
        } finally {
            if (accountId != null) client.accounts.delete(false, accountId);
        }
    }

    @Test
    @Order(32)
    @DisplayName("Published collect estimate without signers is exercised when deployed to the sandbox")
    void collectEstimateWithoutSigners() throws Exception {
        AssinafyClient client = client();
        String accountId = null;
        try {
            accountId = client.accounts.create(
                    new AccountPayload("SDK Collect Estimate Account " + shortId())).getId();
            FieldDefinition field = client.fields.create(
                    new CreateFieldPayload("text", "SDK Collect Estimate " + shortId()), accountId);
            Signer signer = client.signers.create(
                    new CreateSignerPayload("SDK Collect Estimate Signer", testEmail()), accountId);
            DocumentDetails doc = client.documents.upload(
                    samplePdf(), "sdk-collect-estimate-" + shortId() + ".pdf", accountId);
            DocumentDetails ready = client.documents.waitUntilReady(doc.getId(), 60_000, 2_000);
            CollectAssignmentEntry entry = new CollectAssignmentEntry(ready.getPages().get(0).getId(), List.of(
                    new CollectFieldPlacement(signer.getId(), field.getId(),
                            new DisplaySettings(10, 10, 100, 30, 12))));

            try {
                assertThat(client.assignments.estimateCost(doc.getId(), new CreateAssignmentPayload()
                        .setMethod("collect").setCollectEntries(List.of(entry)))).isNotNull();
            } catch (ApiException e) {
                if (e.getStatusCode() == 400 && e.getMessage() != null
                        && e.getMessage().contains("signatários")) {
                    Assumptions.assumeTrue(false,
                            "Published signer-free collect estimate is not deployed to the current sandbox");
                }
                throw e;
            }
        } finally {
            if (accountId != null) client.accounts.delete(false, accountId);
        }
    }

    @Test
    @Order(33)
    @DisplayName("Published account notification_sender_type create field is exercised when deployed")
    void accountNotificationSenderType() {
        AssinafyClient client = client();
        String accountId = null;
        try {
            accountId = client.accounts.create(new AccountPayload("SDK Sender Type " + shortId())
                    .setNotificationSenderType("Account")).getId();
            assertThat(client.accounts.get(accountId).getNotificationSenderType()).isEqualTo("Account");
        } catch (ApiException e) {
            if (e.getStatusCode() == 400 && e.getMessage() != null
                    && e.getMessage().contains("notification_sender_type")) {
                Assumptions.assumeTrue(false,
                        "Published notification_sender_type create field is not deployed to the current sandbox");
            }
            throw e;
        } finally {
            if (accountId != null) client.accounts.delete(false, accountId);
        }
    }

    @Test
    @Order(34)
    @EnabledIfEnvironmentVariable(named = "ASSINAFY_LIVE_EMAILS", matches = "(?i)true")
    @DisplayName("High-level upload-and-request workflow creates an assignment and cleans up")
    void uploadAndRequestSignaturesRoundTrip() throws Exception {
        AssinafyClient client = client();
        String email = testEmail();
        Signer signer = client.signers.findByEmail(email);
        boolean createdSigner = signer == null;
        if (createdSigner) {
            signer = client.signers.create(new CreateSignerPayload("SDK Workflow Signer", email));
        }
        UploadAndRequestSignaturesResult result = null;
        try {
            try {
                result = client.uploadAndRequestSignatures(new UploadAndRequestSignaturesOptions(
                        samplePdf(), "sdk-workflow-" + shortId() + ".pdf",
                        List.of(new UploadAndRequestSignaturesSigner("SDK Workflow Signer", email))));
            } catch (ApiException e) {
                skipIfInsufficientResources(e);
                throw e;
            }

            assertThat(result.getDocument().getId()).isNotBlank();
            assertThat(result.getAssignment().getId()).isNotBlank();
            assertThat(result.getSignerIds()).containsExactly(signer.getId());
        } finally {
            try {
                if (result != null) client.documents.delete(result.getDocument().getId());
            } finally {
                if (createdSigner) client.signers.delete(signer.getId());
            }
        }
    }

    private static Signer ensureTestSigner(AssinafyClient client) {
        return client.signers.findOrCreate(new CreateSignerPayload("SDK Smoke Signer", testEmail()));
    }

    private static void skipIfInsufficientResources(ApiException e) {
        if (e.getStatusCode() == 400 && INSUFFICIENT_RESOURCES_MESSAGE.equals(e.getMessage())) {
            Assumptions.assumeTrue(false, "The sandbox account has insufficient documents or credits");
        }
    }

    private static String testEmail() {
        return requiredEnvironment("ASSINAFY_TEST_EMAIL");
    }

    private static String secondTestEmail() {
        return requiredEnvironment("ASSINAFY_SECOND_TEST_EMAIL");
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        Assumptions.assumeTrue(value != null && !value.isBlank(), name + " is required for this live test");
        return value;
    }

    private static <T> T publishedSandboxRoute(Supplier<T> call) {
        try {
            return call.get();
        } catch (ApiException e) {
            if (e.getStatusCode() == 404 && "Página não encontrada.".equals(e.getMessage())) {
                Assumptions.assumeTrue(false,
                        "Published production route is not deployed to the current sandbox version");
            }
            throw e;
        }
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static byte[] samplePdf() throws Exception {
        try (InputStream in = LiveSmokeTest.class.getResourceAsStream("/sample.pdf")) {
            assertThat(in).as("sample.pdf test resource").isNotNull();
            return in.readAllBytes();
        }
    }
}
