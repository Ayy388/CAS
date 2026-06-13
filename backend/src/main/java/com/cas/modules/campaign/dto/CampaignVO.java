package com.cas.modules.campaign.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampaignVO {
    private Long id;
    private String name;
    private Long semesterId;
    private String semesterName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
}