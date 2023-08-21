package com.junsaga.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.junsaga.myapplication.adapter.PostAdapter;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Utmk;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

public class MapFragmentActivity extends FragmentActivity
    implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSIONI_REQUEST_CODE=1000;

    private static final String[] PERMISSIONS ={
           android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    RecyclerView recyclerView;
    PostAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fragment);

        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("1npnqdm1vz"));
        if( ActivityCompat.checkSelfPermission(MapFragmentActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions(MapFragmentActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION} ,
                    LOCATION_PERMISSIONI_REQUEST_CODE);
            return;
        }

        LatLng latLng = new LatLng(37.5666103, 126.9783882);
        Utmk utmk = Utmk.valueOf(latLng);
        LatLng coord = new LatLng(37.5670135, 126.9783740);
        Toast.makeText(MapFragmentActivity.this,
                "위도: " + coord.latitude + ", 경도: " + coord.longitude,
                Toast.LENGTH_SHORT).show();
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("1npnqdm1vz"));
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if(mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map,mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSIONI_REQUEST_CODE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                // 현재 위치를 맵에 표시
                Marker marker = new Marker();
                marker.setPosition(currentLatLng);
                marker.setMap(naverMap);

                // 현재 위치로 카메라 이동
                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
                        .animate(CameraAnimation.Linear);
                naverMap.moveCamera(cameraUpdate);

                // 위치 업데이트 리스너 해제
                locationManager.removeUpdates(this);
            }
        };


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }else{
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        ActivityCompat.requestPermissions(this,PERMISSIONS,LOCATION_PERMISSIONI_REQUEST_CODE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MapFragmentActivity.this));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();
            }
        });
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        Marker marker = new Marker();
        marker.setPosition(new LatLng(37.5670135, 126.9783740));
        View customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
        TextView numberTextView = customMarkerView.findViewById(R.id.numberTextView);
        numberTextView.setText("1"); // Set the desired number
        marker.setMap(naverMap);
        marker.setIcon(OverlayImage.fromView(customMarkerView));
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3000, 0, locationListener);
        }
    }


    }
