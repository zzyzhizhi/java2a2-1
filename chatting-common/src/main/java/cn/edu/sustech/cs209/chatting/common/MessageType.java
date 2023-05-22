package cn.edu.sustech.cs209.chatting.common;

public enum MessageType {
  CONNECT,SUCCESS,FAIL,
  requireAllPost,giveLike,share,favorite,
  likeList,shareList,favoriteList,
  addFollow,cancelFollow,requireIFollow,
  newPost,newReplyToPost,newReplyToReply,
  getMyPost,getMyReply,
  PostDetail

}