package com.example.administrator.getdepth;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/7/28.
 */
public class WebUtil {
    public static JSONObject getJSONobjectByWeb(String methodName,
                                                JSONObject params) {
        String returnValue = "";
        JSONObject result = null;


        try {
            String spec = Config.SERVER_IP + "/MyJFinalApp/"  //String spec = Config.SERVER_IP + "/MyJFinalApp/"
                    + methodName;
            URL url = new URL(spec);
            // 根据URL对象打开链接
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            // 设置请求的方式
            urlConnection.setRequestMethod("POST");
            // 设置请求的超时时间
            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);

            String data=params.toString();
            //urlConnection.setRequestProperty();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            //获取输出流
            OutputStream os = urlConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            if (urlConnection.getResponseCode() == 200) {
                Log.v("123","789");
                // 获取响应的输入流对象
                InputStream is = urlConnection.getInputStream();
                // 创建字节输出流对象
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 定义读取的长度
                int len = 0;
                // 定义缓冲区
                byte buffer[] = new byte[1024];
                // 按照缓冲区的大小，循环读取
                while ((len = is.read(buffer)) != -1) {
                    // 根据读取的长度写入到os对象中
                    baos.write(buffer, 0, len);
                }
                // 释放资源
                is.close();
                baos.close();
                // 返回字符串
                returnValue= new String(baos.toByteArray());
                result= new JSONObject(returnValue);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;


    }

}
