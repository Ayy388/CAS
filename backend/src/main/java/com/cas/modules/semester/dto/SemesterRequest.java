package com.cas.modules.semester.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterRequest {

    @NotBlank(message = "学期名称不能为空")
    private String name;

    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @NotBlank(message = "学期类型不能为空")
    private String semesterType;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
}