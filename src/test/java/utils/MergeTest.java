package utils;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

/**
 * @Author LYaopei
 */
public class MergeTest {
    private String path = "./douban";

    @Test
    public void listFile() throws IOException {
        Stream<Path> filesStream = Files.walk(Paths.get(path));
        List<String> txtFiles = filesStream.parallel().unordered()
                .filter(file -> file.toString().endsWith(".txt"))
                .map(file -> file.toString())
                .collect(Collectors.toList());
        txtFiles.forEach(System.out::println);

    }

    @Test
    public void parseTxtEventIdtest(){
        String path = "./douban/txt/shenzhen3.txt";
        List<Integer> eventId = DataMergerUtil.parseTxtEventId(path);
        System.out.println("size:"+eventId.size());
        eventId.forEach(System.out::println);
    }

    @Test
    public void parseTxtParticipantsIdtest(){
        String path = "./douban/txt/douban3.txt";
        List<Integer> participantId = DataMergerUtil.parseTxtParticipantsId(path);
        System.out.println("size:"+participantId.size());
        participantId.forEach(System.out::println);
    }

    @Test
    public void parseJsonParticipantsIdTest(){
        String path = "/Users/lyaopei/IdeaProjects/DoubanCrawler/doubanCrawler/douban/json/doubanJson0Mac.txt";
        DataMergerUtil merger = new DataMergerUtil();
        List<Integer> list = merger.parseJsonParticipantsId(path);
        System.out.println(list.size());
        list.forEach(System.out::println);
    }

    @Test
    public void parseJsonEventIdTest(){
        String path = "/Users/lyaopei/IdeaProjects/DoubanCrawler/doubanCrawler/douban/json/doubanJson0Mac.txt";
        DataMergerUtil merger = new DataMergerUtil();
        List<Integer> list = merger.parseJsonEventId(path);
        System.out.println(list.size());
        list.forEach(System.out::println);
    }

    @Test
    public void mergeTest()throws IOException{
        String txtPath = "./douban/txt/";
        String jsonPath = "./douban/json/";

        List<Integer> eventId = new ArrayList<>();
        List<Integer> participantId = new ArrayList<>();

        List<String> txtFiles =
                Files.list(Paths.get(txtPath))
                        //.filter(file -> file.endsWith(".txt"))
                        .map(file -> file.toString())
                        .collect(Collectors.toList());
        for(String file:txtFiles){
            System.out.println("file:"+file);
            List<Integer> tempEventid = DataMergerUtil.parseTxtEventId(file);
            eventId.addAll(tempEventid);
            if(file.startsWith("douban")){
                List<Integer> tempParticipantsId = DataMergerUtil.parseTxtParticipantsId(file);
                participantId.addAll(tempParticipantsId);
            }

        }

        List<String> jsonFiles =
                Files.list(Paths.get(jsonPath))
                       // .filter(file -> file.endsWith(".txt"))
                        .map(file -> file.toString())
                        .collect(Collectors.toList());
        for(String file:jsonFiles){
            System.out.println("file:"+file);
            List<Integer> tempEvent = DataMergerUtil.parseJsonEventId(file);
            eventId.addAll(tempEvent);
            List<Integer> tempUser = DataMergerUtil.parseJsonParticipantsId(file);
            participantId.addAll(tempUser);
        }


        System.out.println("event size:"+eventId.size());
        List<Integer> distinctEvent = eventId.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("distinct event size:"+distinctEvent.size());

        System.out.println("user size:"+participantId.size());
        List<Integer> distinctUser = participantId.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println("distinct user size:"+distinctUser.size());

        String eventOutput = "./douban/seed/events.txt";
        String userOutput = "./douban/seed/users.txt";

        try(BufferedWriter eventWriter = Files.newBufferedWriter(Paths.get(eventOutput),
                StandardCharsets.UTF_8);
            BufferedWriter userWriter = Files.newBufferedWriter(Paths.get(userOutput),
                    StandardCharsets.UTF_8);
        ){
            for(int event:distinctEvent){
                eventWriter.write(event+"\n");
            }
            eventWriter.flush();

            for(int user:distinctUser){
                userWriter.write(user+"\n");
            }
            userWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
