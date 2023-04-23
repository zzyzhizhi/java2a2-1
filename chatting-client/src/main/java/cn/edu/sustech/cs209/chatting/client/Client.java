package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OC;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Client implements Runnable{
  public boolean isconnect;
  private Socket socket ;
  private String username;
  private Controller chatController;
  private OC.MyObjectInputStream ois;
  private OC.MyObjectOutputStream os;


  public Client(Socket socket,String username,Controller chatController) throws IOException{
    //socket = new Socket("10.24.19.165",50000);
    this.socket = socket;
    this.username = username;
    this.chatController = chatController;
    os = new OC.MyObjectOutputStream(socket.getOutputStream());
    ois = new OC.MyObjectInputStream(socket.getInputStream());
  }

@Override
  public void run() {
    Message message ;
    try {
      while (true){

        try {
          message = (Message) ois.readObject();
          System.out.println("Clientsd");
          switch (message.getType()){
            case SUCCESS:
              chatController.newChatList(message);
              chatController.freshUsernameandCount(message);
              break;
            case FAIL:
              isconnect = false;
              break;
            case SINGLEREQUIRE:
                chatController.newChatList(message);
              System.out.println("SINGLEREQUIREsd");
              break;
            case SINGLE:
              chatController.singleMsgFromServer(message);
              System.out.println("SINGLEWsd");
              break;
            case CHATSHEETNEW:
              chatController.freshChatSheet(message.chatMessage);
              System.out.println("CHATSHEETNEWsd");
              break;
            case QUERYCHATSHEET:
              chatController.freshQueryChatSheet(message.queryChatMsg);
              System.out.println("QUERYCHATSHEETsd");
              break;
            case USERLIST:
              chatController.currentUsers.clear();
              chatController.currentUsers.addAll(message.getUserList());
              chatController.newChatList(message);
              chatController.freshUsernameandCount(message);
              break;
            case QUERYCHAT:
              chatController.currentQuery.add(message.queryName);
              chatController.newChatList(message);
              break;
            case QUERYMSG:
              chatController.queryMsgFromServer(message);
              System.out.println("QUERYMSGsd");
              break;
            case FINDONRQUERYMEMBER:
              chatController.currentQueryMember = message.userList;
              break;
            case FILE:
              downloadFile(message);
              break;

          }
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }catch (SocketException e){
          System.out.println("服务器已断开");
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
public void downloadFile(Message message){
  FileOutputStream fos = null;
  String fileName = message.fileName;
  try {
    fos = new FileOutputStream("C:\\Users\\32362\\Desktop\\"+fileName);
  } catch (FileNotFoundException e) {
    e.printStackTrace();
  }
  assert fos != null;
  BufferedOutputStream bos = new BufferedOutputStream(fos);
    try {
      bos.write(message.content, 0, message.content.length);
      bos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
}


  public OC.MyObjectOutputStream getOs(){
    return os;
  }
}

