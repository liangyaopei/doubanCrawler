package parser;

import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    public void getDetailsFromJsonTest() throws IOException{
        String url = "https://api.douban.com/v2/event/31317328/participants";
        //String url = "https://api.douban.com/v2/event/31920676/wishers ";
        String jsonData = getJson(url);
        int current = 100;
        System.out.println(DoubanJsonparser.getDetailsFromJson(jsonData,
                "users","id",new HashSet<>(),current));

    }
    @Test
    public void getParticipantsIdThroughEventJsonTest() throws IOException{
       String url = "https://api.douban.com/v2/event/31317328/participants";
        //String url = "https://api.douban.com/v2/event/31920676/wishers ";
        String jsonData = getJson(url);
        Set<Integer> set = new HashSet<>();
        int start =0;
        DoubanJsonparser
                .getParticipantsIdThroughEventJson(jsonData,set,start);
    }

    @Test
    public void getEventIdThroughParticipantsJsonTest()throws IOException{
       // String url = "https://api.douban.com/v2/event/user_participated/3956041?start=100&count=100";
        String url =" https://api.douban.com/v2/event/user_wished/3956041";


        String jsonData = getJson(url);
        Set<Integer> set = new HashSet<>();
        int start =0;
        DoubanJsonparser
                .getEventIdThroughParticipantsJson(jsonData,set,start);
    }
}
