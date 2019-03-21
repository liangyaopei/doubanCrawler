package application;

import download.DownLoadTaskPool;

import java.io.File;
import java.util.Scanner;

/**
 * @Author LYaopei
 */
public class Main {
    public static void main(String[] args) {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        System.out.println("programming begins");

        String baseURL = "https://api.douban.com/v2/event/";
        String destDir = "./douban/";

        int start,end,numThreads = 1;

        if(args.length == 0){
             start = 30_000_000;
             end = 32_000_000;
             numThreads = Runtime.getRuntime().availableProcessors()/4;
            //start = 31754698;
            //end = 31754798;
            //numThreads = 1;
        }else if(args.length == 1){
            Scanner scanner = new Scanner(System.in);
            System.out.println("input start");
            start = Integer.parseInt(scanner.nextLine());
            System.out.println("input end");
            end = Integer.parseInt(scanner.nextLine());
            System.out.println("input numThread");
            numThreads = Integer.parseInt(scanner.nextLine());
        }else{
            start = Integer.parseInt(args[0]);
            end = Integer.parseInt(args[1]);
            numThreads = Integer.parseInt(args[2]);
        }



        File directory = new File(destDir);
        if(!directory.exists())
            directory.mkdir();

         DownLoadTaskPool pool = new DownLoadTaskPool(numThreads,
                baseURL,start,end,destDir);
         pool.execute();
    }
}
