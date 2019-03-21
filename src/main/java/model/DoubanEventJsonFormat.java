package model;

import java.util.List;

/**
 * id,url,title,time,location,address,content
 * @Author LYaopei
 */
public class DoubanEventJsonFormat {
    public String id;
    public String title;
    public String time_str;
    public String content;
    public String loc_name;
    public String address;
    public String url;
    public String begin_time;
    public String geo;
    public int participant_count;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("id:"+id)
                .append("\n")
                .append("url:"+url)
                .append("\n")
                .append("title:"+title)
                .append("\n")
                .append("time:"+time_str)
                .append("\n")
                .append("begin_time:"+begin_time)
                .append("\n")
                .append("loc_name:"+loc_name)
                .append("\n")
                .append("address:"+address)
                .append("\n")
                .append("geo:"+geo)
                .append("\n")
                .append("content:"+content)
                .append("\n")
                .append("participant_count:"+participant_count)
                .append("\n");
        return builder.toString();
    }
}
