package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.OC;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public  class ServerThread implements Runnable{
  private Socket s;
  private OC.MyObjectInputStream ois = null;
  public  ServerThread(Socket s) {
   this.s = s;
  }
  public void run(){
    try {
      ois = new OC.MyObjectInputStream(s.getInputStream());
      //os = new OC.MyObjectOutputStream(s.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    try{
      while (s.isConnected()){
        Message message = null;
        try {
          message = (Message) ois.readObject();
          System.out.println("DataContent:" +message.getData());

        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        if (message != null ){
          //对客户端的消息进行分析并作出相应的动作
          switch (message.getType()){
            case CONNECT:
              checkConnect(message);
              System.out.println("CONNECTsd");
              break;
            case DISCONNECT:
              closeConnect(message);
              System.out.println("DISCONNECTsd");
              break;
            case MSG:
              sendMSG(message);
              System.out.println("MSGsd");
              break;
            case QUERY:
              sendUserInfoList();
              System.out.println("QUERYsd");
              break;
            case SINGLEREQUIRE:
              newPrivateChat(message);
              System.out.println("SINGLEREQUIREsd");
              break;
            case SINGLE:
              recordMsg(message);
              System.out.println("SINGLEsd");
              break;
            case CHATSHEETNEW:
              sendSingleChatMessage(message);
              System.out.println("CHATSHEETNEWsd");
              break;
            case QUERYCHATSHEET:
              sendQueryChatMessage(message);
              System.out.println("QUERYCHATSHEETsd");
            case USERLIST:
              sendUserInfoList();
              break;
            case QUERYCHAT:
              getQueryMsg(message);
              System.out.println("QUERYCHATsd");
              break;
            case QUERYMSG:
              recordQueryMsg(message);
              System.out.println("QUERYMSGsd");
              break;
            case FINDONRQUERYMEMBER:
              getOneQueryMember(message);
              break;
            case FILE:
              sendFile(message);
              break;
            default:
              System.out.println("没识别成功");
              break;
          }
        }
      }
    }
    catch (IOException e){
      System.out.println("捕捉到异常：" + s);
      e.printStackTrace();
    }
  }
  public void sendSingleChatMessage(Message message){
    Message m = new Message();
    if (!Server.twoNameToMsgList.containsKey(message.getSentBy())||!Server.twoNameToMsgList.get(message.getSentBy()).containsKey(message.getSendTo())) {
      m.setType(MessageType.CHATSHEETNEW);
      try {
        send(m, Server.socketsfromUserNames.get(message.getSentBy()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      m.chatMessage.addAll(Server.twoNameToMsgList.get(message.getSentBy()).get(message.getSendTo()));
      m.setType(MessageType.CHATSHEETNEW);
      try {
        send(m, Server.socketsfromUserNames.get(message.getSentBy()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void sendQueryChatMessage(Message message){
    Message m =new Message();
    if (!Server.totalQueryMSG.containsKey(message.getSendTo())){
      m.setType(MessageType.QUERYCHATSHEET);
      try {
        send(m, Server.socketsfromUserNames.get(message.getSentBy()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    else {
      m.queryChatMsg.addAll(Server.totalQueryMSG.get(message.getSendTo()));
      m.setType(MessageType.QUERYCHATSHEET);
      try {
        send(m, Server.socketsfromUserNames.get(message.getSentBy()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void recordMsg(Message message){
    String from = message.getSentBy();
    String to = message.getSendTo();
    if (Server.twoNameToMsgList.containsKey(from)&&Server.twoNameToMsgList.get(from).containsKey(to)){
      Server.twoNameToMsgList.get(from).get(to).add(message);
      Server.twoNameToMsgList.get(to).get(from).add(message);
    }
    else if (!Server.twoNameToMsgList.containsKey(from)&&!Server.twoNameToMsgList.containsKey(to)){
      ArrayList<Message> m = new ArrayList<>();
      ArrayList<Message> n = new ArrayList<>();
      m.add(message);
      n.add(message);
      Map<String,ArrayList<Message>> a = new HashMap<>();
      a.put(to,m);
      Map<String,ArrayList<Message>> b = new HashMap<>();
      b.put(from,n);
      Server.twoNameToMsgList.put(from,a);
      Server.twoNameToMsgList.put(to,b);
    }
    else if ((Server.twoNameToMsgList.containsKey(from)&&!Server.twoNameToMsgList.get(from).containsKey(to)&&!Server.twoNameToMsgList.containsKey(to))){
      ArrayList<Message> m = new ArrayList<>();
      ArrayList<Message> n = new ArrayList<>();
      m.add(message);
      n.add(message);
      Server.twoNameToMsgList.get(from).put(to,m);
      Map<String,ArrayList<Message>> b = new HashMap<>();
      b.put(from,n);
      Server.twoNameToMsgList.put(to,b);
    }
    else if ((!Server.twoNameToMsgList.get(to).containsKey(from)&&Server.twoNameToMsgList.containsKey(to)&&!Server.twoNameToMsgList.containsKey(from))){
      ArrayList<Message> m = new ArrayList<>();
      ArrayList<Message> n = new ArrayList<>();
      m.add(message);
      n.add(message);
      Server.twoNameToMsgList.get(to).put(from,m);
      Map<String,ArrayList<Message>> b = new HashMap<>();
      b.put(to,n);
      Server.twoNameToMsgList.put(from,b);
    }
    else if (Server.twoNameToMsgList.containsKey(to)&&Server.twoNameToMsgList.containsKey(from)&&!Server.twoNameToMsgList.get(to).containsKey(from)){
      ArrayList<Message> m = new ArrayList<>();
      ArrayList<Message> n = new ArrayList<>();
      m.add(message);
      n.add(message);
      Server.twoNameToMsgList.get(to).put(from,m);
      Server.twoNameToMsgList.get(from).put(to,n);
    }
    message.chatMessage = Server.twoNameToMsgList.get(from).get(to);
    try {
      System.out.println(message.getData());
      send(message,Server.socketsfromUserNames.get(message.getSentBy()));
      send(message,Server.socketsfromUserNames.get(message.getSendTo()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void newPrivateChat(Message message){
    String from;
    String to;
    from = message.getSentBy();
    to = message.getSendTo();
    Socket socket1 = Server.socketsfromUserNames.get(from);
    Socket socket2 = Server.socketsfromUserNames.get(to);
    Message message1 = new Message();
    Message message2 = new Message();
    message1.setType(MessageType.SINGLEREQUIRE);
    message2.setType(MessageType.SINGLEREQUIRE);
    message1.setSentBy(from);
    message1.setSendTo(to);
    message2.setSentBy(to);
    message2.setSendTo(from);
    try {
      send(message1,socket1);
      System.out.println("数据发送1");
      send(message2,socket2);
      System.out.println("数据发送2");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
  public void send(Message message,Socket socket) throws IOException{
    OC.MyObjectOutputStream os = new OC.MyObjectOutputStream(socket.getOutputStream());
    os.writeObject(message);
    os.flush();
    System.out.println("send数据发送完成");
  }
  public void sendAll(Message message,boolean isRemoveLocalUser) throws IOException{
    PrintStream ps =null;
    if (isRemoveLocalUser){
      for (Map.Entry<String,Socket> entry : Server.socketsfromUserNames.entrySet()){
        if (!entry.getKey().equals(message.getSentBy())){
          ObjectOutputStream os = new ObjectOutputStream(entry.getValue().getOutputStream());
          os.writeObject(message);
        }
      }
    }
    else {
      for (Socket socket : Server.socketsfromUserNames.values()){
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        os.writeObject(message);
      }
    }
    System.out.println("数据发送完成");
  }
  public  void checkConnect(Message message) throws IOException{
    String username = message.getSentBy();
    if (!Server.socketsfromUserNames.containsKey(username)){
      Server.socketsfromUserNames.put(username,s);
      Server.userInfoList.add(message.getSentBy());
      System.out.println("用户登陆成功");
      Message sResult = new Message();
      sResult.setType(MessageType.SUCCESS);
      sResult.setSentBy(username);
      sResult.setUserList(Server.userInfoList);
      sResult.setData(username + " connect successfully!");
      for (String str:Server.userInfoList){
        send(sResult,Server.socketsfromUserNames.get(str));
      }

    }else {
      Message fResult = new Message();
      fResult.setType(MessageType.FAIL);
      fResult.setData(message.getSentBy() + "connect fail");
      send(fResult,s);
    }
  }
  public  void closeConnect(Message message) throws IOException{
    String userName = message.getSentBy();
    Socket socket = Server.socketsfromUserNames.get(userName);
    if (socket != null){
      Server.socketsfromUserNames.remove(userName);
      for (String user : Server.userInfoList){
        if (user.equals(userName)){
          Server.userInfoList.remove(user);
          break;
        }
      }
    }
    System.out.println(Server.socketsfromUserNames);
    System.out.println(Server.userInfoList);
    //创建用户集反馈信息
    for (String str : Server.userInfoList){
      Message m = new Message();
      m.setUserList(Server.userInfoList);
      m.setType(MessageType.USERLIST);
      send(m,Server.socketsfromUserNames.get(str));
    }
    for (String str:Server.memberInQuery.keySet()){
      Server.memberInQuery.get(str).remove(userName);
    }
    //sendUserInfoList();
    //发送用户下线通知

  }
  public  void sendUserInfoList() throws IOException{
    if (Server.userInfoList.isEmpty()) return;
    Message uResult = new Message();
    uResult.setUserList(Server.userInfoList);
    uResult.setType(MessageType.USERLIST);
      send(uResult,s);
  }




  public void sendNotification(boolean isAllUsers, String notices) throws IOException{
    Message message = new Message();
    message.setType(MessageType.NOTIFICATION);
    message.setSentBy(notices);
    if (isAllUsers) sendAll(message,false);
    else send(message,s);
  }
  public void sendMSG(Message message) throws IOException{
    String username = message.getSentBy();
    String toUser = message.getSendTo();
    sendAll(message,true);
  }

  public void getQueryMsg(Message message){
    List<String> s = new ArrayList<>(message.sendToLIst);
    s.add(message.getSentBy());
    Server.memberInQuery.put(message.queryName,s);
    Message m = new Message();
    m.queryName = message.queryName;
    m.setType(MessageType.QUERYCHAT);
    m.setUserList(Server.userInfoList);
    for (String str : s){
      try {
        send(m,Server.socketsfromUserNames.get(str));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void recordQueryMsg(Message message){
    if (!Server.totalQueryMSG.containsKey(message.getSendTo())){//不存在群名
      ArrayList<Message> a = new ArrayList<>();
      a.add(message);
      Server.totalQueryMSG.put(message.getSendTo(),a);
    }
    else {
      Server.totalQueryMSG.get(message.getSendTo()).add(message);
    }
    message.queryChatMsg = Server.totalQueryMSG.get(message.getSendTo());
    for(String s:Server.memberInQuery.get(message.getSendTo())){
      try {
        send(message,Server.socketsfromUserNames.get(s));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

 public void getOneQueryMember(Message message){
    Message m = new Message();
    m.setType(MessageType.FINDONRQUERYMEMBER);
    m.setUserList((ArrayList<String>) Server.memberInQuery.get(message.getSendTo()));
   try {
     send(m,Server.socketsfromUserNames.get(message.getSentBy()));
   } catch (IOException e) {
     e.printStackTrace();
   }
 }
 public void sendFile(Message message){
   try {
     send(message,Server.socketsfromUserNames.get(message.getSendTo()));
   } catch (IOException e) {
     e.printStackTrace();
   }
 }



}