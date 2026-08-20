package com.assinafy.sdk.models;

import java.io.File;
import java.util.List;

/** Request options for {@code AssinafyClient.uploadAndRequestSignatures}. */
public final class UploadAndRequestSignaturesOptions {

    private File file;
    private byte[] fileBytes;
    private String fileName;
    private final List<UploadAndRequestSignaturesSigner> signers;
    private Boolean waitForReady;
    private String message;
    private String expiresAt;
    private List<String> copyReceivers;
    private String accountId;

    /**
     * Creates the workflow request from a PDF file and signer definitions.
     *
     * @param file required PDF file
     * @param signers required signer definitions
     */
    public UploadAndRequestSignaturesOptions(File file, List<UploadAndRequestSignaturesSigner> signers) {
        this.file = file;
        this.signers = signers;
    }

    /**
     * Creates the workflow request from in-memory PDF bytes, a {@code .pdf} name, and signer definitions.
     *
     * @param fileBytes required PDF bytes
     * @param fileName required filename ending in {@code .pdf}
     * @param signers required signer definitions
     */
    public UploadAndRequestSignaturesOptions(byte[] fileBytes, String fileName,
            List<UploadAndRequestSignaturesSigner> signers) {
        this.fileBytes = fileBytes;
        this.fileName = fileName;
        this.signers = signers;
    }

    /**
     * Returns PDF file, or {@code null} for the byte-array form.
     *
     * @return PDF file, or {@code null} for the byte-array form
     */
    public File getFile() { return file; }

    /**
     * Returns PDF bytes, or {@code null} for the file form.
     *
     * @return PDF bytes, or {@code null} for the file form
     */
    public byte[] getFileBytes() { return fileBytes; }

    /**
     * Returns multipart filename.
     *
     * @return multipart filename
     */
    public String getFileName() {
        return fileName != null ? fileName : (file != null ? file.getName() : null);
    }

    /**
     * Returns signers to create or reuse and assign.
     *
     * @return signers to create or reuse and assign
     */
    public List<UploadAndRequestSignaturesSigner> getSigners() { return signers; }

    /**
     * Returns whether to poll for readiness; {@code null} means {@code true}.
     *
     * @return whether to poll for readiness; {@code null} means {@code true}
     */
    public Boolean getWaitForReady() { return waitForReady; }

    /**
     * Controls document-readiness polling before signer and assignment creation.
     *
     * @param waitForReady whether to wait; {@code null} uses {@code true}
     * @return this options object
     */
    public UploadAndRequestSignaturesOptions setWaitForReady(Boolean waitForReady) {
        this.waitForReady = waitForReady;
        return this;
    }

    /**
     * Returns optional invitation message.
     *
     * @return optional invitation message
     */
    public String getMessage() { return message; }

    /**
     * Sets optional invitation message.
     *
     * @param message optional invitation message
     * @return this options object
     */
    public UploadAndRequestSignaturesOptions setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Returns optional ISO-8601 assignment expiration.
     *
     * @return optional ISO-8601 assignment expiration
     */
    public String getExpiresAt() { return expiresAt; }

    /**
     * Sets optional ISO-8601 assignment expiration.
     *
     * @param expiresAt optional ISO-8601 assignment expiration
     * @return this options object
     */
    public UploadAndRequestSignaturesOptions setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * Returns signer IDs that receive a completed copy without signing.
     *
     * @return signer IDs that receive a completed copy without signing
     */
    public List<String> getCopyReceivers() { return copyReceivers; }

    /**
     * Sets optional signer IDs that receive a completed copy.
     *
     * @param copyReceivers optional signer IDs that receive a completed copy
     * @return this options object
     */
    public UploadAndRequestSignaturesOptions setCopyReceivers(List<String> copyReceivers) {
        this.copyReceivers = copyReceivers;
        return this;
    }

    /**
     * Returns explicit account override, or {@code null} for the client default.
     *
     * @return explicit account override, or {@code null} for the client default
     */
    public String getAccountId() { return accountId; }

    /**
     * Sets optional account override for upload and signer creation.
     *
     * @param accountId optional account override for upload and signer creation
     * @return this options object
     */
    public UploadAndRequestSignaturesOptions setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }
}
