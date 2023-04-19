package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Message implements Serializable {



    private Long timestamp;

    private String sentBy;

    private String sendTo;

    private String data;

    private MessageType type;

    LinkedList<String> userlist = new LinkedList<>();

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

    public void setUserlist(LinkedList<String> userlist) {
        this.userlist = userlist;
    }

    public MessageType getType() {
        return type;
    }

    public List<String> getUserlist() {
        return userlist;
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
}
