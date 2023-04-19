package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.OC;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Server {
    public static HashMap<String, Socket> socketsfromUserNames = new HashMap<>();
    public static LinkedList<String> userInfoList = new LinkedList<>();
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket ss = new ServerSocket(50000);
        try{
            while(true){
                Socket s = ss.accept();
                new Thread(new ServerThread(s)).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ss.close();
    }

    public static class ServerThread implements Runnable{
        private static Socket s = null;
        private BufferedReader br = null;
        private InputStream in = null;
        private static OC.MyObjectOutputStream os = null;
        private static OC.MyObjectInputStream ois = null;

        public  ServerThread(Socket s)throws IOException{
            ServerThread.s = s;

        }

        public void run(){
            try {
                ois = new OC.MyObjectInputStream(s.getInputStream());
                os = new OC.MyObjectOutputStream(s.getOutputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }

            try{

                if (!s.isClosed()){
                    System.out.println(s + "用户已连接服务器！下一步将判断是否能登录成功..");
                }

                while (s.isConnected()){//

                    //String revString = br.readLine();
                    Message message = null;
                    try {
                        System.out.println("sd");
                        message = (Message) ois.readObject();

                        //ObjectInputStream ois = new ObjectInputStream(s.getInputStream());

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (message != null ){
                       // Message message = gson.fromJson(revString, Message.class);
                        //对客户端的消息进行分析并作出相应的动作
                        switch (message.getType()){
                            case CONNECT:
                                checkConnect(message);
                                break;
                            case DISCONNECT:
                                closeConnect(message);
                                break;
                            case MSG:
                                sendMSG(message);
                                break;
                            case QUERY:
                                sendUserInfoList(false);
                                break;
                            default:
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


        public static void send(Message message,Socket socket) throws IOException{
           // String messagesString = gson.toJson(message);
            //System.out.println(messagesString);
            //ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            //PrintStream ps = new PrintStream(socket.getOutputStream());
            //ps.println(messagesString);
            os.writeObject(message);
            System.out.println("数据发送完成");
        }

        public static void sendAll(Message message,boolean isRemoveLocalUser) throws IOException{
            //String messagesString = gson.toJson(message);
            //System.out.println(messagesString);
            PrintStream ps =null;
            //ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            if (isRemoveLocalUser){
                for (Map.Entry<String,Socket> entry : socketsfromUserNames.entrySet()){
                    if (!entry.getKey().equals(message.getSentBy())){
                        ps = new PrintStream(entry.getValue().getOutputStream());
                        ObjectOutputStream os = new ObjectOutputStream(entry.getValue().getOutputStream());
                        os.writeObject(message);
                        //ps.println(messagesString);
                    }
                }
            }
            else {
                for (Socket socket : socketsfromUserNames.values()){
//                    ps = new PrintStream(socket.getOutputStream());
//                    ps.println(messagesString);
                    ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                    os.writeObject(message);
                }
            }
            System.out.println("数据发送完成");
        }

        public static void checkConnect(Message message) throws IOException{
            String username = message.getSentBy();
            if (!socketsfromUserNames.containsKey(username)){
                socketsfromUserNames.put(username,s);
//                if (!message.getUserlist().isEmpty())
//                userInfoList.addAll(message.getUserlist());
                userInfoList.add(message.getSentBy());
                System.out.println("用户登陆成功");
                Message sResult = new Message();
                sResult.setType(MessageType.SUCCESS);
                sResult.setSentBy(username);
                sResult.setUserlist(userInfoList);
                sResult.setData(username + " connect successfully!");
                send(sResult,s);
                /////////
//                sendUserInfoList(true);
//                sendNotification(true,username+"online");

            }else {
                Message fResult = new Message();
                fResult.setType(MessageType.FAIL);
                fResult.setData(message.getSentBy() + "connect fail");
                send(fResult,s);
            }
        }

        public static void closeConnect(Message message) throws IOException{
            String userName = message.getSentBy();
            Socket socket = socketsfromUserNames.get(userName);
            if (socket != null){
                socketsfromUserNames.remove(userName);
                for (String user : userInfoList){
                    if (user.equals(userName)){
                        userInfoList.remove(user);
                        break;
                    }
                }
            }
            System.out.println(socketsfromUserNames);
            System.out.println(userInfoList);
            //创建用户集反馈信息
            sendUserInfoList(true);

            //发送用户下线通知
            sendNotification(true,userName+"offline");
        }

        public static void sendUserInfoList(boolean isAllUsers) throws IOException{
            if (userInfoList.isEmpty()) return;
            Message uResult = new Message();
            uResult.setType(MessageType.USERLIST);
            //uResult.setUserlist(userInfoList);
            if (isAllUsers){
                sendAll(uResult,false);
            }
            else {
                send(uResult,s);
            }
        }

        public static void sendNotification(boolean isAllUsers, String notices) throws IOException{
            Message message = new Message();
            message.setType(MessageType.NOTIFICATION);
            message.setSentBy(notices);
            if (isAllUsers) sendAll(message,false);
            else send(message,s);
        }

        public static void sendMSG(Message message) throws IOException{
            String username = message.getSentBy();
            String toUser = message.getSendTo();
            sendAll(message,true);
        }







    }


}
