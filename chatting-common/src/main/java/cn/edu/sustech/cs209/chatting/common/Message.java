package cn.edu.sustech.cs209.chatting.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Message implements Serializable {
    private int onlineNum;

    private Long timestamp;

    private String sentBy;

    private String sendTo;

    public List<String> sendToLIst = new ArrayList<>();

    private String data;

    public String queryName;

    private MessageType type;

    public ArrayList<String> userList = new ArrayList<>();

    public ArrayList<Message> chatMessage = new ArrayList<>();

    public ArrayList<Message> queryChatMsg = new ArrayList<>();

    public Message() {

    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public MessageType getType() {
        return type;
    }

    public List<String> getUserList() {
        return userList;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public String getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }

    public int getOnlineNum() {
        return onlineNum;
    }

    public void setOnlineNum(int onlineNum) {
        this.onlineNum = onlineNum;
    }
}
