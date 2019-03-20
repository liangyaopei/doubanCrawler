package parse;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

/**
 * @Author LYaopei
 */
public class DoubanCookieTest {
    public static void main(String[] args) throws IOException {
        String login = "https://accounts.douban.com/passport/login?source=main";
        String url = "https://www.douban.com/location/shenzhen/events/20180402-all?start=0";
        Connection.Response res = Jsoup.connect(login)
                .data("username","13267089109","password","9215")
                .method(Connection.Method.POST)
                .execute();
        Map<String, String> cookies = res.cookies();
        System.out.println("cookies"+cookies);
        Document doc = Jsoup.connect(url)
                .cookies(cookies).get();
      System.out.println(doc.html());
    }
}
