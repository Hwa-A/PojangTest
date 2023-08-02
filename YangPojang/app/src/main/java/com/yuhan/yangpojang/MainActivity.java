package com.yuhan.yangpojang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.util.FusedLocationSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    //OnMapReadyCallback : 지도가 준비되었을 때 호출되는 콜백을 처리, onMapReady()메서드 구현해야 함

    private FusedLocationSource locationSource; //안드로이드의 위치 서비스를 이용하여 현재 위치를 가져올 수 있는 클래스
    private NaverMap mNaverMap; //네이버 지도를 표시하고 조작하기 위한 클래스
    private static final int PERMISSION_REQUEST_CODE = 100; //위치 권한 요청을 처리하기 위한 요청 코드
    private static final String[] PERMISSIONS = { //위치 권한을 요청할 때 필요한 권한 선언
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //지도 객체 생성하기
        FragmentManager fragmentManager = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map); //fragmentmanager를 통해 추가된 map이라는 프래그먼트를 찾아서 MapFragment변수에 할당
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMapAsync 호출해 비동기로 onMapReady 콜백 메서드 호출
        mapFragment.getMapAsync(this);

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        locationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        mNaverMap = naverMap;
        mNaverMap.setLocationSource(locationSource); // NaverMap객체에 위치 소스를 지정, 현재 위치 사용

        // 권한 확인, onRequestPermissionResult 콜백 메서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE); // 앱에서 위치 권한을 얻기 위해 권한 요청 대화상자를 표시하는 역할을 함

        //지도 컨트롤 객체, UiSettings : UI관련 설정
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);// 현위치 버튼
        uiSettings.setScaleBarEnabled(false); // 축적바
        uiSettings.setCompassEnabled(false); //나침반
        uiSettings.setZoomControlEnabled(false); // 줌 버튼
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // request code와 권한 획득 여부 확인
        if(requestCode == PERMISSION_REQUEST_CODE){ // onMapReady()메서드에 요청한 권한 요청과 일치하는지 확인
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow); //지도에서 현재 위치를 추적하여 따라가는 모드를 의미
            }
        }// onRequestPermissionsResult()메서드는 권한 요청 결과를 처리하고, 권한이 획득되었을 경우에만 NaverMap객체의 위치 추적 모드를 설정하는 역할을 함
    }
}