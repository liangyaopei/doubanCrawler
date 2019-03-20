package parse;

import org.junit.Before;
import org.junit.Test;

/**
 * @Author LYaopei
 */
public class DoubanListPoolTest {
    private String startDate = "2018-03-21";
    private String endDate = "2019-12-23";
    private String destDir = "/Users/lyaopei/Downloads/douban/";
    private int interval = 7;

    private String shenzhenBaseURL ="https://www.douban.com/location/shenzhen/events/";
    private String shenzhen = "shenzhen";

    @Before
    public void init(){
        /**
         *  To enable roxy tunnel
         */
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    @Test
    public void shenZhenDownLoader(){

        DoubanListPool pool =new DoubanListPool(1,
                startDate,endDate,shenzhenBaseURL,destDir,
                shenzhen,interval);
        pool.run();
    }
}
