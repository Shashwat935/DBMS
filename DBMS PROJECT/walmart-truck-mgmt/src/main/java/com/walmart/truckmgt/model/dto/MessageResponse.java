package com.walmart.truckmgt.model.dto;

import lombok.*;

/**
 * Simple message DTO for success/error responses.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponse {
    private String message;
    private boolean success;
}
