package com.yuhan.yangpojang;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class kakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this,"9ec1b641eb56d8439d41a10ee3241ce37");
    }
    }

