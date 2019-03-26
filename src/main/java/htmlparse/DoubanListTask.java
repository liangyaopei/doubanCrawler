package htmlparse;

import model.EventInformation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * https://www.douban.com/location/shenzhen/events/20190301-all?start=10
 * @Author LYaopei
 */
public class DoubanListTask implements Runnable {
    private String baseURL;
    private List<String> dates;
    private int startIndex;
    private int endIndex;
    private int offsetMax;
    private String destDir;
    private CountDownLatch endController;
    private boolean parseDetails;
    private boolean parseParticipants;

    public DoubanListTask(String baseURL, List<String> dates,
                          int startIndex, int endIndex, int offsetMax,
                          String destDir, CountDownLatch endController,
                          boolean parseDetails,boolean parseParticipants) {
        this.baseURL = baseURL;
        this.dates = dates;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.offsetMax = offsetMax;
        this.destDir = destDir;
        this.endController = endController;
        this.parseDetails = parseDetails;
        this.parseParticipants = parseParticipants;
    }

    @Override
    public void run() {
        Set<EventInformation> allEvents = new HashSet<>();

        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(destDir),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,StandardOpenOption.APPEND)){

            for(int index = startIndex; index<endIndex; index++){
                String date = dates.get(index);

                for(int offset = 0;offset <= offsetMax; offset+=10){
                    String url = baseURL + date + "-all?start="+offset;
                    DoubanListParser parser = new DoubanListParser(url,
                            parseDetails,parseParticipants);

                    Set<EventInformation> partialEventSet = parser.parse();

                    partialEventSet.removeAll(allEvents);

                    writeData(writer,partialEventSet,date);

                    allEvents.retainAll(partialEventSet);

                    System.out.println("url:"+url);
                }

            }

        }catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        endController.countDown();
    }

    public void writeData(BufferedWriter writer,
                          Set<EventInformation> eventSet, String date) throws IOException {
        for(EventInformation event:eventSet){


            writer.write("id:"+event.getId()+"\n");
            writer.write("url:"+event.getEventURL()+"\n");
            writer.write("title:"+event.getTitle()+"\n");
            writer.write("date:"+date+"\n");
            writer.write("location:"+event.getLocation()+"\n");

            if(event.getDetails()!=null)
                writer.write("details:"+event.getDetails()+"\n");

            if(event.getParticipants()!=null)
                writer.write("participants:"+event.getParticipants()+"\n");
            writer.write("\n");
            writer.flush();
        }
    }
}

