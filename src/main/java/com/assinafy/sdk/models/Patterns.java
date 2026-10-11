package com.assinafy.sdk.models;

import java.util.regex.Pattern;

/** Shared validation patterns used by both request models and endpoint resources. */
public final class Patterns {

    /** Basic email shape accepted at API request boundaries: no whitespace, one {@code @}, a dotted domain. */
    public static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Patterns() {}
}
