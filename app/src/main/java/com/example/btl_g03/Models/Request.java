package com.example.btl_g03.Models;

import java.util.Date;

public class Request {
    private String requestId;
    private String postId; // Nhu yếu phẩm được yêu cầu
    private String requesterId; // Người yêu cầu
    private Date requestDate;
    private String message;

    public Request() {
    }

    public Request(String requestId, String postId, String requesterId, Date requestDate, String message) {
        this.requestId = requestId;
        this.postId = postId;
        this.requesterId = requesterId;
        this.requestDate = requestDate;
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
