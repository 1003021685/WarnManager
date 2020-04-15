package cadtoolscom.warnmanager.adapter;

import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.videogo.openapi.bean.EZCameraInfo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import cadtoolscom.warnmanager.R;
import cadtoolscom.warnmanager.been.AlarmMessage;
import cadtoolscom.warnmanager.been.AsyncImageLoader;
import cadtoolscom.warnmanager.been.RoundTransform;
import cadtoolscom.warnmanager.utils.DataUtils;

public class TitleWarningAdatter extends RecyclerView.Adapter<TitleWarningAdatter.MyViewHolder>{
    private List<AlarmMessage> alarmMessageList;
    private String TAG = "GarbageActivity";
    private Context context;
    private OnClickListener OnClickListener;
    private List<EZCameraInfo> cameraInfoList;
    private List<String> read_list;
    private AsyncImageLoader asyncImageLoader;
    private ExecutorService cachedThreadPool;
    private String address;
    private Map<Integer , Boolean> checkStatus = new HashMap<>();
    private ItemCheckListener itemCheckListener;
    private ItemUnCheckListener itemUnCheckListener;
    private boolean isSrolling = false;


    public TitleWarningAdatter(List<AlarmMessage> alarmMessageList, ExecutorService cachedThreadPool, List<EZCameraInfo> cameraInfos, Context context) {
        this.alarmMessageList = alarmMessageList;
        this.cameraInfoList = cameraInfos;
        this.context = context;
        this.cachedThreadPool = cachedThreadPool;
        this.asyncImageLoader = new AsyncImageLoader(this.cachedThreadPool);
//        Log.d(TAG,"alarmMessageList.size="+this.alarmMessageList.size());
//        for ( int i  = 0 ; i < this.alarmMessageList.size() ; i++){
//            checkStatus.put(i,false);
//        }
//        Log.d(TAG,"checkStatues.size="+checkStatus.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        //绑定监听事件
        //view.setOnClickListener(this);
        return viewHolder;
    }
    public void setScrolling(boolean scrolling){
        this.isSrolling = scrolling;
    }

    public void setRead_list(List<String> list){
        this.read_list = list;
    }


    public interface ItemCheckListener{
        void onItemCheck(String id);
    }
    public interface ItemUnCheckListener{
        void onItemUnCheck(String id);
    }
    public void setOnItemClickListener(ItemCheckListener itemClickListener){
        this.itemCheckListener = itemClickListener;
    }
    public void setOnItemUnClickListener(ItemUnCheckListener itemUnClickListener){
        this.itemUnCheckListener = itemUnClickListener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String path = alarmMessageList.get(position).getImgPath();
        //Log.d(TAG,"path = "+path);
        holder.imageView.setTag(path);
        //加载图片
        if (path != null && !path.equals("")) {
            try {
                List<HashMap<String, String>> list = DataUtils.getUrlResouses(path);
                if (list == null) {
                    Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                } else {
                    String avatarTag = (String) holder.imageView.getTag();
                    HashMap<String, String> map = list.get(0);
                    String pic_name = map.get("pic_name");
                    String imagpath = Environment.getExternalStorageDirectory().toString() + "/warnmanager/cash/" + pic_name;
                    File imgFile = new File(imagpath);
                    if (!imgFile.exists()) {
                        //Log.d(TAG,"文件不存在");
                        asyncImageLoader.loadDrawable(map, new AsyncImageLoader.ImageCallback() {
                            @Override
                            public void imageLoaded() {
                                if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                                    Picasso.with(context).load(imgFile).transform(new RoundTransform(20)).resize(600, 300)
                                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                                }
                            }

                            @Override
                            public void imageLoadEmpty() {
                                if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                                    Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                                            .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
                                }
                            }
                        });
                    } else {
                        //Log.d(TAG,"文件存在");
                        if (null == avatarTag || avatarTag.equals(holder.imageView.getTag())) {
                            Picasso.with(context).load(imgFile).transform(new RoundTransform(20)).resize(600, 300)
                                    .error(context.getResources().getDrawable(R.mipmap.ic_launcher)).into(holder.imageView);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Picasso.with(context).load(R.mipmap.load_fail).transform(new RoundTransform(20)).resize(600, 300)
                    .error(context.getResources().getDrawable(R.mipmap.load_fail)).into(holder.imageView);
        }

        //地址
        if (alarmMessageList.get(position).getLatitude() != null || alarmMessageList.get(position).getLongitude() != null) {
            holder.address.setText(alarmMessageList.get(position).getAddress());
        } else {
            holder.address.setText("未知");
        }
        //设置已读
        String id = alarmMessageList.get(position).getId();
        String isPush = alarmMessageList.get(position).getIsPush();
//        holder.checkBox.setOnCheckedChangeListener(null);
//        holder.checkBox.setChecked(checkStatus.get(position));
//        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                checkStatus.put(position,isChecked);
//            }
//        });
//        if (isPush.equals("1")){
//            holder.checkBox.setVisibility(View.GONE);
//        }
//        holder.checkBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (holder.checkBox.isChecked()){
//                    if (itemCheckListener !=null){
//                        String id = alarmMessageList.get(position).getId();
//                        itemCheckListener.onItemCheck(id);
//
//                    }
//                }else{
//                    if (itemUnCheckListener != null){
//                        String id = alarmMessageList.get(position).getId();
//                        itemUnCheckListener.onItemUnCheck(id);
//                    }
//                }
//            }
//        });
        if (read_list.contains(id)) {
            holder.camera_name.setTextColor(context.getResources().getColor(R.color.topBarText));
        } else {
            holder.camera_name.setTextColor(context.getResources().getColor(R.color.a1_blue_color));
        }
        String camera_name = getCameraInfo(cameraInfoList, alarmMessageList.get(position).getChannelNumber());
        holder.camera_name.setText(camera_name);
        holder.message_text.setText(alarmMessageList.get(position).getMessage());
        holder.time_creat.setText(alarmMessageList.get(position).getCreateTime());
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //这里使用getTag方法获取position
                address = holder.address.getText().toString();
                OnClickListener.OnItemClick(view, (Integer) view.getTag(), address);
            }
        });
    }
    private String getCameraInfo(List<EZCameraInfo> cameraInfos , String no){
        if (no!=null&&!no.equals("")){
            for (EZCameraInfo cameraInfo : cameraInfos){
                if (cameraInfo.getCameraNo() == Integer.parseInt(no)){
                    return cameraInfo.getCameraName();
                }
            }
        }
        return "Null";
    }
    @Override
    public int getItemCount() {
        return alarmMessageList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView camera_name;
        public TextView message_text;
        public TextView address;
        public TextView time_creat;
        public CheckBox checkBox;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
            camera_name = itemView.findViewById(R.id.camera_name);
            message_text = itemView.findViewById(R.id.message);
            address = itemView.findViewById(R.id.address);
            time_creat = itemView.findViewById(R.id.time_creat);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    public void setSetOnItemClickListener(OnClickListener onClickListener){
        this.OnClickListener = onClickListener;
    }



    public  interface OnClickListener{
        void OnItemClick(View view, int position, String address);
    }
}
