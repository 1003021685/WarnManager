/* 
 * @ProjectName VideoGoJar
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName EzvizApplication.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package cadtoolscom.warnmanager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;
import com.videogo.util.LogUtil;

import cadtoolscom.warnmanager.been.MyDatabaseHelper;

import static cadtoolscom.warnmanager.been.Contacts.AppKey;


/**
 * 自定义应用
 *
 */
public class MyApplication extends Application {

    //开发者需要填入自己申请的appkey//ABC
    //public static String AppKey = "76d8a02ae81a4260a02e470ebb48077d";
    public  static int user_type;
    public static String table_name;
    private IntentFilter intentFilter;
    private MyDatabaseHelper dbHelper;
    private  SQLiteDatabase db;
    private EzvizBroadcastReceiver receiver;
    private Boolean isPush = false;
    private Boolean isDelete = false;
    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSDK();
        initData();
    }
    public SQLiteDatabase getDatebase(){
        return db;
    }

    public boolean getPush(){
        return isPush;
    }

    public boolean getDelate(){
        return  isDelete;
    }

    public void setPush(Boolean f){
        isPush = f;
    }

    public void setDelete(Boolean f){
        isDelete = f;
    }

    private void initData() {
        dbHelper = new MyDatabaseHelper(this, "filepath2.db", null, 1);
        db = dbHelper.getWritableDatabase();
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.videogo.action.ADD_DEVICE_SUCCESS_ACTION");
        intentFilter.addAction("com.videogo.action.OAUTH_SUCCESS_ACTION");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        receiver = new EzvizBroadcastReceiver();
        registerReceiver(receiver,intentFilter);


    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(receiver);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }



    private void initSDK() {
        {
            /**
             * sdk日志开关，正式发布需要去掉
             */
            EZOpenSDK.showSDKLog(true);

            /**
             * 设置是否支持P2P取流,详见api
             */
            EZOpenSDK.enableP2P(true);

            /**
             * APP_KEY请替换成自己申请的
             */
            EZOpenSDK.initLib(this, AppKey);
        }
    }
    private class EzvizBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.i("TAG","action = "+action);
            if (action.equals(Constant.OAUTH_SUCCESS_ACTION)){
                Log.i("TAG", "onReceive: OAUTH_SUCCESS_ACTION");
                //Intent i = new Intent(context, EZCameraListActivity.class);
//                Intent i = new Intent(context, MainActivity.class);
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                /*******   获取登录成功之后的EZAccessToken对象   *****/
                EZAccessToken token = MyApplication.getOpenSDK().getEZAccessToken();
                context.startActivity(i);
            }
        }
    }
}
