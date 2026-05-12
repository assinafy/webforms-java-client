package com.assinafy.sdk;

import com.assinafy.sdk.models.CreateSignerPayload;
import com.assinafy.sdk.models.DocumentDetails;
import com.assinafy.sdk.models.DocumentListItem;
import com.assinafy.sdk.models.DocumentStatus;
import com.assinafy.sdk.models.FieldDefinition;
import com.assinafy.sdk.models.FieldTypeInfo;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.models.TemplateListItem;
import com.assinafy.sdk.models.UpdateSignerPayload;
import com.assinafy.sdk.models.WebhookEventTypeInfo;
import com.assinafy.sdk.models.WebhookSubscription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test that exercises the SDK against the live Assinafy API.
 *
 * <p>This test is skipped unless both {@code ASSINAFY_API_KEY} and {@code ASSINAFY_ACCOUNT_ID}
 * environment variables are set. Run with:</p>
 *
 * <pre>{@code
 * ASSINAFY_API_KEY=... ASSINAFY_ACCOUNT_ID=... mvn test -Dtest=LiveSmokeTest
 * }</pre>
 *
 * <p>The test exercises only read-only endpoints plus a single create/update/delete signer round-trip
 * so it can safely run against a production account.</p>
 */
@EnabledIfEnvironmentVariable(named = "ASSINAFY_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "ASSINAFY_ACCOUNT_ID", matches = ".+")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveSmokeTest {

    private static final String API_KEY = System.getenv("ASSINAFY_API_KEY");
    private static final String ACCOUNT_ID = System.getenv("ASSINAFY_ACCOUNT_ID");

    private static AssinafyClient client() {
        return AssinafyClient.create(API_KEY, ACCOUNT_ID);
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
    @Order(10)
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
}
