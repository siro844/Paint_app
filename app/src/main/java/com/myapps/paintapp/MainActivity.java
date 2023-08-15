package com.myapps.paintapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kyanogen.signatureview.SignatureView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    int defaultColor;

    SignatureView signatureView;
    ImageButton eraser,color,save;
    SeekBar penSize;
    TextView txtPenSize;

    private static String filename;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/myPaint");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signatureView=findViewById(R.id.signature_view);
        penSize=findViewById(R.id.pensize);
        txtPenSize=findViewById(R.id.textpensize);
        eraser=findViewById(R.id.btneraser);
        color=findViewById(R.id.btncolor);
        save=findViewById(R.id.btnsave);

        //permission to access the storage
        askPermission();

        SimpleDateFormat format= new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date=format.format(new Date());
        filename=path+"/n"+date+".png";

        if(!path.exists()){
            path.mkdirs();
        }
        defaultColor= ContextCompat.getColor(MainActivity.this,R.color.black);
        penSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtPenSize.setText(i +"dp");
                signatureView.setPenSize(i);
                seekBar.setMax(80);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });
        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signatureView.clearCanvas();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        if(!signatureView.isBitmapEmpty()){
                            try {
                                saveImage();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this,"Couldnt' Save ",Toast.LENGTH_SHORT).show();
                            }
                        }
            }
        });

    }
    private void saveImage() throws IOException {
        File file = new File(filename);
        Bitmap bitmap=signatureView.getSignatureBitmap();
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0,bos);
        byte[] bitmapData= bos.toByteArray();

        FileOutputStream fos=new FileOutputStream(file);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        Toast.makeText(this,"Painting saved",Toast.LENGTH_SHORT).show();
    }

    private void openColorPicker() {
        AmbilWarnaDialog dialog=new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor=color;
                signatureView.setPenColor(color);
            }
        });
        dialog.show();
    }

    private void askPermission(){
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                            Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest(); // Continue asking for permission until the user agrees
                    }
                })
                .onSameThread() // Added this line to ensure the permission request runs on the same thread
                .check();
    }

}
