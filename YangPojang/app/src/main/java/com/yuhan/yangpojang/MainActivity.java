package com.yuhan.yangpojang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final MediaType MEDIA_TYPE_FORM = MediaType.parse("application/x-www-form-urlencoded");
    private static final String KAUTH_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAPI_USER_ME_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String AUTH_HEADER_PREFIX = "Bearer ";


    private String clientId;
    private String redirectUri;
    private WebView webView;
    private Button btnLogin;
    private String code;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase 초기화
        FirebaseApp.initializeApp(this);

        // 디버깅 - Firebase 인증 상태 확인
        // Firebase 인증 객체 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();

        // 현재 로그인한 사용자 가져오기
        // onAuthStateChanged 리스너 등록
        mFirebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // 현재 로그인한 사용자 가져오기
                // 현재 로그인한 사용자 가져오기
                mCurrentUser = firebaseAuth.getCurrentUser();
                Log.d("###########MainActivity", "onAuthStateChanged 호출됨");
                Log.d("MainActivity", "mCurrentUser 값: " + mCurrentUser);

                if (mCurrentUser != null) {
                    // 사용자가 로그인되어 있음
                    Log.d("MainActivity", "사용자가 인증되었습니다: " + mCurrentUser.getEmail());

                    // 사용자 정보를 Firebase에 저장하는 메서드 호출
                    saveUserInfoToFirebase("https://example.com/image.jpg", "홍길동", 1234567890, "여러분의_액세스_토큰");

                    // 여기에서 로그인 완료 후 다음 화면으로 이동하거나 다른 작업을 수행할 수 있습니다.
                } else {
                    // 사용자가 로그인되어 있지 않음
                    Log.d("MainActivity", "사용자가 인증되지 않았습니다.");
                }
            }
        });

        // AndroidManifest.xml에서 메타데이터 값을 가져오기
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            clientId = appInfo.metaData.getString("com.kakao.sdk.AppKey");
            redirectUri = appInfo.metaData.getString("com.kakao.sdk.RedirectUri");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 파이어 베이스 연동 - Firebase 인증 객체를 가져옵니다
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        btnLogin = findViewById(R.id.btnLogin);
        // 로그인 버튼을 누르면 requestIngaToken(인가토큰 요청 메서드) 실행
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Step 1: Client가 Kakao Server에게 access token을 요청
                requestIngaToken();
                // 오늘파베 연습
                // mFirebaseAuth.signInWithEmailAndPassword(stremail,strpwd).addOnCompleteListener(LoginActivity.this,new oncompleteListener)
            }
        });
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 리디렉션 URL을 추출
                if (url != null && url.startsWith(redirectUri)) {
                    extractCodeFromUrl(url);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    // buildAuthorizeUrl에서 사용자의 동의를 얻으면 인가코드 요청이 성공함(회원 닉네임, 프로필 사진 등을 요청)
    private void requestIngaToken() {
        // 인가코드 url 추출하는 buildAuthorizeUrl
        String authorizeUrl = buildAuthorizeUrl(clientId, redirectUri);

        // webView 보이게 하고 authorizeUrl로 접속해서 화면에 띄운다
        webView.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);
        webView.loadUrl(authorizeUrl);
    }

    // 사용자 인증 허용 (동의 버튼 누르는 화면)으로 접속되는 주소 만드는 메서드
    private static String buildAuthorizeUrl(String clientId, String redirectUri) {
        // 인가코드 받는 url 공통부분 baseUrl
        String baseUrl = "https://kauth.kakao.com/oauth/authorize";
        // url 에서 변할수 있는 매개변수는 append 등으로 붙여준다

        String responseType = "code";

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl)
                .append("?client_id=").append(clientId)
                .append("&response_type=").append(responseType)
                .append("&redirect_uri=").append(redirectUri);
        return urlBuilder.toString();
    }

    // code - authorizeUrl로 접속해서 동의 버튼 누르면 나오는 code=Djhllfdskfj형태에서 Djhllfdskfj가 인가코드임
    // 여기서 code 가 인가코드 담은 변수임
    private void extractCodeFromUrl(String url) {
        // URL에서 코드 추출
        Uri uri = Uri.parse(url);
        code = uri.getQueryParameter("code"); // code 에 인가코드 담기
        if (code != null) {
            // 추출한 코드를 사용하여 나머지 과정을 처리
            requestAccessToken();
            webView.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
        }

        // 실패시 toast 메시지
        else {
            Toast.makeText(this, "Failed to extract code from URL", Toast.LENGTH_SHORT).show();
            // 또는 Log에 에러 메시지를 출력할 수 있음
            Log.e("extractCodeFromUrl", "인가코드를 URL에서 추출하지 못했습니다");
        }
    }

    // 엑세스 토큰 추출 메서드 extractAccessToken
    private String extractAccessToken(String response) {
        Log.d("+++++++++++++++=", "진행시켜야렂ㅁ대러  로그인하지 않았습니다.");

        try {
            JSONObject jsonResponse = new JSONObject(response);
            String accessToken = jsonResponse.getString("access_token");

            // 엑세스 토큰을 Firebase에 저장합니다
            if (accessToken != null) {
                saveAccessTokenToFirebase(accessToken);
            } else {
                Log.d("extractAccessToken 메서드 오류", "엑세스 토큰이 null입니다");
            }

            return accessToken;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 리프레시 토큰 추출 메서드 extractRefreshToken
    private String extractRefreshToken(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String refreshToken = jsonResponse.getString("refresh_token");
            return refreshToken;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 엑세스 토큰 추출 요청 메서드 requestAccessToken
    private void requestAccessToken() {
        Thread tokenThread = new Thread(new Runnable() {
            @Override
            // OkHttpClient 메서드로 인가코드를 이용해 엑세스 토큰 요청
            public void run() {
                OkHttpClient client = new OkHttpClient();
                // 요청할 주소 생성?
                RequestBody requestBody = new FormBody.Builder()
                        .add("grant_type", "authorization_code")
                        .add("client_id", clientId)
                        .add("redirect_uri", redirectUri)
                        .add("code", code)
                        .build();
                // post 형식으로 access token 요청
                Request request = new Request.Builder()
                        .url(KAUTH_TOKEN_URL)
                        .post(requestBody)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        // 카카오톡 - extractAccessToken 처리 로직
                        String accessToken = extractAccessToken(responseData);
                        // 카카오톡 - extractRefreshToken
                        String refreshToken = extractRefreshToken(responseData);

                        if (accessToken != null && refreshToken != null) {
                            // Access Token을 사용하여 사용자 정보 요청
                            // 추출 성공시 accessToken를 이용해 requestUser 메서드에 유저 정보 요청
                            requestUserInfo(accessToken);
                            // 리프레시 토큰은 여기서 Firebase 등에 저장해두어야 나중에 사용 가능
                            saveRefreshTokenToFirebase(refreshToken);
                            Log.d("+++++++++++++++++++","엑세스, 리프레시 토큰 추출 성공");

                        } else {
                            // 엑세스 토큰이 null인 경우 처리 로직
                            Log.d("requestAccessToken에 엑세스 토큰 null","엑세스 토큰이 null입니다");

                        }
                    } else {
                        // 응답이 성공하지 않은 경우 로직
                        Log.d("requestAccessToken 응답 오류","requestAccessToken - 응답 실패");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 예외 처리 로직
                }
            }
        });
        tokenThread.start();

    }

    // Firebase에 엑세스 토큰 저장 메서드
    private void saveAccessTokenToFirebase(String accessToken) {
        Log.d("dlrudkadf8ewif", "진행시켜야렂ㅁ대러  로그인하지 않았습니다.");

        if (mCurrentUser != null) {
            String userId = mCurrentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("Pojang")
                    .child("UserAccount")
                    .child(String.valueOf(userId));

            // 엑세스 토큰을 Firebase에 저장합니다
            userRef.child("accessToken").setValue(accessToken)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FirebaseAuth", "엑세스 토큰이 Firebase에 저장되었습니다.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FirebaseAuth", "엑세스 토큰 저장 실패: " + e.getMessage());
                        }
                    });
        } else {
            Log.d("FirebaseAuth", "사용자가 로그인하지 않았습니다.");
        }
    }

    // Firebase에 리프레시 토큰 저장 메서드 추가
    private void saveRefreshTokenToFirebase(String refreshToken) {
        // Firebase 인증 객체를 가져옵니다
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        // 현재 로그인된 사용자를 가져옵니다
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // 사용자가 로그인한 경우에만 리프레시 토큰을 Firebase에 저장합니다
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("Pojang")
                    .child("UserAccount")
                    .child(String.valueOf(userId));

            // 리프레시 토큰을 Firebase에 저장합니다
            userRef.child("refreshToken").setValue(refreshToken)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FirebaseAuth", "리프레시 토큰이 Firebase에 저장되었습니다.");
                            // 여기에서 로그인 완료 후 다음 화면으로 이동하거나 다른 작업을 수행할 수 있습니다.
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("FirebaseAuth", "리프레시 토큰 저장 실패: " + e.getMessage());
                        }
                    });
        } else {
            // 사용자가 로그인하지 않은 경우에 대한 처리 (예: 로그인을 요청하도록 유도)
            // 이 부분에서 사용자에게 로그인을 요청하는 UI를 보여주거나, 자동으로 로그인 화면으로 이동시킬 수 있습니다.
            // 예를 들어:
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);

            Log.d("FirebaseAuth", "사용자가 로그인하지 않았습니다.");
        }
    }

    // 유저 정보 요청 메서드 requestUserInfo - 매개변수로 accessToken을 받아옴
    private void requestUserInfo(String accessToken) {  
        Thread userInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(KAPI_USER_ME_URL)
                        .addHeader("Authorization", AUTH_HEADER_PREFIX + accessToken)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        // 사용자 정보 처리 로직
                        // responseData에 id , ImageUrl 닉네임 등이 들어있음
                        handleUserInfoResponse(responseData, accessToken);
                        // 여기서 responseData에서 나온값은 회원번호- long 형태였음
                        // 참고 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api
                    } else {
                        Log.d("requestUserInfo 메서드 오류", " requestUserInfo 응답 실패");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // 예외 처리 로직
                }
            }
        });

        userInfoThread.start();
    }

    private void handleUserInfoResponse(String userInfoResponse, String accessToken) {
        try {
            JSONObject jsonObject = new JSONObject(userInfoResponse);
            Log.d("userInfoResponse값은요은요은요", userInfoResponse);

            JSONObject propertiesObject = jsonObject.getJSONObject("properties");
            String profileImageUrl = jsonObject.getJSONObject("properties").getString("profile_image");
            String nickname = jsonObject.getJSONObject("properties").getString("nickname");
            // id 값 추출
            long userId = jsonObject.getLong("id");

            Intent intent = new Intent(MainActivity.this, ShowUserInfo.class);
            intent.putExtra("profileImageUrl", profileImageUrl);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
            Log.d("handleUserInfoResponse값이 정상적으로 넘어감", "나의 정보값이 정상적으로 넘어감");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("handleUserInfoResponse", "JSONException 발생: " + e.getMessage());
        }
    }

    // new! firebase 에 로그인 정보 저장 메서드
    private void saveUserInfoToFirebase(String profileImageUrl, String nickname, long userId, String accessToken) {
        // 현재 로그인한 사용자 가져오기
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Firebase에 사용자 정보 저장하는 로직
            DatabaseReference userRef = mDatabaseRef.child("Pojang").child("UserAccount").child(String.valueOf(userId));
            userRef.child("nickname").setValue(nickname);
            userRef.child("profileImageUrl").setValue(profileImageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("MainActivity", "사용자 정보가 Firebase에 저장되었습니다.");
                            // 여기에서 로그인 완료 후 다음 화면으로 이동하거나 다른 작업을 수행할 수 있습니다.
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("MainActivity", "사용자 정보 저장 실패: " + e.getMessage());
                        }
                    });
        } else {
            Log.d("MainActivity", "mCurrentUser가 null이어서 사용자 정보를 저장하지 않았습니다.");
        }
    }

    private void showToast(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}