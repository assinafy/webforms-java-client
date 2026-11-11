package com.assinafy.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** Workspace notification rules. Null fields are omitted on update. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class NotificationPolicy {

    @JsonProperty(value = "editable", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean editable;

    @JsonProperty("owners_receive_all")
    private Boolean ownersReceiveAll;

    @JsonProperty("events")
    private Map<String, Boolean> events;

    /** Creates an empty model. */
    public NotificationPolicy() {}

    /**
     * Returns whether the caller owns the workspace (response only).
     * @return whether the caller owns the workspace (response only)
     */
    public Boolean getEditable() { return editable; }

    /**
     * Sets whether the caller owns the workspace (response only).
     * @param value whether the caller owns the workspace (response only)
     * @return this model
     */
    public NotificationPolicy setEditable(Boolean value) { editable = value; return this; }

    /**
     * Returns whether owners receive member document push alerts.
     * @return whether owners receive member document push alerts
     */
    public Boolean getOwnersReceiveAll() { return ownersReceiveAll; }

    /**
     * Sets whether owners receive member document push alerts.
     * @param value whether owners receive member document push alerts
     * @return this model
     */
    public NotificationPolicy setOwnersReceiveAll(Boolean value) { ownersReceiveAll = value; return this; }

    /**
     * Returns event switches; false suppresses this event workspace-wide.
     * @return event switches; false suppresses this event workspace-wide
     */
    public Map<String, Boolean> getEvents() { return events; }

    /**
     * Sets event switches; false suppresses this event workspace-wide.
     * @param value event switches; false suppresses this event workspace-wide
     * @return this model
     */
    public NotificationPolicy setEvents(Map<String, Boolean> value) { events = value; return this; }
}
