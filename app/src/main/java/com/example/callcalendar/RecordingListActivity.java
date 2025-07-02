package com.example.callcalendar;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class RecordingListActivity extends AppCompatActivity implements RecordingAdapter.OnItemClickListener {

    private static final String TAG = "RecordingListActivity";
    private RecyclerView recyclerViewRecordings;
    private RecordingAdapter recordingAdapter;
    private List<RecordingItem> recordingItemsList;
    private MediaPlayer mediaPlayer;

    private static final int REQUEST_READ_STORAGE_PERMISSION = 101;
    private String currentPlayingFileName = ""; // 현재 재생 중인 파일 이름 임시 저장

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        recyclerViewRecordings = findViewById(R.id.recyclerViewRecordings);
        if (recyclerViewRecordings == null) {
            Log.e(TAG, "Critical: RecyclerView with ID 'recyclerViewRecordings' not found in layout 'activity_recording_list.xml'. Please check your layout file.");
            Toast.makeText(this, "레이아웃 구성 오류입니다. 리사이클러뷰를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
            finish(); // RecyclerView가 없으면 액티비티를 더 이상 진행할 수 없음
            return;
        }
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this));

        recordingItemsList = new ArrayList<>();
        recordingAdapter = new RecordingAdapter(recordingItemsList, this);
        recyclerViewRecordings.setAdapter(recordingAdapter);

        checkAndLoadRecordings();
    }

    private void initializeMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            Toast.makeText(RecordingListActivity.this, "재생 완료: " + getCurrentPlayingFileName(), Toast.LENGTH_SHORT).show();
            currentPlayingFileName = ""; // 재생 완료 후 초기화
            // 필요하다면 재생 상태를 나타내는 UI 업데이트 (예: 재생/일시정지 버튼 아이콘 변경)
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(RecordingListActivity.this, "재생 중 오류 발생: " + getCurrentPlayingFileName(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "MediaPlayer Error: what=" + what + ", extra=" + extra + " for file: " + getCurrentPlayingFileName());
            currentPlayingFileName = ""; // 오류 발생 시 초기화
            if (mediaPlayer != null) {
                mediaPlayer.reset(); // 오류 발생 시 리셋
            }
            return true; // true를 반환하면 onCompletionListener가 호출되지 않음
        });
        mediaPlayer.setOnPreparedListener(mp -> {
            Log.d(TAG, "MediaPlayer prepared. Starting playback for: " + getCurrentPlayingFileName());
            mp.start();
            Toast.makeText(RecordingListActivity.this, "재생 시작: " + getCurrentPlayingFileName(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getCurrentPlayingFileName() {
        return currentPlayingFileName;
    }

    private void checkAndLoadRecordings() {
        String permissionToRequest;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            permissionToRequest = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permissionToRequest) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Storage permission granted. Loading audio files.");
            loadAudioFiles();
        } else {
            Log.d(TAG, "Storage permission not granted. Requesting permission.");
            ActivityCompat.requestPermissions(this, new String[]{permissionToRequest}, REQUEST_READ_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Storage permission granted by user. Loading audio files.");
                loadAudioFiles();
            } else {
                Log.w(TAG, "Storage permission denied by user.");
                Toast.makeText(this, "저장소 읽기 권한이 거부되어 녹음 파일을 불러올 수 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // loadAudioFiles 메서드 전체
    private void loadAudioFiles() {
        Log.d(TAG, "loadAudioFiles started");
        recordingItemsList.clear(); // 기존 목록 비우기

        Uri collection;
        // Android 10 (Q) 이상에서는 getVolumeName을 사용하여 외부 저장소의 특정 볼륨을 지정할 수 있습니다.
        // 일반적으로 외부 주 저장소(primary external storage)를 사용합니다.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        Log.d(TAG, "Using collection: " + collection.toString());

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,                 // 각 오디오 파일의 고유 ID
                MediaStore.Audio.Media.DISPLAY_NAME,        // 사용자에게 표시될 파일 이름
                // MediaStore.Audio.Media.DATA,             // 파일의 실제 경로 (API 29 이상에서는 직접 접근 제한)
                MediaStore.Audio.Media.DATE_ADDED,          // 파일이 MediaStore에 추가된 날짜 (정렬에 사용)
                MediaStore.Audio.Media.MIME_TYPE,           // 파일의 MIME 타입 (필터링에 사용)
                MediaStore.Audio.Media.DURATION             // 오디오 파일의 길이 (ms)
        };

        // 필터링 조건: 특정 MIME 타입의 오디오 파일만 선택
        // 실제 통화 녹음 파일이 어떤 MIME 타입을 사용하는지 확인하고 이 부분을 조정해야 합니다.
        // 예: "audio/amr", "audio/3gpp", "audio/mp4" (m4a 파일), "audio/mpeg" (mp3 파일) 등
        // IS_RECORDING 컬럼 (API 29+) 또는 IS_VOICE_CALL_RECORDING (API 34+) 등을 사용할 수도 있으나,
        // 모든 기기나 녹음 앱에서 이 플래그를 올바르게 설정한다는 보장은 없습니다.
        // 따라서 MIME 타입이나 파일명 패턴, 특정 폴더 경로(DATA 컬럼, API 28 이하에서 주로 유효)를 조합하는 것이 일반적입니다.
        String selection = MediaStore.Audio.Media.MIME_TYPE + "=? OR " + // AMR
                MediaStore.Audio.Media.MIME_TYPE + "=? OR " + // 3GP
                MediaStore.Audio.Media.MIME_TYPE + "=? OR " + // M4A (AAC in MP4 container)
                MediaStore.Audio.Media.MIME_TYPE + "=? OR " + // MP3
                MediaStore.Audio.Media.MIME_TYPE + "=?";      // AAC
        String[] selectionArgs = new String[]{
                "audio/amr",    // .amr (Adaptive Multi-Rate) - 많은 통화 녹음 앱에서 사용
                "audio/3gpp",   // .3gp (3GPP container) - 모바일에서 자주 사용
                "audio/mp4",    // .m4a (MPEG-4 Audio, AAC 코덱) - 일부 녹음 앱 또는 변환된 파일
                "audio/mpeg",   // .mp3 (MPEG Audio Layer III)
                "audio/aac"     // .aac (Advanced Audio Coding)
        };

        // 만약 특정 폴더에 있는 녹음 파일만 가져오고 싶다면, DATA 컬럼을 사용한 조건을 추가할 수 있습니다.
        // (API 29 이상에서는 앱이 해당 폴더에 접근 권한이 있어야 하며, MediaStore.Files를 통해 접근해야 할 수도 있음)
        // 예시: CallRecordings 폴더 내의 파일만 (MIME 타입 조건과 AND로 결합)
        // String specificFolderSelection = MediaStore.Audio.Media.DATA + " LIKE '%/CallRecordings/%'";
        // selection = "(" + selection + ") AND " + specificFolderSelection;
        // (selectionArgs는 그대로 사용하거나, LIKE 패턴에 맞는 인자를 추가해야 함)

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"; // 최신 파일이 위로 오도록 정렬

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
            );

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                // int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA); // 직접 경로 사용은 지양
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
                // int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                Log.d(TAG, "Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    // long duration = cursor.getLong(durationColumn);

                    // MediaStore에서 개별 아이템에 접근하기 위한 Content URI 생성
                    Uri contentUri = ContentUris.withAppendedId(collection, id);

                    // Log.d(TAG, "Found audio: Name=" + name + ", Uri=" + contentUri.toString() + ", MIME=" + mimeType + ", Duration=" + duration);

                    // RecordingItem 객체 생성 (RecordingItem 클래스가 Uri를 받도록 수정되었다고 가정)
                    // 두 번째 인자로 contentUri.toString()을 임시로 넣었으나, 실제 파일 경로가 필요하다면 다른 방법 고려
                    recordingItemsList.add(new RecordingItem(name, contentUri.toString(), contentUri));
                }

                if (recordingItemsList.isEmpty()) {
                    Toast.makeText(this, "조건에 맞는 녹음 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No audio files found matching criteria.");
                } else {
                    Log.d(TAG, "Loaded " + recordingItemsList.size() + " audio files.");
                }
                recordingAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경 알림

            } else {
                Log.w(TAG, "MediaStore query returned null cursor.");
                Toast.makeText(this, "오디오 파일을 가져오는 데 실패했습니다 (cursor is null).", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading audio files from MediaStore", e);
            Toast.makeText(this, "오디오 파일 로드 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close(); // 커서 사용 후 반드시 닫기
            }
            Log.d(TAG, "loadAudioFiles finished");
        }
    }

    @Override
    public void onItemClick(RecordingItem item) { // RecordingAdapter.OnItemClickListener의 매개변수 타입에 맞게 수정
        if (item == null || item.getUri() == null) {
            Toast.makeText(this, "재생할 수 없는 파일입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Item clicked: " + item.getFileName() + ", Uri: " + item.getUri().toString());
        currentPlayingFileName = item.getFileName(); // 현재 재생 파일명 설정

        initializeMediaPlayer(); // MediaPlayer 재설정 및 초기화

        try {
            mediaPlayer.setDataSource(getApplicationContext(), item.getUri());
            mediaPlayer.prepareAsync(); // 비동기 준비, 준비 완료 후 onPrepared에서 재생 시작
            // mediaPlayer.prepare(); // 동기 준비 (UI 스레드에서 오래 걸릴 수 있음)
            // mediaPlayer.start(); // prepareAsync 사용 시에는 onPrepared 콜백에서 start() 호출
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source or preparing MediaPlayer for " + item.getUri().toString(), e);
            Toast.makeText(this, "파일을 재생할 수 없습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
            currentPlayingFileName = ""; // 오류 시 초기화
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException for MediaPlayer: " + e.getMessage());
            Toast.makeText(this, "MediaPlayer 상태 오류입니다.", Toast.LENGTH_SHORT).show();
            currentPlayingFileName = "";
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                // 백그라운드 재생을 허용하지 않으려면 여기서 stop 또는 pause
                // mediaPlayer.pause(); // 또는 mediaPlayer.stop();
                Log.d(TAG, "Activity stopped, pausing media player.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // 액티비티 종료 시 MediaPlayer 리소스 해제
            mediaPlayer = null;
        }
        Log.d(TAG, "Activity destroyed, MediaPlayer released.");
    }
}