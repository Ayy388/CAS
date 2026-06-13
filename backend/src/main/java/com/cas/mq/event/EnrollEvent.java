package com.cas.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollEvent implements Serializable {
    private Long enrollmentId;
    private Long studentId;
    private Long offeringId;
    private Long campaignId;
    private String courseName;
}