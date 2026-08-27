package com.assinafy.sdk.resources;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.DocumentStatsRow;
import com.assinafy.sdk.models.NotificationPreferences;
import com.assinafy.sdk.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Client for authenticated-user profile, notification-preference, and KPI endpoints. */
public final class UserResource extends BaseResource {

    /**
     * Creates an instance.
     *
     * @param httpClient shared HTTP client
     * @param baseUrl API base URL
     */
    public UserResource(OkHttpClient httpClient, String baseUrl) {
        super(httpClient, baseUrl, null);
    }

    /**
     * {@code GET /users/self} — return the authenticated user's full profile. Accepts a direct-user payload
     * and the supported {@code {user, accounts}} envelope.
     *
     * @return authenticated user, or {@code null} for an empty response
     */
    public User getSelf() {
        JsonNode data = httpGet("/users/self", JsonNode.class);
        if (data == null || data.isNull()) return null;
        JsonNode user = data.has("user") ? data.get("user") : data;
        return user == null || user.isNull() ? null : MAPPER.convertValue(user, User.class);
    }

    /**
     * {@code GET /users/self/notification-preferences}.
     *
     * @return all nine email notification switches
     */
    public NotificationPreferences getNotificationPreferences() {
        return httpGet("/users/self/notification-preferences", NotificationPreferences.class);
    }

    /**
     * {@code PUT /users/self/notification-preferences} — merge non-null switches and return the complete map.
     * Unknown keys cannot be represented by this typed payload.
     *
     * @param preferences required non-empty partial preference update
     * @return complete preference set after the update
     */
    public NotificationPreferences updateNotificationPreferences(NotificationPreferences preferences) {
        if (preferences == null) throw new ValidationException("Notification preferences are required");
        if (MAPPER.valueToTree(preferences).isEmpty()) {
            throw new ValidationException("At least one notification preference is required");
        }
        return httpPut("/users/self/notification-preferences", preferences, NotificationPreferences.class);
    }

    /**
     * {@code GET /users/self/stats} — return KPIs summed across the user's accounts. Supported query keys are
     * {@code granularity} ({@code monthly|daily}) and {@code month} ({@code YYYY-MM}, required for daily).
     *
     * @param params optional {@code granularity} and {@code month} values
     * @return user-wide document KPI rows, never {@code null}
     */
    public List<DocumentStatsRow> stats(Map<String, String> params) {
        List<DocumentStatsRow> result = httpGet("/users/self/stats", params != null ? params : Map.of(),
                new TypeReference<List<DocumentStatsRow>>() {});
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Returns user-wide monthly document KPI rows.
     *
     * @return user-wide monthly document KPI rows
     */
    public List<DocumentStatsRow> stats() { return stats(null); }
}
