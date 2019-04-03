package bfs;

import org.junit.Test;

import java.io.IOException;

public class DataSaverTest {

    @Test
    public void selectPathTest(){
        //String path ="./douban/events";
        String eventDataPath = "./douban/events";
        String userDataPath = "./douban/users";
        System.out.println(
                DataSaver.selectPath(eventDataPath)
        );
        System.out.println(
                DataSaver.selectPath(userDataPath)
        );
    }
}
