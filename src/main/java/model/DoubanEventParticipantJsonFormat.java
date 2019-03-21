package model;

import java.util.List;

/**
 * @Author LYaopei
 */
public class DoubanEventParticipantJsonFormat {
    public class User{
        public String name;
        public String id;
    }
    public List<User>  users;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("participants:");
        for(User user:users){
            builder.append("["+user.id+":"+user.name+"],");
        }
        return builder
                .append("\n")
                .toString();
    }
}
