package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;

/**
 * One value submitted when completing a collect assignment.
 *
 * @param itemId assignment item identifier
 * @param fieldId field identifier
 * @param pageId document page identifier
 * @param value string representation of the signed value
 */
public record AssignmentSignEntry(String itemId, String fieldId, String pageId, String value) {
    /**
     * Validates the four fields required by the signing endpoint.
     *
     * @param itemId assignment item identifier
     * @param fieldId field identifier
     * @param pageId document page identifier
     * @param value string representation of the signed value
     */
    public AssignmentSignEntry {
        if (itemId == null || itemId.isBlank() || fieldId == null || fieldId.isBlank()
                || pageId == null || pageId.isBlank() || value == null) {
            throw new ValidationException("itemId, fieldId, pageId, and value are required");
        }
    }
}
