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

因为短时间内多次访问网站会被屏蔽，上面的代码进行了代理设置。这里通过购买阿布云的HTTP隧道进行代理设置。

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
https://api.douban.com/v2/event/list?loc=108296&day_type=20170301&type=all
```

根据第2步的list获取id，根据id获取事件详情和参与者

```
GET https://api.douban.com/v2/event/{id}so
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





# 使用BFS爬取豆瓣同城事件

上述步骤中，能够爬取的事件是有限的。











## 爬虫基本思路

1. 先爬取城市列表

   ```
   GET https://api.douban.com/v2/loc/list
   ```

   

2. 根据城市列表，时间，类别，获得时间的list

   例如：

   ```
   https://api.douban.com/v2/event/list?loc=108296&day_type=20170301&type=all
   ```

   

3. 根据第2步的list获取id，根据id获取事件详情和参与者

   ```
   GET https://api.douban.com/v2/event/{id}so
   ```

   获取参与者

   ```
   GET https://api.douban.com/v2/event/{id}/participants
   ```

   











https://www.douban.com/event/12270102/

## 豆瓣同城API

API 文档：https://douban-api-docs.zce.me/event.html#event_get

获取事件的URL,返回json格式

```html
https://api.douban.com/v2/event/{id}
```

返回的数据：

```

```



获取参加人数的URL

```

```

返回数据

```
key:total
value:30.0
key:count
value:20.0
key:start
value:0.0
key:users
value:[{name=ABCDEFGHIGKLMN, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u146871435-4.jpg, uid=146871435, alt=https://www.douban.com/people/146871435/, type=user, id=146871435, large_avatar=https://img3.doubanio.com/icon/up146871435-4.jpg}, {name=一朵儿光, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/user_normal.jpg, uid=102378683, alt=https://www.douban.com/people/102378683/, type=user, id=102378683, large_avatar=https://img3.doubanio.com/icon/user_large.jpg}, {name=沙拉酱, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/u179700819-17.jpg, uid=179700819, alt=https://www.douban.com/people/179700819/, type=user, id=179700819, large_avatar=https://img1.doubanio.com/icon/up179700819-17.jpg}, {name=卷尾巴的猕猴桃, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u49849682-13.jpg, uid=underwater_m, alt=https://www.douban.com/people/underwater_m/, type=user, id=49849682, large_avatar=https://img3.doubanio.com/icon/up49849682-13.jpg}, {name=板牙先生, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/u52854270-17.jpg, uid=52854270, alt=https://www.douban.com/people/52854270/, type=user, id=52854270, large_avatar=https://img1.doubanio.com/icon/up52854270-17.jpg}, {name=慕儿, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/u57882412-7.jpg, uid=57882412, alt=https://www.douban.com/people/57882412/, type=user, id=57882412, large_avatar=https://img1.doubanio.com/icon/up57882412-7.jpg}, {name=betty801219, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/user_normal.jpg, uid=123026595, alt=https://www.douban.com/people/123026595/, type=user, id=123026595, large_avatar=https://img3.doubanio.com/icon/user_large.jpg}, {name=Jelly荔枝汁, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/user_normal.jpg, uid=158987524, alt=https://www.douban.com/people/158987524/, type=user, id=158987524, large_avatar=https://img3.doubanio.com/icon/user_large.jpg}, {name=雨水一盒, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/u153711419-7.jpg, uid=153711419, alt=https://www.douban.com/people/153711419/, type=user, id=153711419, large_avatar=https://img1.doubanio.com/icon/up153711419-7.jpg}, {name=Army03, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u52822442-2.jpg, uid=52822442, alt=https://www.douban.com/people/52822442/, type=user, id=52822442, large_avatar=https://img3.doubanio.com/icon/up52822442-2.jpg}, {name=矫情的, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u189670862-4.jpg, uid=189670862, alt=https://www.douban.com/people/189670862/, type=user, id=189670862, large_avatar=https://img3.doubanio.com/icon/up189670862-4.jpg}, {name=希·陌璃, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u40428362-16.jpg, uid=40428362, alt=https://www.douban.com/people/40428362/, type=user, id=40428362, large_avatar=https://img3.doubanio.com/icon/up40428362-16.jpg}, {name=yuli, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u177954092-2.jpg, uid=177954092, alt=https://www.douban.com/people/177954092/, type=user, id=177954092, large_avatar=https://img3.doubanio.com/icon/up177954092-2.jpg}, {name=小永远, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u67369572-26.jpg, uid=daqiuqiu, alt=https://www.douban.com/people/daqiuqiu/, type=user, id=67369572, large_avatar=https://img3.doubanio.com/icon/up67369572-26.jpg}, {name=锦户琦, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u77565069-2.jpg, uid=77565069, alt=https://www.douban.com/people/77565069/, type=user, id=77565069, large_avatar=https://img3.doubanio.com/icon/up77565069-2.jpg}, {name=yining0304, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/user_normal_f.jpg, uid=164667986, alt=https://www.douban.com/people/164667986/, type=user, id=164667986, large_avatar=https://img3.doubanio.com/icon/user_large_f.jpg}, {name=刘源, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u146868784-1.jpg, uid=146868784, alt=https://www.douban.com/people/146868784/, type=user, id=146868784, large_avatar=https://img3.doubanio.com/icon/up146868784-1.jpg}, {name=袁长庚, is_banned=false, is_suicide=false, avatar=https://img1.doubanio.com/icon/u1339093-37.jpg, uid=yuanchanggeng, alt=https://www.douban.com/people/yuanchanggeng/, type=user, id=1339093, large_avatar=https://img1.doubanio.com/icon/up1339093-37.jpg}, {name=俞小白, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u192013731-1.jpg, uid=192013731, alt=https://www.douban.com/people/192013731/, type=user, id=192013731, large_avatar=https://img3.doubanio.com/icon/up192013731-1.jpg}, {name=h～h, is_banned=false, is_suicide=false, avatar=https://img3.doubanio.com/icon/u191581018-1.jpg, uid=191581018, alt=https://www.douban.com/people/191581018/, type=user, id=191581018, large_avatar=https://img3.doubanio.com/icon/up191581018-1.jpg}]

```

