package com.assinafy.sdk.models;

public final class SigningProgress {

    private final int signed;
    private final int total;
    private final double percentage;
    private final int pending;

    public SigningProgress(int signed, int total, double percentage, int pending) {
        this.signed = signed;
        this.total = total;
        this.percentage = percentage;
        this.pending = pending;
    }

    public int getSigned() { return signed; }
    public int getTotal() { return total; }
    public double getPercentage() { return percentage; }
    public int getPending() { return pending; }
}
