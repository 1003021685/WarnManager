package cadtoolscom.warnmanager.been;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String CREATE_PIC_PATH ="create table picfilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    private static final String CREATE_VIDEO_PATH ="create table videofilepath("+"id integer primary key autoincrement,"+"path text,"+"name text)";
    //用户名，密码，用户类型
    private static final String USER_DATA ="create table userData("+"id integer primary key autoincrement,"+"name text,"+"password text,"+"type integer)";
    //设备序列号，验证码
    private static final String CREATE_VERIFY_CODE = "create table verifycode("+"id integer primary key autoincrement,"+"name text,"+"code text)";
    //报警信息
    private static final String CREAT_ALARM_MESSAGE_6="create table alarmMessage6("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_MESSAGE_7="create table alarmMessage7("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_MESSAGE_8="create table alarmMessage8("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_MESSAGE_9="create table alarmMessage9("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_MESSAGE_10="create table alarmMessage10("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_MESSAGE_11="create table alarmMessage11("+"id integer primary key autoincrement,"+"message text,"+"type text,"+"latitude text,"
            +"longitude text,"+"altitude text,"+"address text,"+"imgPath text,"+"videoPath text,"+"createTime text,"+"startTime text,"+"endTime text,"+"channelNumber text)";
    private static final String CREAT_ALARM_READED="create table alarmReaded("+"id integer primary key autoincrement,"+"type0 text,"+"type1 text,"+"type2 text,"+"type3 text,"+"type4 text,"+"type5 text,"+"type6 text)";
    private static final String CREAT_ALARM_SIZE="create table alarmSize("+"id integer primary key autoincrement,"+"size0 text,"+"size1 text,"+"size2 text,"+"size3 text,"+"size4 text,"+"size5 text,"+"size6 text)";
    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PIC_PATH);
        db.execSQL(CREATE_VIDEO_PATH);
        db.execSQL(USER_DATA);
        db.execSQL(CREATE_VERIFY_CODE);
        db.execSQL(CREAT_ALARM_MESSAGE_6);
        db.execSQL(CREAT_ALARM_MESSAGE_7);
        db.execSQL(CREAT_ALARM_MESSAGE_8);
        db.execSQL(CREAT_ALARM_MESSAGE_9);
        db.execSQL(CREAT_ALARM_MESSAGE_10);
        db.execSQL(CREAT_ALARM_MESSAGE_11);
        db.execSQL(CREAT_ALARM_READED);
        db.execSQL(CREAT_ALARM_SIZE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists picfilepath");
        db.execSQL("drop table if exists videofilepath");
        db.execSQL("drop table if exists userData");
        db.execSQL("drop table if exists verifycode");
        db.execSQL("drop table if exists alarmMessage6");
        db.execSQL("drop table if exists alarmMessage7");
        db.execSQL("drop table if exists alarmMessage8");
        db.execSQL("drop table if exists alarmMessage9");
        db.execSQL("drop table if exists alarmMessage10");
        db.execSQL("drop table if exists alarmMessage11");
        db.execSQL("drop table if exists alarmReaded");
        db.execSQL("drop table if exists alarmSize");
        onCreate(db);
    }
}
