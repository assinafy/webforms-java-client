package com.assinafy.sdk.models;

import java.io.File;
import java.util.List;

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

    public UploadAndRequestSignaturesOptions(File file, List<UploadAndRequestSignaturesSigner> signers) {
        this.file = file;
        this.signers = signers;
    }

    public UploadAndRequestSignaturesOptions(byte[] fileBytes, String fileName,
            List<UploadAndRequestSignaturesSigner> signers) {
        this.fileBytes = fileBytes;
        this.fileName = fileName;
        this.signers = signers;
    }

    public File getFile() { return file; }
    public byte[] getFileBytes() { return fileBytes; }

    public String getFileName() {
        return fileName != null ? fileName : (file != null ? file.getName() : null);
    }

    public List<UploadAndRequestSignaturesSigner> getSigners() { return signers; }

    public Boolean getWaitForReady() { return waitForReady; }
    public UploadAndRequestSignaturesOptions setWaitForReady(Boolean waitForReady) {
        this.waitForReady = waitForReady;
        return this;
    }

    public String getMessage() { return message; }
    public UploadAndRequestSignaturesOptions setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getExpiresAt() { return expiresAt; }
    public UploadAndRequestSignaturesOptions setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public List<String> getCopyReceivers() { return copyReceivers; }
    public UploadAndRequestSignaturesOptions setCopyReceivers(List<String> copyReceivers) {
        this.copyReceivers = copyReceivers;
        return this;
    }

    public String getAccountId() { return accountId; }
    public UploadAndRequestSignaturesOptions setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }
}
