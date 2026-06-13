package com.cas.modules.semester.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterVO {

    private Long id;
    private String name;
    private String academicYear;
    private String semesterType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}