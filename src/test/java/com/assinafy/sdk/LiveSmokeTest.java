package com.assinafy.sdk;

import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.CreateFieldPayload;
import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.CreateTagPayload;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldTypeInfo;
import com.assinafy.sdk.models.FieldValidationResult;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.SignerRef;
import com.assinafy.sdk.models.Tag;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.models.UpdateFieldPayload;
import com.assinafy.sdk.models.UpdateSignerPayload;
import com.assinafy.sdk.models.UpdateTagPayload;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Smoke test that exercises the SDK against the live Assinafy API.
 *
 * <p>This test is skipped unless both {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID}
 * environment variables are set. It targets the <strong>sandbox</strong> base URL by default; override with
 * {@code ASSINAFY_BASE_URL} to point elsewhere. Run with:</p>
 *
 * <pre>{@code
 * ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... mvn test -Dtest=LiveSmokeTest
 * }</pre>
 *
 * <p>The test exercises read-only endpoints plus tag and signer create/update/delete round-trips. Because it
 * performs destructive operations, it defaults to the sandbox base URL and must never be pointed at a
 * production workspace.</p>
 */
@EnabledIfEnvironmentVariable(named = "ASSINAFY_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ASSINAFY_ACCOUNT_ID", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveSmokeTest {

    private static final String API_KEY = System.getenv("ASSINAFY_API_KEY");
    private static final String ACCOUNT_ID = System.getenv("ASSINAFY_ACCOUNT_ID");
    private static final String DEFAULT_SANDBOX_BASE_URL = "https://sandbox.assinafy.com.br/v1";

    private static AssinafyClient client() {
        String baseUrl = System.getenv().getOrDefault("ASSINAFY_BASE_URL", DEFAULT_SANDBOX_BASE_URL);
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
        if (sub != null) {
            assertThat(sub.getEvents()).isNotNull();
        }
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
            client.tags.delete(created.getId(), true);
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
            Map<String, Object> cost = client.assignments.estimateCost(doc.getId(),
                    new CreateAssignmentPayload().setSigners(List.of(
                            new SignerRef().setId(signer.getId()).setVerificationMethod("Email")
                                    .setNotificationMethods(List.of("Email")))));
            assertThat(cost).containsKey("has_sufficient_resources");
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
            Assignment assignment = client.assignments.create(doc.getId(), new CreateAssignmentPayload()
                    .setMethod("virtual")
                    .setSigners(List.of(new SignerRef().setId(signer.getId())
                            .setVerificationMethod("Email").setNotificationMethods(List.of("Email"))))
                    .setExpiresAt("2027-12-31T23:59:00Z"));
            assertThat(assignment.getId()).isNotBlank();

            Assignment reset = client.assignments.resetExpiration(doc.getId(), assignment.getId(),
                    "2028-01-15T12:00:00Z");
            assertThat(reset.getExpiresAt()).isEqualTo("2028-01-15T12:00:00Z");

            Assignment cleared = client.assignments.clearExpiration(doc.getId(), assignment.getId());
            assertThat(cleared.getExpiresAt()).isNull();
        } finally {
            client.documents.delete(doc.getId());
        }
    }

    private static Signer ensureTestSigner(AssinafyClient client) {
        String email = System.getenv().getOrDefault("ASSINAFY_TEST_EMAIL", "billm@billm.org");
        return client.signers.create(new CreateSignerPayload("SDK Smoke Signer", email));
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
