package crawl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;

/**
 * Get events id through participants
 * url = "https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100";
 * @Author LYaopei
 */
public class ParticipantsEventsCrawlwer extends CrawlAction{

    public ParticipantsEventsCrawlwer(List<String> urlList) {
        super(urlList);
    }

    @Override
    public String action() {
        StringBuilder result = new StringBuilder();
        try{
            for(String url:urlList){
                String rawData = crawl(url);
                String data = parseURL(rawData);

                if(data!=null && !data.isBlank()){
                    result.append(data);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private String parseURL(String data){
        StringBuilder builder = new StringBuilder();

        JsonElement jsonElement = new JsonParser().parse(data);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("events");
        for(JsonElement element:jsonArray){
            JsonObject object =element.getAsJsonObject();
            String url = object.get("adapt_url").getAsString();
            if(url!=null){
                builder.append(url)
                        .append("\n");
            }
        }
        return builder.toString();
    }
}
