package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.OC;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable {

  private static OC.MyObjectOutputStream os = null;
  private Socket socket = new Socket();
  private String username;
  private Client client;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 32, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));
//
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Register");
    dialog.setHeaderText(null);

// Set the button types.
    ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the labels and text fields for input
    Label nameLabel = new Label("Name:");
    TextField nameField = new TextField();
    nameField.setPromptText("Name");

    Label idLabel = new Label("ID:");
    TextField idField = new TextField();
    idField.setPromptText("ID");

    Label phoneLabel = new Label("Phone ID:");
    TextField phoneField = new TextField();
    phoneField.setPromptText("Phone ID");

    Label birthLabel = new Label("Birth Date:");
    TextField birthField = new TextField();
    birthField.setPromptText("Birth Date");

// Create a grid pane and add the labels and text fields to it
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(nameLabel, 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(idLabel, 0, 1);
    grid.add(idField, 1, 1);
    grid.add(phoneLabel, 0, 2);
    grid.add(phoneField, 1, 2);
    grid.add(birthLabel, 0, 3);
    grid.add(birthField, 1, 3);

// Add the grid pane to the dialog
    dialog.getDialogPane().setContent(grid);

// Request focus on the first text field by default
    Platform.runLater(() -> nameField.requestFocus());

// Convert the result to a map of entered values when the OK button is clicked
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        Map<String, String> result = new HashMap<>();
        result.put("name", nameField.getText());
        result.put("id", idField.getText());
        result.put("phoneId", phoneField.getText());
        result.put("birthDate", birthField.getText());
        return result;
      }
      return null;
    });

    Optional<Map<String, String>> result = dialog.showAndWait();

    result.ifPresent(values -> {
      System.out.println("Name: " + values.get("name"));
      System.out.println("ID: " + values.get("id"));
      System.out.println("Phone ID: " + values.get("phoneId"));
    });

//    Dialog<String> dialog = new TextInputDialog();
//    dialog.setTitle("Register");
//    dialog.setHeaderText(null);
//    dialog.setContentText("Username:");

    if (result.isPresent() && !result.get().isEmpty()) {
      try {
        username = result.get().get("name");
        socket = new Socket("localhost", 4669);
        client = new Client(socket, username, this);
        threadPoolExecutor.execute(client);
        Message message = new Message();
        message.setSentBy(username);
        message.ID = result.get().get("id");
        message.phoneID = result.get().get("phoneId");
        message.BirthDate = result.get().get("birthDate");
        os = client.getOs();
        message.setType(MessageType.CONNECT);
        client.getOs().writeObject(message);
        client.getOs().flush();
        System.out.println("fasong");
      } catch (IOException e) {
        e.printStackTrace();
      }

    } else {
      System.out.println("Invalid username " + result + ", exiting");
      Platform.exit();
    }
  }

  @FXML
  public void newReplyToReply(){
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle("newReplyToPost");
    dialog.setHeaderText(null);

// Set the button types.
    ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the labels and text fields for input
    Label contentLabel = new Label("content:");
    TextField contentField = new TextField();
    contentField.setPromptText("content");

    Label starsLabel = new Label("stars:");
    TextField starsField = new TextField();
    starsField.setPromptText("stars");

    Label idLabel = new Label("replyId:");
    TextField idField = new TextField();
    idField.setPromptText("replyId");



// Create a grid pane and add the labels and text fields to it
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(idLabel, 0, 0);
    grid.add(idField, 1, 0);
    grid.add(starsLabel, 0, 1);
    grid.add(starsField, 1, 1);
    grid.add(contentLabel, 0, 2);
    grid.add(contentField, 1, 2);

// Add the grid pane to the dialog
    dialog.getDialogPane().setContent(grid);

// Request focus on the first text field by default
    Platform.runLater(() -> starsField.requestFocus());

// Convert the result to a map of entered values when the OK button is clicked
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        Map<String, String> result = new HashMap<>();
        result.put("replyId", idField.getText());
        result.put("stars", starsField.getText());
        result.put("content", contentField.getText());
        return result;
      }
      return null;
    });

    Optional<Map<String, String>> result = dialog.showAndWait();

    result.ifPresent(values -> {
      System.out.println("replyId: " + values.get("replyId"));
      System.out.println("stars: " + values.get("stars"));
      System.out.println("content: " + values.get("content"));
    });

    if (result.isPresent() && !result.get().isEmpty()) {
      try {
        Message message = new Message();
        message.setSentBy(username);
        message.replyId = Integer.parseInt(result.get().get("replyId"));
        message.content = result.get().get("content");
        message.stars = Integer.parseInt(result.get().get("stars"));
        os = client.getOs();
        message.setType(MessageType.newReplyToReply);
        client.getOs().writeObject(message);
        client.getOs().flush();
        System.out.println("fasong");
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  @FXML
  public void getMyPost(){
    Message message = new Message();
    message.setType(MessageType.getMyPost);
    message.setSentBy(username);
    try {
      os.writeObject(message);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void getMyReply(){
    Message message = new Message();
    message.setType(MessageType.getMyReply);
    message.setSentBy(username);
    try {
      os.writeObject(message);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void newReplyToPost(){
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle("newReplyToPost");
    dialog.setHeaderText(null);

// Set the button types.
    ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the labels and text fields for input
    Label contentLabel = new Label("content:");
    TextField contentField = new TextField();
    contentField.setPromptText("content");

    Label starsLabel = new Label("stars:");
    TextField starsField = new TextField();
    starsField.setPromptText("stars");

    Label titleLabel = new Label("title:");
    TextField titleField = new TextField();
    titleField.setPromptText("title");



// Create a grid pane and add the labels and text fields to it
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(titleLabel, 0, 0);
    grid.add(titleField, 1, 0);
    grid.add(starsLabel, 0, 1);
    grid.add(starsField, 1, 1);
    grid.add(contentLabel, 0, 2);
    grid.add(contentField, 1, 2);

// Add the grid pane to the dialog
    dialog.getDialogPane().setContent(grid);

// Request focus on the first text field by default
    Platform.runLater(() -> starsField.requestFocus());

// Convert the result to a map of entered values when the OK button is clicked
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        Map<String, String> result = new HashMap<>();
        result.put("title", titleField.getText());
        result.put("stars", starsField.getText());
        result.put("content", contentField.getText());
        return result;
      }
      return null;
    });

    Optional<Map<String, String>> result = dialog.showAndWait();

    result.ifPresent(values -> {
      System.out.println("stars: " + values.get("stars"));
      System.out.println("content: " + values.get("content"));
    });

    if (result.isPresent() && !result.get().isEmpty()) {
      try {
        Message message = new Message();
        message.setSentBy(username);
        message.PostName = result.get().get("title");
        message.content = result.get().get("content");
        message.stars = Integer.parseInt(result.get().get("stars"));
        os = client.getOs();
        message.setType(MessageType.newReplyToPost);
        client.getOs().writeObject(message);
        client.getOs().flush();
        System.out.println("fasong");
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  @FXML
  public void newPost(){
    Dialog<Map<String, String>> dialog = new Dialog<>();
    dialog.setTitle("newPost");
    dialog.setHeaderText(null);

// Set the button types.
    ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the labels and text fields for input
    Label titleLabel = new Label("title:");
    TextField titleField = new TextField();
    titleField.setPromptText("title");

    Label contentLabel = new Label("content:");
    TextField contentField = new TextField();
    contentField.setPromptText("content");

    Label countryLabel = new Label("country:");
    TextField countryField = new TextField();
    countryField.setPromptText("country");

    Label cityLabel = new Label("city:");
    TextField cityField = new TextField();
    cityField.setPromptText("city");

// Create a grid pane and add the labels and text fields to it
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.add(titleLabel, 0, 0);
    grid.add(titleField, 1, 0);
    grid.add(contentLabel, 0, 1);
    grid.add(contentField, 1, 1);
    grid.add(countryLabel, 0, 2);
    grid.add(countryField, 1, 2);
    grid.add(cityLabel, 0, 3);
    grid.add(cityField, 1, 3);

// Add the grid pane to the dialog
    dialog.getDialogPane().setContent(grid);

// Request focus on the first text field by default
    Platform.runLater(() -> titleField.requestFocus());

// Convert the result to a map of entered values when the OK button is clicked
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == loginButtonType) {
        Map<String, String> result = new HashMap<>();
        result.put("title", titleField.getText());
        result.put("content", contentField.getText());
        result.put("country", countryField.getText());
        result.put("city", cityField.getText());
        return result;
      }
      return null;
    });

    Optional<Map<String, String>> result = dialog.showAndWait();

    result.ifPresent(values -> {
      System.out.println("title: " + values.get("title"));
      System.out.println("content: " + values.get("content"));
      System.out.println("country: " + values.get("country"));
    });

    if (result.isPresent() && !result.get().isEmpty()) {
      try {
        Message message = new Message();
        message.setSentBy(username);
        message.title = result.get().get("title");
        message.content = result.get().get("content");
        message.country = result.get().get("country");
        message.city = result.get().get("city");
        os = client.getOs();
        message.setType(MessageType.newPost);
        client.getOs().writeObject(message);
        client.getOs().flush();
        System.out.println("fasong");
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  @FXML
  public void showMyReply(Message message){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("MyReplyList");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("content");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.MyReply);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();
      }
    });
  }

  @FXML
  public void showMyFollowList(Message message){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("MyFollowList");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("account_name");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.MyFollowList);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();
      }
    });
  }

  @FXML
  public void showShareList(Message message){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("ShareList");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("account_name");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.shareList);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();

      }
    });
  }

  @FXML
  public void addFollow(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.addFollow);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("增加关注");
        dialog.setHeaderText(null);
        dialog.setContentText("AccountName:");
        Optional<String> input = dialog.showAndWait();
        String accountName = input.get();
        message.addFollowName = accountName;
        System.out.println(accountName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void cancelFollow(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.cancelFollow);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("取消关注");
        dialog.setHeaderText(null);
        dialog.setContentText("AccountName:");
        Optional<String> input = dialog.showAndWait();
        String accountName = input.get();
        message.cancelFollowName = accountName;
        System.out.println(accountName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void requireWhoIFollow(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.requireIFollow);
    try {
      os.writeObject(message);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void requireWhoSharePost(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.favoriteList);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("分享列表");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void showFavoriteList(Message message){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("FavoriteList");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("account_name");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.favoriteList);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();

      }
    });
  }

  @FXML
  public void requireWhoFavoritePost(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.favoriteList);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("收藏列表");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void showLikeList(Message message){
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("LikeList");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("account_name");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.likeList);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();

      }
    });
  }

  @FXML
  public void requireWhoLikePost(){
    Message message = new Message();
    message.setSentBy(username);
    message.setType(MessageType.likeList);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("点赞列表");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void Fail() {
    Platform.runLater(() -> {
      // Your JavaFX code here
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("注册失败，用户名重复");
      alert.showAndWait();
    });
  }

  @FXML
  public void showMyPost(Message message){
    // 创建一个新的 Stage
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("My Post");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("PostName");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.MyPost);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();

        table.setOnMouseClicked(event -> {
          if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 2) {
            // 获取所选行
            String selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
              // 将所选行的数据复制到剪贴板中
              ClipboardContent content = new ClipboardContent();
              content.putString(selectedItem);
              Clipboard.getSystemClipboard().setContent(content);
            }
          }
        });


      }
    });

  }

  @FXML
  public void showAllPostName(Message message) {
    // 创建一个新的 Stage
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Stage newStage = new Stage();
        newStage.setTitle("My Table View");
// 创建一个 TableView
        TableView<String> table = new TableView<>();
// ... 配置您的 TableView
        TableColumn<String, String> stringColumn = new TableColumn<>("PostName");
        //stringColumn.setCellValueFactory(new PropertyValueFactory<>("PostName"));
        stringColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        table.getColumns().addAll(stringColumn);
        ObservableList<String> data = FXCollections.observableArrayList(message.AllPost);
        table.setItems(data);
// 将 TableView 添加到布局中
        VBox layout = new VBox(table);
// 创建一个新的 Scene 并将其设置为新 Stage 的场景
        Scene scene = new Scene(layout);
        newStage.setScene(scene);
// 显示新 Stage
        newStage.show();

        table.setOnMouseClicked(event -> {
          if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 2) {
            // 获取所选行
            String selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
              // 将所选行的数据复制到剪贴板中
              ClipboardContent content = new ClipboardContent();
              content.putString(selectedItem);
              Clipboard.getSystemClipboard().setContent(content);
            }
          }
        });


      }
    });


  }


  @FXML
  public void getAllPostName() {
    Message message = new Message();
    message.setType(MessageType.requireAllPost);
    message.setSentBy(username);
    try {
      os.writeObject(message);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void giveLike() {
    Message message = new Message();
    message.setType(MessageType.giveLike);
    message.setSentBy(username);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Choose");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @FXML
  public void giveShare() {
    Message message = new Message();
    message.setType(MessageType.share);
    message.setSentBy(username);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Choose");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
  @FXML
  public void giveFavorite() {
    Message message = new Message();
    message.setType(MessageType.favorite);
    message.setSentBy(username);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Choose");
        dialog.setHeaderText(null);
        dialog.setContentText("PostName:");
        Optional<String> input = dialog.showAndWait();
        String postName = input.get();
        message.PostName = postName;
        System.out.println(postName);
        try {
          os.writeObject(message);
          os.flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }
}
