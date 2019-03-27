package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class SeedManagerUtil {

    public final static String eventPath ="./douban/seed/events.txt";
    public final static String userPath = "./douban/seed/users.txt";

    /**
     * distinct sort list
     * @param path
     * @return
     */
    public static List<Integer> loadSeeds(String path){
        List<Integer> result = new ArrayList<>();
        try {
            result = Files
                    .readAllLines(Paths.get(path))
                    .stream()
                    .map(id -> Integer.parseInt(id))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public static void storeSeed(List<Integer> list,String path){
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            for(Integer item:list){
                writer.write(item);
                writer.write("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
