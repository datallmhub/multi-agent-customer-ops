package com.demo.agentflow.model;

import lombok.Data;

@Data
public class SupportState {
    private String intent = "UNKNOWN";
    private String sentiment = "UNKNOWN";
    private String orderStatus = "UNKNOWN";
    private String decision = "PENDING";
}
