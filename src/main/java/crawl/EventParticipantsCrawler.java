package crawl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * I knows event id, get details and event participants
 * @Author LYaopei
 */
public class EventParticipantsCrawler extends CrawlAction implements Callable<String> {

    public EventParticipantsCrawler(List<String> urlList) {
        super(urlList);
    }

    @Override
    public String call() throws Exception {
        return action();
    }

    @Override
    public String action() {
        StringBuilder builder = new StringBuilder();
        for(String url:urlList){
            try{
                String rawData = crawl(url);
                builder.append(rawData)
                        .append("\n");
            }catch (IOException e){
                e.printStackTrace();
                break;
            }
        }
        return builder.toString();
    }
}
