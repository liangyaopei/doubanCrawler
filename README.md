[TOC]

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

因为短时间内多次访问网站会被屏蔽，上面的代码进行了代理设置。这里通过购买阿布云的HTTP隧道进行代理设置。Java 8以上使用代理，要做如下设置：

```java
  System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
```

也可以使用Cookie来避免屏蔽，但是效率不高。在多线程环境中，注意`Cookie`的初始化，只要初始化一次就好。

```java
 public class DoubanLoginCookie {
    private static final String login =
            "https://accounts.douban.com/passport/login?source=main";
    private static String username=""; //phone number
    private static String password = "4812";

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
    public static Map<String, String> getCookies(){
        return LazyHolder.getCookies();
    }
}
```

## 解析数据

数据解析分为HTML解析和JSON解析。

### HTML解析： 使用Jsoup解析HTML数据。

具体代码见 `htmlparse` package。

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

  下面是如期生成器的代码:

  ```java
  public class DoubanEventDateGenerator {
     public static List<String> findDates(String start, String end,int interval) {
          List<Date> lDate = new ArrayList();
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try{
              Date dBegin = sdf.parse(start);
              Date dEnd = sdf.parse(end);
              lDate.add(dBegin);
              Calendar calBegin = Calendar.getInstance();
              // 使用给定的 Date 设置此 Calendar 的时间
              calBegin.setTime(dBegin);
              Calendar calEnd = Calendar.getInstance();
              // 使用给定的 Date 设置此 Calendar 的时间
              calEnd.setTime(dEnd);
              // 测试此日期是否在指定日期之后
              while (dEnd.after(calBegin.getTime())) {
                  // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
                  calBegin.add(Calendar.DAY_OF_MONTH, interval);
                  lDate.add(calBegin.getTime());
              }
          }catch (ParseException e){
              e.printStackTrace();
          }
          return lDate.stream()
                  .map(date -> sdf.format(date).replace("-",""))
                  .collect(Collectors.toList());
      }
  }
  ```

  

- 另外，根据不同城市的URL，进行上述步骤，获得更多的事件。例如，广州的豆瓣同城URL是

  ```
  https://guangzhou.douban.com/events/20190312-all
  ```

### JSON数据解析：使用GSON来解析

具体代码见`naiveJsonDownload` 包

后来，我在github上发现了豆瓣的API文档，网址是：<https://douban-api-docs.zce.me/>

第一步，通过如下URL可以获得某个城市的事件列表:

```java
 https://api.douban.com/v2/loc/list
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
public class DataSaver {

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
    ....
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

#### 事件 ---> 用户

根据下列API获取事件的参与者id，存放到用户队列中。

```
https://www.douban.com/event/{id}/participant
```

java代码：

```java
public class DoubanEventDownloader extends AbstractDownloader {
    public String getParticipantsUrl(int start){
        String url = "https://api.douban.com/v2/event/%d/participants?start=%d&count=100";
        //String url = "https://api.douban.com/v2/event/%d/participants";
        return String.format(url,identity,start);
 }
}

```

每次最多只返回100条数据，如果事件的参见人数超过100，要改变start值。获得参加者id后，将其存放到队列中。

```java
public class DoubanEventDownloader extends AbstractDownloader {
/**
     *
     * @return event information + "\n" + event participants in json format
     * if any of the three request fails, then the result is empty string
     * if result is empty, means that it failed to fetch content
     */
    @Override
    public String download() {
        ....
        do{
                String participantURL = getParticipantsUrl(start);
                start +=count;
                String participantJsonData = downloadJsonWithProxy(participantURL);
                 more = DoubanJsonparser
                        .getParticipantsIdThroughEventJson(participantJsonData,participantsSet,start);

            }while (more == true);
            userQueue.addAll(participantsSet);
        ...
    }
}

```

返回的Json中会告诉参加人数的个数，做如下解析：

```java
public class DoubanJsonparser {

    public static boolean getDetailsFromJson(String jsonData,
                                           String arrayMemberName,String attribute,
                                           Set<Integer> result,
                                             int current)
            throws NumberFormatException{
        JsonElement jsonElement = new JsonParser().parse(jsonData);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray(arrayMemberName);
        for(JsonElement element:jsonArray){
            JsonObject object = element.getAsJsonObject();
            int id = object.get(attribute).getAsInt();
            result.add(id);
        }
        int count = jsonElement.getAsJsonObject().get("total").getAsInt();
        if(count - current > 100)
            return true;
        else
            return false;
    }
}
```

具体下载代码如下，如果下载过程中出现异常，将会丢弃下载到的数据，返回空字符串。`DataSaver`类遇到字符串时，不会讲结果写入。

```java
public class DoubanEventDownloader extends AbstractDownloader {
/**
     *
     * @return event information + "\n" + event participants in json format
     * if any of the three request fails, then the result is empty string
     * if result is empty, means that it failed to fetch content
     */
    @Override
    public String download() {
        StringBuilder builder = new StringBuilder();
        try{
            visitedEvent.put(identity,true);

            String eventURL = getEventUrl();
            int start,count = 100;


            Set<Integer> participantsSet = new HashSet<>();
            start = 0;
            boolean more ;
            do{
                String participantURL = getParticipantsUrl(start);
                start +=count;
                String participantJsonData = downloadJsonWithProxy(participantURL);
                 more = DoubanJsonparser
                        .getParticipantsIdThroughEventJson(participantJsonData,participantsSet,start);

            }while (more == true);
            userQueue.addAll(participantsSet);


            start = 0;
            Set<Integer> wishersSet = new HashSet<>();
            do{
                String wisherURL = getWishersUrl(start);
                start += count;

                String wisherJsonData = downloadJsonWithProxy(wisherURL);
                more = DoubanJsonparser
                        .getParticipantsIdThroughEventJson(wisherJsonData,wishersSet,start);

            }while (more == true);
            userQueue.addAll(wishersSet);



            String eventJsonData = downloadJsonWithProxy(eventURL);
            Gson gson = new Gson();
            JsonObject eventObject  = gson.fromJson(eventJsonData, JsonObject.class);
            JsonElement idElement = gson.toJsonTree(identity,
                    new TypeToken<Integer>(){}.getType());
            eventObject.add("eventId",idElement);

            List<Integer> userList = participantsSet.stream().collect(Collectors.toList());
            JsonElement userElement = gson.toJsonTree(userList,
                    new TypeToken<List<Integer>>(){}.getType());
            eventObject.add("participants",userElement);

            List<Integer> wishersList = wishersSet.stream().collect(Collectors.toList());
            JsonElement wishersElement = gson.toJsonTree(wishersList,
                    new TypeToken<List<Integer>>(){}.getType());
            eventObject.add("wishers",wishersElement);

            eventObject.remove("image");
            eventObject.remove("owner");
            eventObject.remove("alt");

           
            builder.append(eventObject)
           
        }catch (IOException | NumberFormatException e){
            e.printStackTrace();
            System.err.println("event:"+identity);
            builder = new StringBuilder();
        }
        return builder.toString();
    }
    ...
}
```



#### 用户 ----> 事件

根据以下API获取用户参加和感兴趣的事件，提取事件id，存放到事件队列中。

```
https://api.douban.com/v2/event/user_participated/%d?start=0&count=100"
```

java 代码

```java
public class DoubanUserDownloader extends AbstractDownloader {

public String getParticipantsEventUrl(int start){
        String url = "https://api.douban.com/v2/event/user_participated/%d?start=%d&count=50";
        //String url = "https://api.douban.com/v2/event/user_participated/%d";
        return String.format(url,identity,start);
    }
}
```



#### 下载

下载的代码如下：

java代码如下，当事件队列和用户队列都为空时，程序结束。

```java
public class DoubanBFSDownload {
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
            //保存将要访问的用户id
            SeedManagerUtil.storeSeed(userQueue.stream().distinct().sorted().collect(Collectors.toList()), userNewSeedPath);

            while (!userQueue.isEmpty()){
                Integer userId = userQueue.poll();
                if(!userSet.containsKey(userId)){
                    DoubanUserDownloader downloader = new DoubanUserDownloader(userId,
                            eventQueue,userQueue,eventSet,userSet);
                    Future<String> task = executorService.submit(downloader);
                }
            }

            DataSaver.saveData(result,userDataPath);
            System.out.println("Now,user queue is empty");
            //保存将要访问的事件id
            SeedManagerUtil.storeSeed(eventQueue.stream().distinct().sorted().collect(Collectors.toList()), eventNewSeedPath);


            if(eventQueue.isEmpty() && userQueue.isEmpty()){
                break;
            }
        }
        executorService.shutdown();
    }
   ....
}
```

因为程序可能随机停止和启动，所以每次下载时都要讲seed中的数据和已经下载的数据进行去重。

```java
public class DoubanBFSDownload {
     public void setup(){
        Set<Integer> eventSeedIdList = SeedManagerUtil.loadSeeds(eventSeedPath).stream().collect(Collectors.toSet());
        Set<Integer> visitedEventList = getVisitedData(eventDataPath,"id").stream().collect(Collectors.toSet());
        eventSeedIdList.removeAll(visitedEventList);
        eventQueue.addAll(eventSeedIdList);

        Set<Integer> userSeedIdList = SeedManagerUtil.loadSeeds(userSeedPath).stream().collect(Collectors.toSet());
        Set<Integer> visitedUserList = getVisitedData(userDataPath,"userId").stream().collect(Collectors.toSet());
        userSeedIdList.removeAll(visitedUserList);
        userQueue.addAll(userSeedIdList);

        System.out.println("event size;"+eventQueue.size());
        System.out.println("user size:"+userQueue.size());
    }
    ....
}
```

# 程序运行

见每个包的测试代码。

## 豆瓣同城API文档

API 文档：https://douban-api-docs.zce.me/event.html#event_get

