package com.assinafy.sdk.models;

import java.util.Collections;
import java.util.List;

public final class PaginatedResult<T> {

    private final List<T> data;
    private final PaginationMeta meta;

    public PaginatedResult(List<T> data, PaginationMeta meta) {
        this.data = data != null ? Collections.unmodifiableList(data) : Collections.emptyList();
        this.meta = meta;
    }

    public List<T> getData() {
        return data;
    }

    public PaginationMeta getMeta() {
        return meta;
    }
}
