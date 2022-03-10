package com.bowhead.tap;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.File;

/**
 * JS和Android互调的桥梁
 */
public class JsInterface {
    public static String JS_INTERFACE = "jsInterface";
    public static String imagePath = "";
    private Activity mActivity;
    private WebView mWebView;
    private Context mContext;
    public static final int TAKE_PHOTO = 1000;

    public JsInterface(Activity activity, Context context, WebView webView) {
        mActivity = activity;
        mWebView = webView;
        mContext = context;
    }

    public String filePath = Environment.getExternalStorageDirectory() + File.separator + "myH5camera" + File.separator + "aaa.jpg";

    @JavascriptInterface
    public void takePhoto() {
        //调用系统摄像头
        Log.d("调用前置摄像头：", "");
        startCamera(TAKE_PHOTO, filePath);
    }

    private void startCamera(int type, String imgName) {
        File fileDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        String filePath = fileDir.getAbsolutePath() + "/" + fileName;

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Pictures");
            imagePath = fileDir.getAbsolutePath() + "/DCIM/Pictures/" + fileName;
            //Log.i(this.getClass().getName(), "<><JsInterface.startCamera>absolutePath: " + fileDir.getAbsolutePath());
            //Log.i(this.getClass().getName(), "<><JsInterface.startCamera>imagePath: " + imagePath);
        } else {
            contentValues.put(MediaStore.Images.Media.DATA, filePath);
        }
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG");

        Uri uri = mContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mActivity.startActivityForResult(intent, type);
    }
}