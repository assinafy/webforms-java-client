package com.assinafy.sdk;

import com.assinafy.sdk.exceptions.ValidationException;
import com.assinafy.sdk.models.PaginatedResult;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImmutableSnapshotTest {

    @Test
    void validationExceptionSnapshotsAndProtectsErrors() {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("email", "invalid");

        ValidationException exception = new ValidationException("Invalid input", source);
        source.put("name", "missing");

        assertThat(exception.getErrors()).containsExactly(Map.entry("email", "invalid"));
        assertThatThrownBy(() -> exception.getErrors().put("extra", true))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void paginatedResultSnapshotsAndProtectsData() {
        List<String> source = new ArrayList<>(List.of("first"));

        PaginatedResult<String> page = new PaginatedResult<>(source, null);
        source.set(0, "changed");
        source.add("second");

        assertThat(page.getData()).containsExactly("first");
        assertThatThrownBy(() -> page.getData().add("third"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(new PaginatedResult<String>(null, null).getData()).isEmpty();
    }

    @Test
    void workflowResultSnapshotsAndProtectsSignerIds() {
        List<String> source = new ArrayList<>(List.of("signer-1"));

        UploadAndRequestSignaturesResult result = new UploadAndRequestSignaturesResult(null, null, source);
        source.set(0, "changed");

        assertThat(result.getSignerIds()).containsExactly("signer-1");
        assertThatThrownBy(() -> result.getSignerIds().add("signer-2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
