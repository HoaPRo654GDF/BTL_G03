package com.example.btl_g03.Models;

import java.util.Date;

public class Notification {
    private String notificationId;
    private String userId; // Người nhận thông báo
    private String content;
    private Date createdDate;

    public Notification() {
    }

    public Notification(String notificationId, String userId, String content, Date createdDate) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.content = content;
        this.createdDate = createdDate;
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
}
