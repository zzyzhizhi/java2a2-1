package cn.edu.sustech.cs209.chatting.common;

public class Post {
  String id;
  String content;
  String author;

  public Post(String id, String content, String author) {
    this.id = id;
    this.content = content;
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
