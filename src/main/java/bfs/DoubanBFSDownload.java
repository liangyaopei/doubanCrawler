package bfs;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import downloader.DoubanEventDownloader;
import downloader.DoubanUserDownloader;
import utils.SeedManagerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
    private int numThread = 4;
    private ExecutorService executorService;

    private ConcurrentHashMap<Integer,Boolean> eventSet;
    private ConcurrentHashMap<Integer,Boolean> userSet;
    private ConcurrentLinkedQueue<Integer> eventQueue;
    private ConcurrentLinkedQueue<Integer> userQueue;

    public DoubanBFSDownload() {
        eventSet = new ConcurrentHashMap<>();
        userSet = new ConcurrentHashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
        userQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(numThread);

    }

    public DoubanBFSDownload(int numThread) {
        this.numThread = numThread;
        executorService = Executors.newFixedThreadPool(numThread);
        eventSet = new ConcurrentHashMap<>();
        userSet = new ConcurrentHashMap<>();
        eventQueue = new ConcurrentLinkedQueue<>();
        userQueue = new ConcurrentLinkedQueue<>();
    }

    public DoubanBFSDownload(int numThread,
                             String eventSeedPath, String userSeedPath,
                             String eventDataPath, String userDataPath,
                             String eventNewSeedPath, String userNewSeedPath) {
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

    public void repeatDownloadSetup(){
        Set<Integer> visitedEventIdSet = getVisitedData(eventDataPath,"id").stream().collect(Collectors.toSet());
        Set<Integer> visitedUserIdSet = getVisitedData(userDataPath,"userId").stream().collect(Collectors.toSet());

        //get seed user from eventsJson.txt
        Set<Integer> seedUserIdSet = getSeedData(eventDataPath,"participants","wishers").stream().collect(Collectors.toSet());
        //get seed event from usersJson.txt
        Set<Integer> seedEventIdSet  = getSeedData(userDataPath,"userEvents","wishEvents").stream().collect(Collectors.toSet());

        seedEventIdSet.removeAll(visitedEventIdSet);
        seedUserIdSet.removeAll(visitedUserIdSet);

        eventQueue.addAll(seedEventIdSet);
        userQueue.addAll(seedUserIdSet);

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

    public static List<Integer> getSeedData(String path,String memberName1,String memberName2){
        List<Integer> result = new ArrayList<>();
        try(Stream<String> lines = Files.lines(Paths.get(path),StandardCharsets.UTF_8)){
            JsonParser jsonParser = new JsonParser();
            Gson gson = new Gson();
            result = lines
                    .map(line -> {
                        JsonObject jsonObject = jsonParser.parse(line).getAsJsonObject();
                        Type listType = new TypeToken<List<Integer>>(){}.getType();
                        JsonElement jsonElement1 = jsonObject.get(memberName1);
                        List<Integer> list1 = gson.fromJson(jsonElement1,listType);

                        JsonElement jsonElement2 = jsonObject.get(memberName2);
                        List<Integer> list2 = gson.fromJson(jsonElement2,listType);
                        list2.addAll(list1);
                        return list2;
                    })
                    .flatMap(list -> list.stream())
                    .distinct()
                    .collect(Collectors.toList());
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }
}
