package parseTest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import setting.DoubanProxySetting;

import java.io.IOException;

public class SinglePageParserTest {

    @Before
    public void init(){
        /**
         *  To enable roxy tunnel
         */
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    @Test
    public  void detailsTest() {
        String url = "https://www.douban.com/event/31826370/";
        try{
            Document doc = Jsoup.connect(url)
                    .timeout(3000)
                    .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                    .proxy(DoubanProxySetting.getProxy())
                    .get();
            Elements elements = doc.select("[id=edesc_s]");
            System.out.println(elements.text());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
