package htmlparse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class DoubanEventDateGenerator {




    public static List<String> findDates(String start, String end,int interval) {
        List<Date> lDate = new ArrayList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date dBegin = sdf.parse(start);
            Date dEnd = sdf.parse(end);
            lDate.add(dBegin);
            Calendar calBegin = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calBegin.setTime(dBegin);
            Calendar calEnd = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calEnd.setTime(dEnd);
            // 测试此日期是否在指定日期之后
            while (dEnd.after(calBegin.getTime())) {
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
                calBegin.add(Calendar.DAY_OF_MONTH, interval);
                lDate.add(calBegin.getTime());
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        return lDate.stream()
                .map(date -> sdf.format(date).replace("-",""))
                .collect(Collectors.toList());
    }
}
