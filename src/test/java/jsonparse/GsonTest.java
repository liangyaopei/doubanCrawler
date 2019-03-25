package jsonparse;

import com.google.gson.*;
import model.DoubanEventJsonFormat;
import model.DoubanEventParticipantJsonFormat;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author LYaopei
 */
public class GsonTest {
   // String eventURL = "https://api.douban.com/v2/event/31754598";


    public String getData(String url) throws IOException{
        String data = Jsoup
                .connect(url).ignoreContentType(true)
                .execute().body();
        return data;
    }

    @Test
    public void eventFormatTest() throws IOException {
        String eventURL = "https://api.douban.com/v2/event/31754598";

        String data = getData(eventURL);
        Gson gson = new Gson();
        DoubanEventJsonFormat event = gson.fromJson(data,DoubanEventJsonFormat.class);
        System.out.println(event);

    }

    @Test
    public void participantFormatTest() throws IOException{
        String participantsURL = "https://api.douban.com/v2/event/31754598"+"/participants";
        String data = getData(participantsURL);
        Gson gson = new Gson();
        DoubanEventParticipantJsonFormat participants = gson.fromJson(data,
                DoubanEventParticipantJsonFormat.class);
        System.out.println(participants);
    }

    @Test
    public void checkKeyValue() throws IOException{
      //  String participantsURL = "https://api.douban.com/v2/event/31754598"+"/participants";
       // String url = "https://api.douban.com/v2/event/list?loc=118282&districts=all&day=future&type=all&count=100";
        //String url = "https://api.douban.com/v2/event/user_participated/40524069";
        String url = "https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100";

        String data = getData(url);
        GsonBuilder builder = new GsonBuilder();
        Map<String,Object> map =builder.create().fromJson(data, HashMap.class);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("key:"+pair.getKey());
            System.out.println("value:"+pair.getValue());
        }
    }


    @Test
    public void parse()throws IOException {
       // String url = "https://api.douban.com/v2/event/list?loc=118282&day_type=week&type=all&start=800&count=20";
       // String url = "https://api.douban.com/v2/event/list?loc=118282";
        String url = "https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100";

        String data = getData(url);

        JsonElement jelement = new JsonParser().parse(data);
        JsonObject  jobject = jelement.getAsJsonObject();
       // jobject = jobject.getAsJsonObject("data");
        JsonArray jarray = jobject.getAsJsonArray("events");
        int count=1;
        for(JsonElement element:jarray){
            JsonObject object =element.getAsJsonObject();

            System.out.println("count:"+(count++)+",title:"+object.get("title"));
            System.out.println("adapt_url:"+object.get("adapt_url"));
            System.out.println("time:"+object.get("time_str"));
        }
     //   jobject = jarray.get(0).getAsJsonObject();
      //  String result = jobject.get("translatedText").getAsString();
       // return result;
    }

}
