package application;

import bfs.DoubanBFSDownload;
import setting.DoubanProxySetting;

public class BFSMain {
    public static void main(String[] args) {
        System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
        DoubanBFSDownload bfsDownload = new DoubanBFSDownload();
        bfsDownload.repeatDownloadSetup();
        bfsDownload.beginDownload();
    }
}
