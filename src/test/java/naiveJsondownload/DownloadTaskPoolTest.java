package naiveJsondownload;

import org.junit.Test;

import java.io.File;

/**
 * @Author LYaopei
 */
public class DownloadTaskPoolTest {
    private int start = 31870808;
    private int end = 31870898;
    private String baseURL = "https://api.douban.com/v2/event/";
   // private String destDir = "/Users/lyaopei/Downloads/douban/";
    private String destDir = "./douban/";


    @Test
    public void test(){
        int numThreads = 1;

        File directory = new File(destDir);
        if(!directory.exists())
            directory.mkdir();


        DownLoadTaskPool pool = new DownLoadTaskPool(numThreads,
                    baseURL,start,end,destDir);
        pool.execute();




    }
}
