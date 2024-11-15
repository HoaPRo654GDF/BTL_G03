package com.example.btl_g03.Models;

import java.util.Date;

public class Notification {
    private String notificationId;
    private String userId; // Người nhận thông báo
    private String content;
    private Date createdDate;
    private String postId;
    private String requestId;

    public Notification() {
    }

    public Notification(String notificationId, String userId, String content, Date createdDate,String postId,String requestId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.content = content;
        this.createdDate = createdDate;
        this.postId = postId;
        this.requestId = requestId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
