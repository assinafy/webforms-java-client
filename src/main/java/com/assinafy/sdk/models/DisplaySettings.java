package com.assinafy.sdk.models;

import com.assinafy.sdk.exceptions.ValidationException;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Pixel geometry for a collect field on Assinafy's 150-DPI page image.
 *
 * @param left distance from the left page edge
 * @param top distance from the top page edge
 * @param width rectangle width; must be positive
 * @param height rectangle height; must be positive
 * @param fontSize font size; must be positive
 * @param fontFamily optional font family
 * @param backgroundColor optional CSS-compatible background color
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DisplaySettings(
        double left,
        double top,
        double width,
        double height,
        double fontSize,
        String fontFamily,
        String backgroundColor) {

    /**
     * Validates finite, non-negative offsets and positive dimensions.
     *
     * @param left distance from the left page edge
     * @param top distance from the top page edge
     * @param width rectangle width
     * @param height rectangle height
     * @param fontSize font size
     * @param fontFamily optional font family
     * @param backgroundColor optional CSS-compatible background color
     * @throws ValidationException if geometry is non-finite or outside the supported numeric range
     */
    public DisplaySettings {
        if (!Double.isFinite(left) || !Double.isFinite(top) || !Double.isFinite(width)
                || !Double.isFinite(height) || !Double.isFinite(fontSize)
                || left < 0 || top < 0 || width <= 0 || height <= 0 || fontSize <= 0) {
            throw new ValidationException(
                    "Display settings require finite non-negative offsets and positive dimensions/fontSize");
        }
    }

    /**
     * Creates geometry without optional presentation metadata.
     *
     * @param left distance from the left page edge
     * @param top distance from the top page edge
     * @param width rectangle width
     * @param height rectangle height
     * @param fontSize font size
     * @throws ValidationException if geometry is non-finite or outside the supported numeric range
     */
    public DisplaySettings(double left, double top, double width, double height, double fontSize) {
        this(left, top, width, height, fontSize, null, null);
    }
}
