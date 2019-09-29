package com.ddf.ddf1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    // 数据库文件名
    public static final String DB_NAME = "DDF.db";
    // 数据库表名
    public static final String TABLE_NAME = "t_audio";
    // 数据库版本号
    public static final int DB_VERSION = 1;
    private DBHelper mHelper = null;
    private SQLiteDatabase mDatabase = null;

    public static final String AUDIO_NAME = "audio_name";
    public static final String AUDIO_DESCRIPTION = "audio_description";
    public static final String IMAGE_ID = "image_id";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表
        String sql = "create table if not exists " +
                TABLE_NAME + "(" +//"(_id integer primary key autoincrement, " +
                AUDIO_NAME + " varchar(100) primary key, " +
                AUDIO_DESCRIPTION + " varchar(100), " +
                IMAGE_ID + " integer"
                + ");";
        db.execSQL(sql);
        // 清空
        sql = "delete from " + TABLE_NAME;  //清空数据
        db.execSQL(sql);
//        sql = "update sqlite_sequence SET seq = 0 where name = '" + TABLE_NAME + "'";//自增长ID为0
//        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 插入数据
    public void insertData(String audio_name, String audio_description, int image_id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.AUDIO_NAME, audio_name);
        values.put(DBHelper.AUDIO_DESCRIPTION, audio_description);
        values.put(DBHelper.IMAGE_ID, image_id);
        db.insert(DBHelper.TABLE_NAME, null, values);
        db.close();
    }

    // 查询
    public ContentValues queryByFileName(String name) {
        // 根据文件名称（主键）查询，应该只有唯一结果
        ContentValues ans = new ContentValues();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME,
                new String[]{DBHelper.AUDIO_NAME, DBHelper.AUDIO_DESCRIPTION, DBHelper.IMAGE_ID},
                DBHelper.AUDIO_NAME + "=?",
                new String[]{name},
                null,
                null,
                null);
        cursor.moveToFirst();
        String audio_name = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_NAME));
        String audio_description = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_DESCRIPTION));
        int image_id = cursor.getInt(cursor.getColumnIndex(DBHelper.IMAGE_ID));
        ans.put("audio_name", audio_name);
        ans.put("audio_description", audio_description);
        ans.put("image_id", image_id);
        return ans;
    }

    // 查询所有
    public List<ContentValues> queryAll() {
        // 根据文件名称（主键）查询，应该只有唯一结果
        List<ContentValues> ans = new ArrayList<ContentValues>();
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(DBHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        //cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                ContentValues cv = new ContentValues();
                String audio_name = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_NAME));
                String audio_description = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_DESCRIPTION));
                int image_id = cursor.getInt(cursor.getColumnIndex(DBHelper.IMAGE_ID));
                cv.put("audio_name", audio_name);
                cv.put("audio_description", audio_description);
                cv.put("image_id", image_id);
                ans.add(cv);
                cursor.moveToNext();
            }
        }
//        String audio_name = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_NAME));
//        String audio_description = cursor.getString(cursor.getColumnIndex(DBHelper.AUDIO_DESCRIPTION));
//        int image_id = cursor.getInt(cursor.getColumnIndex(DBHelper.IMAGE_ID));
//        ans.put("audio_name", audio_name);
//        ans.put("audio_description", audio_description);
//        ans.put("image_id", image_id);
        return ans;
    }
}
