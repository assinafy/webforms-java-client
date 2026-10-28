package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.ContractExternalReference;
import com.assinafy.sdk.models.ContractRenewalPayload;
import com.assinafy.sdk.models.ContractTerminationPayload;
import com.assinafy.sdk.models.DocumentContract;
import com.assinafy.sdk.models.DocumentContractAmendment;
import com.assinafy.sdk.models.DocumentContractInput;
import com.assinafy.sdk.models.DocumentContractResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContractResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockWebServer server;
    private ContractResource resource;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        resource = new ContractResource(new OkHttpClient(), server.url("/").toString(), "acc");
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private MockResponse okJson(Object data) throws Exception {
        return new MockResponse().setHeader("Content-Type", "application/json")
                .setBody(MAPPER.writeValueAsString(Map.of("status", 200, "data", data)));
    }

    @Test
    void listSendsFiltersAndParsesPaginationAndCompleteContract() throws Exception {
        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json")
                .setHeader("X-Pagination-Current-Page", "2")
                .setHeader("X-Pagination-Per-Page", "10")
                .setHeader("X-Pagination-Total-Count", "11")
                .setHeader("X-Pagination-Page-Count", "2")
                .setBody(MAPPER.writeValueAsString(Map.of("status", 200, "data", List.of(Map.ofEntries(
                        Map.entry("document_id", "doc-1"),
                        Map.entry("status", "active"), Map.entry("value", new BigDecimal("120000.50")),
                        Map.entry("currency", "BRL"), Map.entry("auto_renew", true), Map.entry("term", 1),
                        Map.entry("external_references", List.of(Map.of("system", "hubspot", "id", "98765"))),
                        Map.entry("created_at", "2026-10-20T12:00:00Z"),
                        Map.entry("updated_at", "2026-10-23T12:00:00Z")))))));

        var page = resource.list(Map.of("status", "active,expired", "expand", "document", "page", "2",
                "per_page", "10"));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getRequestUrl().encodedPath()).isEqualTo("/accounts/acc/contracts");
        assertThat(request.getRequestUrl().queryParameter("status")).isEqualTo("active,expired");
        assertThat(request.getRequestUrl().queryParameter("expand")).isEqualTo("document");
        assertThat(request.getRequestUrl().queryParameter("per-page")).isEqualTo("10");
        assertThat(page.getMeta().getTotal()).isEqualTo(11);
        assertThat(page.getData()).singleElement().satisfies(contract -> {
            assertThat(contract.getDocumentId()).isEqualTo("doc-1");
            assertThat(contract.getValue()).isEqualByComparingTo("120000.50");
            assertThat(contract.getExternalReferences()).containsExactly(
                    new ContractExternalReference("hubspot", "98765"));
        });
    }

    @Test
    void getParsesContractAndAmendmentByResourceDiscriminator() throws Exception {
        server.enqueue(okJson(Map.of("document_id", "doc-1",
                "status", "active", "auto_renew", false, "term", 1)));
        server.enqueue(okJson(Map.of("document_id", "doc-2",
                "document_name", "Amendment.pdf", "parent_document_id", "doc-1",
                "relation_type", "amendment", "status", "pending", "document_status", "pending_signature")));

        DocumentContractResult contract = resource.get("doc-1");
        DocumentContractResult amendment = resource.get("doc-2", "other");

        assertThat(contract).isInstanceOf(DocumentContract.class);
        assertThat(amendment).isInstanceOfSatisfying(DocumentContractAmendment.class,
                value -> assertThat(value.getParentDocumentId()).isEqualTo("doc-1"));
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/acc/documents/doc-1/contract");
        assertThat(server.takeRequest().getPath()).isEqualTo("/accounts/other/documents/doc-2/contract");
    }

    @Test
    void setSendsPartialTermsAndPreservesExplicitNull() throws Exception {
        server.enqueue(okJson(Map.of("resource", "document_contract", "document_id", "doc-1",
                "status", "active", "auto_renew", false, "term", 1)));
        DocumentContractInput input = new DocumentContractInput()
                .setCounterpartyName("Acme Ltda")
                .setValue(new BigDecimal("120000.50"))
                .setCurrency("BRL")
                .setEndDate(null)
                .setExternalReferences(List.of(new ContractExternalReference("hubspot", "98765")))
                .setUpdatedAt("2026-10-23T12:00:00Z");

        resource.set("doc-1", input);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getPath()).isEqualTo("/accounts/acc/documents/doc-1/contract");
        JsonNode body = MAPPER.readTree(request.getBody().readUtf8());
        assertThat(body.get("counterparty_name").asText()).isEqualTo("Acme Ltda");
        assertThat(body.get("value").decimalValue()).isEqualByComparingTo("120000.50");
        assertThat(body.get("end_date").isNull()).isTrue();
        assertThat(body.get("external_references").get(0).get("system").asText()).isEqualTo("hubspot");
        assertThat(body.get("updated_at").asText()).isEqualTo("2026-10-23T12:00:00Z");
    }

    @Test
    void relatedReadsRootContractFamily() throws Exception {
        server.enqueue(okJson(Map.of("resource", "document_contract", "document_id", "doc-1",
                "status", "active", "auto_renew", false, "term", 1,
                "amendments", List.of(Map.of("document_id", "doc-2", "parent_document_id", "doc-1",
                        "relation_type", "renewal",
                        "status", "applied", "document_status", "certificated")))));

        DocumentContract result = resource.related("doc-2");

        assertThat(server.takeRequest().getPath())
                .isEqualTo("/accounts/acc/documents/doc-2/contract/related");
        assertThat(result.getAmendments()).singleElement()
                .extracting(DocumentContractAmendment::getRelationType).isEqualTo("renewal");
    }

    @Test
    void renewSendsIdempotencyInputsExactlyOnce() throws Exception {
        server.enqueue(okJson(Map.of("resource", "document_contract", "document_id", "doc-1",
                "status", "active", "auto_renew", true, "term", 2)));

        resource.renew("doc-1", new ContractRenewalPayload().setTerm(1)
                .setValue(new BigDecimal("130000.00")).setUpdatedAt("2026-10-23T12:00:00Z"));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/accounts/acc/documents/doc-1/contract/renew");
        assertThat(MAPPER.readTree(request.getBody().readUtf8())).isEqualTo(MAPPER.readTree(
                "{\"term\":1,\"value\":130000.00,\"updated_at\":\"2026-10-23T12:00:00Z\"}"));
    }

    @Test
    void terminateSendsOptionalReasonAndPrecondition() throws Exception {
        server.enqueue(okJson(Map.of("resource", "document_contract", "document_id", "doc-1",
                "status", "terminated", "auto_renew", false, "term", 1)));

        resource.terminate("doc-1", new ContractTerminationPayload().setReason("Replaced")
                .setUpdatedAt("2026-10-23T12:00:00Z"));

        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/accounts/acc/documents/doc-1/contract/terminate");
        assertThat(request.getBody().readUtf8())
                .isEqualTo("{\"reason\":\"Replaced\",\"updated_at\":\"2026-10-23T12:00:00Z\"}");
    }

    @Test
    void validatesAccountDocumentPayloadAndTerminationReasonBeforeHttp() {
        ContractResource noAccount = new ContractResource(new OkHttpClient(), server.url("/").toString(), null);

        assertThatThrownBy(noAccount::list).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.get("../bad")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.set("doc-1", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.set("doc-1", new DocumentContractInput()))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> resource.terminate("doc-1",
                new ContractTerminationPayload().setReason("x".repeat(501))))
                .isInstanceOf(ValidationException.class);
        assertThat(server.getRequestCount()).isZero();
    }
}
