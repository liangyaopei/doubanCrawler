package downloader;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author LYaopei
 */
public class DoubanEventDownloaderTest {
    private Integer id = 31317328;
    private ConcurrentHashMap<Integer,Boolean> eventSet = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,Boolean> userSet = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Integer> eventQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Integer> userQueue = new ConcurrentLinkedQueue<>();
    DoubanEventDownloader downloader = new DoubanEventDownloader(id,
            eventQueue,userQueue,eventSet,userSet);


    @Test
    public void urlTest(){
        DoubanEventDownloader downloader = new DoubanEventDownloader(31993290,
        null,null,null,null);
       // String url = "https://api.douban.com/v2/event/31754598/%d";
        System.out.println(downloader.getEventUrl());
        System.out.println(downloader.getParticipantsUrl());
        System.out.println(downloader.getWishersUrl());
    }

    @Test
    public void downloadTest(){
        System.out.println(downloader.download());
    }
}
