package com.gdc.backend.treatment.dto;

import java.util.List;

public record FollowUpContextResponse(
        TreatmentResponse latestTreatment,
        List<FollowUpResponse> previousFollowUps
) {
}
