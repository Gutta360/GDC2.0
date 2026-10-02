package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.ImpactionType;
import com.gdc.backend.treatment.entity.ProblemType;

import java.util.List;

public record ProblemResponse(
        ProblemType problemType,
        List<Integer> teeth,
        String notes,
        ImpactionType impactionType,
        ImplantDetailResponse implantDetail,
        List<RootCanalLengthResponse> rootCanalLengths
) {
}
