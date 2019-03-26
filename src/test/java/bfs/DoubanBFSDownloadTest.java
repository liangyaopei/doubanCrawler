package bfs;

import org.junit.Test;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownloadTest {

    private String eventPath = "./douban/seed/events.txt";
    private String userPath = "./douban/seed/users.txt";
    private String eventDatPath = "./douban/data/eventsJson.txt";
    private String userDataPath = "./douban/data/usersJson.txt";
    private String eventOutputPath = "./douban/visited/events.txt";
    private String userOutputPath = "./douban/visited/users.txt";
    private int numThread = 3;

    @Test
    public void beginDownloadTest(){
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        System.out.println("programming begins");
       // DataSaver.saveData(null,outputPath);

        DoubanBFSDownload download = new DoubanBFSDownload(numThread,eventPath,
                userPath,eventDatPath,userDataPath,
                eventOutputPath,userOutputPath);
        download.beginDownload();

    }
}
