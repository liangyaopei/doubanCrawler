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
        String participantsURL = "https://api.douban.com/v2/event/31754598"+"/participants";
        String data = getData(participantsURL);
        GsonBuilder builder = new GsonBuilder();
        Map<String,Object> map =builder.create().fromJson(data, HashMap.class);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("key:"+pair.getKey());
            System.out.println("value:"+pair.getValue());
        }
    }


    public String parse(String jsonLine) {

        JsonElement jelement = new JsonParser().parse(jsonLine);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("data");
        JsonArray jarray = jobject.getAsJsonArray("translations");
        jobject = jarray.get(0).getAsJsonObject();
        String result = jobject.get("translatedText").getAsString();
        return result;
    }

}
