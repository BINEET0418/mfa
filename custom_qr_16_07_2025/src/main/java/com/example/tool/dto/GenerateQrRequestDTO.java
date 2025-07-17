package com.example.tool.dto;

public class GenerateQrRequestDTO {
    private String userId;

    public GenerateQrRequestDTO() {}

    public GenerateQrRequestDTO(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
