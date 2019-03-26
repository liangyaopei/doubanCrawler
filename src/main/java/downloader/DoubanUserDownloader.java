package downloader;

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
     * return eventID
     * @return
     */

    @Override
    public String download() {

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

        }catch (IOException | NumberFormatException e){
            e.printStackTrace();
        }

        return "";
    }

    public String getParticipantsEventUrl(){
        String url = "https://api.douban.com/v2/event/user_participated/%d?start=0&count=100";
        //String url = "https://api.douban.com/v2/event/user_participated/%d";
        return String.format(url,identity);
    }

    public String getWisherEventUrl(){
        String url = "https://api.douban.com/v2/event/user_wished/%d";
        return String.format(url,identity);
    }
}
