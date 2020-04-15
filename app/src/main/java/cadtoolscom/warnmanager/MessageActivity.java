package cadtoolscom.warnmanager;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cadtoolscom.warnmanager.adapter.TitleWarningAdatter;
import cadtoolscom.warnmanager.been.AlarmMessage;
import cadtoolscom.warnmanager.been.Contacts;
import cadtoolscom.warnmanager.been.SnCal;
import cadtoolscom.warnmanager.been.WaitDialog;
import cadtoolscom.warnmanager.utils.CommItemDecoration;
import cadtoolscom.warnmanager.utils.OkHttpUtil;
import cadtoolscom.warnmanager.utils.ToastNotRepeat;
import cadtoolscom.warnmanager.utils.WarnLinearLayoutManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MessageActivity extends Activity {
    private int alarm_type;
    private String TAG = "GarbageActivity";
    private List<String> title = new ArrayList<>();
    private List<String> time_title = new ArrayList<>();
    private List<String> list_ids = new ArrayList<>();
    private List<AlarmMessage> alarmMessageList = new ArrayList<>();
    private List<EZCameraInfo> cameraInfoList = new ArrayList<>();
    private List<String> read_list = new ArrayList<>();
    private ExecutorService cachedThreadPool;
    private ExecutorService cachedThreadPool_1;
    private TextView date;
    private Date queryDate = null;
    private RecyclerView rv;
    private int page = 1;
    private int page_size = 30;
    private int list_size = 0;
    private SQLiteDatabase db;
    private RefreshLayout refreshLayout;
    private TitleWarningAdatter adatper;
    private Spinner spinner_time;
    private Spinner spinner_location;
    private TextView title_text;
    private List<AlarmMessage> list = new ArrayList<>();
    private ImageButton query;
    private ImageButton back;
    private Button push;
    private String table_name;
    private Boolean refreshType = true;
    private String s1 = "全部";
    private String s2 = "全部";
    private SharedPreferences sharedPreferences;
    private String userid;
    private Context context;
    private WaitDialog mWaitDlg = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    break;
                case 1:
                    break;
                case 104:
                    Bundle bundle3 = msg.getData();
                    read_list.clear();
                    read_list = bundle3.getStringArrayList("read_list");
                    adatper.setRead_list(read_list);
                    adatper.notifyDataSetChanged();
                    break;
                case 105:
                    if (refreshType) {
                        adatper.notifyDataSetChanged();
                    }else{
                        if (list_size >= page_size) {
                            adatper.notifyItemRangeInserted(alarmMessageList.size() - list_size, alarmMessageList.size());
                            adatper.notifyItemRangeChanged(alarmMessageList.size() - list_size, alarmMessageList.size());
                        }
                    }
                    break;
                case 102:
                    ToastNotRepeat.show(getApplicationContext(), "网络异常！");
                    break;
                case 101:
                    try {
                        if (mWaitDlg != null && mWaitDlg.isShowing()) {
                            mWaitDlg.dismiss();
                        }
                        Bundle bundle2 = msg.getData();
                        List<AlarmMessage> list = new ArrayList<>();
                        list = bundle2.getParcelableArrayList("datalist");
                        list_size = list.size();
                        Log.d("refresh", "list.isze=" + list_size);
                        if (refreshType) {
                            //刷新
                            if (alarmMessageList.size() != 0) {
                                alarmMessageList.clear();
                            }
                            alarmMessageList.addAll(list);
                            if (alarmMessageList.size() == 0){
                                adatper.notifyDataSetChanged();
                            }else {
                                for (AlarmMessage alarmMessage : alarmMessageList){
                                    queryLocation(alarmMessage);
                                }
                            }
                        } else {
                            //加载更多
                            if (list_size>=page_size){
                                for (AlarmMessage alarmMessage : list){
                                    queryLocation(alarmMessage);
                                }
                                alarmMessageList.addAll(list);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        try {
            initView();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        initdata();
    }

    private void initView() throws ParseException {
        context = getApplicationContext();
        db = ((MyApplication) getApplication()).getDatebase();
        sharedPreferences = getSharedPreferences("userid", MODE_PRIVATE);
        userid = sharedPreferences.getString("id", "1");
        cachedThreadPool = Executors.newFixedThreadPool(3);
        cachedThreadPool_1 = Executors.newFixedThreadPool(24);
        refreshLayout = findViewById(R.id.refreshLayout);
        spinner_time = findViewById(R.id.spinner_1);
        spinner_location = findViewById(R.id.spinner_2);
        query = findViewById(R.id.query);
        back = findViewById(R.id.back);
        push = findViewById(R.id.push);
        title_text = findViewById(R.id.title);
        table_name = MyApplication.table_name;
        alarm_type = getIntent().getIntExtra("type", 0);
        String str = getIntent().getStringExtra("title");
        cameraInfoList = getIntent().getParcelableArrayListExtra("camerainfo_list");
        title_text.setText(str);
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
        date = findViewById(R.id.date);
        long systime = System.currentTimeMillis();
        queryDate = longToDate(systime,"yyyy-MM-dd");
        date.setText(dateToString(queryDate,"yyyy-MM-dd"));
        //查询数据
        queryDataFromService(alarm_type, 1,date.getText().toString());
        page++;
        queryReadId();
        rv = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new WarnLinearLayoutManager(context);
        rv.setLayoutManager(layoutManager);
        rv.addItemDecoration(CommItemDecoration.createVertical(context, getResources().getColor(R.color.blue_bg), 4));
        rv.setItemAnimator(new DefaultItemAnimator());
        adatper = new TitleWarningAdatter(alarmMessageList ,cachedThreadPool_1,cameraInfoList, context);
        rv.setAdapter(adatper);
        adatper.setSetOnItemClickListener(new TitleWarningAdatter.OnClickListener() {
            @Override
            public void OnItemClick(View view, int position, String address) {
                Intent intent = new Intent(context, ManagerPlaybackActivity2.class);
                intent.putExtra("alarmMessage", alarmMessageList.get(position));
                intent.putExtra("address", address);
                Log.d("TAG","url="+alarmMessageList.get(position).getImgPath());
                startActivity(intent);
                updateRead(alarmMessageList.get(position).getId());
            }
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    adatper.setScrolling(false);
                    Log.d("TAG", "***********************************************************");
                    adatper.notifyDataSetChanged();
                } else {
                    adatper.setScrolling(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCalendar();
            }
        });
        adatper.setOnItemClickListener(new TitleWarningAdatter.ItemCheckListener() {
            @Override
            public void onItemCheck(String id) {
                list_ids.add(id);
                Log.d(TAG,"ids="+list_ids.toString());
            }
        });

        adatper.setOnItemUnClickListener(new TitleWarningAdatter.ItemUnCheckListener() {
            @Override
            public void onItemUnCheck(String id) {
                list_ids.remove(id);
                Log.d(TAG,"ids="+list_ids.toString());
            }
        });

        push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWaitDlg.show();
                String url = Contacts.service_url + "/api/batchFilter";
                Map<String, String> map = new HashMap<>();
                //map.put("ids", alarmMessage.getId());
                OkHttpUtil.post(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Message message = Message.obtain();
                        message.what = 303;
                        //playBackHandler.sendMessage(message);
                        Log.d(TAG, "onFailure: ", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseBody = response.body().string();
                        Log.d(TAG, "responseBody="+responseBody);
                        try{
                            JSONObject object = new JSONObject(responseBody);
                            String result = object.get("success").toString();
                            Log.d(TAG, "result="+result);
                            Message message = Message.obtain();
                            message.what = 304;
                            Bundle bundle = new Bundle();
                            bundle.putString("flag",result);
                            message.setData(bundle);
                            //playBackHandler.sendMessage(message);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                },map);
            }
        });
    }

    private void updateRead(String userid) {
        if (!read_list.contains(userid)) {
            ContentValues values = new ContentValues();
            values.put("type0", userid);
            db.insert("alarmReaded", null, values);
            read_list.add(userid);
            adatper.setRead_list(read_list);
            adatper.notifyDataSetChanged();
        }
    }

    private void initdata() {
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                Log.d("TAG", "刷新");
                //刷新
                refreshType = true;
                page = 1;
                queryDataFromService(alarm_type, page,date.getText().toString());
                refreshLayout.finishRefresh(1000);
                page++;
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refreshType = false;
                Log.d("refresh","alarm_type="+alarm_type);
                queryDataFromService(alarm_type, page,date.getText().toString());
                if (list_size < page_size) {
                    ToastNotRepeat.show(getApplicationContext(), "暂无更多的数据啦");
                    refreshLayout.finishLoadMoreWithNoMoreData();
                    return;
                } else {
                    refreshLayout.setEnableLoadMore(true);
                    refreshLayout.finishLoadMore(1000);
                    page++;
                }
            }
        });
        //自动刷新
        //refreshLayout.autoRefresh();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWaitDlg.show();
                refreshType = true;
                page = 1;
                queryDataFromService(alarm_type, page,date.getText().toString());
                page++;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (((MyApplication) getApplication()).getPush()||((MyApplication) getApplication()).getDelate()){
            Log.d(TAG,"refresh");
            mWaitDlg.show();
            refreshType = true;
            page = 1;
            queryDataFromService(alarm_type, page,date.getText().toString());
            page++;
            ((MyApplication) getApplication()).setPush(false);
            ((MyApplication) getApplication()).setDelete(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        cachedThreadPool.shutdown();
    }
    private void queryReadId(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"startquery");
                Cursor cursor = db.query("alarmReaded", null, null, null, null, null, null);
                List<String> read_list = new ArrayList<>();
                if (cursor.moveToFirst()) {
                    do {
                        String type_read = cursor.getString(cursor.getColumnIndex("type0"));
                        read_list.add(type_read);
                    } while (cursor.moveToNext());
                }
                Message msg= Message.obtain();
                msg.what = 104;
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("read_list", (ArrayList<String>) read_list);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        };
        cachedThreadPool.execute(runnable);
    }

    private void queryDataFromService(int type, int page,String date) {
        String url = Contacts.service_url + "api/getEarlyWarning";
        Map<String, String> map = new HashMap<>();
        map.put("userId", userid);
        map.put("type", String.valueOf(type));
        map.put("limit", String.valueOf(page_size));
        map.put("page", String.valueOf(page));
        map.put("createTime",date);
        OkHttpUtil.post(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = Message.obtain();
                message.what = 102;
                handler.sendMessage(message);
                Log.d(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                List<AlarmMessage> alarmMessageList = new ArrayList<>();
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String result = object.get("success").toString();
                    if (result.equals("true")) {
                        String data = object.get("data").toString();
                        JSONObject objectdata = new JSONObject(data);
                        String count = objectdata.get("count").toString();
                        if (Integer.parseInt(count)>0){
                            Gson gson = new Gson();
                            List<JsonObject> list_objects = gson.fromJson(objectdata.get("data").toString(), new TypeToken<List<JsonObject>>() {
                            }.getType());
                            for (JsonObject object1 : list_objects) {
                                AlarmMessage alarmMessage = gson.fromJson(object1, AlarmMessage.class);
                                alarmMessageList.add(alarmMessage);
                            }
                            Message message = Message.obtain();
                            message.what = 101;
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) alarmMessageList);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }else{
                            Message message = Message.obtain();
                            message.what = 101;
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) alarmMessageList);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, map);
    }

    private void querydata(int alarmtype) {
        if (!refreshType && alarmMessageList.size() != 0) {
            alarmMessageList.addAll(list.subList(page_size * page, page_size * (page + 1)));
            adatper.notifyItemRangeInserted(list.size() - page_size, list.size());
            adatper.notifyItemRangeChanged(list.size() - page_size, list.size());
            page++;
        } else {
            Log.d("TAG", "startrefresh");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = db.query(table_name, null, "type = ?", new String[]{String.valueOf(alarmtype)}, null, null, null);
                    List<AlarmMessage> list = new ArrayList<>();
                    if (cursor.moveToFirst()) {
                        do {
                            String message = cursor.getString(cursor.getColumnIndex("message"));
                            String type = cursor.getString(cursor.getColumnIndex("type"));
                            String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                            String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                            String altitude = cursor.getString(cursor.getColumnIndex("altitude"));
                            String address = cursor.getString(cursor.getColumnIndex("address"));
                            String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
                            String videoPath = cursor.getString(cursor.getColumnIndex("videoPath"));
                            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
                            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
                            String endTime = cursor.getString(cursor.getColumnIndex("endTime"));
                            String channelNumber = cursor.getString(cursor.getColumnIndex("channelNumber"));
                            AlarmMessage alarmMessage = new AlarmMessage(message, type, latitude, longitude, altitude,
                                    address, imgPath, videoPath, createTime, startTime, endTime, channelNumber);
                            list.add(0, alarmMessage);
                        } while (cursor.moveToNext());
                    }
                    Message message = new Message();
                    message.what = 103;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("datalist", (ArrayList<? extends Parcelable>) list);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            };
        }
    }
    public  void queryLocation( AlarmMessage alarmMessage) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String la = alarmMessage.getLatitude();
        String ln = alarmMessage.getLongitude();
        String url = Contacts.location_url;
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        map.put("location",la+","+ln);
        map.put("coordtype","wgs84ll");
        map.put("radius","500");
        map.put("extensions_poi","1");
        map.put("output","json");
        map.put("ak","KNAeq1kjoe2u24PTYfeL4kO0KvGaqNak");
        String sn = SnCal.getSnKry(map);
        OkHttpUtil.get(url, sn,new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TAG", "onFailure: ",e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                String address = "";
                try {
                    JSONObject object = new JSONObject(responseBody);
                    String status = object.get("status").toString();
                    if (status.equals("0")){
                        String result = object.get("result").toString();
                        JSONObject objectdata = new JSONObject(result);
                        String formatted_address = objectdata.get("formatted_address").toString();
                        String sematic_description = objectdata.get("sematic_description").toString();
                        if (sematic_description==null || sematic_description.equals("")){
                            address = formatted_address;
                        } else {
                            address = formatted_address + "(" + sematic_description + ")";
                        }
                        if (address.equals("")||address==null){
                            alarmMessage.setAddress("未知");
                        }else{
                            alarmMessage.setAddress(address);
                        }
                        handler.sendEmptyMessage(105);
                    } else {
                        alarmMessage.setAddress("未知");
                        handler.sendEmptyMessage(105);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },map);
    }
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }
    private void goToCalendar() {
        if (getMinDate() != null && new Date().before(getMinDate())) {
            ToastNotRepeat.show(getApplicationContext(),"请先将日期设置到2012/01/01之后");
            return;
        }
        showDatePicker();
    }
    private Date getMinDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse("2012-01-01");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void showDatePicker() {
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.setTime(queryDate);
        DatePickerDialog dpd = new DatePickerDialog(this, null, nowCalendar.get(Calendar.YEAR),
                nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        dpd.setCancelable(true);
        dpd.setTitle(R.string.select_date);
        dpd.setCanceledOnTouchOutside(true);
        dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.certain),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dg, int which) {
                        DatePicker dp = null;
                        Field[] fields = dg.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            if (field.getName().equals("mDatePicker")) {
                                try {
                                    dp = (DatePicker) field.get(dg);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        dp.clearFocus();
                        Calendar selectCalendar = Calendar.getInstance();
                        selectCalendar.set(Calendar.YEAR, dp.getYear());
                        selectCalendar.set(Calendar.MONTH, dp.getMonth());
                        selectCalendar.set(Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
                        queryDate = (Date) selectCalendar.getTime();
                        date.setText(dateToString(queryDate,"yyyy-MM-dd"));
                    }
                });
        dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogUtil.debugLog("Picker", "Cancel!");
                        if (!isFinishing()) {
                            dialog.dismiss();
                        }

                    }
                });

        dpd.show();
    }
}
