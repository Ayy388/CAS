package com.cas.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEvent implements Serializable {
    private Long offeringId;
    private String type; // APPROVED / REJECTED
}