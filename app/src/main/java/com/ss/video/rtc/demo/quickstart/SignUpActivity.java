package com.ss.video.rtc.demo.quickstart;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {
    EditText etUsername;
    EditText etPassword;
    EditText etPasswordConfirm;
    SQLiteDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 获取 SQLiteDBHelper 对象
        dbHelper = SQLiteDBHelper.getDbHelper(this);

        etUsername = findViewById(R.id.editTextTextUsername);
        etPassword = findViewById(R.id.editTextTextPassword);
        etPasswordConfirm = findViewById(R.id.editTextTextPasswordConfirm);
    }

    public void onclick(View view) {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String passwordConfirm = etPasswordConfirm.getText().toString();
        Log.d(TAG, "onclick: " + password + passwordConfirm);
        if (username.isEmpty()) {
            Toast.makeText(this,"用户名不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this,"密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (passwordConfirm.isEmpty()) {
            Toast.makeText(this,"确认密码不能为空",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this,"两次密码输入不同",Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("user",new String[]{"name"}, "name=?",new String[]{username},null,null,null,"0,1");

        if (cursor.moveToNext()) {
            cursor.close();
            Toast.makeText(this,"已存在的用户名",Toast.LENGTH_SHORT).show();
            return;
        }
        cursor.close();



        password = shaEncrypt(password + "sjtu");
        /**
         * Create and/or open a database.
         * */

        String insertSql = "INSERT INTO user (name, password) VALUES ('" + username + "','" + password + "')";
        db.execSQL(insertSql);
        Toast.makeText(this,"⭐️ 注册成功！",Toast.LENGTH_LONG).show();
        finish();

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
}