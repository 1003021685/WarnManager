package cadtoolscom.warnmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.videogo.main.EzvizWebViewActivity;
import com.videogo.openapi.EZOpenSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cadtoolscom.warnmanager.been.Contacts;
import cadtoolscom.warnmanager.been.User;
import cadtoolscom.warnmanager.utils.ExampleUtil;
import cadtoolscom.warnmanager.utils.OkHttpUtil;
import cadtoolscom.warnmanager.utils.ToastNotRepeat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static cadtoolscom.warnmanager.been.Contacts.AppKey;
import static cadtoolscom.warnmanager.been.Contacts.Secret;

/**
 * Created by Administrator on 2020/3/14.
 */

public class LoginActivity extends Activity{
    private String TAG = "LoginActivity";
    private Handler handler ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getAccessToken();
        initHandler();
        login();
    }

    private void getAccessToken() {
        String url = "https://open.ys7.com/api/lapp/token/get";
        Map<String,String> map = new HashMap<>();
        map.put("appKey",AppKey);
        map.put("appSecret",Secret);
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                //Log.d(TAG,"response="+responseBody);
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String data = object.get("data").toString();
                    JSONObject obj = new JSONObject(data);
                    String accessToken = obj.get("accessToken").toString();
                    Message message = new Message();
                    message.what = 104;
                    Bundle bundle = new Bundle();
                    bundle.putString("accessToken",accessToken);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 102:
                        Bundle bundle = msg.getData();
                        String flag = bundle.getString("flag");
                        //用户名密码正确跳转
                        if (flag.equals("true")) {
                            Intent it = new Intent(LoginActivity.this, EzvizWebViewActivity.class);//启动MainActivity
                            startActivity(it);
                            LoginActivity.this.finish();//关闭当前Activity，防止返回到此界面
                        } else {
                            Toast.makeText(LoginActivity.this, "密码错误！", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 103:
                        Toast.makeText(LoginActivity.this, "网络异常！", Toast.LENGTH_LONG).show();
                        break;
                    case 104:
                        Bundle bundle1 = msg.getData();
                        String accessToken = bundle1.getString("accessToken");
                        EZOpenSDK.getInstance().setAccessToken(accessToken);
                        break;
                }
            }
        };
    }

    private void login() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ox();
            }
        },200);
    }
    private void ox(){
        if(!ExampleUtil.isConnected(LoginActivity.this)){
            ToastNotRepeat.show(LoginActivity.this,"确认网络是否断开！");
        }else{
            //验证登录
            String url = Contacts.service_url+"api/login";
            Map<String,String> map = new HashMap<>();
            map.put("userName","admin");
            map.put("password","111111");
            OkHttpUtil.post(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: ");
                    Message message = new Message();
                    message.what = 103;
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "result="+responseBody);
                    try{
                        JSONObject object = new JSONObject(responseBody);
                        String result = object.get("success").toString();
                        String data = object.get("data").toString();
                        Gson gson = new Gson();
                        Message message = new Message();
                        message.what = 102;
                        Bundle bundle = new Bundle();
                        bundle.putString("flag",result);
                        if (result.equals("true")){
                            User user = gson.fromJson(data,User.class);
                            bundle.putSerializable("user",user);
                        }
                        message.setData(bundle);
                        handler.sendMessage(message);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            },map);
        }
    }
}
