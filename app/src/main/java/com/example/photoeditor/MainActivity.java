package com.example.photoeditor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Color;
import android.text.InputType;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private ImageView imageView;
    private Bitmap originalBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Проверка и запрос разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

        imageView = findViewById(R.id.imageView);
        Button btnChoose = findViewById(R.id.btnChoose);
        Button btnFilter1 = findViewById(R.id.btnFilter1);
        Button btnFilter2 = findViewById(R.id.btnFilter2);
        Button btnFilter3 = findViewById(R.id.btnFilter3);
        Button btnFilter4 = findViewById(R.id.btnFilter4);
        Button btnAddText = findViewById(R.id.btnAddText);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnGoToVideoEditor = findViewById(R.id.btnGoToVideoEditor);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnFilter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter1();
            }
        });

        btnFilter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter2();
            }
        });

        btnFilter3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter3();
            }
        });

        btnFilter4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyFilter4();
            }
        });

        btnAddText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTextDialog();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });

        btnGoToVideoEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVideoEditor();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageView.setImageBitmap(originalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyFilter1() {
        Glide.with(this)
                .load(originalBitmap)
                .transform(new CenterCrop(), new GrayscaleTransformation())
                .into(imageView);
    }

    private void applyFilter2() {
        Glide.with(this)
                .load(originalBitmap)
                .transform(new CenterCrop(), new RoundedCornersTransformation(30, 0))
                .into(imageView);
    }

    private void applyFilter3() {
        Glide.with(this)
                .load(originalBitmap)
                .transform(new CenterCrop(), new BlurTransformation(25))
                .into(imageView);
    }

    private void applyFilter4() {
        // Применение фильтра контрастности
        Glide.with(this)
                .load(originalBitmap)
                .transform(new CenterCrop(), new ContrastFilterTransformation(2.0f))
                .into(imageView);
    }

    private void showAddTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Мәтін қосу");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                addTextToImage(text);
            }
        });
        builder.setNegativeButton("Болдырмау", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addTextToImage(String text) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(100);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setAntiAlias(true);
        paint.setShadowLayer(10f, 1f, 1f, Color.DKGRAY);

        // Нанесение текста в центре изображения
        int x = (canvas.getWidth() - (int) paint.measureText(text)) / 2;
        int y = (canvas.getHeight() / 2);

        canvas.drawText(text, x, y, paint);
        imageView.setImageBitmap(mutableBitmap);
    }

    private void saveImage() {
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Сохранение в галерею
        String savedImagePath = null;
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/YourAppName");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(byteArray);
                fos.close();
                Toast.makeText(this, "Сурет сақталды", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void goToVideoEditor() {
        Intent intent = new Intent(MainActivity.this, VideoEditorActivity.class);
        startActivity(intent);
    }
}
