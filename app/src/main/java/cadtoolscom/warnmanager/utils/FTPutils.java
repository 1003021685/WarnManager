package cadtoolscom.warnmanager.utils;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cadtoolscom.warnmanager.been.Constant;


public class FTPutils {
//    private String TAG = getClass().getSimpleName();
private String TAG = "GarbageActivity";
private FTPClient mFtpClient = null;
// -------------------------------------------------------初始化设置------------------------------------------------

public FTPutils() {
    mFtpClient = new FTPClient();
    Log.d("TAG","ftpConnect="+mFtpClient.isConnected());
}

public void setFtpClient(FTPClient mFtpClient) {
    this.mFtpClient = mFtpClient;
}

public void useCompressedTransfer(FTPClient mFtpClient) {
    try {
        //mFtpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.COMPRESSED_TRANSFER_MODE);
        mFtpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
        // 使用被动模式设为默认
        mFtpClient.enterLocalPassiveMode();
        // 二进制文件支持
        mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
        //设置缓存
        mFtpClient.setBufferSize(1024*2);
        //设置编码格式，防止中文乱码
        mFtpClient.setControlEncoding("UTF-8");
        //设置连接超时时间
        mFtpClient.setConnectTimeout(1000);
        //设置数据传输超时时间
        mFtpClient.setDataTimeout(10*1000);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// -------------------------------------------------------FTP登录------------------------------------------------

/**
 * 连接
 * @param ip
 * @param userName
 * @param pass
 * @return
 * @throws Exception
 */
public boolean connect(String ip, int port,String userName, String pass)  {
    boolean status = false;

    try {
        if(!mFtpClient.isConnected()){
            mFtpClient.connect(ip,port);
            Log.d(TAG, "connect: " + mFtpClient.isConnected());
            status = mFtpClient.login(userName, pass);
//                Constant.ftpLoginResult = status;
        }
        useCompressedTransfer(mFtpClient);
    } catch (IOException e) {
        e.printStackTrace();
    }

    return status;
}

// -------------------------------------------------------FTP上传文件------------------------------------------------

/**
 * 上传单个文件
 *Stream 方式
 * @param srcFileStream
 * @param name
 * @throws Exception
 */
public void uploadFile(final InputStream srcFileStream, final String name)  {
    new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                File file = new File( name );
                mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);    //图片支持二进制上传  如果采用ASCII_FILE_TYPE(默认)，虽然上传后有数据，但图片无法打开
                boolean status = mFtpClient.storeFile(name, srcFileStream);
                srcFileStream.close();
            } catch (Exception e) {
                Log.i(TAG, "run: "+e.toString());
            }
        }
    }).start();
}
/**
 * 上传单个文件 URI
 *URI 方式
 * @param uri
 * @param name
 * @throws Exception
 */
public void uploadFile(String uri, String name) throws Exception {
    try {
        File file = new File(uri);
        mFtpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        FileInputStream srcFileStream = new FileInputStream(file);
        boolean status = mFtpClient.storeFile(name, srcFileStream);
        Log.e("Status", String.valueOf(status));
        srcFileStream.close();
    } catch (Exception e) {
        throw e;
    }
}

/**
 * FTP上传文件
 * filePathName 文件路径方式
 * @param filePathName
 * @return
 */
public boolean  ftpUploadFile(String filePathName){
    boolean result = false;
    try {
        File file = new File( filePathName );
        Log.i(TAG, "doInBackground: " + file.getPath());
        String  dataDirectory = file.getName().substring(0,8);
        Log.i(TAG, "ftpUploadFile: "+dataDirectory);
        if (file.exists()) {
            FileInputStream fileInputStream = new FileInputStream(file);
             uploadDirectoryFile(fileInputStream, dataDirectory, file.getName());
            Log.i(TAG, "ftpUploadFile: Upload Successful");
            result = true;  //上传成功
        } else {
            Log.i(TAG, "ftpUploadFile: 文件路径不存在");
        }
    } catch (Exception e) {
        Log.i(TAG, "ftpUploadFile: "+"Failure : " + e.getLocalizedMessage());
    }
    return result ;
}

/**
 * 上传文件夹
 *文件流 上传后指定根目录 文件名称
 * @param srcFileStream   文件流
 * @param directoryName   ftp存储的文件夹名称
 * @param name            ftp存储的文件名称
 * @throws Exception
 */
public void uploadDirectoryFile(InputStream srcFileStream, String directoryName, String name) throws Exception {
    try {
        mFtpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
         //判断当前FTP工作目录
        String pwd = mFtpClient.printWorkingDirectory();
        if(pwd!=null){
//                if(!("/"+Constant.ftpDirectoryFile).equals(pwd)){
//                    getDirectoryExist(Constant.ftpDirectoryFile);
//                }
        }
        Log.i(TAG, "uploadDirectoryFile:当前工作目录--前- "+pwd);
        getDirectoryExist(directoryName);
         mFtpClient.storeFile(name, srcFileStream);
        String pwd2 = mFtpClient.printWorkingDirectory();
        Log.i(TAG, "uploadDirectoryFile:当前工作目录--中- "+pwd2);
        changeToParentDirectory();
        String pwd1 = mFtpClient.printWorkingDirectory();
        Log.i(TAG, "uploadDirectoryFile:当前工作目录--后- "+pwd1);
        srcFileStream.close();
    } catch (Exception e) {
        throw e;
    }
}

// -------------------------------------------------------文件下载方法------------------------------------------------

/**
 * 下载单个文件，可实现断点下载.
 *
 * @param serverPath Ftp目录及文件路径（文件夹+文件名）
 *
 * @param localPath 本地目录文件夹目录（文件夹）
 *
 * @param fileName 下载之后的文件名称（文件名）
 *
 * @param listener 监听器
 *
 * @throws IOException
 */
public void downloadSingleFile(String serverPath, String localPath, String fileName, FtpProgressListener listener) throws Exception {

    listener.onFtpProgress(Constant.FTP_CONNECT_SUCCESS, 0, null);

    mFtpClient.enterLocalPassiveMode();

    // 先判断服务器文件是否存在
    FTPFile[] files = mFtpClient.listFiles(serverPath);
    if (files!=null &&files.length == 0) {
        listener.onFtpProgress(Constant.FTP_FILE_NOTEXISTS, 0, null);
        Log.d(TAG,"ftp文件不存在");
        return;
    }

    // 创建本地文件夹
    File mkFile = new File(localPath);
    if (!mkFile.exists()) {
        boolean ismake=mkFile.mkdirs();
    }
    localPath = localPath + File.separator+fileName;
    // 接着判断下载的文件是否能断点下载
    long serverSize = files[0].getSize(); // 获取远程文件的长度
    File localFile = new File(localPath);
    long localSize = 0;
    if (localFile.exists()) {
        localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
        if (localSize >= serverSize) {
            //Log.d(TAG,"------------------------------");
            listener.onFtpProgress(Constant.LOCAL_FILE_AIREADY_COMPLETE, 0, localFile);
            localFile.delete();
            localFile.createNewFile();
            localSize=0;
        }else {
            listener.onFtpProgress(Constant.FTP_DOWN_CONTINUE, 0, null);
        }
    }else {
        localFile.createNewFile();
        //Log.d(TAG, "creatFile"+fileName+","+Thread.currentThread().getId());
    }
    // 进度
    long step = serverSize / 100;
    long process = 0;
    long currentSize = localSize;
    // 开始准备下载文件
    mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
    mFtpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
    //Log.d(TAG, "downloadSingleFile: 开始准备"+fileName+","+Thread.currentThread().getId());
    OutputStream out = new FileOutputStream(localFile, true);
    mFtpClient.setRestartOffset(localSize);
    InputStream input = mFtpClient.retrieveFileStream(serverPath);//在调用此方法后，一定要在流关闭后再调用completePendingCommand结束整个事务
    byte[] b = new byte[1024];
    int length = 0;
    while ((length = input.read(b)) !=-1) {
        //Log.d(TAG, "downloadSingleFile:正在下载"+step);
        out.write(b, 0, length);
        currentSize = currentSize + length;
        if (currentSize / step != process) {
            process = currentSize / step;
            if (process % 1 == 0) { // 每隔%1的进度返回一次
                listener.onFtpProgress(Constant.FTP_DOWN_LOADING, process, null);
            }
        }
//            if(length!=b.length){
//                Log.e(TAG,"theory_last_read"+"########"+(serverSize%b.length));
//                Log.e(TAG,"actual_last_read"+"########"+length);
//            }
    }
    out.flush();
    out.close();
    input.close();
    mFtpClient.completePendingCommand();
    // 下载完成之后关闭连接
    this.disconnect();
    listener.onFtpProgress(Constant.FTP_DISCONNECT_SUCCESS, 0, null);

    // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
    if (mFtpClient.completePendingCommand()) {
        listener.onFtpProgress(Constant.FTP_DOWN_SUCCESS, process, localFile);
        //Log.d(TAG, "下载成功"+Thread.currentThread().getId());
        return;
    } else {
        listener.onFtpProgress(Constant.FTP_DOWN_FAIL, 0, null);
        //Log.d(TAG, "下载失败"+Thread.currentThread().getId());
        return;
    }
}

    public void downloadSingleFile2(String serverPath, String localPath, String fileName, FtpProgressListener listener) throws Exception {

        listener.onFtpProgress(Constant.FTP_CONNECT_SUCCESS, 0, null);

        mFtpClient.enterLocalPassiveMode();

        // 先判断服务器文件是否存在
        FTPFile[] files = mFtpClient.listFiles(serverPath);
        if (files!=null &&files.length == 0) {
            listener.onFtpProgress(Constant.FTP_FILE_NOTEXISTS, 0, null);
            Log.d("TAG","文件不存在");
            return;
        }

        // 创建本地文件夹
        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            boolean ismake=mkFile.mkdirs();
        }
        localPath = localPath + File.separator+fileName;
        //Log.d("TAG","localpath="+localPath);
        // 接着判断下载的文件是否能断点下载
        long serverSize = files[0].getSize(); // 获取远程文件的长度
        Log.d("ManagerPlaybackActivity2","serverSize="+serverSize);
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length(); // 如果本地文件存在，获取本地文件的长度
            //Log.d("ManagerPlaybackActivity2","localSize="+localSize);
            if (localSize >= serverSize) {
                listener.onFtpProgress(Constant.LOCAL_FILE_AIREADY_COMPLETE, 0, localFile);
                //加载本地图片

            }else {
                localFile.delete();
                localFile.createNewFile();
                localSize = 0;
                listener.onFtpProgress(Constant.FTP_DOWN_CONTINUE, 0, null);
            }
        }else {
            localFile.createNewFile();
            //Log.d(TAG, "creatFile"+fileName+","+Thread.currentThread().getId());
        }
        // 进度
        long step = serverSize / 100;
        long process = 0;
        long currentSize = localSize;
        // 开始准备下载文件
        mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
        mFtpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
        //Log.d(TAG, "downloadSingleFile: 开始准备"+fileName+","+Thread.currentThread().getId());
        OutputStream out = new FileOutputStream(localFile, true);
        mFtpClient.setRestartOffset(localSize);
        InputStream input = mFtpClient.retrieveFileStream(serverPath);//在调用此方法后，一定要在流关闭后再调用completePendingCommand结束整个事务
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) !=-1) {
            //Log.d(TAG, "downloadSingleFile:正在下载"+step);
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 1 == 0) { // 每隔%1的进度返回一次
                    listener.onFtpProgress(Constant.FTP_DOWN_LOADING, process, null);
                }
            }
//            if(length!=b.length){
//                Log.e(TAG,"theory_last_read"+"########"+(serverSize%b.length));
//                Log.e(TAG,"actual_last_read"+"########"+length);
//            }
        }
        //Log.d(TAG, "downloadSingleFile: 下载完毕"+Thread.currentThread().getId());
        out.flush();
        out.close();
        input.close();
        mFtpClient.completePendingCommand();
        this.disconnect();
        listener.onFtpProgress(Constant.FTP_DISCONNECT_SUCCESS, 0, null);
        // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
        if (mFtpClient.completePendingCommand()) {
            listener.onFtpProgress(Constant.FTP_DOWN_SUCCESS, process, localFile);
            //Log.d(TAG, "下载成功"+Thread.currentThread().getId());
        } else {
            listener.onFtpProgress(Constant.FTP_DOWN_FAIL, 0, null);
            //Log.d(TAG, "下载失败"+Thread.currentThread().getId());
        }

        // 下载完成之后关闭连接

        return;
    }


// -------------------------------------------------------文件删除方法------------------------------------------------

/**
 * 删除Ftp下的文件.
 *
 * @param serverPath
 *            Ftp目录及文件路径
 * @param listener
 *            监听器
 * @throws IOException
 */
public void deleteSingleFile(String serverPath, FtpDeleteFileListener listener) throws Exception {

    listener.onFtpDelete(Constant.FTP_CONNECT_SUCCESS);

    // 先判断服务器文件是否存在
    FTPFile[] files = mFtpClient.listFiles(serverPath);
    if (files.length == 0) {
        listener.onFtpDelete(Constant.FTP_FILE_NOTEXISTS);
        return;
    }

    // 进行删除操作
    boolean flag = true;
    flag = mFtpClient.deleteFile(serverPath);
    if (flag) {
        listener.onFtpDelete(Constant.FTP_DELETEFILE_SUCCESS);
    } else {
        listener.onFtpDelete(Constant.FTP_DELETEFILE_FAIL);
    }
    listener.onFtpDelete(Constant.FTP_DISCONNECT_SUCCESS);

    return;
}
// ---------------------------------------------------上传下载进度、删除、获取文件监听---------------------------------------------

/*
 * 进度监听器
 */
public interface FtpProgressListener {
    /**
     *
     * @Description  FTP 文件长传下载进度触发
     * @param currentStatus
     *            当前FTP状态
     * @param process
     *            当前进度
     * @param targetFile
     *            目标文件
     */
    public void onFtpProgress(int currentStatus, long process, File targetFile);
}

/*
 * 文件删除监听
 */
public interface FtpDeleteFileListener {
    /**
     *
     * @Description  删除FTP文件
     * @param currentStatus
     *            当前FTP状态
     */
    public void onFtpDelete(int currentStatus);
}

/*
 * 获取文件监听
 */
public interface FtpListFileListener {
    /**
     *
     * @Description  列出FTP文件
     * @param currentStatus
     *            当前FTP状态
     * @param ftpFileList
     *            获取的List<FTPFile>
     */
    public void onFtpListFile(int currentStatus, List<FTPFile> ftpFileList);
}
// -------------------------------------------------------常用的方法------------------------------------------------

/**
 * 获取根目录下所有文件的名称
 *
 * @return
 * @throws Exception
 */
public String[] listName() throws Exception {
    try {
        return mFtpClient.listNames();
    } catch (Exception e) {
        throw e;
    }
}
/**
 * 获取根目录下所有文件的名称
 *
 * @return
 * @throws Exception
 */
public FTPFile[] listName(String file) throws Exception {
    try {
        FTPFile[] ftpFiles = mFtpClient.listFiles(file);
        return ftpFiles;
    } catch (Exception e) {
        throw e;
    }
}

/**
 * 判断FTP中是否包含上传的文件夹
 *
 * @param DirectoryName
 */
public void getDirectoryExist(String DirectoryName) {

    try {
            try {
                boolean b = makeDir(DirectoryName);
                mFtpClient.changeWorkingDirectory(DirectoryName);
            } catch (Exception e) {
                e.printStackTrace();
            }

    } catch (Exception e) {
        e.printStackTrace();
    }

}

/**
 * 获取FTP当前工作的根目录
 * @return
 */
public String getFTPfile(){
    String FTPWorkingPath= "";
    try {
        FTPWorkingPath = mFtpClient.printWorkingDirectory();
        Log.i(TAG, "getFTPfile: WorkingDirectory"+FTPWorkingPath);
    } catch (IOException e) {
        e.printStackTrace();
    }
    return FTPWorkingPath;
}

/**
 * 更改文件的存储路径
 *
 * @param directoryName
 */
public void ftputilsChangeWorkingDirectory(String directoryName) {
    try {
        mFtpClient.changeWorkingDirectory(directoryName);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 设置连接超时
 *
 * @param seconds
 * @throws Exception
 */
public void setTimeout(int seconds) throws Exception {
    try {
        mFtpClient.setConnectTimeout(seconds * 1000);
    } catch (Exception e) {
        throw e;
    }
}

/**
 * 创建文件夹
 *
 * @param dir
 * @return
 * @throws Exception
 */
public boolean makeDir(String dir) throws Exception {
    try {
        return mFtpClient.makeDirectory(dir);
    } catch (Exception e) {
        throw e;
    }
}

/**
 * 更改至父目录下
 */
public void changeToParentDirectory() {
    try {
        mFtpClient.changeToParentDirectory();
    } catch (IOException e) {

    }
}

/**
 * 断开连接
 */
public void disconnect() {
    try {
        if(mFtpClient!=null&&mFtpClient.isConnected()){
            mFtpClient.logout();//退出
            mFtpClient.disconnect();//断开
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * 判断是否登录
 * @return
 */
public boolean isConnected(){

    boolean connected =false;
    if(mFtpClient!=null){
        mFtpClient.isConnected();
    }

    return connected;
}
}


