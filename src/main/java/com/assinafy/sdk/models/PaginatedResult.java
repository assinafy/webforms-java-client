package com.assinafy.sdk.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable page data plus pagination metadata parsed from response headers.
 *
 * @param <T> item model type
 */
public final class PaginatedResult<T> {

    private final List<T> data;
    private final PaginationMeta meta;

    /**
     * Creates an instance.
     *
     * @param data page items; copied defensively, with {@code null} treated as empty
     * @param meta pagination headers, or {@code null} when the endpoint did not provide them
     */
    public PaginatedResult(List<T> data, PaginationMeta meta) {
        this.data = data != null
                ? Collections.unmodifiableList(new ArrayList<>(data))
                : Collections.emptyList();
        this.meta = meta;
    }

    /**
     * Returns immutable page items, never {@code null}.
     *
     * @return immutable page items, never {@code null}
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Returns parsed pagination metadata, or {@code null} when unavailable.
     *
     * @return parsed pagination metadata, or {@code null} when unavailable
     */
    public PaginationMeta getMeta() {
        return meta;
    }
}
