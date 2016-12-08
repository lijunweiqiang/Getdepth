package com.example.administrator.getdepth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class IPSettingActivity extends AppCompatActivity {

    //layout组件
    private EditText mFirstIP;
    private EditText mSecondIP;
    private EditText mThirdIP;
    private EditText mFourthIP;
    private EditText port;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_setting);
        initLayout();
        preferences=getSharedPreferences("IPset", Context.MODE_PRIVATE);
    }
    //Layout 组件绑定
    public void initLayout(){
        mFirstIP = (EditText)findViewById(R.id.ip_first);
        mSecondIP = (EditText)findViewById(R.id.ip_second);
        mThirdIP = (EditText)findViewById(R.id.ip_third);
        mFourthIP = (EditText)findViewById(R.id.ip_fourth);
        port=(EditText)findViewById(R.id.Port);
        String IP= Config.SERVER_IP.substring(7);
        Log.v("ip",IP);
        String[] IPStrArray =IP.split("\\.");
        Log.v("ip",String.valueOf(IPStrArray.length));
        String[] ip4=IPStrArray[3].split("\\:");
        IPStrArray[3]=ip4[0];
        mFirstIP.setText(IPStrArray[0]);
        mSecondIP.setText(IPStrArray[1]);
        mThirdIP.setText(IPStrArray[2]);
        mFourthIP.setText(IPStrArray[3]);
        if(ip4.length==2){
            port.setText(ip4[1]);
        }

    }
    //修改IP
    public void onIPOKButtonClicked(View v) {
        if(port.getText()!=null){
            Config.SERVER_IP ="http://"+ mFirstIP.getText()+"."+mSecondIP.getText()+"."+mThirdIP.getText()+"."+mFourthIP.getText()+":"+port.getText();
            editor = preferences.edit();
            editor.putString("SERVER_IP",Config.SERVER_IP);
            // 提交所有存入的数据
            editor.commit();
        }else{
            Config.SERVER_IP ="http://"+ mFirstIP.getText()+"."+mSecondIP.getText()+"."+mThirdIP.getText()+"."+mFourthIP.getText();
            editor = preferences.edit();
            editor.putString("SERVER_IP",Config.SERVER_IP);
            // 提交所有存入的数据
            editor.commit();
        }
            Intent intent1 = new Intent(IPSettingActivity.this,MainActivity.class);
            startActivity(intent1);
            finish();

    }
    //取消
    public void onIPCancleButtonClicked(View v){
        Intent intent1 = new Intent(IPSettingActivity.this, MainActivity.class);
        startActivity(intent1);
        finish();
    }

}
