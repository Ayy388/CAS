package com.cas.modules.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollRequest {
    @NotNull(message = "开课ID不能为空")
    private Long offeringId;
}