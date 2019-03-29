package bfs;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownloadTest {

    private String eventSeedPath = "./douban/seed/events.txt";
    private String userSeedPath = "./douban/seed/users.txt";
    private String eventDataPath = "./douban/eventsJson.txt";
    private String userDataPath = "./douban/usersJson.txt";
    private String invalidDataPath = "./douban/invalid.txt";

    /**
     * 4 thread is good for my PC after testing
     */
    private int numThread = 4;

    @Test
    public void beginDownloadTest(){
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        System.out.println("programming begins");
       // DataSaver.saveData(null,outputPath);

        DoubanBFSDownload download = new DoubanBFSDownload(numThread,eventSeedPath,
                userSeedPath,eventDataPath,userDataPath,invalidDataPath);
      //  download.setup();
        download.repeatDownloadSetup();
        //download.beginDownload();
        download.bfsDownlaod();
    }

    @Test
    public void getVisitedDataSet(){
        String path ="./douban/data/testEventsJson.txt";
        List<Integer> eventIdList = DoubanBFSDownload.getVisitedData(eventDataPath,"eventId");
        System.out.println(eventIdList.size());
    }

    @Test
    public void getSeedDataTest(){
         String eventDataPath = "./douban/eventsJson.txt";
         String userDataPath = "./douban/usersJson.txt";
     //    Set<Integer> seedEventIdSet = DoubanBFSDownload
      //          .getSeedData(eventDataPath,"participants","wishers").stream().collect(Collectors.toSet());
         DoubanBFSDownload doubanBFSDownload = new DoubanBFSDownload();
         doubanBFSDownload.repeatDownloadSetup();
      //  System.out.println("seed user size:"+seedEventIdSet.size());

    }
}
