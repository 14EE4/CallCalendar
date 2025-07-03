package com.example.callcalendar;



import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private static final String TAG = "MainActivity";
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnShowSamsungGuide = findViewById(R.id.btn_show_samsung_guide); // 레이아웃에 정의된 버튼 ID
        if (btnShowSamsungGuide != null) {
            btnShowSamsungGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSamsungCallRecordingGuideDialog();
                }
            });
        } else {
            Log.e(TAG, "버튼 ID 'btn_show_samsung_guide'를 activity_main.xml에서 찾을 수 없습니다.");
        }

        btn = findViewById(R.id.btnShowRecordings);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordingListActivity.class);
            startActivity(intent);
        });


        checkPermissions();


    }

    private void checkPermissions() {
        boolean phoneStateGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        Log.d("PermissionsCheck", "Phone State Granted: " + phoneStateGranted);
        Log.d("PermissionsCheck", "Storage Granted: " + storageGranted); // 이 로그를 확인

        if (!phoneStateGranted || !storageGranted) { // 조건은 동일
            Log.d("PermissionsCheck", "Requesting permissions..."); // 요청 직전 로그
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            Toast.makeText(this, "권한이 모두 허용되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSamsungCallRecordingGuideDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); // 'this'는 MainActivity 컨텍스트
        builder.setTitle("삼성 전화 녹음 설정 안내");
        builder.setMessage("통화 녹음 기능을 사용하려면 삼성 전화 앱의 설정을 확인하거나 활성화해야 합니다.\n\n" +
                "경로 예시:\n" +
                "1. 전화 앱 실행\n" +
                "2. 우측 상단 '더보기(점3개)' 선택\n" +
                "3. '설정' 선택\n" +
                "4. '통화 녹음' 메뉴에서 설정 확인\n\n" +
                "'확인'을 누르면 전화 앱으로 이동합니다.");

        builder.setPositiveButton("확인 (전화 앱으로 이동)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSamsungDialer();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openSamsungDialer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "전화 앱을 열 수 없습니다. 수동으로 전화 앱을 실행해주세요.", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // @NonNull 어노테이션을 추가하는 것이 좋습니다.
        // super.onRequestPermissionsResult(requestCode, permissions, grantResults); // 호출 위치 변경 권장

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            // grantResults가 비어있거나 요청한 권한 수와 다를 경우를 대비해야 합니다.
            if (grantResults.length > 0 && grantResults.length == permissions.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            } else {
                allGranted = false; // 권한 요청이 취소되었거나 결과가 비정상적인 경우
            }


            if (!allGranted) {
                Toast.makeText(this, "권한이 필요합니다. 설정 화면으로 이동합니다.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                // setData 대신 Uri.fromParts 사용 권장
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } else {
                Toast.makeText(this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // 여기에 호출하는 것이 더 일반적입니다.
    }
}
