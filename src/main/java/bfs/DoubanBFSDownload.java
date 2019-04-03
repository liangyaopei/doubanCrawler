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
import java.nio.file.Path;
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
    private String eventDataPath = "./douban/events";
    private String userDataPath = "./douban/users";
    private String invalidDataPath = "./douban/invalid.txt";

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
                             String invalidDataPath) {
        this.numThread = numThread;
        this.eventSeedPath = eventSeedPath;
        this.userSeedPath = userSeedPath;
        this.eventDataPath = eventDataPath;
        this.userDataPath = userDataPath;
        this.invalidDataPath = invalidDataPath;

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
        System.out.println("Running repeated downloading");
        Set<Integer> visitedEventIdSet = getVisitedData(eventDataPath,"id").stream().collect(Collectors.toSet());
        Set<Integer> visitedUserIdSet = getVisitedData(userDataPath,"userId").stream().collect(Collectors.toSet());

        //get seed event from usersJson.txt
        Set<Integer> seedEventIdSet  =
                getSeedData(userDataPath,"userEvents","wishEvents")
                        .stream().collect(Collectors.toSet());
        //get seed user from eventsJson.txt
        Set<Integer> seedUserIdSet =
                getSeedData(eventDataPath,"participants","wishers")
                        .stream().collect(Collectors.toSet());

        seedEventIdSet.removeAll(visitedEventIdSet);
        seedUserIdSet.removeAll(visitedUserIdSet);

        Set<Integer> invalidEventIdSet = getInvalidData(invalidDataPath,"event:");
        seedEventIdSet.removeAll(invalidEventIdSet);

        Set<Integer> invalidUserIdSet = getInvalidData(invalidDataPath,"user:");
        seedUserIdSet.removeAll(invalidUserIdSet);


        System.out.println("size of invalid events:"+invalidEventIdSet.size());
        System.out.println("size of invalid users: "+invalidUserIdSet.size());

        eventQueue.addAll(seedEventIdSet);
        userQueue.addAll(seedUserIdSet);

        System.out.println("size of events to be downloaded: "+eventQueue.size());
        System.out.println("size of users to be downloaded: "+userQueue.size());
    }

    /**
     * This method is an improved version of beginDownload
     * it is more memory friendly
     */
    public void bfsDownlaod(){
        List<Future<String>> result = new LinkedList<>();
        int downoadEachtime = 10;

        while (eventQueue.isEmpty()==false && userQueue.isEmpty() == false){
            for(int count=0; eventQueue.isEmpty()== false && count<downoadEachtime;count++){
                Integer eventId = eventQueue.poll();
                DoubanEventDownloader eventDownloader = new DoubanEventDownloader(eventId,
                        eventQueue,userQueue,eventSet,userSet);
                Future<String> task = executorService.submit(eventDownloader);
                result.add(task);
            }
            DataSaver.saveData(result,eventDataPath,invalidDataPath);

            for(int count=0; userQueue.isEmpty()==false && count<downoadEachtime;count++){
                Integer userId = userQueue.poll();
                DoubanUserDownloader userDownloader = new DoubanUserDownloader(userId,
                        eventQueue,userQueue,eventSet,userSet);
                Future<String> task = executorService.submit(userDownloader);
                result.add(task);
            }
            DataSaver.saveData(result,userDataPath,invalidDataPath);
        }

    }

    /**
     * When the eventQueue or userQueue is too large, this may cause that
     * eventQueue(userQueue) occupies all the execution(12 hours in practise)
     * and at the same time, the other queue cannot be executed
     * So use bfsDownload may be better
     */
    @Deprecated
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
            DataSaver.saveData(result,eventDataPath,invalidDataPath);
            System.out.println("Now,event queue is empty");

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                if(!userSet.containsKey(userId)){
                    DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);

                    result.add(task);
                }
            }

            DataSaver.saveData(result,userDataPath,invalidDataPath);
            System.out.println("Now,user queue is empty");

            if(eventQueue.isEmpty() && userQueue.isEmpty()){
                break;
            }
        }
        executorService.shutdown();
    }

    public static List<Integer> getVisitedData(String path,String memberName){
        List<Integer> result = new ArrayList<>();
        try{
            List<Path> pathList = Files.list(Paths.get(path)).collect(Collectors.toList());
            for(Path filePath: pathList){
                try(Stream<String> lines = Files.lines(filePath)){
                    JsonParser jsonParser = new JsonParser();
                    result = lines.map(line ->

                            jsonParser.parse(line).getAsJsonObject().get(memberName).getAsInt()

                    )
                            .distinct().sorted().collect(Collectors.toList());

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    public static Set<Integer> getInvalidData(String path,String keyword){
        Set<Integer> result = new HashSet<>();
        try{
            result = Files.lines(Paths.get(path))
                    .filter(line -> line.startsWith(keyword))
                    .map(line -> Integer.parseInt(
                            line.substring(line.indexOf(":")+1)
                    ))
                    .collect(Collectors.toSet());
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public static List<Integer> getSeedData(String path,String memberName1,String memberName2){
        List<Integer> result = new ArrayList<>();
        try{
            List<Path> pathList = Files.list(Paths.get(path)).collect(Collectors.toList());
            for(Path filePath:pathList){
                try(Stream<String> lines = Files.lines(filePath,StandardCharsets.UTF_8)){
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
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }
}
