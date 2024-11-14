package com.example.btl_g03.Models;

public enum PostType {
    SHARE, // Bài đăng chia sẻ nhu yếu phẩm
    REQUEST; // Bài đăng nhận nhu yếu phẩm
    @Override
    public String toString() {
        switch (this) {
            case SHARE:
                return "Chia sẻ nhu yếu phẩm";
            case REQUEST:
                return "Nhận nhu yếu phẩm";
            default:
                return super.toString();
        }
    }

}
