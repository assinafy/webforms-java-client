package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * One document page and its field placements for a collect assignment.
 *
 * @param pageId document page identifier
 * @param fields fields placed on that page
 */
public record CollectAssignmentEntry(
        @JsonProperty("page_id") String pageId,
        List<CollectFieldPlacement> fields) {
    /**
     * Validates the page and takes an immutable snapshot of its fields.
     *
     * @param pageId document page identifier serialized as {@code page_id}
     * @param fields fields placed on the page
     */
    public CollectAssignmentEntry {
        if (pageId == null || pageId.isBlank() || fields == null || fields.isEmpty()
                || fields.stream().anyMatch(java.util.Objects::isNull)) {
            throw new ValidationException("pageId and at least one non-null field are required");
        }
        fields = List.copyOf(fields);
    }
}
