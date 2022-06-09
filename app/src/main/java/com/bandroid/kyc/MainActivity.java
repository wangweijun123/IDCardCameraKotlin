package com.bandroid.kyc;

import static com.bandroid.kyc.camera.CameraActivity.FILENAME;
import static com.bandroid.kyc.camera.CameraActivity.PHOTO_EXTENSION;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bandroid.kyc.camera.CameraActivity;
import com.bandroid.kyc.camera.CameraPreview;
import com.bandroid.kyc.camera.utils.ImageUtils;
import com.bandroid.kyc.camera.utils.MyImageUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public final static String IMAGE_PATH = "image_path";//图片路径标记

    private ImageView mIvFront;
    private ImageView mIvBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIvFront = (ImageView) findViewById(R.id.iv_front);
        mIvBack = (ImageView) findViewById(R.id.iv_back);


    }

    /**
     * 身份证正面
     */
    public void frontNew(View view) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.putExtra("camera_title", "this is title");
        intent.putExtra("camera_subtitle", "this is sub title");
        intent.putExtra("img_uri", "/storage/emulated/0/Android/media/com.wildma.wildmaidcardcamera/wangweijun/2000-03-27-16-11-59-111.jpg");
        intent.putExtra("camera_file_type", "2");
        startActivityForResult(intent, 1);
    }

    public void backNew(View view) {
        File cropfile = ImageUtils.createFile(ImageUtils.getOutputDirectory(getApplicationContext()),
                FILENAME, PHOTO_EXTENSION);
        if (!cropfile.exists()) {
            try {
                cropfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(CameraPreview.TAG, "Main传过来的文件地址 exists?"+cropfile.exists() + " "+cropfile.getAbsolutePath());
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.putExtra("camera_title", "this is title");
        intent.putExtra("camera_subtitle", "this is sub title");
        intent.putExtra("img_uri", cropfile.getAbsolutePath());
        intent.putExtra("camera_file_type", "2");
        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //获取图片路径，显示图片
            if (data != null) {
                final String path = data.getStringExtra(IMAGE_PATH);
                if (!TextUtils.isEmpty(path)) {
                    mIvFront.setImageBitmap(BitmapFactory.decodeFile(path));
//                    try {
//                        // Bitmap bitmap = BitmapFactory.decodeFile(path);
//                        Bitmap bitmap = MyImageUtils.decode22(path);
//                        Log.d(CameraPreview.TAG, "显示的图片大小 width="
//                                +bitmap.getWidth()+", height="+bitmap.getHeight());
//
//                        mIvFront.setImageBitmap(bitmap);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                }
            }
        }
    }

    boolean front = false;
    public void jumpSystemCamera(View view) {
        front = !front;
        File targetFile = new File(getCacheDir(), System.currentTimeMillis()+".jpg");
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent intent = null;
        if (front) {
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            intent.putExtra("android.intent.extras.CAMERA_FACING",
                    android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        } else{
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(targetFile));
        startActivityForResult( intent, 100);
    }
}
