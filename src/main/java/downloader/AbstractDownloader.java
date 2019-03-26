package downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import setting.DoubanProxySetting;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractDownloader implements Callable<String> {
    protected Integer identity;
    protected ConcurrentLinkedQueue<Integer> eventQueue;
    protected ConcurrentLinkedQueue<Integer> userQueue;
    protected ConcurrentHashMap<Integer,Boolean> visitedEvent;
    protected ConcurrentHashMap<Integer,Boolean> visitedUser;

    public AbstractDownloader(Integer identity,
                              ConcurrentLinkedQueue<Integer> eventQueue, ConcurrentLinkedQueue<Integer> userQueue,
                              ConcurrentHashMap<Integer, Boolean> visitedEvent, ConcurrentHashMap<Integer, Boolean> visitedUser) {
        this.identity = identity;
        this.eventQueue = eventQueue;
        this.userQueue = userQueue;
        this.visitedEvent = visitedEvent;
        this.visitedUser = visitedUser;
    }

    public String downloadJsonWithProxy(String url) throws IOException {
        String data = Jsoup.connect(url)
                .timeout(3000)
             //   .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
             //   .proxy(DoubanProxySetting.getProxy())
                .ignoreContentType(true)
                .execute().body();
        return data;
    }

    public Document downloadHtmlWithProxy(String url)throws IOException{
        return Jsoup.connect(url)
                .timeout(3000)
                .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                .proxy(DoubanProxySetting.getProxy())
                .get();
    }

   public abstract String download();

    @Override
    public String call() throws Exception {
        return download();
    }
}
