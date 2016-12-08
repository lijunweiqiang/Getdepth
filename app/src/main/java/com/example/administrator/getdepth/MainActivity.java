package com.example.administrator.getdepth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private EditText edWorkingAge;
    private Button submit_workingAge;
    private PopupWindow popupWindow;
    private NumberPicker numberPicker;
    private View workingAge_view;
    private int workingAge = 0;
    private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
    private InputStream is;    //输入流，用来接收蓝牙数据
    private String smsg = "";    //显示用数据缓存
    private String fmsg = "";    //保存用数据缓存
    private boolean linksucess=false;       //判断是否连接成功
    String t_MarkerId="";
    String Display="";
    BluetoothDevice _device = null;     //蓝牙设备
    BluetoothSocket _socket = null;      //蓝牙通信socket
    boolean _discoveryFinished = false;
    boolean bRun = true;
    boolean bThread = false;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    // int fuc_state=-1;
    int Location=-1;
    JSONArray depthList;
    private ListView listview;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    SharedPreferences preferences_IP;

    private MyAdapter adapter;
    private List<Data> contacts = new ArrayList<Data>();
    private List<Data> contactSelectedList = new ArrayList<Data>(); 	//记录被选中过的item

    String c_RSS="";
    private boolean isLongClick = false;//是否长按
    private boolean isSelectAll = false;//是否全选
    String initJdata="";
    private List<Data> initdata= new ArrayList<Data>();
    private LinearLayout ll;//控制底部布局的隐藏与显示
    private LinearLayout lin;//控制底部布局的隐藏与显示
    JSONObject JSendData=new JSONObject();
    private CheckBox selectAll_cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences_IP=getSharedPreferences("IPset", Context.MODE_PRIVATE);
        Config.SERVER_IP=preferences_IP.getString("SERVER_IP",Config.SERVER_IP);
        preferences=getSharedPreferences("SaveList", Context.MODE_PRIVATE);
        listview = (ListView) findViewById(R.id.show_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ll = (LinearLayout) findViewById(R.id.ll);
        lin=(LinearLayout) findViewById(R.id.lin);
        selectAll_cb = (CheckBox) findViewById(R.id.selectAll_cb);
        selectAll_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                isSelectAll = true;
                for (int i = 0; i < contacts.size(); i++) {
                    adapter.setItemisSelectedMap(i, true);
                    contactSelectedList.add(contacts.get(i));
                }
            }
                else {
                    isSelectAll = false;
                    for (int i = 0; i < contacts.size(); i++) {
                        adapter.setItemisSelectedMap(i, false);
                    }
                    contactSelectedList.clear();
                }
            }
        });
        edWorkingAge = (EditText) findViewById(R.id.edLength);
        edWorkingAge.setText(workingAge + "厘米");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        depthList=new JSONArray();
        initNumberPicker();
        init();

    //如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (_bluetooth == null){
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 设置设备可以被搜索
        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();
//动态获取权限，6.0以上需要
    //判断是否有权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
    //请求权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
    //判断是否需要 向用户解释，为什么要申请该权限
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
            }
        }
        // 选择服务年限
        edWorkingAge.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 设置初始值
                numberPicker.setValue(workingAge);

                // 强制隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // 填充布局并设置弹出窗体的宽,高
                popupWindow = new PopupWindow(workingAge_view,
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // 设置弹出窗体可点击
                popupWindow.setFocusable(true);
                // 设置弹出窗体动画效果
                popupWindow.setAnimationStyle(R.style.AnimBottom);
                // 触屏位置如果在选择框外面则销毁弹出框
                popupWindow.setOutsideTouchable(true);
                // 设置弹出窗体的背景
                popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.showAtLocation(workingAge_view,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

                // 设置背景透明度
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

                // 添加窗口关闭事件
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.alpha = 1f;
                        getWindow().setAttributes(lp);
                    }

                });
            }

        });

        // 确定服务年限
        submit_workingAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingAge = numberPicker.getValue();
                edWorkingAge.setText(workingAge + "厘米");
                popupWindow.dismiss();
            }
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK )
        {
            // 创建退出对话框
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            // 设置对话框标题
            isExit.setTitle("系统提示");
            // 设置对话框消息
            isExit.setMessage("确定要退出吗");
            // 添加选择按钮并注册监听
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            // 显示对话框
            isExit.show();

        }

        return false;

    }
    /**监听对话框里面的button点击事件*/
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int which)
        {
            switch (which)
            {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };

    //保存操作
    public void onSaveButtonClicked(View v){
        if(depthList!=null){
            Log.v("Json",depthList.toString());
            editor = preferences.edit();
             editor.putString("JsonList",depthList.toString());
            // 提交所有存入的数据
            editor.commit();
            Toast.makeText(this, "保存成功", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "没有要保存的数据", Toast.LENGTH_LONG).show();
        }

    }
    //取消
public void cancelBtn(View v){
    ll.setVisibility(View.GONE);
    int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
    int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
    lin.measure(w, h);
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    lp.setMargins(0,110, 0, 0);
    listview.setLayoutParams(lp);
    contactSelectedList.clear();
    adapter = new MyAdapter(this, contacts, false);
    listview.setAdapter(adapter);
    isLongClick=false;
}
//添加按钮 测试
public void onAddButtonClicked(View v){
    String str = "123";

    if (str == null || str.equals("")) {

        Toast.makeText(getApplicationContext(),
                "请先读取信号值", Toast.LENGTH_SHORT)
                .show();
    } else {
        Data t_data=new Data();
        t_data.Length=String.valueOf(workingAge);
        t_data.Rss=str;
        try {
            JSONObject depthCell = new JSONObject();
            depthCell.put("Depth", workingAge);
            depthCell.put("RSS", 123);
            depthList.put(depthCell);
        }catch (JSONException e){
            e.printStackTrace();
        }
        contacts.add(t_data);
        // 如果在前面添加一条数据添加
        // listItem.add(map);
        adapter.notifyDataSetChanged();

        listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        c_RSS="";
    }
}
    //删除按钮
    public void deleteBtn(View v){
        if(contactSelectedList.size()!=0) {
            new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_TRADITIONAL)
                    .setMessage("您一共选中了" + contactSelectedList.size() + "项," + "确定要删除吗")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ll.setVisibility(View.GONE);
                                    for (Data d : contactSelectedList) {
                                        int i=contacts.indexOf(d);
                                        JSONArray tempJA=new JSONArray();
                                        for(int j=0;j<depthList.length();j++){
                                            if(j!=i) {
                                                try {
                                                    tempJA.put(depthList.get(j));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }else{
                                                continue;
                                            }
                                            }
                                        depthList=tempJA;
                                        contacts.remove(d);
                                    }
                                    int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
                                    int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
                                    lin.measure(w, h);
                                    int top=lin.getMeasuredHeight();
                                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lp.setMargins(0,110, 0, 0);
                                    listview.setLayoutParams(lp);
                                    contactSelectedList.clear();
                                    adapter = new MyAdapter(getApplicationContext(), contacts, false);
                                    listview.setAdapter(adapter);
                                    isLongClick=false;
                                }
                    })
                                .setNegativeButton("取消", null).show();

    }else{
            Toast.makeText(MainActivity.this, "您未选中任何条目,不能删除", Toast.LENGTH_SHORT).show();
        }
    }
    //读取数据按键响应

    public void onReadingButtonClicked(View v){
        if(linksucess) {
            fmsg = "";
            //fuc_state = 1;
            String read = "7F 7F 02 5C 00 7F 00";

            try {
                OutputStream os = _socket.getOutputStream();   //蓝牙连接输出流
                os.write(CHexConver.hexStr2Bytes(read));
                os.write(CHexConver.hexStr2Bytes(read));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    //接收活动结果，响应startActivityForResult()
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras()
                            .getString(DeviveListActivity.EXTRA_DEVICE_ADDRESS);
                    // 得到蓝牙设备句柄
                    _device = _bluetooth.getRemoteDevice(address);

                    // 用服务号得到socket
                    try{
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    }catch(IOException e){
                        Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                    }
                    //连接socket
                    ImageButton ib_connect = (ImageButton) findViewById(R.id.ib_connect);
                    TextView tv_connect=(TextView) findViewById(R.id.tv_connect);
                    try{
                        _socket.connect();
                        Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
                        linksucess=true;
                        tv_connect.setText("断开");
                        ib_connect.setImageDrawable(getResources().getDrawable(R.drawable.disconnect));
                    }catch(IOException e){
                        try{
                            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                            _socket.close();
                            _socket = null;
                        }catch(IOException ee){
                            Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }

                    //打开接收线程
                    try{
                        is = _socket.getInputStream();   //得到蓝牙数据输入流
                    }catch(IOException e){
                        Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(bThread==false){
                        ReadThread.start();
                        bThread=true;
                    }else{
                        bRun = true;
                    }
                }
                break;
            default:break;
        }
    }
    //上传按钮
    public void onUploadClicked(View v){
        if(depthList==null){
            Toast.makeText(MainActivity.this,"请先读取数据",Toast.LENGTH_SHORT).show();
        }else {

            final EditText inputServer = new EditText(this);
            inputServer.setFocusable(true);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请输入描述")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setView(inputServer)
                    .setPositiveButton("确定",new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            String context = inputServer.getText().toString();
                            try {
                                JSendData.put("Describe", context);
                                JSendData.put("DataList",depthList);
                                JSendData.put("DepthType","equipment1" );
                                executeDepth(JSendData.toString());
                            }catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }
    //
    private void executeDepth(String markid) {
        new DepthTast().execute(markid);
    }

    private void onDepthComplete(JSONObject result){
        if(result==null){
            Toast.makeText(MainActivity.this, "通讯失败", Toast.LENGTH_SHORT)
                    .show();
        }else{

            JSONObject resp;
            resp=result;

            try {
                boolean state=resp.getBoolean("State");
                if(state){

                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT)
                            .show();
                    depthList=new JSONArray();
                    JSendData=new JSONObject();
                    Data t_data=new Data();
                    contacts.add(t_data);
                    adapter.notifyDataSetChanged();
                    editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    contacts.clear();
                    adapter = new MyAdapter(this, contacts, false);
                    listview.setAdapter(adapter);
                }else{
                    Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_SHORT)
                            .show();
                }

            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }
    private class DepthTast extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            // TODO Auto-generated method stub
            JSONObject result = null;
            JSONObject reqValue;

            try {
                //将用户名和密码封装到JSONArray中，进行HTTP通信
                reqValue = new JSONObject(params[0]);
                Log.v("send",reqValue.toString());
                JSONObject rec = WebUtil.getJSONobjectByWeb(Config.METHOD_DEPT,
                        reqValue);
                if (rec != null) {//如果成功获取用户ID
                    result = rec;
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            //回调
            onDepthComplete(result);
        }
    }
    // 初始化数据
    private void init() {
        initJdata=preferences.getString("JsonList",initJdata);
        Log.v("Saved",initJdata);
        if(initJdata!=null){
            try {
                depthList = new JSONArray(initJdata);
                int l=depthList.length();
                for (int i=0;i<l;i++){
                    int depth=depthList.getJSONObject(i).getInt("Depth");
                    int Rss=depthList.getJSONObject(i).getInt("RSS");
                    Data contact = new Data();
                    contact.Length = ""+depth;
                    contact.Rss = ""+Rss;
                    contacts.add(contact);

                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        /**
         * 第三个参数表示是否显示checkbox
         */
        adapter = new MyAdapter(this, contacts, false);
        listview.setAdapter(adapter);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onMultchoose(view);
                //isLongClick = true;
                //ll.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }

    //批量删除按键
    public void onMultchoose(View v){
        if(!isLongClick) {
            ll.setVisibility(View.VISIBLE);
            int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            ll.measure(w, h);
            lin.measure(w, h);
            int height = ll.getMeasuredHeight();
            contactSelectedList.clear();    //清空被选中的item项
            adapter = new MyAdapter(this, contacts, true);
            listview.setAdapter(adapter);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 110, 0, height);
            listview.setLayoutParams(lp);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int position, long arg3) {
                    boolean isSelect = adapter.getisSelectedAt(position);

                    if (!isSelect) {
                        //当前为被选中，记录下来，用于删除
                        contactSelectedList.add(contacts.get(position));
                    } else {
                        contactSelectedList.remove(contacts.get(position));
                    }

                    //选中状态的切换
                    adapter.setItemisSelectedMap(position, !isSelect);
                }
            });
            isLongClick=true;
        }else if(isLongClick){
            cancelBtn(v);
        }
    }
    //连接按键响应函数
    public void onConnectButtonClicked(View v){
        if(_bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
            _bluetooth.enable();
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }


        //如未连接设备则打开DeviceListActivity进行设备搜索
        ImageButton ib_connect = (ImageButton) findViewById(R.id.ib_connect);
        TextView tv_connect=(TextView) findViewById(R.id.tv_connect);
        if(_socket==null){
            Intent serverIntent = new Intent(MainActivity.this, DeviveListActivity.class); //跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
        }
        else{
            //关闭连接socket
            try{

                is.close();
                _socket.close();
                _socket = null;
                bRun = false;
                tv_connect.setText("连接");
                ib_connect.setImageDrawable(getResources().getDrawable(R.drawable.connect));
                linksucess=false;
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return;
    }
    Thread ReadThread=new Thread(){

        public void run(){
            int num = 0;


            byte[] buffer= new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            //buffer=null;
            //接收线程
            while(true){
                int count=0;
                try{
                    //while(is.available()==0){
                    //   while(bRun == false){

                    //
                    // }
                    while(true){
                        num = is.read(buffer);         //读入数据
                        count+=num;
                        //Integer a=num;
                        String s0 = CHexConver.byte2HexStr(buffer,num);//.concat(" ");          //new String(buffer,0,num);
                        fmsg+=s0;
                        if(is.available()==0&&count>=20){
                            try {
                                Thread.sleep(200);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            break;  //短时间没有数据才跳出进行显示
                        }
                    }
                    //发送显示消息，进行显示刷新
                    handler.sendMessage(handler.obtainMessage());

                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    };
    //消息处理队列
    Handler handler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            final int t=fmsg.indexOf("7F7F");
            int end=fmsg.indexOf("7F00",t);
            if(t!=-1&&end!=-1&&t<end) {
                smsg = fmsg.substring(t, end);
                if(smsg.length()>=10) {
                    String Errcode = smsg.substring(8, 9);
                    if (Errcode.equals("F")) {
                        t_MarkerId = smsg.substring(10, 11);
                        String RSS = smsg.substring(26, 30);

                        // String depth=edit0.getText().toString();
                        //int depthforsend=Integer.parseInt(depth);
                        // t_MarkerId=CHexConver.String2Int(t_MarkerId);
                        //String t_Display ="距离类型："+t_MarkerId+" 距离："+depth+" 信号："+RSS+"\n";
                        try {
                            JSONObject depthCell = new JSONObject();
                            depthCell.put("Depth", workingAge);
                            //depthCell.put("DepthType", t_MarkerId);
                            byte[] temp = CHexConver.hexStr2Bytes(RSS);
                            int a = CHexConver.bytesToInt(temp, 0);
                            RSS = String.valueOf(a);
                            c_RSS=RSS;
                            depthCell.put("RSS", a);
                            depthList.put(depthCell);

                            //Log.v("send2", depthList.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Data t_data=new Data();
                        t_data.Length=String.valueOf(workingAge);
                        t_data.Rss=c_RSS;
                        contacts.add(t_data);
                        adapter.notifyDataSetChanged();
                        listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                        c_RSS="";
                    }
                }
                fmsg = "";
                //sv.scrollTo(0,dis.getMeasuredHeight()); //跳至数据最后一页

            }
        }
    };
    private void startFlick( View view ){
        if( null == view ){
            return;
        }
        Animation alphaAnimation = new AlphaAnimation( 1, 0.1f );
        alphaAnimation.setDuration( 150 );
        alphaAnimation.setInterpolator( new LinearInterpolator( ) );
        alphaAnimation.setRepeatCount(1 );
        alphaAnimation.setRepeatMode( Animation.REVERSE );
        view.startAnimation( alphaAnimation );
    }
    //关闭程序掉用处理部分
    @Override
    public void onDestroy(){
        super.onDestroy();

        try{is.close();}
        catch(Exception e){
            e.printStackTrace();
        }
        if(_socket!=null)  //关闭连接socket
            try{
                _socket.close();
                _socket=null;
                _bluetooth.disable();
            }catch(IOException e){
                e.printStackTrace();
            }
        _bluetooth.disable();  //关闭蓝牙服务
    }
    /**
     * 初始化滚动框布局
     */
    private void initNumberPicker() {
        workingAge_view = LayoutInflater.from(this).inflate(R.layout.popupwindow, null);
        submit_workingAge = (Button) workingAge_view.findViewById(R.id.submit_workingAge);
        numberPicker = (NumberPicker) workingAge_view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){

            @Override
            public void onValueChange(NumberPicker picker, int oldVal,
                                      int newVal) {
                // TODO Auto-generated method stub
                if(newVal==300&&oldVal==0){

                }
                else if(newVal!=0){
                    picker.setValue((newVal < oldVal)?oldVal-5:oldVal+5);
                }
            }

        });
        numberPicker.setFocusable(false);
        numberPicker.setFocusableInTouchMode(false);
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerDividerColor(numberPicker);

    }

    /**
     * 自定义滚动框分隔线颜色
     */
    private void setNumberPickerDividerColor(NumberPicker number) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(number, new ColorDrawable(ContextCompat.getColor(this, R.color.numberpicker_line)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(MainActivity.this,IPSettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
