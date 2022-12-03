package com.ss.video.rtc.demo.quickstart;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SQLiteLoginActivity extends AppCompatActivity {
    private static final String TAG = "SQLiteLoginActivity";
    SQLiteDBHelper dbHelper;

    EditText etUsername;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sqlite_login);

        // 获取 SQLiteDBHelper 对象
        dbHelper = SQLiteDBHelper.getDbHelper(this);

        // 通过 id 找到 布局管理器中的 相关属性
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    // 当用户点击提交按钮时,就会到这里(单击事件)
    public void onClickLogin(View view) {

        // 获取用户的用户名和密码进行验证
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        password = shaEncrypt(password + "sjtu");

        /**
         * Create and/or open a database.
         * */
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        /**
         *
         *  查询用户信息,放回值是一个游标(结果集,遍历结果集)
         *
         * */
        Cursor cursor = db.query("user",new String[]{"name","password"}, "name=?",new String[]{username},null,null,null,"0,1");

        // 游标移动进行校验
        if(cursor.moveToNext()) {
            // 从数据库获取密码进行校验
            String dbPassword = "";
            int passwordIndex = cursor.getColumnIndex("password");
            if(passwordIndex != -1) {
                // 找到了 Username 对应的密码，比对密码是否正确
                dbPassword = cursor.getString(passwordIndex);

                // 关闭游标
                cursor.close();
                if(password.equals(dbPassword)) {
                    // 校验成功则跳转到 LoginActivity
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra(Constants.USER_ID_EXTRA, username);
                    startActivity(intent);
                    return;
                } else {
                    cursor.close();
                    Toast.makeText(this,"⛔️ 输入信息有误，密码错误！",Toast.LENGTH_LONG).show();
                }
            }
            else {
                cursor.close();
            }
        }
        else {
            // 没有找到，说明用户未注册
//            Log.d(TAG, "onClickLogin: 未注册");
            cursor.close();
            Toast.makeText(this,"⛔️ 用户未注册，请先注册！",Toast.LENGTH_LONG).show();
        }
    }

    public void onClickSignUp(View view) {
        // 跳转到注册页面
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
    public static String shaEncrypt(String strSrc) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-256");// 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }
    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}

