package com.smartchat.beebuddy;

import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity implements AsyncResponse,View.OnClickListener {

    private Button button_login;
    private EditText txt_name;
    private EditText txt_phone;
    public String dbName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            String myname=sp.getString("name",null);
            if(myname!=null) {
                Intent intent = new Intent(this,ContactListActivity.class);
                String dbName = sp.getString("phoneno",null)+".db";
                intent.putExtra("dbName", dbName);
                startActivity(intent);
                finish();
            }
            else {
                Log.w("name", "Is null now");
            }
        }
        catch(Exception e)
        {
            Log.w("Error","Problem in getting shared preference");
        }
        button_login = (Button) findViewById(R.id.button_login);
        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_phone = (EditText) findViewById(R.id.txt_phone);


        //txt_name.setText(txt_name.getText().toString().toLowerCase());
        //txt_ip = (EditText) findViewById(R.id.ip);
        button_login.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {

        // Log.d("No Error","Database created Successfully");
        HashMap postData = new HashMap();
        postData.put("name",txt_name.getText().toString().trim());
        postData.put("phoneno",txt_phone.getText().toString().trim());
        SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("name", txt_name.getText().toString().trim());
        editor.putString("phoneno", txt_phone.getText().toString().trim());
        editor.apply();
        dbName = txt_phone.getText().toString().trim()+".db";
        PostResponseAsyncTask task = new PostResponseAsyncTask(this,postData);
        // change proxy settings if required and enable the below lines
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "http://proxy.iiit.ac.in");
        sysProperties.put("proxyPort", "8080");
        sysProperties.put("proxySet", "true");
        //String myurl = "http://"+txt_ip.getText().toString().trim()+"/user_control2.php";
//            String myurl="http://10.0.2.2/user_control.php";
        String myurl="http://10.2.130.233/user_control.php";
        task.execute(myurl);
    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    @Override
    public void processFinish(String id)
    {
        if( !doesDatabaseExist(this,dbName)  )
        {
            // create table
            try {


                SQLiteDatabase mydatabase = openOrCreateDatabase(dbName, MODE_PRIVATE, null);
                // Toast.makeText(MainActivity.this, String.valueOf("in database"), Toast.LENGTH_LONG).show();
                boolean x = doesDatabaseExist(this,dbName);
                if( x )
                {
                    //Toast.makeText(LoginActivity.this, String.valueOf(dbName), Toast.LENGTH_LONG).show();
                    Toast.makeText(LoginActivity.this, String.valueOf("Success"), Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Toast.makeText(LoginActivity.this, String.valueOf("lode lge"), Toast.LENGTH_LONG).show();
                }
                mydatabase.execSQL("CREATE TABLE UsersList(name VARCHAR,phoneno VARCHAR,time VARCHAR);");

                ///////////////////////////////////////////////////////////////////////////////////////////////
                // Testing the app
                mydatabase.execSQL("INSERT INTO UsersList VALUES('Satyam','9996387773',"+ String.valueOf(System.currentTimeMillis()) +");");
                mydatabase.execSQL("INSERT INTO UsersList VALUES('Ganesh','7738899339',"+ String.valueOf(System.currentTimeMillis()) +");");
                mydatabase.execSQL("INSERT INTO UsersList VALUES('Sonam','8886408787',"+ String.valueOf(System.currentTimeMillis()) +");");
                mydatabase.execSQL("INSERT INTO UsersList VALUES('BoT','0000000000',"+ String.valueOf(System.currentTimeMillis()) +");");
                ///////////////////////////////////////////////////////////////////////////////////////////////

                mydatabase.close();
            }
            catch (SQLiteException e) {
                ;

            }

        }
       // Toast.makeText(LoginActivity.this, String.valueOf(id), Toast.LENGTH_LONG).show();
        SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        Intent intent = new Intent(this,ContactListActivity.class);
        String dbName = sp.getString("phoneno",null)+".db";
        intent.putExtra("dbName", dbName);
        startActivity(intent);
        finish();

    }
}

