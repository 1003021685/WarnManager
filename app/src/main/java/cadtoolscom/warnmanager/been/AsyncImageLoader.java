package cadtoolscom.warnmanager.been;

import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import cadtoolscom.warnmanager.utils.FTPutils;

public class AsyncImageLoader {
    private ExecutorService cachedThreadPool;
    private String TAG = "GarbageActivity";

    public AsyncImageLoader(ExecutorService cachedThreadPool) {
        this.cachedThreadPool = cachedThreadPool;
    }

    public Drawable loadDrawable(final HashMap<String,String> map,ImageCallback imageCallback ) {
//        String imageUrl = "ftp://wh:wanghao@192.168.60.81/uftp/78b8998fe074fcfc708f8d91d93678aa.jpg";
        //HashMap<String,String> map = DataUtils.getUrlResouse(imageUrl);
        //HashMap<String,String> map = DataUtils.getUrlResouses(imageUrl).get(0);
        String ip = map.get("ip");
        String port = map.get("port");
        String name = map.get("name");
        String password = map.get("password");
        String pic_name = map.get("pic_name");
        String server_name = map.get("server_name");
        //Log.d(TAG,"server_name: "+server_name);
        Handler handler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what){
                    case 0:
                        imageCallback.imageLoaded();
                        break;
                    case 1:
                        imageCallback.imageLoadEmpty();
                        break;
                }
            }
        };
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                FTPutils ftPutils = new FTPutils();
                String localpath = Environment.getExternalStorageDirectory().toString()+"/warnmanager/cash";
                Boolean flag = ftPutils.connect(ip,Integer.parseInt(port),name,password);
                //Log.d(TAG,"flag="+flag);
                if (flag){
                    try {
                        ftPutils.downloadSingleFile(server_name, localpath, pic_name, new FTPutils.FtpProgressListener() {
                                    @Override
                                    public void onFtpProgress(int currentStatus, long process, File targetFile) {
                                        if (currentStatus == Constant.FTP_FILE_NOTEXISTS){
                                            Message message = Message.obtain();
                                            message.what = 1;
                                            handler.sendMessage(message);
                                        }else {
                                            if (process == 100){
                                                Message message = Message.obtain();
                                                message.what = 0;
                                                handler.sendMessage(message);
                                            }
                                        }
                                    }

                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Message message = Message.obtain();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        };
        cachedThreadPool.execute(runnable);
        return null;
    }

    public interface ImageCallback {
        public void imageLoaded();
        public void imageLoadEmpty();
    }
}
