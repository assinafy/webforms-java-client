package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.AssignmentSignEntry;
import com.assinafy.sdk.models.CollectAssignmentEntry;
import com.assinafy.sdk.models.CollectFieldPlacement;
import com.assinafy.sdk.models.CostEstimate;
import com.assinafy.sdk.models.CreateAssignmentPayload;
import com.assinafy.sdk.models.DisplaySettings;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.ResendCostEstimate;
import com.assinafy.sdk.models.ResendResult;
import com.assinafy.sdk.models.SignerRef;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentResourceTest {

    private MockWebServer server;
    private AssignmentResource resource;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new AssignmentResource(new OkHttpClient(), server.url("/").toString(), "acc");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private MockResponse okJson(Object data) throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data", data));
        return new MockResponse().setBody(body).setHeader("Content-Type", "application/json");
    }

    @Test
    void identityVerificationParsesTheCompleteReview() throws Exception {
        server.enqueue(new MockResponse().setBody("""
                {"status":200,"data":{"id":"v1","status":"InReview","provider":"AssinafyVerify",
                 "verification_method":"IdDocumentFaceMatchBasic",
                 "reason_codes":[{"code":"NAME_MISMATCH","description":"Name differs"}],
                 "evidence_available":true,"expected":{"name":"Test Signer","cpf":null},
                 "document":{"type":"CNH","name":"TEST SIGNER","cpf":null},
                 "matches":{"cpf":true,"name":0.5217,"birth_date":null},
                 "checks":[{"name":"name","result":"review","score":0.5217,"reasons":["NAME_MISMATCH"]}],
                 "images":["document_front","face"],"ruleset_version":"2026-10-12.2","evidence_sha256":"digest",
                 "reviewed_by":{"id":"u1","name":"Reviewer"},"reviewed_at":"2026-10-13T21:05:00Z",
                 "review_note":"Private note","completed_at":"2026-10-13T21:05:00Z","future_field":true}}
                """));

        var review = resource.getIdentityVerification("d1", "a1", "s1");

        assertThat(review.id()).isEqualTo("v1");
        assertThat(review.status()).isEqualTo("InReview");
        assertThat(review.provider()).isEqualTo("AssinafyVerify");
        assertThat(review.verificationMethod()).isEqualTo("IdDocumentFaceMatchBasic");
        assertThat(review.reasonCodes().getFirst().code()).isEqualTo("NAME_MISMATCH");
        assertThat(review.reasonCodes().getFirst().description()).isEqualTo("Name differs");
        assertThat(review.evidenceAvailable()).isTrue();
        assertThat(review.expected().name()).isEqualTo("Test Signer");
        assertThat(review.document().type()).isEqualTo("CNH");
        assertThat(review.document().name()).isEqualTo("TEST SIGNER");
        assertThat(review.expected().cpf()).isNull();
        assertThat(review.document().cpf()).isNull();
        assertThat(review.matches().cpf()).isTrue();
        assertThat(review.matches().name()).isEqualTo(0.5217);
        assertThat(review.matches().birthDate()).isNull();
        assertThat(review.checks().getFirst().result()).isEqualTo("review");
        assertThat(review.checks().getFirst().score()).isEqualTo(0.5217);
        assertThat(review.checks().getFirst().reasons()).containsExactly("NAME_MISMATCH");
        assertThat(review.images()).containsExactly("document_front", "face");
        assertThat(review.rulesetVersion()).isEqualTo("2026-10-12.2");
        assertThat(review.evidenceSha256()).isEqualTo("digest");
        assertThat(review.reviewedBy().id()).isEqualTo("u1");
        assertThat(review.reviewedBy().name()).isEqualTo("Reviewer");
        assertThat(review.reviewedAt()).isEqualTo("2026-10-13T21:05:00Z");
        assertThat(review.reviewNote()).isEqualTo("Private note");
        assertThat(review.completedAt()).isEqualTo("2026-10-13T21:05:00Z");
        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("GET");
        assertThat(req.getPath()).isEqualTo("/documents/d1/assignments/a1/signers/s1/identity-verification");
    }

    @Test
    void identityReviewSendsBothDecisionsAndOmitsAnAbsentNote() throws Exception {
        for (String decision : List.of("Approved", "Rejected")) {
            String status = "Approved".equals(decision) ? "Approved" : "Declined";
            server.enqueue(okJson(Map.of("id", "v1", "status", status)));
            var review = resource.reviewIdentityVerification("d1", "a1", "s1", decision,
                    "Approved".equals(decision) ? "Private note" : null);
            assertThat(review.status()).isEqualTo(status);
            RecordedRequest req = server.takeRequest();
            assertThat(req.getMethod()).isEqualTo("POST");
            assertThat(req.getPath())
                    .isEqualTo("/documents/d1/assignments/a1/signers/s1/identity-verification/review");
            assertThat(MAPPER.readTree(req.getBody().readUtf8())).isEqualTo(MAPPER.readTree(
                    "Approved".equals(decision) ? "{\"decision\":\"Approved\",\"note\":\"Private note\"}"
                            : "{\"decision\":\"Rejected\"}"));
        }
    }

    @Test
    void identityEvidenceDownloadsAllDocumentedFormatsAndSurfacesEnvelopeErrors() throws Exception {
        for (String name : List.of("document_front", "document_back", "document_pdf", "face")) {
            server.enqueue(new MockResponse().setBody("evidence")
                    .setHeader("Content-Type", name.equals("document_pdf") ? "application/pdf" : "image/jpeg"));
            assertThat(resource.downloadIdentityVerificationImage("d1", "a1", "s1", name))
                    .isEqualTo("evidence".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            assertThat(server.takeRequest().getPath())
                    .isEqualTo("/documents/d1/assignments/a1/signers/s1/identity-verification/images/" + name);
        }
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setBody("{\"status\":404,\"message\":\"Review closed\",\"data\":null}"));
        assertThatThrownBy(() -> resource.downloadIdentityVerificationImage("d1", "a1", "s1", "face"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ApiException.class).hasMessageContaining("Review closed");
    }

    @Test
    void identityReviewRejectsInvalidInputsBeforeSending() {
        for (String invalid : new String[]{null, "", "../x", "a/b"}) {
            assertThatThrownBy(() -> resource.getIdentityVerification(invalid, "a1", "s1"))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> resource.getIdentityVerification("d1", invalid, "s1"))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> resource.getIdentityVerification("d1", "a1", invalid))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> resource.downloadIdentityVerificationImage("d1", "a1", "s1", invalid))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> resource.reviewIdentityVerification("d1", "a1", "s1", invalid, null))
                    .isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> resource.reviewIdentityVerification("d1", "a1", "s1", "Approved",
                "x".repeat(2001))).isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void createAndEstimateSupportEveryVerificationAndNotificationPair() throws Exception {
        for (String method : List.of("Email", "Whatsapp", "DigitalCertificate", "IdDocumentFaceMatchBasic")) {
            for (String channel : method.equals("Email") ? List.of("Email")
                    : method.equals("Whatsapp") ? List.of("Whatsapp") : List.of("Email", "Whatsapp")) {
                SignerRef signer = SignerRef.of("s1").setVerificationMethod(method)
                        .setNotificationMethods(List.of(channel)).setStep(1);
                CreateAssignmentPayload payload = new CreateAssignmentPayload().setSigners(List.of(signer));
                server.enqueue(okJson(Map.of("id", "a1", "signers", List.of(Map.of(
                        "id", "s1", "identity_verification_status", "InReview")))));
                assertThat(resource.create("d1", payload).getSigners().getFirst().getIdentityVerificationStatus())
                        .isEqualTo("InReview");
                var created = MAPPER.readTree(server.takeRequest().getBody().readUtf8()).path("signers").get(0);
                assertThat(created.path("verification_method").asText()).isEqualTo(method);
                assertThat(created.path("notification_methods").get(0).asText()).isEqualTo(channel);
                assertThat(created.path("step").asInt()).isEqualTo(1);
                server.enqueue(okJson(Map.of("total", 1)));
                resource.estimateCost("d1", payload);
                var priced = MAPPER.readTree(server.takeRequest().getBody().readUtf8()).path("signers").get(0);
                assertThat(priced.size()).isEqualTo(2);
                assertThat(priced.path("verification_method").asText()).isEqualTo(method);
                assertThat(priced.path("notification_methods").get(0).asText()).isEqualTo(channel);
            }
        }
    }

    @Test
    void createAndEstimateRejectCertificateCollectRequestsWithStableCode() {
        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setMethod("collect")
                .setSigners(List.of(SignerRef.of("s1").setVerificationMethod("DigitalCertificate")))
                .setEntries(List.of(new CollectAssignmentEntry("p1", List.of(
                        new CollectFieldPlacement("s1", "f1",
                                new DisplaySettings(10, 20, 100, 30, 12, "Arial", "#D5EBFF"))))));

        assertThatThrownBy(() -> resource.create("doc-1", payload))
                .isInstanceOfSatisfying(ValidationException.class, exception -> assertThat(exception.getErrors())
                        .containsEntry("code", AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD)
                        .containsEntry("method", "collect"));
        assertThatThrownBy(() -> resource.estimateCost("doc-1", payload))
                .isInstanceOfSatisfying(ValidationException.class, exception -> assertThat(exception.getErrors())
                        .containsEntry("code", AssignmentResource.DIGITAL_CERTIFICATE_REQUIRES_VIRTUAL_METHOD)
                        .containsEntry("method", "collect"));
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void create_postsToCorrectUrl() throws Exception {
        server.enqueue(okJson(Map.of("id", "assignment-1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload().setSignerStrings("s1", "s2");
        Assignment result = resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/documents/doc-1/assignments");
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(result.getId()).isEqualTo("assignment-1");
    }

    @Test
    void create_sendsNormalisedBody() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.create("doc-1", new CreateAssignmentPayload().setSignerStrings("s1", "s2"));

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"signers\"");
        assertThat(body).contains("\"s1\"");
        assertThat(body).contains("\"s2\"");
    }

    @Test
    void create_includesOptionalFields() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSignerStrings("s1")
                .setMessage("hi")
                .setExpiresAt("2024-12-31")
                .setCopyReceivers(List.of("copy@example.com"));
        resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"message\":\"hi\"");
        assertThat(body).contains("\"expires_at\":\"2024-12-31\"");
        assertThat(body).contains("\"copy_receivers\"");
    }

    @Test
    void create_throwsOnEmptySigners() {
        assertThatThrownBy(() -> resource.create("doc-1", new CreateAssignmentPayload()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_requiresDocumentId() {
        assertThatThrownBy(() -> resource.create("", new CreateAssignmentPayload().setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_parsesDocumentedAssignmentShape() throws Exception {
        server.enqueue(okJson(Map.of(
                "id", "a1",
                "copy_receivers", List.of(Map.of("id", "copy-1", "full_name", "Copy Receiver")),
                "items", List.of(Map.of(
                        "id", "item-1",
                        "page", Map.of("id", "page-1", "number", 1),
                        "signer", Map.of("id", "signer-1", "full_name", "Signer"),
                        "field", Map.of("id", "field-1", "name", "Signature", "type", "signature"),
                        "display_settings", Map.of("top", 10),
                        "completed", false
                )),
                "summary", Map.of(
                        "signer_count", 1,
                        "completed_count", 0,
                        "signers", List.of(Map.of("id", "signer-1", "completed", false))
                ),
                "signing_urls", List.of(Map.of("signer_id", "signer-1", "url", "https://example.com/sign"))
        )));

        Assignment result = resource.create("doc-1", new CreateAssignmentPayload().setSignerStrings("signer-1"));

        assertThat(result.getCopyReceivers()).hasSize(1);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getSummary().getSigners()).hasSize(1);
        assertThat(result.getSigningUrls().get(0).getSignerId()).isEqualTo("signer-1");
    }

    @Test
    void create_parsesSequentialSignerFields() throws Exception {
        // Sequential-signing data (step/notified) and notification_history are returned on assignment signers.
        server.enqueue(okJson(Map.of(
                "id", "a1",
                "signers", List.of(Map.of(
                        "id", "signer-1",
                        "full_name", "Bill M",
                        "verification_method", "Email",
                        "notification_methods", List.of("Email"),
                        "step", 2,
                        "notified", true,
                        "completed", false,
                        "notification_history", List.of(Map.of(
                                "event", "signature_request",
                                "status", "sent",
                                "sent_at", "2026-06-05T20:49:18Z"
                        ))
                ))
        )));

        Assignment result = resource.create("doc-1", new CreateAssignmentPayload().setSignerStrings("signer-1"));

        var signer = result.getSigners().get(0);
        assertThat(signer.getStep()).isEqualTo(2);
        assertThat(signer.getNotified()).isTrue();
        assertThat(signer.getNotificationHistory()).hasSize(1);
        assertThat(signer.getNotificationHistory().get(0).getEvent()).isEqualTo("signature_request");
        assertThat(signer.getNotificationHistory().get(0).getStatus()).isEqualTo("sent");
        assertThat(signer.getNotificationHistory().get(0).getSentAt()).isEqualTo("2026-06-05T20:49:18Z");
    }

    @Test
    void create_acceptsSignerRefObjects() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSigners(List.of(SignerRef.of("s1"), new SignerRef().setId("s2")));
        resource.create("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"s1\"");
        assertThat(body).contains("\"s2\"");
    }

    @Test
    void create_serializesSequentialSigningStep() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.create("doc-1", new CreateAssignmentPayload()
                .setSigners(List.of(SignerRef.of("s1").setStep(2))));

        assertThat(server.takeRequest().getBody().readUtf8()).contains("\"step\":2");
    }

    @Test
    void resendNotification_requiresAllThreeIds() {
        assertThatThrownBy(() -> resource.resendNotification("", "a", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "", "s"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.resendNotification("d", "a", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void estimateCost_acceptsSignerDescriptorsWithoutIds() throws Exception {
        server.enqueue(okJson(Map.of("total_credits", 0.45)));

        CreateAssignmentPayload payload = new CreateAssignmentPayload()
                .setSigners(List.of(new SignerRef().setVerificationMethod("Whatsapp")));
        resource.estimateCost("doc-1", payload);

        RecordedRequest req = server.takeRequest();
        String body = req.getBody().readUtf8();
        assertThat(body).contains("Whatsapp");
    }

    @Test
    void estimateCost_sendsSignersAlongsideCollectEntries() throws Exception {
        server.enqueue(okJson(Map.of("total_credits", 0)));

        resource.estimateCost("doc-1", new CreateAssignmentPayload()
                .setMethod("collect")
                .setSigners(List.of(new SignerRef().setVerificationMethod("Email")))
                .setEntries(List.of(new CollectAssignmentEntry("p1", List.of(
                        new CollectFieldPlacement("s1", "f1",
                                new DisplaySettings(10, 20, 100, 30, 12, "Arial", "#D5EBFF")))))));

        String body = server.takeRequest().getBody().readUtf8();
        assertThat(body)
                .contains("\"method\":\"collect\"")
                .contains("\"page_id\":\"p1\"")
                .contains("\"signer_id\":\"s1\"")
                .contains("\"field_id\":\"f1\"")
                .contains("\"fontSize\":12.0")
                .contains("\"backgroundColor\":\"#D5EBFF\"");
        // collect is priced per signer too, so the channels must reach the API.
        assertThat(body).contains("\"verification_method\":\"Email\"");
    }

    @Test
    void estimateCost_requiresAtLeastOneSignerInEitherMethod() {
        // The API refuses a signer-less estimate in either mode.
        assertThatThrownBy(() -> resource.estimateCost("doc-1", new CreateAssignmentPayload()
                .setMethod("collect")
                .setEntries(List.of(new CollectAssignmentEntry("p1", List.of(
                        new CollectFieldPlacement("s1", "f1", new DisplaySettings(10, 20, 100, 30, 12, "Arial", "#D5EBFF"))))))))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("At least one signer");
    }

    @Test
    void estimateCost_sendsOnlyPricedSignerChannels() throws Exception {
        server.enqueue(okJson(Map.of("total_credits", 0)));

        resource.estimateCost("doc-1", new CreateAssignmentPayload().setSigners(List.of(
                new SignerRef().setId("s1").setStep(2).setVerificationMethod("Email")
                        .setNotificationMethods(List.of("Email", "Whatsapp")))));

        assertThat(server.takeRequest().getBody().readUtf8()).isEqualTo("{\"method\":\"virtual\",\"signers\":["
                + "{\"verification_method\":\"Email\",\"notification_methods\":[\"Email\",\"Whatsapp\"]}]}");
    }

    @Test
    void decline_rejectsReasonsOverTheLimit() {
        assertThatThrownBy(() -> resource.decline("d", "a", "code", "x".repeat(2001)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("2000");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void estimateCost_omitsCreateOnlyFields() throws Exception {
        server.enqueue(okJson(Map.of("total_credits", 0)));

        resource.estimateCost("doc-1", new CreateAssignmentPayload()
                .setSignerStrings("s1")
                .setMessage("not part of estimate")
                .setExpiresAt("2027-01-01T00:00:00Z")
                .setCopyReceivers(List.of("copy-1")));

        assertThat(server.takeRequest().getBody().readUtf8())
                .contains("\"method\":\"virtual\"", "\"signers\"")
                .doesNotContain("message", "expires_at", "copy_receivers");
    }

    @Test
    void signerSetterFormsReplaceRatherThanMixState() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.create("doc-1", new CreateAssignmentPayload()
                .setSignerStrings("old")
                .setSignerIds(List.of("new")));

        assertThat(server.takeRequest().getBody().readUtf8())
                .contains("\"id\":\"new\"")
                .doesNotContain("old");
    }

    @Test
    void estimateCost_requiresDocumentId() {
        assertThatThrownBy(() -> resource.estimateCost("",
                new CreateAssignmentPayload().setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void sign_postsEntriesArray() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.sign("doc-1", "a1", "code", List.of(
                new AssignmentSignEntry("item-1", "field-1", "page-1", "John Doe")));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath())
                .isEqualTo("/documents/doc-1/assignments/a1?signer-access-code=code");
        String body = req.getBody().readUtf8();
        assertThat(body).startsWith("[")
                .contains("\"itemId\":\"item-1\"", "\"fieldId\":\"field-1\"",
                        "\"pageId\":\"page-1\"", "\"value\":\"John Doe\"");
    }

    @Test
    void sign_rejectsEmptyEntries() {
        assertThatThrownBy(() -> resource.sign("doc-1", "a1", "code", List.of()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createCollectRequiresEntriesAndMethodIsValidated() {
        assertThatThrownBy(() -> resource.create("doc-1", new CreateAssignmentPayload()
                .setMethod("collect").setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("collect entry");
        assertThatThrownBy(() -> resource.create("doc-1", new CreateAssignmentPayload()
                .setMethod("paper").setSignerStrings("s1")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("method");
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void typedEntryModelsValidateRequiredGeometryAndIdentifiers() {
        assertThatThrownBy(() -> new AssignmentSignEntry("", "f", "p", "value"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new AssignmentSignEntry("i", "", "p", "value"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new AssignmentSignEntry("i", "f", "", "value"))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new AssignmentSignEntry("i", "f", "p", null))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new DisplaySettings(0, 0, 0, 10, 12))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new DisplaySettings(Double.NaN, 0, 10, 10, 12))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new DisplaySettings(0, -1, 10, 10, 12))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectFieldPlacement("", "f", new DisplaySettings(0, 0, 10, 10, 12)))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectFieldPlacement("s", "", new DisplaySettings(0, 0, 10, 10, 12)))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectFieldPlacement("s", "f", null))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectAssignmentEntry("", List.of(
                new CollectFieldPlacement("s", "f", new DisplaySettings(0, 0, 10, 10, 12)))))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectAssignmentEntry("p", List.of()))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
        assertThatThrownBy(() -> new CollectAssignmentEntry("p",
                java.util.Collections.singletonList(null)))
                .isInstanceOf(com.assinafy.sdk.exceptions.ValidationException.class);
    }

    @Test
    void decline_putsRejectEndpoint() throws Exception {
        server.enqueue(okJson(List.of()));

        resource.decline("doc-1", "a1", "code-xyz", "Not happy");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath())
                .isEqualTo("/documents/doc-1/assignments/a1/reject?signer-access-code=code-xyz");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("\"decline_reason\":\"Not happy\"");
    }

    @Test
    void decline_requiresReason() {
        assertThatThrownBy(() -> resource.decline("doc-1", "a1", "code", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void resetExpiration_rejectsBlankButNonNull() {
        assertThatThrownBy(() -> resource.resetExpiration("doc-1", "a1", ""))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void resetExpiration_allowsNullToClearExpiration() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1", "expires_at", "value")));

        resource.resetExpiration("doc-1", "a1", null);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath())
                .isEqualTo("/documents/doc-1/assignments/a1/reset-expiration");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"expires_at\":null}");
    }

    @Test
    void clearExpiration_sendsNullExpiresAt() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.clearExpiration("doc-1", "a1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getBody().readUtf8()).isEqualTo("{\"expires_at\":null}");
    }

    @Test
    void resetExpiration_sendsProvidedTimestamp() throws Exception {
        server.enqueue(okJson(Map.of("id", "a1")));

        resource.resetExpiration("doc-1", "a1", "2027-01-15T12:00:00Z");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getBody().readUtf8()).contains("\"expires_at\":\"2027-01-15T12:00:00Z\"");
    }

    @Test
    void whatsappNotifications_returnsList() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "wa-1", "phone_number", "+15555550100"))));

        var notifications = resource.whatsappNotifications("doc-1", "a1");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/documents/doc-1/assignments/a1/whatsapp-notifications");
        assertThat(notifications).hasSize(1);
    }

    @Test
    void list_sendsCamelCaseAccountIdQueryAndParsesPagination() throws Exception {
        String body = MAPPER.writeValueAsString(Map.of("status", 200, "data",
                List.of(Map.of("id", "a1", "method", "virtual"))));
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Content-Type", "application/json")
                .setHeader("x-pagination-current-page", "1")
                .setHeader("x-pagination-total-count", "10"));

        PaginatedResult<Assignment> page = resource.list();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/assignments?accountId=acc");
        assertThat(page.getData().get(0).getId()).isEqualTo("a1");
        assertThat(page.getMeta().getTotal()).isEqualTo(10);
    }

    @Test
    void listSendsPaginationAlongsideTheAccountContext() throws Exception {
        server.enqueue(okJson(List.of(Map.of("id", "a1", "method", "virtual"))));

        PaginatedResult<Assignment> page = resource.list(Map.of("page", "2", "per_page", "5"));

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).startsWith("/assignments?")
                .contains("page=2", "per-page=5", "accountId=acc");
        assertThat(page.getData().get(0).getId()).isEqualTo("a1");
    }

    @Test
    void estimateCost_parsesTypedCostEstimate() throws Exception {
        server.enqueue(okJson(Map.of(
                "documents", 1,
                "credits", 0,
                "needs_extra_document", false,
                "total_credits", 0,
                "breakdown", List.of(),
                "document_balance", 86,
                "credit_balance", 0,
                "has_sufficient_resources", true)));

        CostEstimate cost = resource.estimateCost("doc-1",
                new CreateAssignmentPayload().setSignerStrings("s1"));

        assertThat(server.takeRequest().getPath()).isEqualTo("/documents/doc-1/assignments/estimate-cost");
        assertThat(cost.getDocuments()).isEqualTo(1);
        assertThat(cost.getHasSufficientResources()).isTrue();
        assertThat(cost.getBlockingReason()).isNull();
    }

    @Test
    void resendNotification_parsesTypedResult() throws Exception {
        server.enqueue(okJson(Map.of("is_sent", true, "document_id", "doc-1", "signer_id", "s1")));

        ResendResult result = resource.resendNotification("doc-1", "a1", "s1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("PUT");
        assertThat(req.getPath()).isEqualTo("/documents/doc-1/assignments/a1/signers/s1/resend");
        assertThat(result.getSent()).isTrue();
        assertThat(result.getDocumentId()).isEqualTo("doc-1");
    }

    @Test
    void estimateResendCost_parsesCompactShape() throws Exception {
        server.enqueue(okJson(Map.of(
                "total", 0,
                "breakdown", List.of(Map.of("code", "NotificationEmailResend", "name", "Email Resend", "cost", 0)),
                "credit_balance", 0,
                "has_sufficient_credits", true)));

        ResendCostEstimate cost = resource.estimateResendCost("doc-1", "a1", "s1");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath())
                .isEqualTo("/documents/doc-1/assignments/a1/signers/s1/estimate-resend-cost");
        assertThat(cost.getTotal()).isZero();
        assertThat(cost.getTotalCredits()).isZero();
        assertThat(cost.getHasSufficientCredits()).isTrue();
        assertThat(cost.getHasSufficientResources()).isTrue();
        assertThat(cost.getBreakdown().get(0).getCode()).isEqualTo("NotificationEmailResend");
    }

    @Test
    void estimateResendCost_parsesCompleteShape() throws Exception {
        server.enqueue(okJson(Map.of(
                "total_credits", 2.5,
                "credit_balance", 7.0,
                "has_sufficient_resources", true,
                "documents", 1)));

        ResendCostEstimate cost = resource.estimateResendCost("doc-1", "a1", "s1");

        assertThat(cost.getTotal()).isEqualTo(2.5);
        assertThat(cost.getTotalCredits()).isEqualTo(2.5);
        assertThat(cost.getHasSufficientCredits()).isTrue();
        assertThat(cost.getDocuments()).isEqualTo(1);
    }

    @Test
    void resendCompactSettersKeepAliasesSynchronized() {
        ResendCostEstimate cost = new ResendCostEstimate();
        cost.setTotal(2.5);
        cost.setHasSufficientCredits(true);

        cost.setTotal(null);
        cost.setHasSufficientCredits(null);

        assertThat(cost.getTotal()).isNull();
        assertThat(cost.getTotalCredits()).isNull();
        assertThat(cost.getHasSufficientCredits()).isNull();
        assertThat(cost.getHasSufficientResources()).isNull();
    }
}
