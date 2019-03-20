package parseTest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        String url =
                "https://www.douban.com/location/shenzhen/events/future-all?start=10";
        try{
            Document doc = Jsoup.connect(url).get();
            String[] lasts = doc
                    .select("[class=paginator]")
                    .text()
                    .split("\\s+");
            String last = lasts[lasts.length-2];

                   // .attr("a[href]:last");
                   // .text();
            System.out.println("last:"+last);
            Elements links = doc
                    .select("ul.events-list.events-list-pic100.events-list-psmall")
                    .select("li.list-entry");
            links = null;
            for(Element element:links){
                /*
                Elements details = element
                        .select("[class=event-time]");
                        //.select("ul.event-meta")
                        //.select("li.title");
                Elements title = element
                        .select("[itemprop=summary]");
                System.out.println("title:"+title.text());
                String startTime = element
                        .select("[itemprop=startDate]")
                        .attr("datetime");
                String endTime = element
                        .select("[itemprop=endDate]")
                        .attr("datetime");
                System.out.println("time:"+startTime+" to "+endTime);

                String location = element
                        .select("[itemprop=location]")
                        .attr("content");
                System.out.println("location:"+location);
                   */
                String detailURL = element
                        .select("[itemprop=url]")
                        .attr("href");
                String id = detailURL.substring(0,detailURL.length()-1);
                id = id.substring(id.lastIndexOf("/")+1);
                System.out.println("id:"+id);

                String participantsURL = detailURL+"participant";

                Document externalDoc = Jsoup.connect(participantsURL).get();

                String extraLinks = externalDoc
                      //  .select("div.obss.name.indent")
                        .select("[class=obu]")
                        .select("dd a").text();
                System.out.println(extraLinks);

                        //.select()
                /*
                for(Element extra:extraLinks){
                    String other = extra
                            .select("[class=obu]")
                            .select("dd a").text();

                    System.out.println(other);
                }
                */

             //   System.out.println("URL:"+participantsURL);
            }
           // System.out.println(links);
           // System.out.println(links.eachText());
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
