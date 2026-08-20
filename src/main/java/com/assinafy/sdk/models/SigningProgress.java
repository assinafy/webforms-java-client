package com.assinafy.sdk.models;

/** Immutable summary of document signing completion. */
public final class SigningProgress {

    private final int signed;
    private final int total;
    private final double percentage;
    private final int pending;

    /**
     * Creates an instance.
     *
     * @param signed number of completed signers
     * @param total total number of signers
     * @param percentage completion percentage from {@code 0.0} to {@code 100.0}
     * @param pending number of incomplete signers
     */
    public SigningProgress(int signed, int total, double percentage, int pending) {
        this.signed = signed;
        this.total = total;
        this.percentage = percentage;
        this.pending = pending;
    }

    /**
     * Returns number of completed signers.
     *
     * @return number of completed signers
     */
    public int getSigned() { return signed; }

    /**
     * Returns total number of signers.
     *
     * @return total number of signers
     */
    public int getTotal() { return total; }

    /**
     * Returns completion percentage from {@code 0.0} to {@code 100.0}.
     *
     * @return completion percentage from {@code 0.0} to {@code 100.0}
     */
    public double getPercentage() { return percentage; }

    /**
     * Returns number of incomplete signers.
     *
     * @return number of incomplete signers
     */
    public int getPending() { return pending; }
}
