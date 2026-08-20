package com.assinafy.sdk.models;

/** Optional pagination and filter query parameters for listing webhook dispatch history. */
public final class ListDispatchesParams {

    /** Creates an empty webhook-dispatch query. */
    public ListDispatchesParams() {}

    private Integer page;
    private Integer perPage;
    private String event;
    private Boolean delivered;
    private Long from;
    private Long to;

    /**
     * Returns one-based result page, or {@code null} for the API default.
     *
     * @return one-based result page, or {@code null} for the API default
     */
    public Integer getPage() { return page; }

    /**
     * Sets one-based result page.
     *
     * @param page one-based result page
     * @return this parameter object
     */
    public ListDispatchesParams setPage(Integer page) { this.page = page; return this; }

    /**
     * Returns {@code per-page} limit, or {@code null} for the API default of 20.
     *
     * @return {@code per-page} limit, or {@code null} for the API default of 20
     */
    public Integer getPerPage() { return perPage; }

    /**
     * Sets number of items requested per page.
     *
     * @param perPage number of items requested per page
     * @return this parameter object
     */
    public ListDispatchesParams setPerPage(Integer perPage) { this.perPage = perPage; return this; }

    /**
     * Returns webhook event-type filter, or {@code null} for all events.
     *
     * @return webhook event-type filter, or {@code null} for all events
     */
    public String getEvent() { return event; }

    /**
     * Sets webhook event type, such as {@code document_ready}.
     *
     * @param event webhook event type, such as {@code document_ready}
     * @return this parameter object
     */
    public ListDispatchesParams setEvent(String event) { this.event = event; return this; }

    /**
     * Returns delivery-status filter, or {@code null} for both states.
     *
     * @return delivery-status filter, or {@code null} for both states
     */
    public Boolean getDelivered() { return delivered; }

    /**
     * Sets whether to include delivered or undelivered dispatches.
     *
     * @param delivered whether to include delivered or undelivered dispatches
     * @return this parameter object
     */
    public ListDispatchesParams setDelivered(Boolean delivered) { this.delivered = delivered; return this; }

    /**
     * Returns lower Unix timestamp cutoff, or {@code null}.
     *
     * @return lower Unix timestamp cutoff, or {@code null}
     */
    public Long getFrom() { return from; }

    /**
     * Sets include entries after this Unix timestamp.
     *
     * @param from include entries after this Unix timestamp
     * @return this parameter object
     */
    public ListDispatchesParams setFrom(Long from) { this.from = from; return this; }

    /**
     * Returns upper Unix timestamp cutoff, or {@code null}.
     *
     * @return upper Unix timestamp cutoff, or {@code null}
     */
    public Long getTo() { return to; }

    /**
     * Sets include entries before this Unix timestamp.
     *
     * @param to include entries before this Unix timestamp
     * @return this parameter object
     */
    public ListDispatchesParams setTo(Long to) { this.to = to; return this; }
}
