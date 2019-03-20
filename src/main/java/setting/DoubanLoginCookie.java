package setting;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Map;

/**
 * @Author LYaopei
 */
public class DoubanLoginCookie {
    private static final String login =
            "https://accounts.douban.com/passport/login?source=main";
    private static String username="13267089109";
    private static String password = "4812";

    private static class LazyHolder{
        private static Map<String, String> cookies;
        public static Map<String, String> getCookies(){
            if(cookies == null){
                try{
                    Connection.Response res = Jsoup.connect(login)
                            .data("username",username,"password",password)
                            .method(Connection.Method.POST)
                            .execute();
                    cookies = res.cookies();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return cookies;
        }
    }
    public static Map<String, String> getCookies(){
        return LazyHolder.getCookies();
    }
}
