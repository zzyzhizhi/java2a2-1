package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OC;

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
            case FAIL:
              chatController.Fail();
              break;
            case requireAllPost:
              chatController.showAllPostName(message);
              break;
            case likeList:
              chatController.showLikeList(message);
              break;
            case shareList:
              chatController.showShareList(message);
              break;
            case favoriteList:
              chatController.showFavoriteList(message);
              break;
            case requireIFollow:
              chatController.showMyFollowList(message);
              break;
            case getMyPost:
              chatController.showMyPost(message);
              break;
            case getMyReply:
              chatController.showMyReply(message);
              break;
            case PostDetail:
              chatController.showPostDetail(message);
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

  public OC.MyObjectOutputStream getOs(){
    return os;
  }
}

