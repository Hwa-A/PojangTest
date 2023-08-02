package com.yuhan.yangpojang;

import com.google.gson.annotations.SerializedName;

public class VerificationResponse {
    @SerializedName("firebase_token")
    private String firebaseToken;

    // isSuccess() 메서드 추가
    public boolean isSuccess() {
        // 적절한 조건을 확인하여 true 또는 false를 반환
        // 예: 특정 필드 값을 확인하여 성공/실패 여부를 판단
        // 여기서는 firebaseToken이 null이 아닌 경우에만 성공으로 간주하도록 가정
        return firebaseToken != null;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }
}
