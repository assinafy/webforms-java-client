package com.assinafy.sdk.models;

public final class ListDispatchesParams {

    private Integer page;
    private Integer perPage;
    private String event;
    private Boolean delivered;
    private Long from;
    private Long to;

    public Integer getPage() { return page; }
    public ListDispatchesParams setPage(Integer page) { this.page = page; return this; }

    public Integer getPerPage() { return perPage; }
    public ListDispatchesParams setPerPage(Integer perPage) { this.perPage = perPage; return this; }

    public String getEvent() { return event; }
    public ListDispatchesParams setEvent(String event) { this.event = event; return this; }

    public Boolean getDelivered() { return delivered; }
    public ListDispatchesParams setDelivered(Boolean delivered) { this.delivered = delivered; return this; }

    public Long getFrom() { return from; }
    public ListDispatchesParams setFrom(Long from) { this.from = from; return this; }

    public Long getTo() { return to; }
    public ListDispatchesParams setTo(Long to) { this.to = to; return this; }
}
