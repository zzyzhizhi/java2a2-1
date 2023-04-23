package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.OC;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public static HashMap<String, Socket> socketsfromUserNames = new HashMap<>();
    public static  Map<String,Map<String, ArrayList<Message>>> twoNameToMsgList = new HashMap<>();
    public static Map<String,ArrayList<Message>> totalQueryMSG = new HashMap<>();
    //
    public static ArrayList<String> userInfoList = new ArrayList<>();
    public static HashMap<String, List<String>> memberInQuery = new HashMap<>();
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket ss = new ServerSocket(4669);
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
}
