package cadtoolscom.warnmanager.been;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2020/3/14.
 */

public class Contacts {
    public static final int MESSAGE_TYPE_TRUCK_IDENTITY = 0 ;
    public static final int MESSAGE_TYPE_ILLEGAL_BUILDING= 1 ;
    public static final int MESSAGE_TYPE_ILLEGAL_PLANT= 2 ;
    public static final int MESSAGE_TYPE_STRAW_BURNING= 3 ;
    public static final int MESSAGE_TYPE_RIVER_MONITOR= 4 ;
    public static final int MESSAGE_TYPE_COMPANY_MANAGE= 5 ;
    public static final String DEVICE_SERIAL_NUM = "D85325086";
    public static final String location_url ="http://api.map.baidu.com/reverse_geocoding/v3/";
    public static final String AppKey = "bec27f333fd04a95a352bec49d466754";
    public static final String Secret = "d55a02b00c894303eedf279419d2bd94";
    public static final String service_url = "http://183.208.120.226:18080/";//西城大厦
    public static final String short_str="_1080";
    public static List<String> list_super = new ArrayList<>();
    public static String[] typeString = new String[]{"违法乱建","违章种植","垃圾倾倒","漂浮物","渣土车","火情预警"};

    public static final List<String> getList_super(){
        list_super.clear();
        list_super.add(typeString[0]);
        list_super.add(typeString[1]);
        list_super.add(typeString[2]);
        list_super.add(typeString[3]);
        list_super.add(typeString[4]);
        list_super.add(typeString[5]);
        return list_super;
    }
}
