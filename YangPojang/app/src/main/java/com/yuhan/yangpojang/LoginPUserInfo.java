package com.yuhan.yangpojang;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class LoginPUserInfo extends AppCompatActivity {

    private Button selectDateBtn;
    private Button checkUniquenessBtn;
    private Button confirmDateBtn;
    private Button signCompleteBtn;
    private TextView selectedDate;
    private DatePicker birthDatePicker;
    private RadioGroup sexGroup;

    // 닉네임 저장 변수
    private String userNickname;

    //선택된 생년월일 저장할 변수들
    private int selectedYear; // 선택된 년도를 저장할 변수
    private int selectedMonth; // 선택된 월을 저장할 변수
    private int selectedDay; // 선택된 일을 저장할 변수

    // 선택된 성별을 저장할 변수
    private String userGender;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_puser_info);
        // Firebase 인증 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();

        Log.d("#$#$#$#$#$#$##$#$", String.valueOf(mFirebaseAuth));


        // Firebase 초기화
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseRef = firebaseDatabase.getReference("Pojang").child("UserAccount"); // "Pojang>UserAccount"는 데이터베이스 내의 사용자 정보가 저장된 경로입니다.

        selectDateBtn = findViewById(R.id.selectDateBtn);
        confirmDateBtn = findViewById(R.id.confirmDateBtn);
        selectedDate = findViewById(R.id.showDate);
        birthDatePicker = findViewById(R.id.birthDatePicker);
        signCompleteBtn = findViewById(R.id.signCompleteBtn);
        sexGroup = findViewById(R.id.sexGroup);
        checkUniquenessBtn = findViewById(R.id.checkUniquenessBtn);

        checkUniquenessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userNickname = getUserNickname();
                if (userNickname.isEmpty()) {
                    // User didn't enter a nickname
                    Toast.makeText(LoginPUserInfo.this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
                } else if (userNickname.matches(".*[^a-zA-Z0-9_가-힣].*")) {
                    // Check if the nickname contains any special characters except underscore
                    Toast.makeText(LoginPUserInfo.this, "_를 제외한 특수문자는 닉네임에서 사용할수 없습니다", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform the uniqueness check here (e.g., check against a database)
                    // If the nickname is unique, you can display a success message
                    Toast.makeText(LoginPUserInfo.this, "사용가능한 닉네임 입니다", Toast.LENGTH_SHORT).show();
                }
                Log.d("#$#$#$#$#$#$##$#$", userNickname);

            }

        });

        selectDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 날짜 선택 버튼을 클릭하면 DatePicker를 표시
                birthDatePicker.setVisibility(View.VISIBLE);
                confirmDateBtn.setVisibility(View.VISIBLE);
            }
        });

        sexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button is selected
                if (checkedId == R.id.sexM) {
                    userGender = "male";
                } else if (checkedId == R.id.sexF) {
                    userGender = "female";
                }
            }
        });

        confirmDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 확인 버튼을 클릭하면 DatePicker를 숨김
                birthDatePicker.setVisibility(View.GONE);
                confirmDateBtn.setVisibility(View.GONE);
            }
        });

        // DatePicker에서 날짜를 선택했을 때의 리스너
        birthDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //선택한 날짜를 변수에 저장
                selectedYear = year;
                selectedMonth = monthOfYear + 1; // 월은 0부터 시작하므로 +1 해줌
                selectedDay = dayOfMonth;

                // 선택한 날짜를 TextView에 표시
                String selectedDateStr = String.format(Locale.getDefault(), "%d년 %d월 %d일", year, monthOfYear + 1, dayOfMonth);
                selectedDate.setText(selectedDateStr);
            }
        });

        signCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfoToFirebase();
            }
        });
    }
    private void saveUserInfoToFirebase() {
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // 현재 로그인한 사용자의 고유 ID를 가져옴
            String userId = currentUser.getUid();

            // 사용자 정보를 Firebase에 저장
            DatabaseReference userRef = databaseRef.child("Pojang").child("UserAccount").child(userId);
            userRef.child("appnickname").setValue(getUserNickname());
//            userRef.child("profileImageUrl").setValue(getUserProfileImageUrl());
            userRef.child("gender").setValue(userGender);
            userRef.child("birthYear").setValue(selectedYear);
            userRef.child("birthMonth").setValue(selectedMonth);
            userRef.child("birthDay").setValue(selectedDay);

            Toast.makeText(LoginPUserInfo.this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginPUserInfo.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUserNickname() {
        EditText userNicknameEditText = findViewById(R.id.userNickname);
        return userNicknameEditText.getText().toString().trim();
    }
    // 선택된 년도를 반환하는 메서드
    private int getSelectedYear() {
        return selectedYear;
    }

    // 선택된 월을 반환하는 메서드
    private int getSelectedMonth() {
        return selectedMonth;
    }

    // 선택된 일을 반환하는 메서드
    private int getSelectedDay() {
        return selectedDay;
    }




}
