package crawl;

import org.jsoup.Jsoup;
import setting.DoubanProxySetting;

import java.io.IOException;
import java.util.List;

public abstract class CrawlAction {
    protected List<String> urlList;

    public CrawlAction(List<String> urlList) {
        this.urlList = urlList;
    }

    public String crawl(String url) throws IOException {
        String data = Jsoup.connect(url)
                .timeout(3000)
                .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                .proxy(DoubanProxySetting.getProxy())
                .ignoreContentType(true)
                .execute().body();
        return data;
    }

   public abstract String action();
}
