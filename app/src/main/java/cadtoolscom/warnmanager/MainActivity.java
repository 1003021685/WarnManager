package cadtoolscom.warnmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.ConnectionDetector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cadtoolscom.warnmanager.adapter.WarningAdapter;
import cadtoolscom.warnmanager.been.Contacts;
import cadtoolscom.warnmanager.utils.ToastNotRepeat;

import static cadtoolscom.warnmanager.MyApplication.getOpenSDK;
import static cadtoolscom.warnmanager.been.Contacts.typeString;

public class MainActivity extends Activity {
    private String TAG ="WarningActivity";
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private List<Integer> type_list = new ArrayList<>();
    private RecyclerView rv;
    private ImageButton back;
    private ExecutorService executors;
    private ExecutorService executors_1;
    private WarningAdapter adapter;
    private List<String> list;
    private int user_type;
    private int alarm_type;
    private Button chaxun;
    private String userid;
    private List<EZDeviceInfo> list_ezdevices = new ArrayList<>();
    private static String[] allpermissions = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_SETTINGS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
    };
    private boolean isNeedCheck = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck){
            checkpermission();
        }
    }

    private void initView() {
        MyTask myTask = new MyTask(MainActivity.this);
        myTask.execute();
        rv = findViewById(R.id.rv);
        back = findViewById(R.id.back);
        user_type = MyApplication.user_type;
        chaxun = findViewById(R.id.chaxun);
        executors = Executors.newFixedThreadPool(5);
        executors_1 = Executors.newFixedThreadPool(5);
        list = Contacts.getList_super();
        for (int i = 0 ; i < list.size() ; i++){
            int type= gettype(list.get(i));
            type_list.add(type);
        }
        LinearLayoutManager layoutManager =new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(layoutManager);
        //添加分割线
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL));
        adapter = new WarningAdapter(getApplicationContext(), list, new WarningAdapter.setOnclick() {

            @Override
            public void onClick(View view, int position, int size_url) {
                alarm_type = gettype(list.get(position));
                Log.d("TAG","alarm_type="+alarm_type);
                String title = list.get(position);
                Intent i1 = new Intent(view.getContext(), MessageActivity.class);
                i1.putExtra("type",alarm_type);
                i1.putExtra("title",title);
                i1.putParcelableArrayListExtra("camerainfo_list", (ArrayList<? extends Parcelable>) cameraInfoList);
                startActivity(i1);
            }
        });
        rv.setAdapter(adapter);
    }
    public static int gettype(String str){
        if (str.equals(typeString[0])){
            return Contacts.MESSAGE_TYPE_TRUCK_IDENTITY;
        }else if (str.equals(typeString[1])){
            return Contacts.MESSAGE_TYPE_ILLEGAL_BUILDING;
        }else if (str.equals(typeString[2])){
            return Contacts.MESSAGE_TYPE_ILLEGAL_PLANT;
        }else if (str.equals(typeString[3])){
            return Contacts.MESSAGE_TYPE_STRAW_BURNING;
        }else if (str.equals(typeString[4])){
            return Contacts.MESSAGE_TYPE_RIVER_MONITOR;
        }else if (str.equals(typeString[5])){
            return Contacts.MESSAGE_TYPE_COMPANY_MANAGE;
        }
        return 0;
    }
    /**
     * 获取事件消息任务
     */
    private class MyTask extends AsyncTask<Void, Void, List<EZDeviceInfo>> {
        private int mErrorCode = 0;
        private WeakReference<MainActivity> activityReference;
        MyTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<EZDeviceInfo> doInBackground(Void... voids) {
            if (MainActivity.this.isFinishing()){
                return null;
            }
            if (!ConnectionDetector.isNetworkAvailable(MainActivity.this)){
                mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
                return null;
            }
            try {
                List<EZDeviceInfo> result = null;
                    result = getOpenSDK().getDeviceList(0, 30);
                    list_ezdevices.addAll(result);
                return result;
            }catch (BaseException e){
                ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                mErrorCode = errorInfo.errorCode;
                Log.i("TAG","eooro = "+errorInfo.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EZDeviceInfo> result) {
            MainActivity activity2 = activityReference.get();
            if (activity2 == null || activity2.isFinishing() || activity2.isDestroyed()){
                return;
            }
            if (result!=null){
                Log.d(TAG,"result.size="+result.size());
                for (EZDeviceInfo ezDeviceInfo : result){
                    for (EZCameraInfo cameraInfo : ezDeviceInfo.getCameraInfoList()){
                        cameraInfoList.add(cameraInfo);
                    }
                }
            }
        }
    }
    /**
     * 权限管理
     */
    private void checkpermission() {
        if (Build.VERSION.SDK_INT>=23){
            boolean needapply = false;
            for(int i = 0;i <allpermissions.length;i++ ){
                int checkpermission = ContextCompat.checkSelfPermission(getApplicationContext(),allpermissions[i]);
                if (checkpermission!= PackageManager.PERMISSION_GRANTED){
                    needapply = true;
                }
            }
            if(needapply){
                ActivityCompat.requestPermissions(MainActivity.this,allpermissions,1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ;i<grantResults.length;i++){
            if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                isNeedCheck = false;
            }else{
            }
        }
    }
    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            exitByTwoClick(); //调用双击退出函数
        }
        return false;
    }
    /**
     * 双击退出函数
     */
    private static Boolean isExit=false;
    private void exitByTwoClick() {
        Timer tExit=null;
        if(isExit==false){
            isExit=true;//准备退出
            ToastNotRepeat.show(this,"再按一次退出程序");
            tExit=new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit=false;//取消退出
                }
            },2000);//// 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        }else {
            finish();
            System.exit(0);
        }
    }
}
