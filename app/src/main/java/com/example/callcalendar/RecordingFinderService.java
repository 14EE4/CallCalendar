package com.example.callcalendar;


import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class RecordingFinderService extends IntentService {

    public RecordingFinderService() {
        super("RecordingFinderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "Recordings/Call"); // 제조사에 따라 경로 변경 가능

        if (recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles();

            if (files != null && files.length > 0) {
                File latestFile = null;
                long latestModified = 0;

                for (File file : files) {
                    if (file.lastModified() > latestModified) {
                        latestModified = file.lastModified();
                        latestFile = file;
                    }
                }

                if (latestFile != null) {
                    Log.d("RecordingFinder", "최근 녹음 파일: " + latestFile.getAbsolutePath());
                    // 파일 복사, 업로드 등 추가 처리 가능
                }
            }
        } else {
            Log.d("RecordingFinder", "녹음 폴더가 존재하지 않음");
        }
    }
}

