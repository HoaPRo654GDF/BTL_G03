package com.example.btl_g03.Models;
import org.mindrot.jbcrypt.BCrypt;
public class User {
    private String userId;

    private String email;
    private String passwordHash;
    private String phoneNumber;
    private String address;
    private String fullName;
    private String profileImageUrl;



    public User() {
    }

    public User(String userId,String email,String passwordHash ,String fullName, String phoneNumber, String address, String profileImageUrl) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImageUrl = profileImageUrl;


    }


    public boolean checkPassword(String password) {
        // Kiểm tra mật khẩu người dùng nhập vào so với mật khẩu đã mã hóa
        return BCrypt.checkpw(password, this.passwordHash);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password) {
        // Mã hóa mật khẩu trước khi lưu vào Firestore
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
