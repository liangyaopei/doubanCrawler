package bfs;

import org.junit.Test;

import java.util.List;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownloadTest {

    private String eventSeedPath = "./douban/seed/events.txt";
    private String userSeedPath = "./douban/seed/users.txt";
    private String eventDataPath = "./douban/data/eventsJson.txt";
    private String userDataPath = "./douban/data/usersJson.txt";
    private String eventNewSeedPath = "./douban/seed/events1.txt";
    private String userNewSeedPath = "./douban/seed/users1.txt";

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
                userSeedPath,eventDataPath,userDataPath,
                eventNewSeedPath,userNewSeedPath);
        download.setup();
        download.beginDownload();

    }

    @Test
    public void getVisitedDataSet(){
        String path ="./douban/data/testEventsJson.txt";
        List<Integer> eventIdList = DoubanBFSDownload.getVisitedData(eventDataPath,"eventId");
        System.out.println(eventIdList.size());
    }
}
