package download;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author LYaopei
 */
public class DownloadTask implements Runnable {
    private String baseURL;
    private List<String> range;
    private int startIndex;
    private int endIndex;
    private String downloadFilename;
    private CountDownLatch endController;

    public DownloadTask(String baseURL, List<String> range,
                        int startIndex, int endIndex,
                        String downloadFilename,
                        CountDownLatch latch) {
        this.baseURL = baseURL;
        this.range = range;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.downloadFilename = downloadFilename;
        this.endController = latch;
    }

    @Override
    public void run() {
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(downloadFilename),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){

            for(int index = startIndex;index<endIndex;index++){
                String eventURL = baseURL +range.get(index);
                String participantsURl = eventURL + "/participants";

                System.out.println("before download, eventURL:"+eventURL);
                Downloader downloader = new Downloader(eventURL,participantsURl);
                String data = downloader.download();

                if(data!=null && !data.isEmpty()){

                    writer.write(data);
                    writer.write("\n");
                    writer.flush();
                   System.out.println("after eventURL:"+eventURL);
                    if(index % 100 ==0){
                        System.out.println("eventURL:"+eventURL);
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            endController.countDown();
        }

    }
}
