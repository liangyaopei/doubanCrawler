# 爬虫基本步骤

## 下载数据

使用Jsoup框架进行下载。具体代码：

```java
 public String downloadJsonWithProxy(String url) throws IOException {
        String data = Jsoup.connect(url)
                .timeout(3000)
              .header(DoubanProxySetting.ProxyHeadKey,DoubanProxySetting.ProxyHeadVal)
                .proxy(DoubanProxySetting.getProxy())
                .ignoreContentType(true)
                .execute().body();
        return data;
    }
```

### 避免屏蔽

因为短时间内多次访问网站会被屏蔽，上面的代码进行了代理设置。这里通过购买阿布云的HTTP隧道进行代理设置。Java 8以上使用代理，要坐如下设置：

```java
  System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
```

也可以使用Cookie来避免屏蔽，但是效率不高。

```java
 private static class LazyHolder{
        private static Map<String, String> cookies;
        public static Map<String, String> getCookies(){
            if(cookies == null){
                try{
                    Connection.Response res = Jsoup.connect(login)
                            .data("username",username,"password",password)
                            .method(Connection.Method.POST)
                            .execute();
                    cookies = res.cookies();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return cookies;
        }
    }
```

## 解析数据

数据解析分为HTML解析和JSON解析。

### HTML解析： 使用Jsoup解析HTML数据。

第一步，根据豆瓣网站上的列表获取事件的id。

```html
https://www.douban.com/location/shenzhen/events/20190303-all?start=10
```

具体代码：

```java
 Document document = crawlContent(baseURL);
            Elements events = document
                    .select("ul.events-list.events-list-pic100.events-list-psmall")
                    .select("li.list-entry");
```

第二步，根据列表中每一个事件id，获取事件的详情，url如下：

```
https://www.douban.com/event/31718330/
```

java代码：

```java
 private String parseDetails(String detailsURL) throws IOException {
        Document detailsDoc = crawlContent(detailsURL);

        String details = detailsDoc
                .select("[id=edesc_s]")
                .text();
        return details;
    }
```

第三步，然后根据事件的ID，获取参加人员的id:

```
https://www.douban.com/event/31718330/participant
```

java代码

```java
 private String parseParticipant(String participantURL) throws IOException {
        Document participantDoc = crawlContent(participantURL);

        String participants = participantDoc
                .select("[class=obu]")
                .select("dd a").text();
        return participants;
    }
```

- 注意到，第一步中URL中，时间是可以变化的，豆瓣的网站提供过去一年的事件查询，通过变化URL中时间，来获取不同的事件。

- 另外，根据不同城市的URL，进行上述步骤，获得更多的事件。例如，广州的豆瓣同城URL是

  ```
  https://guangzhou.douban.com/events/20190312-all
  ```

### JSON数据解析：使用GSON来解析

后来，我在github上发现了豆瓣的API文档，网址是：<https://douban-api-docs.zce.me/>

第一步，通过如下URL可以获得某个城市的事件列表:

```java
GET https://api.douban.com/v2/loc/list
```

第二步，根据城市列表，时间，类别，获得时间的list

```java
https://api.douban.com/v2/event/list?loc=108296&day_type=week&type=all
```

根据第2步的list获取id，根据id获取事件详情：

```
GET https://api.douban.com/v2/event/{id}
```

获取参与者:

```
GET https://api.douban.com/v2/event/{id}/participants
```



## 存储数据

本项目使用多线程去爬取数据，使用单线程来存储数据。

使用线程池去爬取，将下载任务放到线程池中，获得一个Future的list。

```java
int numThread = Runtime.getRuntime().availableProcessors();
ExecutorService executorService = Executors.newFixedThreadPool(numThread);
List<Future<String>> result = new LinkedList<>();
DoubanEventDownloader downloader = new DoubanEventDownloader(eventId,
                            eventQueue,userQueue,eventSet,userSet);
Future<String> task = executorService.submit(downloader);
result.add(task);
```

然后逐个获得list中的结果，保存到本地

```java
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
                        System.out.println(Thread.currentThread().getName()+" saving data");
                    }
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
```

### 数据储存格式

将事件和用户分别保存，a按行储存，保存格式为JSON。

事件：

```
事件内容的Json + 参与者的Json + 感兴趣用户的Json
```

用户：

```
参加事件的Json + 感兴趣事件的Json
```

# 使用BFS爬取豆瓣同城事件

上述步骤中，能够爬取的事件是有限的。因为下列url中`daytype` 参数只能为week, weekend。这决定了只能下载近期的事件，而不能下载过去的事件。

```
https://api.douban.com/v2/event/list?loc=108296&day_type=week&type=all
```

为了尽可能多地下载事件，可以根据事件的id进行枚举。但是，id的位数是位数，如果从1增加到近期事件id(例如：31815618)，需要太长事件，并且有一些id对应的URL是空的，没有详情。

```
https://www.douban.com/event/12270102/
```

## BFS爬取

根据前面爬取的一些事件，可以构成事件-用户的二部图。

```
Event           User
eid1   ------>  uid
eid2   <------
```

根据下列API获取事件的参与者id，存放到用户队列中。

```
https://www.douban.com/event/{id}/participant
```

根据以下API获取用户参加和感兴趣的事件，提取事件id，存放到事件队列中。

```
https://api.douban.com/v2/event/user_participated/%d?start=0&count=100"
```

和

```
https://api.douban.com/v2/event/user_wished/%d?start=0&count=100
```

java代码:

```java

    public void beginDownload(){
        List<Future<String>> result = new LinkedList<>();
        while (true){
            while (!eventQueue.isEmpty()){
                Integer eventId = eventQueue.poll();
                if(!eventSet.containsKey(eventId)){
                    DoubanEventDownloader downloader = new DoubanEventDownloader(eventId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);
                    result.add(task);
                }
            }
            DataSaver.saveData(result,eventDataPath);
            System.out.println("Now,event queue is empty");
            SeedManagerUtil.storeSeed(eventSet.keySet(),eventOutpttPath);

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                if(!userSet.containsKey(userId)){
                    DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);
                    result.add(task);
                }
            }

            DataSaver.saveData(result,userDataPath);
            System.out.println("Now,user queue is empty");
            SeedManagerUtil.storeSeed(userSet.keySet(),userOutputPath);

            if(eventQueue.isEmpty() && userQueue.isEmpty()){
                break;
            }
        }
        executorService.shutdown();
    }
```



## 豆瓣同城API文档

API 文档：https://douban-api-docs.zce.me/event.html#event_get

