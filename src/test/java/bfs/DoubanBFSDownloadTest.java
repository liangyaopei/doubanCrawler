package bfs;

import org.junit.Test;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownloadTest {

    private String eventPath = "./douban/seed/events.txt";
    private String userPath = "./douban/seed/users.txt";
    private String outputPath = "./douban/dataJson.txt";
    private int numThread = 1;

    @Test
    public void beginDownloadTest(){
        DoubanBFSDownload download = new DoubanBFSDownload(numThread,eventPath,
                userPath,outputPath);
        download.beginDownload();
    }
}
