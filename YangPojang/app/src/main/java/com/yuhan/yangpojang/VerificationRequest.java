package com.yuhan.yangpojang;

public class VerificationRequest {
    private String accessToken;

    public VerificationRequest(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
