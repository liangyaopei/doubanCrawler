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
public class DoubanEventDownloader extends AbstractDownloader {

    /**
     * event information:  https://api.douban.com/v2/event/31993290
     * event participants: https://api.douban.com/v2/event/31993290/participants
     * event wishers:      https://api.douban.com/v2/event/31993290/wishers
     *
     * @param identity
     * @param eventQueue
     * @param userQueue
     * @param visitedEvent
     * @param visitedUser
     */
    public DoubanEventDownloader(Integer identity,
                                 ConcurrentLinkedQueue<Integer> eventQueue,
                                 ConcurrentLinkedQueue<Integer> userQueue,
                                 ConcurrentHashMap<Integer, Boolean> visitedEvent,
                                 ConcurrentHashMap<Integer, Boolean> visitedUser) {
        super(identity, eventQueue, userQueue, visitedEvent, visitedUser);
    }



    /**
     *
     * @return event information + "\n" + event participants in json format
     * if any of the three request fails, then the result is empty string
     * if result is empty, means that it failed to fetch content
     */
    @Override
    public String download() {
        StringBuilder builder = new StringBuilder();
        try{
            visitedEvent.put(identity,true);

            String eventURL = getEventUrl();
            String participantURL = getParticipantsUrl();
            String wisherURL = getWishersUrl();

            String participantJsonData = downloadJsonWithProxy(participantURL);
            Set<Integer> participantsData = DoubanJsonparser
                    .getParticipantsIdThroughEventJson(participantJsonData);
            userQueue.addAll(participantsData);

            String wisherJsonData = downloadJsonWithProxy(wisherURL);
            Set<Integer> wishersData = DoubanJsonparser
                    .getParticipantsIdThroughEventJson(wisherJsonData);
            userQueue.addAll(wishersData);

            String eventJsonData = downloadJsonWithProxy(eventURL);
            Gson gson = new Gson();
            JsonObject eventObject  = gson.fromJson(eventJsonData, JsonObject.class);
            JsonObject participantsObject = gson.fromJson(participantJsonData,
                    JsonObject.class);
            JsonObject wishers = gson.fromJson(wisherJsonData,JsonObject.class);
            eventObject.add("participants",participantsObject);
            eventObject.add("wishers",wishers);
            builder.append(eventObject)
                    .append("\n");
            /*
            builder.append(eventJsonData)
                    .append("\n")
                    .append(participantJsonData)
                    .append("\n");
            */

        }catch (IOException | NumberFormatException e){
            e.printStackTrace();
            builder = new StringBuilder();
        }
        return builder.toString();
    }

    public String getEventUrl(){
        String url = "https://api.douban.com/v2/event/%d";
        return String.format(url, identity);
    }

    public String getParticipantsUrl(){
        String url = "https://api.douban.com/v2/event/%d/participants?start=0&count=100";
        return String.format(url,identity);
    }

    public String getWishersUrl(){
        String url = "https://api.douban.com/v2/event/%d/wishers?start=0&count=100";
        return String.format(url,identity);
    }
}
