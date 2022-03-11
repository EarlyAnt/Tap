package com.bowhead.tap;

import android.app.Activity;
import android.app.DownloadManager;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * JS和Android互调的桥梁
 */
public class JsInterface {
    public static String JS_INTERFACE = "jsInterface";
    public static String imagePath = "";
    public static final int TAKE_PHOTO = 1000;
    private String TAG = this.getClass().getName();
    private Activity mActivity;
    private WebView mWebView;
    private Context mContext;
    public String filePath = Environment.getExternalStorageDirectory() + File.separator + "myH5camera" + File.separator + "aaa.jpg";

    public JsInterface(Activity activity, Context context, WebView webView) {
        mActivity = activity;
        mWebView = webView;
        mContext = context;
    }

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
            //Log.i(TAG, "<><JsInterface.startCamera>absolutePath: " + fileDir.getAbsolutePath());
            //Log.i(TAG, "<><JsInterface.startCamera>imagePath: " + imagePath);
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

    @JavascriptInterface
    public ResponseBody upload(String url, String fileName) throws Exception {
        Log.i(TAG, String.format("url: %1$s, fileName: %2$s", url, fileName));

        String fileList[] = mContext.getAssets().list("");//获取assets目录下的所有文件及目录名
        Log.i(TAG, "files.length: " + fileList.length);
        File file = null;
        byte[] bytes = null;
        if (fileList.length > 0) {//如果是目录
            for (String oneFile : fileList) {
                //Log.i(TAG, "[" + oneFile + "]");
                if (oneFile.equals(fileName)) {
                    file = new File(fileName);
                    InputStream inputStream = mContext.getAssets().open(fileName);
                    bytes = readBinaryFileContent(inputStream);
                    Log.i(TAG, "convert input stream to byte array + + + + + >>" + bytes.length);
                }
            }
        }
        Log.i(TAG, "-------------------------------------");

        OkHttpClient client = new OkHttpClient();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileName, fileName,
                        RequestBody.create(bytes, MediaType.parse("multipart/form-data")))
                .build();

/*        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(file, MediaType.parse("multipart/form-data")))
                .build();*/

        Log.i(TAG, String.format("--------------------------  url: %1$s", url));

        Request request = new Request.Builder()
                .header("Content-Type", "multipart/form-data")
                .url(url)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        return response.body();
    }

    private byte[] readBinaryFileContent(InputStream inputStream) {
        try {
            if (inputStream == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) != -1) {
                baos.write(buff, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

/*
application/from-data
application/octet-stream
multipart/form-data

 */