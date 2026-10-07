package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.WebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;

/**
 * Verifies the Standard Webhooks signature on an Assinafy webhook delivery and parses its body.
 *
 * <p>Deliveries to an endpoint with {@code signing_enabled} carry three headers: {@code webhook-id},
 * {@code webhook-timestamp}, and {@code webhook-signature}. Build one verifier per endpoint from the secret
 * returned by {@code webhooks.getEndpointSecret(...)}, and pass it the headers and the <strong>raw</strong>
 * request body exactly as received; re-serialized JSON does not verify. Deliveries whose timestamp is more than
 * {@link #TOLERANCE} away from the local clock are rejected as replays.</p>
 *
 * <p>The {@code webhook-id} header is identical on every attempt of the same event to the same endpoint; use it
 * to deduplicate. Rotating the secret takes effect immediately, so replace the verifier at the same time.</p>
 *
 * <p>Instances are immutable and thread-safe.</p>
 */
public final class WebhookVerifier {

    /** Maximum distance between {@code webhook-timestamp} and the local clock. */
    public static final Duration TOLERANCE = Duration.ofMinutes(5);

    private static final String SECRET_PREFIX = "whsec_";
    private static final String HMAC = "HmacSHA256";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SecretKeySpec key;
    private final Clock clock;

    /**
     * Creates a verifier for one endpoint's signing secret.
     *
     * @param secret endpoint secret: {@code whsec_} followed by the base64-encoded key
     * @throws ValidationException when the secret is missing or not valid base64
     */
    public WebhookVerifier(String secret) {
        this(secret, Clock.systemUTC());
    }

    WebhookVerifier(String secret, Clock clock) {
        if (secret == null || secret.isBlank()) {
            throw new ValidationException("Webhook signing secret is required");
        }
        String encoded = secret.startsWith(SECRET_PREFIX) ? secret.substring(SECRET_PREFIX.length()) : secret;
        try {
            this.key = new SecretKeySpec(Base64.getDecoder().decode(encoded), HMAC);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Webhook signing secret is not valid base64");
        }
        this.clock = clock;
    }

    /**
     * Verifies a delivery and parses its body.
     *
     * @param webhookId value of the {@code webhook-id} header
     * @param webhookTimestamp value of the {@code webhook-timestamp} header, in Unix seconds
     * @param webhookSignature value of the {@code webhook-signature} header: one or more space-separated
     *        {@code v1,<signature>} entries
     * @param body raw request body bytes
     * @return parsed delivery
     * @throws ValidationException when a header is missing, the timestamp is outside {@link #TOLERANCE}, no
     *         signature matches, or the body is not a webhook event
     */
    public WebhookEvent verify(String webhookId, String webhookTimestamp, String webhookSignature, byte[] body) {
        if (webhookId == null || webhookId.isBlank() || webhookTimestamp == null || webhookSignature == null
                || body == null) {
            throw new ValidationException("Webhook id, timestamp, signature, and body are required");
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(webhookTimestamp.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Webhook timestamp is not a Unix timestamp");
        }
        if (Math.abs(clock.instant().getEpochSecond() - timestamp) > TOLERANCE.toSeconds()) {
            throw new ValidationException("Webhook timestamp is outside the allowed tolerance");
        }

        byte[] expected = sign(webhookId + "." + webhookTimestamp + ".", body);
        boolean matched = false;
        for (String entry : webhookSignature.trim().split(" +")) {
            if (entry.startsWith("v1,")) {
                matched |= MessageDigest.isEqual(expected, entry.substring(3).getBytes(StandardCharsets.US_ASCII));
            }
        }
        if (!matched) {
            throw new ValidationException("Webhook signature does not match");
        }

        try {
            return MAPPER.readValue(body, WebhookEvent.class);
        } catch (IOException e) {
            throw new ValidationException("Webhook body is not a valid webhook event", e);
        }
    }

    /**
     * Verifies a delivery whose raw body is already a string, encoding it as UTF-8.
     *
     * @param webhookId value of the {@code webhook-id} header
     * @param webhookTimestamp value of the {@code webhook-timestamp} header, in Unix seconds
     * @param webhookSignature value of the {@code webhook-signature} header
     * @param body raw request body, unmodified
     * @return parsed delivery
     * @throws ValidationException when the delivery fails verification
     */
    public WebhookEvent verify(String webhookId, String webhookTimestamp, String webhookSignature, String body) {
        return verify(webhookId, webhookTimestamp, webhookSignature,
                body == null ? null : body.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] sign(String prefix, byte[] body) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(key);
            mac.update(prefix.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encode(mac.doFinal(body));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is unavailable", e);
        }
    }
}
