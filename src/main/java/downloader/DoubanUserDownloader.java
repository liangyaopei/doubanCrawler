package downloader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import parser.DoubanJsonparser;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class DoubanUserDownloader extends AbstractDownloader {


    /**
     * example url:example url: https://api.douban.com/v2/event/user_participated/3956041?start=0&count=100
     *                          https://api.douban.com/v2/event/user_wished/40524069
     * @param identity
     * @param eventQueue
     * @param userQueue
     * @param visitedEvent
     * @param visitedUser
     */
    public DoubanUserDownloader(Integer identity,
                                ConcurrentLinkedQueue<Integer> eventQueue,
                                ConcurrentLinkedQueue<Integer> userQueue,
                                ConcurrentHashMap<Integer, Boolean> visitedEvent,
                                ConcurrentHashMap<Integer, Boolean> visitedUser) {
        super(identity, eventQueue, userQueue, visitedEvent, visitedUser);
    }



    /**
     * To get event ids, and add ids into eventQueue
     * return eventID
     * @return
     */
    @Override
    public String download() {
        StringBuilder builder = new StringBuilder();

        try{
            visitedUser.put(identity,true);

            int start,count =100;
            boolean more;
            start = 0;
            Set<Integer> userEventSet = new HashSet<>();
            do{
                String participatedEventURL = getParticipantsEventUrl(start);
                start += count;
                String eventJsonData = downloadJsonWithProxy(participatedEventURL);
                more = DoubanJsonparser
                        .getEventIdThroughParticipantsJson(eventJsonData,userEventSet,start);
            }while (more == true);
            eventQueue.addAll(userEventSet);

            start = 0;
            Set<Integer> wishersEventSet = new HashSet<>();
            do{
                String wishersURL = getWisherEventUrl(start);
                start += count;
                String eventJsonData = downloadJsonWithProxy(wishersURL);
                more = DoubanJsonparser
                        .getEventIdThroughParticipantsJson(eventJsonData,wishersEventSet,start);
            }while (more == true);
            eventQueue.addAll(wishersEventSet);


            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId",identity);
            List<Integer> userEventList = userEventSet.stream().collect(Collectors.toList());
            JsonElement userEventListElement = gson.toJsonTree(userEventList,
                    new TypeToken<List<Integer>>(){}.getType());
            jsonObject.add("userEvents",userEventListElement);

            List<Integer> userWishList = wishersEventSet.stream().collect(Collectors.toList());
            JsonElement userWishListElement = gson.toJsonTree(userWishList,
                    new TypeToken<List<Integer>>(){}.getType());
            jsonObject.add("wishEvents",userWishListElement);

         //   JsonObject participantsObject = gson.fromJson(jsonData,JsonObject.class);
         //   JsonObject wishesObject = gson.fromJson(wisherJsonData,JsonObject.class);
         //   participantsObject.add("wishEvent",wishesObject);

            builder.append(jsonObject)
                    .append("\n");

        }catch (IOException | NumberFormatException e){
            e.printStackTrace();
            System.err.println("user:"+identity);
            builder = new StringBuilder();
        }

        return builder.toString();
    }

    public String getParticipantsEventUrl(int start){
        String url = "https://api.douban.com/v2/event/user_participated/%d?start=%d&count=100";
        //String url = "https://api.douban.com/v2/event/user_participated/%d";
        return String.format(url,identity,start);
    }

    public String getWisherEventUrl(int start){
        String url = "https://api.douban.com/v2/event/user_wished/%d?start=%d&count=100";
        //String url = "https://api.douban.com/v2/event/user_wished/%d";
        return String.format(url,identity,start);
    }
}
