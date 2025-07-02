package com.example.callcalendar;

import android.net.Uri;

// RecordingItem.java (예시)
public class RecordingItem {
    private String fileName;
    private Uri uri; // 파일 경로 대신 Uri
    // private String filePath; // 이전에 사용했다면 Uri로 대체

    public RecordingItem(String fileName, String legacyPath, Uri uri) { // 생성자 수정
        this.fileName = fileName;
        this.uri = uri;
        // this.filePath = legacyPath; // 필요하다면 유지, 하지만 Uri 사용 권장
    }

    public String getFileName() {
        return fileName;
    }

    public Uri getUri() { // Uri getter
        return uri;
    }

    // public String getFilePath() { return filePath; } // 필요하다면 유지
}