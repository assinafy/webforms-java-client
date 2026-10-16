package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Account review of a signer's biometric verification. InReview permits signing but holds certification.
 *
 * @param id verification identifier
 * @param status Pending, InReview, Approved, Declined, Expired, or Abandoned
 * @param provider verification provider
 * @param verificationMethod wire verification method, IdDocumentFaceMatchBasic
 * @param reasonCodes reasons for review in provider order
 * @param evidenceAvailable whether the evidence summary is available
 * @param expected identity registered for the signer, or null
 * @param document identity read from the document, or null
 * @param matches identity match results, or null
 * @param checks provider checks
 * @param images available evidence image names, only while InReview
 * @param rulesetVersion provider ruleset, or null
 * @param evidenceSha256 evidence digest, or null
 * @param reviewedBy account reviewer, or null
 * @param reviewedAt review time in ISO-8601, or null
 * @param reviewNote private review note, or null
 * @param completedAt verification completion time in ISO-8601, or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignerIdentityReview(
        String id,
        String status,
        String provider,
        @JsonProperty("verification_method") String verificationMethod,
        @JsonProperty("reason_codes") List<Reason> reasonCodes,
        @JsonProperty("evidence_available") boolean evidenceAvailable,
        Expected expected,
        IdentityDocument document,
        Matches matches,
        List<Check> checks,
        List<String> images,
        @JsonProperty("ruleset_version") String rulesetVersion,
        @JsonProperty("evidence_sha256") String evidenceSha256,
        @JsonProperty("reviewed_by") Reviewer reviewedBy,
        @JsonProperty("reviewed_at") String reviewedAt,
        @JsonProperty("review_note") String reviewNote,
        @JsonProperty("completed_at") String completedAt) {

    /**
     * Reason the provider requested review.
     * @param code provider reason code
     * @param description explanation, or the code itself for an unknown code
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reason(String code, String description) {}

    /**
     * Registered signer identity.
     * @param name registered name, or null
     * @param cpf registered CPF, or null
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Expected(String name, String cpf) {}

    /**
     * Identity read from the government document.
     * @param type document type, or null
     * @param name document name, or null
     * @param cpf document CPF, or null
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IdentityDocument(String type, String name, String cpf) {}

    /**
     * Identity matching results.
     * @param cpf whether the CPF matches, or null
     * @param name name similarity from zero to one, or null
     * @param birthDate whether the birth date matches, or null
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Matches(Boolean cpf, Double name, @JsonProperty("birth_date") Boolean birthDate) {}

    /**
     * Provider check result.
     * @param name check name
     * @param result pass, review, skipped, or a provider failure result
     * @param score check score, or null
     * @param reasons reason codes
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Check(String name, String result, Double score, List<String> reasons) {}

    /**
     * Account user who decided the review.
     * @param id user identifier
     * @param name user name
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reviewer(String id, String name) {}
}
