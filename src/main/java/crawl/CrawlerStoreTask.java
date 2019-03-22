package crawl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author LYaopei
 */
public class CrawlerStoreTask implements Runnable{
    private int startIndex;
    private int endIndex;
    /**
     * download event
     */
    private List<Future<String>> futureEvent;
    /**
     * destination file of data
     */
    private String destDir;
    /**
     * notify the main thread to stop after executing all the tasks
     */
    private CountDownLatch endController;

    public CrawlerStoreTask(int startIndex, int endIndex,
                            List<Future<String>> futureEvent,
                            String destDir, CountDownLatch endController) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.futureEvent = futureEvent;
        this.destDir = destDir;
        this.endController = endController;
    }

    @Override
    public void run(){
        System.out.println("name:"+Thread.currentThread().getName());
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(destDir),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,StandardOpenOption.APPEND)
                ){
            for(int index=startIndex; index < endIndex; index++){
                storeData(index,writer);
            }

        }catch (IOException | InterruptedException | ExecutionException e){
            e.printStackTrace();
        }finally {
            endController.countDown();
        }
    }

    private void storeData(int index,BufferedWriter writer)throws IOException,
            InterruptedException, ExecutionException {
        Future<String> future = futureEvent.get(index);
    //    System.out.println(Thread.currentThread().getName()+" waiting for data");
        String data = future.get();
        System.out.println(Thread.currentThread().getName()+" getting data");
        if(!data.isEmpty()){
            writer.write(data);
            writer.write("\n");
        }
    }
}
