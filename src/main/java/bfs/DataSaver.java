package bfs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @Author LYaopei
 */
public class DataSaver {

    /**
     * This method is to store the data from the executor pool
     * @param list request task to get data
     * @param path path to store the data
     */
    public static void saveData(List<Future<String>> list,String path,String errPath){
        String realPath = selectPath(path);
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(realPath),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        BufferedWriter errWriter = Files.newBufferedWriter(Paths.get(errPath),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            Iterator<Future<String>> iterator = list.iterator();
            while (iterator.hasNext()){
                try{
                    Future<String> task = iterator.next();
                    String data = task.get();
                    if(data.startsWith("event:") || data.startsWith("user:")){
                        if(data.contains("timeout") == false){
                            errWriter.write(data);
                            System.out.println(Thread.currentThread().getName()+":"+errPath+"writing invalid data");
                        }
                    }else {
                        writer.write(data);
                        writer.flush();
                        System.out.println(Thread.currentThread().getName()+":"+path+". saving data ");
                    }
                   // Thread.sleep(300);
                }catch (InterruptedException | ExecutionException e){
                    e.printStackTrace();
                }
                iterator.remove();
            }
            writer.flush();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * TO select a file that is smaller than 1 GB
     * @param path
     * @return
     */
    public static String selectPath(String path){
        String filePath = "";
        int count = 0;
        long GB = 1024*1024*1024;
        for(File file : new File(path).listFiles()){
           if(file.length() <= 1 * GB){
               filePath =  path + "/" + file.getName();
           }
           count++;
        }
        if(filePath.isEmpty()){
            filePath = path + "/"+ "data"+ count+".txt";
        }
        return filePath;
    }
}
