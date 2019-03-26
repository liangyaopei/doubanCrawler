package parser;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author LYaopei
 */
public class DoubanJsonparser {

    private static void getDetailsFromJson(String jsonData,
                                           String arrayMemberName,String attribute,
                                           Set<Integer> result)
            throws NumberFormatException{
        JsonElement jsonElement = new JsonParser().parse(jsonData);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray(arrayMemberName);
        for(JsonElement element:jsonArray){
            JsonObject object = element.getAsJsonObject();
            int id = object.get(attribute).getAsInt();
            result.add(id);
        }
    }

    /**
     * example url: https://api.douban.com/v2/event/31920676/participants
     *              https://api.douban.com/v2/event/31920676/wishers
     * @param jsonData
     * @return the participants Id of event
     */
    public static Set<Integer> getParticipantsIdThroughEventJson(String jsonData) {
        Set<Integer> result = new HashSet<>();
        getDetailsFromJson(jsonData,"users","id",result);
        return result;
    }


    /**
     * example url: https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100
     *              https://api.douban.com/v2/event/user_wished/40524069
     * @param jsonData
     * @return
     */
    public static Set<Integer> getEventIdThroughParticipantsJson(String jsonData){
        Set<Integer> result = new HashSet<>();
        getDetailsFromJson(jsonData,"events","id",result);
        return result;
    }
}
