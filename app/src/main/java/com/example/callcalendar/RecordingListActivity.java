package com.example.callcalendar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class RecordingListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecordingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<File> recordings = FileUtils.getCallRecordings();
        adapter = new RecordingAdapter(recordings);
        recyclerView.setAdapter(adapter);
    }
}
