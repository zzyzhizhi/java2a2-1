package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.OC;

import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ServerThread implements Runnable {
  static Connection conn = null;
  private Socket s;
  private OC.MyObjectInputStream ois = null;

  public ServerThread(Socket s) {
    this.s = s;
  }

  public void run() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    // 连接到数据库
    String url = "jdbc:postgresql://localhost:5432/cs307";
    String username = "checker";
    String password = "123456";
    try {
      conn = DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    try {
      ois = new OC.MyObjectInputStream(s.getInputStream());
      //os = new OC.MyObjectOutputStream(s.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      while (s.isConnected()) {
        Message message = null;
        try {
          message = (Message) ois.readObject();
          //System.out.println("DataContent:" + message.getData());
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
        if (message != null) {
          //对客户端的消息进行分析并作出相应的动作
          switch (message.getType()) {
            case CONNECT:
              checkConnect(message);
              System.out.println("CONNECTsd");
              break;
            case requireAllPost:
              getAllPost(message);
              System.out.println("requireAllPost");
              break;
            case giveLike:
              addGiveLike(message);
              break;
            case share:
              addShare(message);
              break;
            case favorite:
              addFavorite(message);
              break;
            case likeList:
              getLikeList(message);
              break;
            case shareList:
              getShareList(message);
              break;
            case favoriteList:
              getFavoriteList(message);
              break;
            case addFollow:
              addFollow(message);
              break;
            case cancelFollow:
              cancelFollow(message);
              break;
            case requireIFollow:
              getIFollow(message);
              break;
            case newPost:
              createNewPost(message);
              break;
            case newReplyToPost:
              creatNewFirstReply(message);
              break;
            case newReplyToReply:
              creatNewSecondReply(message);
              break;
            case getMyPost:
              getPost(message);
              break;
            case getMyReply:
              getReply(message);
              break;
            default:
              System.out.println("没识别成功");
              break;
          }
        }
      }
    } catch (IOException e) {
      System.out.println("捕捉到异常：" + s);
      e.printStackTrace();
    }
  }

  public void getReply(Message message){
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql1 = "select reply_content from reply where reply_author = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setString(1,username);
      ResultSet rs = pstmt.executeQuery();
      s1.add("First Reply:");
      while (rs.next()) {
        s1.add(rs.getString("reply_content"));
      }
      s1.add("Second Reply:");
      rs.close();
      String sql2 = "select reply_content from sec_reply where reply_author = ?";
      pstmt = conn.prepareStatement(sql2);
      pstmt.setString(1,username);
      ResultSet rs1 = pstmt.executeQuery();
      while (rs1.next()) {
        s1.add(rs1.getString("reply_content"));
      }
      rs1.close();
      pstmt.close();
      Message m = new Message();
      m.MyReply = s1;
      m.setType(MessageType.getMyReply);
      try {
        send(m,s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void getPost(Message message){
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql1 = "select title from post where post_author = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setString(1,username);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("title"));
      }
      rs.close();
      pstmt.close();
      Message m = new Message();
      m.MyPost = s1;
      m.setType(MessageType.getMyPost);
      try {
        send(m,s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void creatNewSecondReply(Message message){
    String username = message.getSentBy();
    int replyId = message.replyId;
    PreparedStatement pstmt = null;
    try {
      String sql1 = "INSERT INTO sec_reply (reply_id, reply_content,stars,reply_author) VALUES (?, ?, ?,?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, replyId);
      pstmt.setString(2,message.content);
      pstmt.setInt(3,message.stars);
      pstmt.setString(4,username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void creatNewFirstReply(Message message){
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    List<String> s2 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql2 = "select count(*) from reply";
      pstmt = conn.prepareStatement(sql2);
      ResultSet rs1 = pstmt.executeQuery();
      while (rs1.next()) {
        s1.add(rs1.getString("count"));
      }
      rs.close();
      int id = Integer.parseInt(s1.get(1))+1;
      String sql1 = "INSERT INTO reply (id,post_id, reply_content,stars,reply_author) VALUES (?,?, ?, ?,?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1,id);
      pstmt.setInt(2, postId);
      pstmt.setString(3,message.content);
      pstmt.setInt(4,message.stars);
      pstmt.setString(5,username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void createNewPost(Message message) {
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    try {
      Message m = new Message();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      java.util.Date date = null;
      java.util.Date now = new java.util.Date();
      SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String currentTime = ft.format(now);
      try {
        date = sdf.parse(currentTime);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      long now_time = date.getTime();
      String sql1 = "INSERT INTO post (title, post_content,post_time,loc_country,loc_city,post_author) VALUES (?, ?, ?,?,?,?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setObject(1, message.title);
      pstmt.setObject(2, message.content);
      pstmt.setObject(3, new Timestamp(now_time));
      pstmt.setObject(4, message.country);
      pstmt.setObject(5, message.city);
      pstmt.setObject(6, username);
      pstmt.executeUpdate();
      pstmt.close();
      //conn.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Records created successfully");

  }

  public void getIFollow(Message message) {
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql = "SELECT followed FROM follow where follower = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, username);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("followed"));
      }
      rs.close();
      Message m = new Message();
      m.setType(MessageType.requireIFollow);
      m.MyFollowList = s1;
      m.setSendTo(username);
      try {
        send(m, s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void cancelFollow(Message message) {
    String username = message.getSentBy();
    String cancelfollowName = message.cancelFollowName;
    PreparedStatement pstmt = null;
    try {
      String sql1 = "delete from follow where followed = ? and follower = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setString(1, cancelfollowName);
      pstmt.setString(2, username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addFollow(Message message) {
    String username = message.getSentBy();
    String followName = message.addFollowName;
    PreparedStatement pstmt = null;
    try {
      String sql1 = "INSERT INTO follow (follower,followed) VALUES (?, ?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setString(1, username);
      pstmt.setString(2, followName);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void getShareList(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    List<String> s2 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql1 = "select account_name from share where post_id = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      ResultSet rs1 = pstmt.executeQuery();
      while (rs1.next()) {
        s2.add(rs1.getString("account_name"));
      }
      rs1.close();
      pstmt.close();
      Message m = new Message();
      m.setType(MessageType.shareList);
      m.shareList = s2;
      m.setSendTo(username);
      try {
        send(m, s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void getFavoriteList(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    List<String> s2 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql1 = "select account_name from favorite where post_id = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      ResultSet rs1 = pstmt.executeQuery();
      while (rs1.next()) {
        s2.add(rs1.getString("account_name"));
      }
      rs1.close();
      pstmt.close();
      Message m = new Message();
      m.setType(MessageType.favoriteList);
      m.favoriteList = s2;
      m.setSendTo(username);
      try {
        send(m, s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void getLikeList(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    List<String> s2 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql1 = "select account_name from give_like where post_id = ?";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      ResultSet rs1 = pstmt.executeQuery();
      while (rs1.next()) {
        s2.add(rs1.getString("account_name"));
      }
      rs1.close();
      pstmt.close();
      Message m = new Message();
      m.setType(MessageType.likeList);
      m.likeList = s2;
      m.setSendTo(username);
      try {
        send(m, s);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addFavorite(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql1 = "INSERT INTO favorite (post_id,account_name) VALUES (?, ?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      pstmt.setString(2, username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addShare(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("id"));
      }
      rs.close();
      int postId = Integer.parseInt(s1.get(0));
      String sql1 = "INSERT INTO share (post_id,account_name) VALUES (?, ?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      pstmt.setString(2, username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addGiveLike(Message message) {
    String username = message.getSentBy();
    String postname = message.PostName;
    System.out.println("title: " + postname);
    PreparedStatement pstmt = null;
    List<Integer> s1 = new ArrayList<>();
    try {
      String sql = "SELECT id FROM post where title = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, postname);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getInt("id"));
      }
      rs.close();
      int postId = s1.get(0);
      String sql1 = "INSERT INTO give_like (post_id,account_name) VALUES (?, ?)";
      pstmt = conn.prepareStatement(sql1);
      pstmt.setInt(1, postId);
      pstmt.setString(2, username);
      pstmt.executeUpdate();
      pstmt.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void getAllPost(Message message) {
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    List<String> s1 = new ArrayList<>();
    try {
      String sql = "SELECT title FROM post ";
      pstmt = conn.prepareStatement(sql);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        s1.add(rs.getString("title"));
      }
      rs.close();
      pstmt.close();
      //conn.close();
      Message m = new Message();
      m.AllPost = s1;
      m.setSentBy(username);
      m.setType(MessageType.requireAllPost);
      send(m, s);
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void checkConnect(Message message) throws IOException {
    String username = message.getSentBy();
    PreparedStatement pstmt = null;
    try {
      String sql = "SELECT * FROM account WHERE username = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setObject(1, username);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        Message m = new Message();
        m.setType(MessageType.FAIL);
        send(m, s);
      } else {
        Message m = new Message();
        m.setType(MessageType.SUCCESS);
        send(m, s);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        java.util.Date date = null;
        java.util.Date date1 = null;
        java.util.Date now = new java.util.Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = ft.format(now);
        try {
          date = sdf.parse(currentTime);
          date1 = sdf1.parse(message.BirthDate);
        } catch (ParseException e) {
          e.printStackTrace();
        }
        long register_time = date.getTime();
        long birtTime = date1.getTime();
        String sql1 = "INSERT INTO account (username, identity_card, phone, register_time,birth_time) VALUES (?, ?, ?, ?,?)";
        pstmt = conn.prepareStatement(sql1);
        pstmt.setObject(1, username);
        pstmt.setObject(2, message.ID);
        pstmt.setObject(3, message.phoneID);
        pstmt.setObject(4, new Timestamp(register_time));
        pstmt.setObject(5, new Timestamp(birtTime));
        pstmt.executeUpdate();
      }
      rs.close();
      pstmt.close();
      //conn.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Records created successfully");
  }

  public void send(Message message, Socket socket) throws IOException {
    OC.MyObjectOutputStream os = new OC.MyObjectOutputStream(socket.getOutputStream());
    os.writeObject(message);
    os.flush();
    System.out.println("send数据发送完成");
  }
}