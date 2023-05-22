package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
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
