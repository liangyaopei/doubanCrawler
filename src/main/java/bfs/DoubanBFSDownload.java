package bfs;

import downloader.DoubanEventDownloader;
import downloader.DoubanUserDownloader;
import utils.SeedManagerUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownload {
    private String eventPath = "./douban/seed/events.txt";
    private String userPath = "./douban/seed/users.txt";
    private String outputPath = "";
    private int numThread = 1;
    private ExecutorService executorService;

    private ConcurrentHashMap<Integer,Boolean> eventSet;
    private ConcurrentHashMap<Integer,Boolean> userSet;
    private ConcurrentLinkedQueue<Integer> eventQueue;
    private ConcurrentLinkedQueue<Integer> userQueue;




    public DoubanBFSDownload(int numThread,
                             String eventPath, String userPath,
                             String outputPath) {
        this.numThread = numThread;
        this.eventPath = eventPath;
        this.userPath = userPath;
        this.outputPath = outputPath;

        eventSet = new ConcurrentHashMap<>();
        userSet = new ConcurrentHashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
        userQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(numThread);

        Set<Integer> eventId = loadData(eventPath);
        eventQueue.addAll(eventId);
        Set<Integer> userId = loadData(userPath);
        userQueue.addAll(userId);
    }


    public void beginDownload(){
        List<Future<String>> result = new LinkedList<>();
        while (true){
            while (!eventQueue.isEmpty()){
                Integer eventId = eventQueue.poll();
                DoubanEventDownloader downloader = new DoubanEventDownloader(eventId,
                        eventQueue,userQueue,eventSet,userSet);
                Future<String> task = executorService.submit(downloader);
                result.add(task);
            }
            DataSaver.saveData(result,outputPath);

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                        eventQueue,userQueue,eventSet,userSet);
                Future<String> task = executorService.submit(downloader);
                result.add(task);
            }

            DataSaver.pullData(result);

            if(eventQueue.isEmpty() && userQueue.isEmpty()){
                break;
            }
        }
        executorService.shutdown();
    }


    public Set<Integer> loadData(String path){
        return SeedManagerUtil.loadSeeds(path)
                .stream()
                .map(id -> Integer.parseInt(id))
                .collect(Collectors.toSet());
    }
}
