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
    private String eventOutpttPath ="";
    private String userOutputPath = "";
    private int numThread = 1;
    private ExecutorService executorService;

    private ConcurrentHashMap<Integer,Boolean> eventSet;
    private ConcurrentHashMap<Integer,Boolean> userSet;
    private ConcurrentLinkedQueue<Integer> eventQueue;
    private ConcurrentLinkedQueue<Integer> userQueue;




    public DoubanBFSDownload(int numThread,
                             String eventPath, String userPath,
                             String outputPath,
                             String eventOutpttPath,String userOutputPath) {
        this.numThread = numThread;
        this.eventPath = eventPath;
        this.userPath = userPath;
        this.outputPath = outputPath;
        this.eventOutpttPath = eventOutpttPath;
        this.userOutputPath = userOutputPath;

        eventSet = new ConcurrentHashMap<>();
        userSet = new ConcurrentHashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
        userQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(numThread);

        Set<Integer> eventId = loadData(eventPath);
        eventQueue.addAll(eventId);
        Set<Integer> userId = loadData(userPath);
        userQueue.addAll(userId);

        System.out.println("event size;"+eventQueue.size());
        System.out.println("user size:"+userQueue.size());
    }


    public void beginDownload(){
        List<Future<String>> result = new LinkedList<>();
        while (true){
            while (!eventQueue.isEmpty()){
                Integer eventId = eventQueue.poll();
                if(!eventSet.containsKey(eventId)){
                    DoubanEventDownloader downloader = new DoubanEventDownloader(eventId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);
                    result.add(task);
                }
            }
            DataSaver.saveData(result,outputPath);
            System.out.println("Now,event queue is empty");
            SeedManagerUtil.storeSeed(eventSet.keySet(),eventOutpttPath);

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                if(!userSet.containsKey(userId)){
                    DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);
                    result.add(task);
                }
            }

            DataSaver.pullData(result);
            System.out.println("Now,user queue is empty");
            SeedManagerUtil.storeSeed(userSet.keySet(),userOutputPath);

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
