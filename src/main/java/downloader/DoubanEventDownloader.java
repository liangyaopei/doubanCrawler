package downloader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
            // old version of visited user set
            Set<Integer> visitedUserSet = visitedUser.keySet();

            String eventURL = getEventUrl();
            int start,count = 100;


            Set<Integer> participantsSet = new HashSet<>();
            start = 0;
            boolean more ;
            do{
                String participantURL = getParticipantsUrl(start);
                start +=count;
                String participantJsonData = downloadJsonWithProxy(participantURL);
                 more = DoubanJsonparser
                        .getParticipantsIdThroughEventJson(participantJsonData,participantsSet,start);

            }while (more == true);
            // remove visited Set
            participantsSet.removeAll(visitedUserSet);
            userQueue.addAll(participantsSet);


            start = 0;
            Set<Integer> wishersSet = new HashSet<>();
            do{
                String wisherURL = getWishersUrl(start);
                start += count;

                String wisherJsonData = downloadJsonWithProxy(wisherURL);
                more = DoubanJsonparser
                        .getParticipantsIdThroughEventJson(wisherJsonData,wishersSet,start);

            }while (more == true);
            wishersSet.removeAll(visitedUserSet);
            userQueue.addAll(wishersSet);



            String eventJsonData = downloadJsonWithProxy(eventURL);
            Gson gson = new Gson();
            JsonObject eventObject  = gson.fromJson(eventJsonData, JsonObject.class);
            JsonElement idElement = gson.toJsonTree(identity,
                    new TypeToken<Integer>(){}.getType());
            eventObject.add("eventId",idElement);

            List<Integer> userList = participantsSet.stream().collect(Collectors.toList());
            JsonElement userElement = gson.toJsonTree(userList,
                    new TypeToken<List<Integer>>(){}.getType());
            eventObject.add("participants",userElement);

            List<Integer> wishersList = wishersSet.stream().collect(Collectors.toList());
            JsonElement wishersElement = gson.toJsonTree(wishersList,
                    new TypeToken<List<Integer>>(){}.getType());
            eventObject.add("wishers",wishersElement);

            eventObject.remove("image");
            eventObject.remove("owner");
            eventObject.remove("alt");

           // System.out.println("wisher:"+wishersList.size());
            /*
            JsonObject participantsObject = gson.fromJson(participantJsonData,
                    JsonObject.class);
            JsonObject wishers = gson.fromJson(wisherJsonData,JsonObject.class);
            eventObject.add("participants",participantsObject);
            eventObject.add("wishers",wishers);
            */
            builder.append(eventObject)
                    .append("\n");
            /*
            builder.append(eventJsonData)
                    .append("\n")
                    .append(participantJsonData)
                    .append("\n");
            */

        }catch (IOException | NumberFormatException e){
            StringBuilder exceptionMsg = new StringBuilder();
            exceptionMsg
                    .append("event:")
                    .append(identity)
                    .append("\n")
                    .append(e.getClass().getName())
                    .append("\n");
            System.err.println(exceptionMsg);

            //Make result empty
            builder = new StringBuilder().append(exceptionMsg);
        }
        return builder.toString();
    }

    public String getEventUrl(){
        String url = "https://api.douban.com/v2/event/%d";
        return String.format(url, identity);
    }

    public String getParticipantsUrl(int start){
        String url = "https://api.douban.com/v2/event/%d/participants?start=%d&count=100";
        //String url = "https://api.douban.com/v2/event/%d/participants";
        return String.format(url,identity,start);
    }

    public String getWishersUrl(int start){
        String url = "https://api.douban.com/v2/event/%d/wishers?start=%d&count=100";
        //String url = "https://api.douban.com/v2/event/%d/wishers";
        return String.format(url,identity,start);
    }
}
