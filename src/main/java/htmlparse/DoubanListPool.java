package htmlparse;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author LYaopei
 */
public class DoubanListPool {
    private int numThreads= 0;
    /**
     * format : 2018-02-01
     */
    private String startDate ;
    private String endDate ;
    /**
     * https://www.douban.com/location/shenzhen/events/
     * + 20190301
     * + -all?start=10
     */
    private String baseURL ;
    private String destDir;
    private String city;
    /**
     * date interval
     */
    private int interval;
    private int offsetMax;
    private CountDownLatch latch;

    public DoubanListPool(int numThreads,
                          String startDate, String endDate,
                          String baseURL, String destDir,
                          String city, int interval) {
        this.numThreads = numThreads;
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseURL = baseURL;
        this.destDir = destDir;
        this.city = city;
        this.interval = interval;
        this.offsetMax = 700;
    }

    public DoubanListPool(int numThreads,
                          String startDate, String endDate,
                          String baseURL, String destDir,
                          String city, int interval, int offsetMax,
                          CountDownLatch latch) {
        this.numThreads = numThreads;
        this.startDate = startDate;
        this.endDate = endDate;
        this.baseURL = baseURL;
        this.destDir = destDir;
        this.city = city;
        this.interval = interval;
        this.offsetMax = offsetMax;
        this.latch = latch;
    }

    public void run(){

        if(numThreads!=1)
            numThreads = Runtime.getRuntime().availableProcessors();
       // System.out.println("numThread:"+numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        /**
         * to let the main thread wait for other thread to finish
         */
        CountDownLatch endController = new CountDownLatch(numThreads);

        List<String> dates = DoubanEventDateGenerator.findDates(startDate,endDate,interval);

        int length = dates.size() / numThreads;
        int startIndex=0,endIndex =length;

        try{
            for(int i=0;i<numThreads;++i){
                String dest = destDir + city + i +".txt";

                DoubanListTask task = new DoubanListTask(baseURL,dates,
                        startIndex,endIndex,offsetMax,
                        dest,endController,
                        true,true);

                startIndex = endIndex;
                if(i < numThreads-2){
                    endIndex = endIndex+length;
                }else{
                    endIndex = dates.size();
                }
                executor.execute(task);
            }

            endController.await();

        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(latch != null)
            latch.countDown();
        //close the pool
       // pool.shutdown();
    }
}
