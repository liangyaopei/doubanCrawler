package application;

import bfs.DoubanBFSDownload;

public class BFSMain {
    public static void main(String[] args) {
        DoubanBFSDownload bfsDownload = new DoubanBFSDownload();
        bfsDownload.repeatDownloadSetup();
        bfsDownload.beginDownload();
    }
}
