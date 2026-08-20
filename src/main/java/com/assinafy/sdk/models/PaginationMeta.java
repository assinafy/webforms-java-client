package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Pagination values read from the API's {@code X-Pagination-*} response headers. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PaginationMeta {

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("per_page")
    private Integer perPage;

    private Integer total;

    @JsonProperty("last_page")
    private Integer lastPage;

    /** Creates empty pagination metadata for response parsing. */
    public PaginationMeta() {}

    /**
     * Returns wire {@code current_page}, or {@code null} when absent.
     *
     * @return wire {@code current_page}, or {@code null} when absent
     */
    public Integer getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets wire {@code current_page} parsed from the response header.
     *
     * @param currentPage wire {@code current_page} parsed from the response header
     */
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Returns wire {@code per_page}, or {@code null} when absent.
     *
     * @return wire {@code per_page}, or {@code null} when absent
     */
    public Integer getPerPage() {
        return perPage;
    }

    /**
     * Sets wire {@code per_page} parsed from the response header.
     *
     * @param perPage wire {@code per_page} parsed from the response header
     */
    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    /**
     * Returns total matching records, or {@code null} when absent.
     *
     * @return total matching records, or {@code null} when absent
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Sets total matching records parsed from the response header.
     *
     * @param total total matching records parsed from the response header
     */
    public void setTotal(Integer total) {
        this.total = total;
    }

    /**
     * Returns final page number, or {@code null} when absent.
     *
     * @return final page number, or {@code null} when absent
     */
    public Integer getLastPage() {
        return lastPage;
    }

    /**
     * Sets final page number parsed from the response header.
     *
     * @param lastPage final page number parsed from the response header
     */
    public void setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
    }
}
