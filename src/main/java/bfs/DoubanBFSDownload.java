package bfs;


import com.google.gson.JsonParser;
import downloader.DoubanEventDownloader;
import downloader.DoubanUserDownloader;
import utils.SeedManagerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author LYaopei
 */
public class DoubanBFSDownload {


    private String eventSeedPath = "./douban/seed/events.txt";
    private String userSeedPath = "./douban/seed/users.txt";
    private String eventDataPath = "./douban/data/eventsJson.txt";
    private String userDataPath = "./douban/data/usersJson.txt";
    private String eventNewSeedPath = "./douban/seed/events1.txt";
    private String userNewSeedPath = "./douban/seed/users1.txt";
    private int numThread = 3;
    private ExecutorService executorService;

    private ConcurrentHashMap<Integer,Boolean> eventSet;
    private ConcurrentHashMap<Integer,Boolean> userSet;
    private ConcurrentLinkedQueue<Integer> eventQueue;
    private ConcurrentLinkedQueue<Integer> userQueue;




    public DoubanBFSDownload(int numThread,
                             String eventSeedPath, String userSeedPath,
                             String eventDataPath,String userDataPath,
                             String eventNewSeedPath,String userNewSeedPath) {
        this.numThread = numThread;
        this.eventSeedPath = eventSeedPath;
        this.userSeedPath = userSeedPath;
        this.eventDataPath = eventDataPath;
        this.userDataPath = userDataPath;
        this.eventNewSeedPath = eventNewSeedPath;
        this.userNewSeedPath = userNewSeedPath;


        eventSet = new ConcurrentHashMap<>();
        userSet = new ConcurrentHashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
        userQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(numThread);



    }

    public void setup(){
        Set<Integer> eventSeedIdList = SeedManagerUtil.loadSeeds(eventSeedPath).stream().collect(Collectors.toSet());
        Set<Integer> visitedEventList = getVisitedData(eventDataPath,"id").stream().collect(Collectors.toSet());
        eventSeedIdList.removeAll(visitedEventList);
        eventQueue.addAll(eventSeedIdList);

        Set<Integer> userSeedIdList = SeedManagerUtil.loadSeeds(userSeedPath).stream().collect(Collectors.toSet());
        Set<Integer> visitedUserList = getVisitedData(userDataPath,"userId").stream().collect(Collectors.toSet());
        userSeedIdList.removeAll(visitedUserList);
        userQueue.addAll(userSeedIdList);

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
                    /*
                    List<Future<String>> temp =new ArrayList<>(1);
                    temp.add(task);
                    DataSaver.saveData(temp,eventDataPath);
                    */
                }
            }
            DataSaver.saveData(result,eventDataPath);
            System.out.println("Now,event queue is empty");
            //保存将要访问的用户id
            SeedManagerUtil.storeSeed(userQueue.stream().distinct().sorted().collect(Collectors.toList()), userNewSeedPath);

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                if(!userSet.containsKey(userId)){
                    DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);

                    result.add(task);
                    /*
                    List<Future<String>> temp =new ArrayList<>(1);
                    temp.add(task);
                    DataSaver.saveData(temp,eventDataPath);
                    */
                }
            }

            DataSaver.saveData(result,userDataPath);
            System.out.println("Now,user queue is empty");
            //保存将要访问的事件id
            SeedManagerUtil.storeSeed(eventQueue.stream().distinct().sorted().collect(Collectors.toList()), eventNewSeedPath);


            if(eventQueue.isEmpty() && userQueue.isEmpty()){
                break;
            }
        }
        executorService.shutdown();
    }

    public static List<Integer> getVisitedData(String path,String memberName){
        List<Integer> result = new ArrayList<>();
        try(Stream<String> lines = Files.lines(Paths.get(path))){
            JsonParser jsonParser = new JsonParser();
            result = lines.map(line ->

                        jsonParser.parse(line).getAsJsonObject().get(memberName).getAsInt()

            )
                    .distinct().sorted().collect(Collectors.toList());

        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }
}
