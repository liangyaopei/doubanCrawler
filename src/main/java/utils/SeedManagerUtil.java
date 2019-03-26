package utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author LYaopei
 */
public class SeedManagerUtil {

    public final static String eventPath ="./douban/seed/events.txt";
    public final static String userPath = "./douban/seed/users.txt";

    public static Set<String> loadSeeds(String path){
        Set<String> result = new HashSet<>();
        try {
            result = Files.readAllLines(Paths.get(path))
            .stream().collect(Collectors.toSet());

        }catch (IOException e){
            e.printStackTrace();
        }
        return result;
    }

    public static void storeSeed(Set<Integer> set,String path){
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            for(Integer item:set){
                writer.write(item);
                writer.write("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
