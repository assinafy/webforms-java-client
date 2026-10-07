package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.WebhookEvent;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookVerifierTest {

    // Standard Webhooks reference test vector.
    private static final String SECRET = "whsec_MfKQ9r8GKYqrTwjUPD8ILPZIo2LaLaSw";
    private static final String ID = "msg_p5jXN8AQM9LWM0D4loKWxJek";
    private static final String TIMESTAMP = "1614265330";
    private static final String BODY = "{\"test\": 2432232314}";
    private static final String SIGNATURE = "v1,g0hM9SsE+OTPJTGt/tmIKtSyZlE3uFJELVlNIOLJ1OE=";

    private static WebhookVerifier verifierAt(long epochSecond) {
        return new WebhookVerifier(SECRET, Clock.fixed(Instant.ofEpochSecond(epochSecond), ZoneOffset.UTC));
    }

    @Test
    void acceptsTheStandardWebhooksReferenceSignature() {
        assertThat(verifierAt(1614265330).verify(ID, TIMESTAMP, SIGNATURE, BODY)).isNotNull();
    }

    @Test
    void acceptsAnyMatchingEntryAmongSeveralAndIgnoresOtherVersions() {
        String header = "v1a,abc v1,bm90LXRoZS1zaWduYXR1cmU= " + SIGNATURE;
        assertThat(verifierAt(1614265330).verify(ID, TIMESTAMP, header, BODY)).isNotNull();
    }

    @Test
    void acceptsASecretWithoutThePrefix() {
        WebhookVerifier verifier = new WebhookVerifier(SECRET.substring("whsec_".length()),
                Clock.fixed(Instant.ofEpochSecond(1614265330), ZoneOffset.UTC));
        assertThat(verifier.verify(ID, TIMESTAMP, SIGNATURE, BODY)).isNotNull();
    }

    @Test
    void rejectsATamperedBodyIdOrSignature() {
        WebhookVerifier verifier = verifierAt(1614265330);
        assertThatThrownBy(() -> verifier.verify(ID, TIMESTAMP, SIGNATURE, "{\"test\": 2432232315}"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("signature");
        assertThatThrownBy(() -> verifier.verify("msg_other", TIMESTAMP, SIGNATURE, BODY))
                .isInstanceOf(ValidationException.class).hasMessageContaining("signature");
        assertThatThrownBy(() -> verifier.verify(ID, TIMESTAMP, "v1,AAAA", BODY))
                .isInstanceOf(ValidationException.class).hasMessageContaining("signature");
    }

    @Test
    void rejectsTimestampsOutsideTheTolerance() {
        long tolerance = WebhookVerifier.TOLERANCE.toSeconds();
        assertThat(verifierAt(1614265330 + tolerance).verify(ID, TIMESTAMP, SIGNATURE, BODY)).isNotNull();
        assertThatThrownBy(() -> verifierAt(1614265330 + tolerance + 1).verify(ID, TIMESTAMP, SIGNATURE, BODY))
                .isInstanceOf(ValidationException.class).hasMessageContaining("tolerance");
        assertThatThrownBy(() -> verifierAt(1614265330 - tolerance - 1).verify(ID, TIMESTAMP, SIGNATURE, BODY))
                .isInstanceOf(ValidationException.class).hasMessageContaining("tolerance");
        assertThatThrownBy(() -> verifierAt(1614265330).verify(ID, "soon", SIGNATURE, BODY))
                .isInstanceOf(ValidationException.class).hasMessageContaining("timestamp");
    }

    @Test
    void rejectsMissingHeadersAndInvalidSecrets() {
        assertThatThrownBy(() -> verifierAt(1614265330).verify(ID, TIMESTAMP, null, BODY))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new WebhookVerifier(" ")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> new WebhookVerifier("whsec_not base64!"))
                .isInstanceOf(ValidationException.class).hasMessageContaining("base64");
    }

    @Test
    void parsesAVerifiedDelivery() throws Exception {
        String body = """
                {"id":184467,"event":"signer_viewed_document","message":null,"payload":{"step":1},
                 "origin":{"ip":"203.0.113.7","user-agent":"Mozilla/5.0"},"created_at":1790000000,
                 "subject":{"type":"Signer","id":"s1"},"object":{"type":"Document","id":"d1"},
                 "account_id":"acc"}""";
        String timestamp = "1790000000";
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(Base64.getDecoder().decode(SECRET.substring(6)), "HmacSHA256"));
        String signature = "v1," + Base64.getEncoder().encodeToString(
                mac.doFinal(("msg_1." + timestamp + "." + body).getBytes(StandardCharsets.UTF_8)));

        WebhookEvent event = verifierAt(1790000010).verify("msg_1", timestamp, signature,
                body.getBytes(StandardCharsets.UTF_8));

        assertThat(event.id()).isEqualTo(184467);
        assertThat(event.event()).isEqualTo("signer_viewed_document");
        assertThat(event.createdAt()).isEqualTo(1790000000L);
        assertThat(event.accountId()).isEqualTo("acc");
        assertThat(event.payload()).containsEntry("step", 1);
        assertThat(event.origin()).containsEntry("user-agent", "Mozilla/5.0");
        assertThat(event.subject()).containsEntry("type", "Signer");
        assertThat(event.object()).containsEntry("type", "Document");
    }
}
