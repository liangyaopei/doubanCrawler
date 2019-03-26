package downloader;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author LYaopei
 */
public class DoubanUserDownloaderTest {
    private Integer id = 159180031;
    private ConcurrentHashMap<Integer,Boolean> eventSet = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer,Boolean> userSet = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Integer> eventQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Integer> userQueue = new ConcurrentLinkedQueue<>();
    private DoubanUserDownloader downloader = new DoubanUserDownloader(id,
            eventQueue,userQueue,eventSet,userSet);

    @Test
    public void urlTest(){
        System.out.println(downloader.getParticipantsEventUrl());
        System.out.println(downloader.getWisherEventUrl());
    }

    @Test
    public void downloadTest(){
        downloader.download();
        System.out.println("event:");
        eventQueue.forEach(System.out::println);
    }
}
