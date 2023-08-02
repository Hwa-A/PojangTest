package com.yuhan.yangpojang;

// 나의 중요한 정보를 넣어두는 파일
public class AppConfig {


    private static final String CLIENT_ID ="b6b3653fb7c3dc528e5e4619e2d58601" ;
    private static final String REDIRECT_URI = "https://example.com/pojang";
    private String INGA_CODE;

    public void setINGA_CODE(String INGA_CODE) {
        this.INGA_CODE = INGA_CODE;
    }


    public static String getClientId() {
        return CLIENT_ID;
    }

    public static String getRedirectUri() {
        return REDIRECT_URI;
    }

}
