package com.yuhan.yangpojang;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class ShowUserInfo extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView nicknameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_info);
        profileImageView = findViewById(R.id.profileImageView);
        nicknameTextView = findViewById(R.id.nicknameTextView);

        // Intent를 통해 전달된 사용자 정보 데이터 받기
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String profileImageUrl = extras.getString("profileImageUrl");
            String nickname = extras.getString("nickname");

            // 프로필 사진 업데이트
            Glide.with(this).load(profileImageUrl).into(profileImageView);
            // 닉네임 업데이트
            nicknameTextView.setText(nickname);
        }
    }
}