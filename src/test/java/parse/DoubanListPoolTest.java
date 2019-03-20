package parse;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author LYaopei
 */
public class DoubanListPoolTest {
    private String startDate = "2018-03-21";
    private String endDate = "2019-03-20";
   // private String destDir = "/Users/lyaopei/Downloads/douban/";
    private String windowsDir = "C:\\Users\\LYaopei\\Downloads\\douban\\";
    private int offsetMax = 700;
    private int interval = 7;

    private String shenzhenBaseURL ="https://www.douban.com/location/shenzhen/events/";
    private String shenzhen = "shenzhen";

    private String shanghaiBaseURL = "https://shanghai.douban.com/events/";
    private String shanghai = "shanghai";

    private String beijingBaseURL = "https://beijing.douban.com/events/";
    private String beijing = "beijing";

    private String guangzhouBaseURL = "https://guangzhou.douban.com/events/";
    private String guangzhou = "guangzhou";

    @Test
    public void otherCityTest() throws InterruptedException{
        CountDownLatch beiLatch = new CountDownLatch(1);
        DoubanListPool beiPool =new DoubanListPool(0,
                startDate,endDate,beijingBaseURL,windowsDir,
                beijing,interval,offsetMax,
                beiLatch);
        beiPool.run();
        beiLatch.await();

        CountDownLatch shangLatch = new CountDownLatch(1);
        DoubanListPool shangPool =new DoubanListPool(0,
                startDate,endDate,shanghaiBaseURL,windowsDir,
                shanghai,interval,offsetMax,
                beiLatch);
        shangPool.run();
        shangLatch.await();

        CountDownLatch guangLatch = new CountDownLatch(1);
        DoubanListPool guangPool =new DoubanListPool(0,
                startDate,endDate,guangzhouBaseURL,windowsDir,
                guangzhou,interval,offsetMax,
                beiLatch);
        guangPool.run();
        guangLatch.await();
    }

    @Before
    public void init(){
        /**
         *  To enable roxy tunnel
         */
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
    }

    @Test
    public void shenZhenDownLoader(){

        DoubanListPool pool =new DoubanListPool(0,
                startDate,endDate,shenzhenBaseURL,windowsDir,
                shenzhen,interval);
        pool.run();
    }
}
