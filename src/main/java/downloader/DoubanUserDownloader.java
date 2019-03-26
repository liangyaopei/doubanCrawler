package downloader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import parser.DoubanJsonparser;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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

            String participatedEvenURL = getParticipantsEventUrl();
            String wisherEventURL = getWisherEventUrl();

            String jsonData = downloadJsonWithProxy(participatedEvenURL);
            Set<Integer> events = DoubanJsonparser
                    .getEventIdThroughParticipantsJson(jsonData);
            eventQueue.addAll(events);

            String wisherJsonData = downloadJsonWithProxy(wisherEventURL);
            Set<Integer> wisherEvents = DoubanJsonparser
                    .getEventIdThroughParticipantsJson(wisherJsonData);
            eventQueue.addAll(wisherEvents);
            Gson gson = new Gson();
            JsonObject participantsObject = gson.fromJson(jsonData,JsonObject.class);
            JsonObject wishesObject = gson.fromJson(wisherJsonData,JsonObject.class);
            participantsObject.add("wishEvent",wishesObject);

            builder.append(participantsObject)
                    .append("\n");

        }catch (IOException | NumberFormatException e){
            e.printStackTrace();
            builder = new StringBuilder();
        }

        return builder.toString();
    }

    public String getParticipantsEventUrl(){
        String url = "https://api.douban.com/v2/event/user_participated/%d?start=0&count=100";
        //String url = "https://api.douban.com/v2/event/user_participated/%d";
        return String.format(url,identity);
    }

    public String getWisherEventUrl(){
        String url = "https://api.douban.com/v2/event/user_wished/%d?start=0&count=100";
        return String.format(url,identity);
    }
}
