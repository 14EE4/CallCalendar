package com.example.callcalendar;

import android.os.Environment;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<File> getCallRecordings() {
        File recordingsDir = new File(Environment.getExternalStorageDirectory(), "Recordings/Call");
        List<File> recordings = new ArrayList<>();

        if (recordingsDir.exists()) {
            File[] files = recordingsDir.listFiles((dir, name) ->
                    name.endsWith(".mp3") || name.endsWith(".m4a") || name.endsWith(".amr"));

            if (files != null) {
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                recordings.addAll(Arrays.asList(files));
            }
        }

        return recordings;
    }
}
