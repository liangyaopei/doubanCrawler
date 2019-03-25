package utils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.DoubanEventJsonFormat;
import model.DoubanEventParticipantJsonFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author LYaopei
 */
public class MergerUtil {

    private static List<Integer> merge(String path,MergerParser parser){
        List<Integer> result = null;
        try(Stream<String> lines = Files.lines(Paths.get(path))){
            result = parser.parse(lines);
        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public static List<Integer> parseTxtEventId(String path){
        MergerParser parser = (Stream<String> lines) -> lines
                  .parallel().unordered()
                  .filter(line -> line.startsWith("id:"))
                  .map(line -> Integer.parseInt(
                          line.substring(line.indexOf(":")+1)
                  ))
                  .distinct()
                  .sorted()
                  .collect(Collectors.toList());

        return merge(path,parser);
    }

    public static List<Integer> parseTxtParticipantsId(String path){
        MergerParser parser = (Stream<String> lines) -> lines
                .parallel().unordered()
                .filter(line -> line.startsWith("participants:["))
                .map(line ->{
                    List<Integer> result = new ArrayList<>();
                    String data = line.substring(line.indexOf(":")+1);
                    String[] slices = data.split(",");
                    for(String item:slices){
                        String[] ids = item.split(":");
                        int uid = Integer.parseInt(
                                ids[0].substring(1)
                        );
                        result.add(uid);
                    }
                    return result;
                } )
                .flatMap(list -> list.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return merge(path,parser);
    }



    public static List<Integer> parseJsonEventId(String path){
       MergerParser parser = (Stream<String> lines) ->
               lines.parallel().unordered()
               .filter(line -> line.startsWith("{\"count\":") == false)
               .map(line -> {
                   try{
                       Gson gson = new Gson();
                       DoubanEventJsonFormat event = gson.fromJson(line,DoubanEventJsonFormat.class);

                       return Integer.parseInt(event.id);
                   }catch (JsonSyntaxException e){

                   }
                   return 0;

               })
                       .distinct()
                       .sorted()
                       .collect(Collectors.toList());
       return merge(path,parser);
    }

    public static List<Integer> parseJsonParticipantsId(String path){
        MergerParser parser = (Stream<String> lines) -> lines.parallel().unordered()
                .filter(line -> line.startsWith("{\"count\":"))
                .map(line ->{
                    List<Integer> temp = new ArrayList<>();
                    try{
                        Gson gson = new Gson();
                        DoubanEventParticipantJsonFormat participants = gson.fromJson(line,
                                DoubanEventParticipantJsonFormat.class);

                        for(DoubanEventParticipantJsonFormat.User user:participants.users){
                            temp.add(Integer.parseInt(user.id));
                        }
                    }catch (JsonSyntaxException e){
                      //  System.out.println(line);
                       // e.printStackTrace();
                    }

                    return temp;
                })
                .flatMap(userList -> userList.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return merge(path,parser);

    }
}
