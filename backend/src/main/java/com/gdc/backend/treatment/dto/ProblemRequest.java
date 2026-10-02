package com.gdc.backend.treatment.dto;

import com.gdc.backend.treatment.entity.ImpactionType;
import com.gdc.backend.treatment.entity.ProblemType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProblemRequest(
        @NotNull(message = "Problem type is required")
        ProblemType problemType,

        @NotEmpty(message = "At least one tooth is required")
        List<Integer> teeth,

        @Size(max = 2000, message = "Problem notes are too long")
        String notes,

        ImpactionType impactionType,

        @Valid
        ImplantDetailRequest implantDetail,

        @Valid
        List<RootCanalLengthRequest> rootCanalLengths
) {
}
