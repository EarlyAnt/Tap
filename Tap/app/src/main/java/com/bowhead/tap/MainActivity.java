package com.bowhead.tap;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private final static int PHOTO_REQUEST = 1000;
    private final static int PHOTO_STORY_REQUEST = 101;
    private final static int CONTACT_REQUEST = 102;
    private String TAG = this.getClass().getName();
    private WebView webView;
    private JsInterface jsInterface;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        init();
        initListener();
    }

    private void findViewById() {
        webView = findViewById(R.id.webview);
    }

    private void init() {
        WebSettings mSettings = webView.getSettings();
        mSettings.setUseWideViewPort(true);
        mSettings.setLoadWithOverviewMode(true);
        mSettings.setDomStorageEnabled(true);
        mSettings.setDefaultTextEncodingName("UTF-8");
        // 是否可访问Content Provider的资源，默认值 true
        mSettings.setAllowContentAccess(true);
        // 是否可访问本地文件，默认值 true
        mSettings.setAllowFileAccess(true);
        mSettings.setJavaScriptEnabled(true);

        jsInterface = new JsInterface(MainActivity.this, this, webView);
        webView.addJavascriptInterface(jsInterface, JsInterface.JS_INTERFACE);
        webView.loadUrl("file:///android_asset/h5_demo.html");
        //webView.loadUrl("http://192.168.0.105:5000/home");
    }

    private void initListener() {
        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                return super.onJsAlert(view, url, message, result);
//            }

            @Override
            public void onProgressChanged(WebView view, final int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

        });

        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                FileInputStream input;

                String url = request.getUrl().toString();
                Log.i(TAG, "url: " + url);

                String key = "http://androidimg";
                /*如果请求包含约定的字段 说明是要拿本地的图片*/
                if (url.contains(key)) {
                    String imgPath = url.replace(key, "");
                    Log.i(TAG, "本地图片路径：" + imgPath.trim());
                    try {
                        /*重新构造WebResourceResponse  将数据已流的方式传入*/
                        input = new FileInputStream(new File(imgPath.trim()));
                        WebResourceResponse response = new WebResourceResponse("image/jpg", "UTF-8", input);

                        /*返回WebResourceResponse*/
                        return response;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //6.0以下

            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult++++    code: " + requestCode);
        if (requestCode == PHOTO_REQUEST) {
            //照相回调
            Log.i(TAG, "拍照++++");

            webView.loadUrl("javascript:receiveImageUrl('" + JsInterface.imagePath + "')");
        } else if (requestCode == PHOTO_STORY_REQUEST) {
            //相册回调
            Log.i(TAG, "相册++++");
        }
    }
}
