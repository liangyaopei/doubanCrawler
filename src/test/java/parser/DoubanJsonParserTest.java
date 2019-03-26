package parser;

import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.IOException;

/**
 * @Author LYaopei
 */
public class DoubanJsonParserTest {
    public String getJson(String url) throws IOException {
        String data = Jsoup
                .connect(url).ignoreContentType(true)
                .execute().body();
        return data;
    }

    @Test
    public void getParticipantsIdThroughEventJsonTest() throws IOException{
       String url = "https://api.douban.com/v2/event/31920676/participants";
        //String url = "https://api.douban.com/v2/event/31920676/wishers ";
        String jsonData = getJson(url);
        DoubanJsonparser
                .getParticipantsIdThroughEventJson(jsonData)
                .forEach(System.out::println);
    }

    @Test
    public void getEventIdThroughParticipantsJsonTest()throws IOException{
       // String url = "https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100";
        String url =" https://api.douban.com/v2/event/user_wished/3956041";


        String jsonData = getJson(url);
        DoubanJsonparser
                .getEventIdThroughParticipantsJson(jsonData)
                .forEach(System.out::println);
    }
}
