package crawl;

import org.jsoup.Jsoup;
import setting.DoubanProxySetting;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @Author LYaopei
 */
public class Crawler implements Callable<String> {
    private String eventURL;
    private String participantsURL;
    private List<String> urlList;

    public Crawler(String eventURL, String participantsURL) {
        this.eventURL = eventURL;
        this.participantsURL = participantsURL;
    }

    public Crawler(List<String> urlList) {
        this.urlList = urlList;
    }

    @Override
    public String call()  {
        String result = "";
        try{
            String eventData =crawl(eventURL);

            String participantsData = crawl(participantsURL);

            StringBuilder builder = new StringBuilder(eventData)
                    .append("\n")
                    .append(participantsData);
            result = builder.toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public String crawl(String url) throws IOException{
        String data = Jsoup.connect(url)
                .timeout(3000)
                .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                .proxy(DoubanProxySetting.getProxy())
                .ignoreContentType(true)
                .execute().body();
        return data;
    }
}
