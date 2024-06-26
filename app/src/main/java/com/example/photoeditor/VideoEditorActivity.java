package com.example.photoeditor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.MediaController;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoEditorActivity extends AppCompatActivity {

    private static final int PICK_VIDEO = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    private VideoView videoView;
    private Uri videoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);

        videoView = findViewById(R.id.videoView);
        Button btnChooseVideo = findViewById(R.id.btnChooseVideo);
        Button btnStartVideo = findViewById(R.id.btnStartVideo);
        Button btnPauseVideo = findViewById(R.id.btnPauseVideo);
        Button btnSaveVideo = findViewById(R.id.btnSaveVideo);

        // Проверка и запрос разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        btnChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });

        btnStartVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoUri != null) {
                    videoView.start();
                } else {
                    Toast.makeText(VideoEditorActivity.this, "Алдымен видеоны таңдаңыз", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPauseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                }
            }
        });

        btnSaveVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVideoToGallery();
            }
        });
    }

    private void chooseVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
        }
    }

    private void saveVideoToGallery() {
        if (videoUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(videoUri);
                File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/YourAppName");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File videoFile = new File(storageDir, "edited_video_" + System.currentTimeMillis() + ".mp4");
                FileOutputStream outputStream = new FileOutputStream(videoFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                Toast.makeText(this, "Видео сақталды", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Видеоны сақтау сәтсіз аяқталды", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Сақтайтын видео жоқ", Toast.LENGTH_SHORT).show();
        }
    }
}
