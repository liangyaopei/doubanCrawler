package application;

import download.DownLoadTaskPool;

import java.io.File;

/**
 * @Author LYaopei
 */
public class Main {
    public static void main(String[] args) {
         int start = 31870808;
         int end = 31870898;
         String baseURL = "https://api.douban.com/v2/event/";
         String destDir = "./douban/";

        File directory = new File(destDir);
        if(!directory.exists())
            directory.mkdir();


         int numThreads = Runtime.getRuntime().availableProcessors();
         DownLoadTaskPool pool = new DownLoadTaskPool(numThreads,
                baseURL,start,end,destDir);
         pool.execute();
    }
}
