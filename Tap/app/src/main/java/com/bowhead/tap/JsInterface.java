package com.bowhead.tap;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

        /*//遍历目录
        String fileList[] = mContext.getAssets().list("");//获取assets目录下的所有文件及目录名
        Log.i(TAG, "files.length: " + fileList.length);
        if (fileList.length > 0) {//如果是目录
            for (String oneFile : fileList) {
                Log.i(TAG, "[" + oneFile + "]");
            }
        }*/

        InputStream inputStream = mContext.getAssets().open(fileName);
        byte[] bytes = readBinaryFileContent(inputStream);
        Log.i(TAG, "OK-------------------------------------OK");

        OkHttpClient client = new OkHttpClient();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(fileName, fileName,
                        RequestBody.create(bytes, MediaType.parse("multipart/form-data")))
                .build();

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

    @JavascriptInterface
    public Bitmap download(String url) {
        //获取okHttp对象get请求
        try {
            Log.i(TAG, "start download + + + + + + + + + + + + +");
            OkHttpClient client = new OkHttpClient();
            //获取请求对象
            Request request = new Request.Builder().url(url).build();
            //获取响应体
            ResponseBody body = client.newCall(request).execute().body();
            //获取流
            InputStream in = body.byteStream();
            Log.i(TAG, "input stream: " + in.toString());
            //转化为bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            Log.i(TAG, "download one image: " + bitmap.toString());

            onSaveBitmap(bitmap, mContext);

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onSaveBitmap(final Bitmap mBitmap, final Context context) {
        // 第一步：首先保存图片
        //将Bitmap保存图片到指定的路径/sdcard/Boohee/下，文件名以当前系统时间命名,但是这种方法保存的图片没有加入到系统图库中
        File appDir = new File(Environment.getExternalStorageDirectory(), "/DCIM/Pictures/");

        if (!appDir.exists()) {
            appDir.mkdir();
        }

        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        String fullName = appDir + File.separator + fileName;
        File file = new File(fullName);
        Log.i(TAG, "save image + + + + + + + " + fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 第二步：其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:receiveImageUrl('" + fullName + "')");
                }
            });
            Log.i(TAG, "insert image into repository " + fullName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*// 第三步：最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));*/
    }
}

/*
application/from-data
application/octet-stream
multipart/form-data

 */