package com.example.btl_g03.Models;

import java.util.Date;

public class Post {

    private String postId;
    private String userId; // Người đăng bài
    private String title;
    private String description;
    private String category; // Loại nhu yếu phẩm
    private String imageUrl;
    private Date postDate;
    private boolean isAvailable;
    private double latitude;
    private double longitude;




    private PostType postType; // Xác định bài đăng là "chia sẻ" hay "nhận"


    public Post() {
    }

    public Post(String postId, String userId, String title, String description, String category, String imageUrl, Date postDate, boolean isAvailable,PostType postType,double latitude, double longitude) {
        this.postId = postId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
        this.postDate = postDate;
        this.isAvailable = isAvailable;
        this.postType = postType;
        this.latitude = latitude;
        this.longitude = longitude;


    }
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

