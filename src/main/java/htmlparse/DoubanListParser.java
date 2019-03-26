package htmlparse;

import model.EventInformation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import setting.DoubanProxySetting;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author LYaopei
 */
public class DoubanListParser {
    private String baseURL;
    private boolean getDetails;
    private boolean getParticipants;
    private int sleepInterval = 200;

    public DoubanListParser(String baseURL, boolean parseDetails, boolean parseParticipants) {
        this.baseURL = baseURL;
        this.getDetails = parseDetails;
        this.getParticipants = parseParticipants;
    }

    public DoubanListParser(String baseURL) {
        this.baseURL = baseURL;
        this.getDetails = false;
        this.getParticipants = false;
    }


    /** Eg. https://www.douban.com/location/shenzhen/events/future-all?start=
     * The web page is a list of events, we can extract information about event
     * from the list
     * @return
     * @throws IOException
     */
    public Set<EventInformation> parse() throws  InterruptedException {
        Set<EventInformation> res = new HashSet<>();

        try{
            Document document = crawlContent(baseURL);
            Elements events = document
                    .select("ul.events-list.events-list-pic100.events-list-psmall")
                    .select("li.list-entry");
            for(Element element:events){
                EventInformation event = new EventInformation();
                String detailsURL = parseDetailsURL(element);
                String id = parseId(detailsURL);
                event.setId(id);
                event.setEventURL(detailsURL);

                String title = parseTitle(element);
                event.setTitle(title);

                String location = parseLocation(element);
                event.setLocation(location);


                if(getDetails == true){
                    Thread.sleep(sleepInterval);
                    String details = parseDetails(detailsURL);
                    //  System.out.println("detailURL:"+detailsURL);

                    //System.out.println("detail:"+details);

                    event.setDetails(details);
                }

                if(getParticipants == true){
                    Thread.sleep(sleepInterval);
                    String participantURL = detailsURL +"participant";
                    String participants = parseParticipant(participantURL);
                    event.setParticipants(participants);
                }

                res.add(event);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
       // Thread.sleep(sleepInterval);
        return res;
    }

    /**
     * download Content
     * @param url
     * @return
     * @throws IOException
     */
    private Document crawlContent(String url)throws IOException{
        return Jsoup.connect(url)
                .timeout(3000)
                .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                .proxy(DoubanProxySetting.getProxy())
                .get();
    }

    private String parseTitle(Element element){
        return element
                .select("[itemprop=summary]").text();
    }

    private String parseLocation(Element element){
        return element
                .select("[itemprop=location]")
                .attr("content");
    }

    private String parseDetailsURL(Element element){
        return element.select("[itemprop=url]")
                .attr("href");
    }

    private String parseId(String detailsURL){
        String id = detailsURL
                .substring(0,detailsURL.length()-1);
        id = id.substring(id.lastIndexOf("/")+1);
        return id;
    }

    /**
     * Note that it needs to use cookies
     * @param detailsURL
     * @return Details of an Event
     * @throws IOException
     */
    private String parseDetails(String detailsURL) throws IOException {
        Document detailsDoc = crawlContent(detailsURL);

        String details = detailsDoc
                .select("[id=edesc_s]")
                .text();
        return details;
    }

    /**
     * Note that it needs to use cookies
     * @param participantURL
     * @return participants of an event
     * @throws IOException
     */
    private String parseParticipant(String participantURL) throws IOException {
        Document participantDoc = crawlContent(participantURL);

        String participants = participantDoc
                .select("[class=obu]")
                .select("dd a").text();
        return participants;
    }

    private String parseStartTime(Element element){
        return element
                .select("[itemprop=startDate]")
                .attr("datetime");
    }

    private String parseEndTime(Element element){
        return element
                .select("[itemprop=endDate]")
                .attr("datetime");
    }
}
