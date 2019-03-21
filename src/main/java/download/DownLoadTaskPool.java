package download;

import parse.DoubanListTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author LYaopei
 */
public class DownLoadTaskPool {

    private int numThreads;
    private String baseURL;
    private int start;
    private int end;
    private String destDir;
    private List<String> eventList;

    public DownLoadTaskPool(int numThreads, String baseURL,
                            int start, int end, String destDir) {
        this.numThreads = numThreads;
        this.baseURL = baseURL;
        this.start = start;
        this.end = end;
        this.destDir = destDir;
        eventList = new ArrayList<>();
        for(int i= start;i<end;i++)
            eventList.add(String.valueOf(i));
    }

    public void execute(){

        System.out.println("numThread:"+numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        /**
         * to let the main thread wait for other thread to finish
         */
        CountDownLatch endController = new CountDownLatch(numThreads);

        int length = eventList.size() / numThreads;
        int startIndex=0,endIndex =length;

        try{
            for(int i=0;i<numThreads;++i){
                String dest = destDir +"douban" +i +".txt";
                System.out.println(dest);


                DownloadTask task =new DownloadTask(baseURL,eventList,
                        startIndex,endIndex,dest,endController);

                startIndex = endIndex;
                if(i < numThreads-2){
                    endIndex = endIndex+length;
                }else{
                    endIndex = eventList.size();
                }
                executor.execute(task);
            }

            endController.await();

        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
