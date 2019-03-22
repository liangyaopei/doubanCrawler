package crawl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author LYaopei
 */
public class CrawlPool {
    private int numThreads;
    /**
     * Example: https://api.douban.com/v2/event/
     */
    private String baseURL;
    private int start;
    private int end;
    private String destDir;

    public CrawlPool(int numThreads, String baseURL, int start, int end, String destDir) {
        this.numThreads = numThreads;
        this.baseURL = baseURL;
        this.start = start;
        this.end = end;
        this.destDir = destDir;
    }

    /**
     * first for loop: submit all the download task into pool
     * second for loop:
     */
    public void execute() {
        ExecutorService service = Executors.newFixedThreadPool(numThreads);


      //  List<Crawler> tasks = new ArrayList<>(end-start+1);
        List<Future<String>> results = new ArrayList<>(end-start +1);

        for(int index= start; index<end;index++){
            String eventURL = baseURL + index;
            String participantsURL = eventURL + "/" + "participants";

            Crawler crawler = new Crawler(eventURL,participantsURL);
            Future<String> future = service.submit(crawler);
            results.add(future);
            //tasks.add(crawler);
        }

        CountDownLatch endController = new CountDownLatch(numThreads);

        try{
          //  results = service.invokeAll(tasks);
            service.shutdown();


            int length = results.size() / numThreads;
            int beginIndex = 0, lastIndex = length;



            for(int i=0 ;i<numThreads; i++){
                String dest = destDir + "doubanJson" +i +".txt";
                System.out.println(dest);

                CrawlerStoreTask storeTask = new CrawlerStoreTask(beginIndex,lastIndex,
                        results,dest,endController);
                System.out.println("Thread "+i+":bIndex:"+beginIndex+",lastIndex:"+lastIndex);
                // begin a new Thread
                new Thread(storeTask).start();

                beginIndex = lastIndex;
                if(i < numThreads -2){
                    lastIndex = lastIndex + length;
                }else{
                    lastIndex = results.size();
                }
            }


            endController.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }
}
