package application;

import bfs.DoubanBFSDownload;
import setting.DoubanProxySetting;

public class BFSMain {
    public static void main(String[] args) {
        DoubanBFSDownload bfsDownload = new DoubanBFSDownload(4);
        bfsDownload.repeatDownloadSetup();
       // bfsDownload.beginDownload();
        bfsDownload.bfsDownlaod();
    }
}
