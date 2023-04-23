package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OC;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

  @FXML
  private ListView<Message> chatContentList;

  @FXML
  private ListView<String> chatList;

  @FXML
  private Label currentOnlineCnt;

  @FXML
  private Button exit;

  @FXML
  private Label currentUsername;

  @FXML
  private TextArea inputArea;

  @FXML
  private Button emojiButton;

  @FXML
  private Button sendFile;

  private Socket socket = new Socket();

  private String username;

  private Message msg = new Message();

  public ArrayList<String> currentUsers = new ArrayList<>();
  
  public ArrayList<String> currentQuery = new ArrayList<>();

  public ArrayList<String> currentQueryMember = new ArrayList<>();

  private static OC.MyObjectOutputStream os = null;

  private Client client;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 32, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Login");
    dialog.setHeaderText(null);
    dialog.setContentText("Username:");

    Optional<String> input = dialog.showAndWait();
    if (input.isPresent() && !input.get().isEmpty()) {
      try {
        username = input.get();
        socket = new Socket("localhost", 4669);
        client = new Client(socket, username, this);
        threadPoolExecutor.execute(client);
        Message message = new Message();
        message.setSentBy(input.get());
        os = client.getOs();
        message.setType(MessageType.CONNECT);
        client.getOs().writeObject(message);
        client.getOs().flush();
        System.out.println("fasong");
      } catch (IOException e) {
        e.printStackTrace();
      }
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
    } else {
      System.out.println("Invalid username " + input + ", exiting");
      Platform.exit();
    }
    chatContentList.setCellFactory(new MessageCellFactory());

    chatList.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
          System.out.println("Left clicked");
          // 在这里添加您的代码，以处理鼠标双击事件
          String str = String.valueOf(chatList.getSelectionModel().getSelectedItems());
          str = str.substring(1,str.length()-1);
          msg.setSentBy(username);
          msg.setSendTo(str);
          os = client.getOs();
          Message m = new Message();
          m.setSentBy(username);
          m.setSendTo(str);
          if (str.contains("...(")){
            msg.setType(MessageType.QUERYMSG);
            m.setType(MessageType.QUERYCHATSHEET);
          }
          else {
            msg.setType(MessageType.SINGLE);
            m.setType(MessageType.CHATSHEETNEW);
          }

            try {
              os.writeObject(m);
              os.flush();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }

        if (event.getButton() == MouseButton.SECONDARY){
          String str = String.valueOf(chatList.getSelectionModel().getSelectedItems());
          str = str.substring(1,str.length()-1);
          System.out.println("Right clicked");
          Message m = new Message();
          m.setType(MessageType.FINDONRQUERYMEMBER);
          m.setSentBy(username);
          m.setSendTo(str);
          try {
            os.writeObject(m);
            os.flush();
          } catch (IOException e) {
            e.printStackTrace();
          }
          ContextMenu contextMenu = new ContextMenu();
          for (String username : currentQueryMember) {
            MenuItem menuItem = new MenuItem(username);
            contextMenu.getItems().add(menuItem);
          }
          contextMenu.show(chatList,event.getScreenX(), event.getScreenY());
        }
      }
    });
    emojiButton.setOnAction(event -> {
      // 创建一个新的 Stage
      Stage emojiStage = new Stage();
      emojiStage.setTitle("选择 Emoji");
      // 创建一个 TilePane
      TilePane tilePane = new TilePane();
      Button smileyButton = new Button("\uD83D\uDE00");
      smileyButton.setOnAction(event1 -> {
        inputArea.appendText("\uD83D\uDE00");
        emojiStage.close();
      });
      tilePane.getChildren().add(smileyButton);
      Button grinButton = new Button("\uD83D\uDE01");
      grinButton.setOnAction(event1 -> {
        inputArea.appendText("\uD83D\uDE01");
        emojiStage.close();
      });
      tilePane.getChildren().add(grinButton);
      // 在这里添加更多的按钮
      // ...
      // 创建一个场景并将其设置到舞台上
      Scene scene = new Scene(tilePane);
      emojiStage.setScene(scene);
      // 显示舞台
      emojiStage.show();
    });

  }




  @FXML
  public void freshUsernameandCount(Message message){
    Platform.runLater(()-> {currentUsername.setText("Username: "+username);currentUsername.impl_updatePeer();});
    Platform.runLater(()-> {currentOnlineCnt.setText("Online: "+message.getUserList().size());currentOnlineCnt.impl_updatePeer();});
  }
  @FXML
  public void createPrivateChat() {
    Message m = new Message();
    m.setType(MessageType.USERLIST);
    try {
      os.writeObject(m);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    AtomicReference<String> user = new AtomicReference<>();
    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();
    ArrayList<String> a = new ArrayList<>();
    for (String s : currentUsers){
      if (!s.equals(username)) a.add(s);
    }
    userSel.getItems().addAll(a);
    // FIXME: get the user list from server, the current user's name should be filtered out

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      Message message = new Message();
      message.setSendTo(userSel.getSelectionModel().getSelectedItem());
      message.setSentBy(username);
      message.setType(MessageType.SINGLEREQUIRE);
      Message message1 = new Message();
      message1.setType((MessageType.SINGLEREQUIRE));
      message1.setSentBy(userSel.getSelectionModel().getSelectedItem());
      message1.setSendTo(username);
      try {
        client.getOs().writeObject(message);
        client.getOs().flush();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();
    // TODO: if the current user already chatted with the selected user, just open the chat with that user
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name.
   * You can select several users that will be joined in the group chat, including yourself.
   * <p>
   * The naming rule for group chats is similar to WeChat:
   * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
   * UserA, UserB, UserC... (10)
   * If there are <= 3 users: do not display the ellipsis, for example:
   * UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() {
    Stage stage = new Stage();
    stage.setTitle("选择用户");
    // 创建一个TilePane
    TilePane tilePane = new TilePane();
    // 创建一个标签
    Label label = new Label("选择用户：");
    // 用户名数组
    // 将签添加到TilePane中
    tilePane.getChildren().add(label);
    for (int i = 0; i < currentUsers.size(); i++) {
      // 为每个用户创建一个复选框
      if (!currentUsers.get(i).equals(username)) {
        CheckBox checkBox = new CheckBox(currentUsers.get(i));
        // 将复选框添加到TilePane中
        tilePane.getChildren().add(checkBox);
      }
    }
    // 创建一个按钮
    Button button = new Button("OK");
    tilePane.getChildren().add(button);
    Scene scene = new Scene(tilePane, 200, 200);
    stage.setScene(scene);
    stage.show();
    button.setOnAction(event -> {
      System.out.println("Button clicked");
      List<String> selectedUsers = new ArrayList<>();
      for (Node node : tilePane.getChildren()) {
        if (node instanceof CheckBox) {
          CheckBox checkBox = (CheckBox) node;
          if (checkBox.isSelected()) {
            selectedUsers.add(checkBox.getText());
          }
        }
      }
      String[] s = new String[selectedUsers.size()+1];
      for (int i = 0; i < selectedUsers.size(); i++) {
        s[i] = selectedUsers.get(i);
      }
      s[selectedUsers.size()] = username;
      Arrays.sort(s);
      String queryName = s[0]+","+s[1]+","+s[2]+"..."+"("+s.length+")";
      //currentQuery.add(queryName);
      Message m = new Message();
      m.sendToLIst.addAll(selectedUsers);
      m.setSentBy(username);
      m.queryName = queryName;
      m.setType(MessageType.QUERYCHAT);
      try {
        os.writeObject(m);
        System.out.println("QUERYCHAT发送");
        os.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
      stage.close();
    });
  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed.
   * After sending the message, you should clear the text input field.
   */



  @FXML
  public void doSendMessage() {
    // TODO
    String str = inputArea.getText();
    if (str.equals("")){
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("警告");
      alert.setHeaderText(null);
      alert.setContentText("不允许发送空消息");
      alert.showAndWait();
    }
    else {
      Message a = new Message();
      os = client.getOs();
      a.setData(str);
      a.setSentBy(msg.getSentBy());
      a.setType(msg.getType());
      a.setSendTo(msg.getSendTo());
      System.out.println(a.getData());
      try {
        os.writeObject(a);
        os.flush();
        System.out.println(a.getData());
      } catch (IOException e) {
        e.printStackTrace();
      }
      Platform.runLater(()->{
        inputArea.clear();
        inputArea.impl_updatePeer();
      });
    }

  }


  @FXML
  public void newChatList(Message message){
    //canTalkTo.add(message.getSendTo());
//    Message m = new Message();
//    m.setType(MessageType.USERLIST);
//    try {
//      os.writeObject(m);
//      os.flush();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    ArrayList<String> s = new ArrayList<>();
    for (String str : message.getUserList()){
      if (!str.equals(username)) s.add(str);
    }
    currentUsers.clear();
    currentUsers.addAll(message.getUserList());
    if (currentQuery!=null) s.addAll(currentQuery);
    ObservableList<String> str = FXCollections.observableArrayList(s);
    Platform.runLater(()-> {
      chatList.setItems(str);
      chatList.impl_updatePeer();
    });
  }

  public void freshChatSheet(ArrayList<Message> list){
    ObservableList<Message> str = FXCollections.observableArrayList(list);
    Platform.runLater(()->{
      chatContentList.setItems(str);
      chatContentList.impl_updatePeer();
    });
  }

  public void freshQueryChatSheet(ArrayList<Message> list){
    ObservableList<Message> str = FXCollections.observableArrayList(list);
    Platform.runLater(()->{
      chatContentList.setItems(str);
      chatContentList.impl_updatePeer();
    });
  }

  @FXML
  public void singleMsgFromServer(Message message) {
    if (message.chatMessage !=  null) {
      ObservableList<Message> str = FXCollections.observableArrayList(message.chatMessage);
      Platform.runLater(() -> {
        chatContentList.setItems(str);
        chatContentList.impl_updatePeer();
      });
    }
    else {
      ArrayList<Message> m = new ArrayList<>();
      ObservableList<Message> str = FXCollections.observableArrayList(m);
      Platform.runLater(() -> {
        chatContentList.setItems(str);
        chatContentList.impl_updatePeer();
      });
    }
  }

  @FXML
  public void queryMsgFromServer(Message message){
    if (message.queryChatMsg!=null){
      ObservableList<Message> str = FXCollections.observableArrayList(message.queryChatMsg);
      Platform.runLater(() -> {
        chatContentList.setItems(str);
        chatContentList.impl_updatePeer();
      });
    }
    else {
      ArrayList<Message> m = new ArrayList<>();
      ObservableList<Message> str = FXCollections.observableArrayList(m);
      Platform.runLater(() -> {
        chatContentList.setItems(str);
        chatContentList.impl_updatePeer();
      });
    }
  }




  /**
   * You may change the cell factory if you changed the design of {@code Message} model.
   * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
   */
  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(msg.getData());

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }



  @FXML
  public void sendFile(){
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showOpenDialog(null);
      if (file != null) {
        try {
          // 读取文件内容
          Message m = new Message();
          // 连接到服务器
          m.setSentBy(username);
          m.setSendTo(msg.getSendTo());
          m.setType(MessageType.FILE);
          m.content = Files.readAllBytes(file.toPath());
          m.fileName = file.getName();
          os.writeObject(m);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
  }

  public void exit(){
    Message m = new Message();
    m.setType(MessageType.DISCONNECT);
    m.setSentBy(username);
    try {
      os.writeObject(m);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    Stage stage = (Stage) exit.getScene().getWindow();
    stage.close();
  }



}
