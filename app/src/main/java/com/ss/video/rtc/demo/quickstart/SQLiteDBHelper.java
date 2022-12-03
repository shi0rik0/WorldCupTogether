package com.ss.video.rtc.demo.quickstart;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * A helper class to manage database creation and version management.
 * */
public class SQLiteDBHelper extends SQLiteOpenHelper {
    static private  SQLiteDBHelper dbHelper = null;

    // 创建数据库语句 初始化了几个虚拟用户
    static final String CREATE_SQL[] = {
            "CREATE TABLE user (" +
                    "_id  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name  varchar," +
                    "password  varchar" +
                    ")",
            "INSERT INTO user VALUES (1,'admin',123)",
            "INSERT INTO user VALUES (2,'zhangsan',123)",
            "INSERT INTO user VALUES (3,'lisi',123)",
            "INSERT INTO user VALUES (4,'wangwu',123)"
    };

    // 调用父类的构造方法
    public SQLiteDBHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // 创建数据库
        Log.i("sqlite_____", "create Database");

        // 执行 SQL 语句
        for (int i = 0; i < CREATE_SQL.length; i++) {
            db.execSQL(CREATE_SQL[i]);
        }

        // 完成数据库的创建
        Log.i("sqlite_____", "Finished Database");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static SQLiteDBHelper getDbHelper(@Nullable Context context) {
        if (dbHelper == null) {
            return new SQLiteDBHelper(context, "sqlite.db",1);
        } else {
            return dbHelper;
        }
    }
}



