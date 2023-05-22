package cn.edu.sustech.cs209.chatting.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {

    public int replyId;

    public String BirthDate;

    private String sentBy;

    public String ID;

    public int stars;

    public String phoneID;

    public String PostName;

    public String addFollowName;

    public String cancelFollowName;

    public String title;

    public String country;

    public String city;

    public String  content;

    private String sendTo;

    public List<String> selectPostId = new ArrayList<>();

    public List<String> selectPostContent = new ArrayList<>();

    public List<String> selectPostAuthor = new ArrayList<>();

    public List<String> AllPost = new ArrayList<>();

    public List<String> MyPost = new ArrayList<>();

    public List<String> MyReply = new ArrayList<>();

    public List<String> MyFollowList = new ArrayList<>();

    public List<String> likeList = new ArrayList<>();

    public List<String> shareList = new ArrayList<>();

    public List<String> favoriteList = new ArrayList<>();

    private MessageType type;

    public Message() {

    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }


    public String getSentBy() {
        return sentBy;
    }

}
