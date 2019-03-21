package download;

import com.google.gson.Gson;
import model.DoubanEventJsonFormat;
import model.DoubanEventParticipantJsonFormat;
import org.jsoup.Jsoup;
import setting.DoubanProxySetting;

import java.io.IOException;

/**
 * @Author LYaopei
 */
public class Downloader {
    private String eventURL;
    private String participantURL;

    public Downloader(String eventURL, String participantURL) {
        this.eventURL = eventURL;
        this.participantURL = participantURL;
    }

    public String download(){
        String result = "";
        try{
            String eventData = Jsoup
                    .connect(eventURL)
                    .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                    .proxy(DoubanProxySetting.getProxy())
                    .ignoreContentType(true)
                    .execute().body();
            DoubanEventJsonFormat event = parseEvent(eventData);

            if(event.participant_count == 0){
                return result;
            }else{
                String participantsData = Jsoup
                        .connect(participantURL).ignoreContentType(true)
                        .execute().body();
                DoubanEventParticipantJsonFormat participants = parseParticipants(participantsData);

                result = new StringBuilder(event.toString())
                        .append(participants.toString())
                        .append("\n")
                        .toString();
            }
        }catch (IOException e){
            //e.printStackTrace();
        }
       return result;
    }

    public DoubanEventJsonFormat parseEvent(String rawData){
        Gson gson = new Gson();
        DoubanEventJsonFormat event = gson
                .fromJson(rawData,DoubanEventJsonFormat.class);
        return event;
    }

    public DoubanEventParticipantJsonFormat parseParticipants(String rawData){
        Gson gson = new Gson();
        DoubanEventParticipantJsonFormat participants = gson.fromJson(rawData,
                DoubanEventParticipantJsonFormat.class);
        return participants;
    }


}
