package bfs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Author LYaopei
 */
public class DataSaver {

    /**
     * This method is to store the data from the executor pool
     * @param list request task to get data
     * @param path path to store the data
     */
    public static void saveData(List<Future<String>> list,String path){
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(path),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            Iterator<Future<String>> iterator = list.iterator();


            while (iterator.hasNext()){
                try{
                    Future<String> task = iterator.next();
                    String data = task.get();
                    if(data.isEmpty()==false){
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
}
