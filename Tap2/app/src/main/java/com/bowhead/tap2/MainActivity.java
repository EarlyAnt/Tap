package com.bowhead.tap2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private BridgeWebView bridgeWebview;
    private TextView txtContext;
    private Button btCallJS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bridgeWebview = findViewById(R.id.bridgeWebview);
        txtContext = findViewById(R.id.txtContext);
        btCallJS = findViewById(R.id.btnCallJs);

        bridgeWebview.registerHandler("jsCallAndroid", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                txtContext.setText("params from JavaScript: " + data);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("nickname", "东华帝君");
                    jsonObject.put("age", "20万岁");
                    jsonObject.put("address", "十三重天");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                function.onCallBack("return data from Android: " + jsonObject.toString());
            }
        });
        bridgeWebview.loadUrl("file:///android_asset/demo.html");

        btCallJS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bridgeWebview.callHandler("androidCallJS", "{ name: \"白浅\" }", new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        txtContext.setText("return data from JavaScript: " + data);
                    }
                });
            }
        });
    }
}