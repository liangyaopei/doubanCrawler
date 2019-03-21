package application;

import download.DownLoadTaskPool;

import java.io.File;

/**
 * @Author LYaopei
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("fuck");
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
         int start = 31870808;
         int end = 318708908;
         String baseURL = "https://api.douban.com/v2/event/";
         String destDir = "./douban/";

        File directory = new File(destDir);
        if(!directory.exists())
            directory.mkdir();


         int numThreads = Runtime.getRuntime().availableProcessors();
        //int numThreads =1;
         DownLoadTaskPool pool = new DownLoadTaskPool(numThreads,
                baseURL,start,end,destDir);
         pool.execute();
    }
}
