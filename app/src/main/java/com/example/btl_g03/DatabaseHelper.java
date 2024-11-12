package com.example.btl_g03;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "product_db";
    private static final int DATABASE_VERSION = 2;

    //sản phẩm
    public static final String TABLE_PRODUCT = "product";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_PATH = "image_path"; // Đường dẫn ảnh

    // người dùng
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD_HASH = "password_hash";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_FULL_NAME = "full_name";
    public static final String COLUMN_PROFILE_IMAGE_URL = "profile_image_url";

    private static final String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_CATEGORY + " TEXT, "
            + COLUMN_IMAGE_PATH + " TEXT)";

    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " ("
            + COLUMN_USER_ID + " TEXT PRIMARY KEY, "
            + COLUMN_EMAIL + " TEXT, "
            + COLUMN_PASSWORD_HASH + " TEXT, "
            + COLUMN_PHONE_NUMBER + " TEXT, "
            + COLUMN_ADDRESS + " TEXT, "
            + COLUMN_FULL_NAME + " TEXT, "
            + COLUMN_PROFILE_IMAGE_URL + " TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PRODUCT); // Tạo bảng khi cơ sở dữ liệu được khởi tạo
        db.execSQL(CREATE_TABLE_USER); // Tạo bảng người dùng
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT); // Xóa bảng nếu đã tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // Hàm thêm sản phẩm vào SQLite
    public void addProduct(String title, String description, String category, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_PATH, imagePath); // Lưu đường dẫn ảnh

        db.insert(TABLE_PRODUCT, null, values); // Chèn vào bảng
        db.close();
    }
    public void addUser(String userId, String email, String phoneNumber, String address, String fullName, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_PROFILE_IMAGE_URL, imagePath);

        db.insert(TABLE_USER, null, values); // Chèn vào bảng
        db.close();
    }
}
