package com.smartchat.beebuddy;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class ContactListActivity extends AppCompatActivity implements AsyncResponse {

    public String dbName = null;
    public String myname = null;
    public String myphoneno = null;
    public List<String> contactNameList = new ArrayList<String>();
    public List<String> contactPhonenoList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this,Background_service.class);
        startService(intent);

        SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        myphoneno = sp.getString("phoneno",null);
        myname = sp.getString("name",null);

        // Checking message on web service
        HashMap postData = new HashMap();
        postData.put("phoneno",myphoneno.trim());
        postData.put("name",myname.trim());
        postData.put("get_msg","1");
        PostResponseAsyncTask task = new PostResponseAsyncTask((AsyncResponse) this, postData);
        // change proxy settings if required and enable the below lines
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "http://proxy.iiit.ac.in");
        sysProperties.put("proxyPort", "8080");
        sysProperties.put("proxySet", "true");
        //String myurl = "http://"+txt_ip.getText().toString().trim()+"/user_control2.php";
        //String myurl="http://10.0.2.2/user_control.php";
        String myurl="http://10.2.130.233/user_control.php";
        //Log.w("URL",myurl);
        //task.execute("http://chatapp.byethost6.com/user_control2.php");
        task.execute(myurl);
        ////////////////////////////////////

        // Set the top bar name -- FOR THIS USE THE SHARED CONTEXT
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        // Set the action bar title as Ganesh or the person logged in the app
        ((TextView) findViewById(R.id.action_bar_title)).setText(myname);

        setContentView(R.layout.activity_contact_list);

        intent = getIntent();
        dbName = intent.getStringExtra("dbName");
        //Toast.makeText(ContactListActivity.this, String.valueOf(dbName), Toast.LENGTH_LONG).show();

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Read the userslist table and

        SQLiteDatabase mydatabase = openOrCreateDatabase(dbName,MODE_PRIVATE,null);
        mydatabase.execSQL("UPDATE UsersList SET time = "+ String.valueOf(System.currentTimeMillis())+" WHERE name = 'BoT';");
        Cursor resultSet = mydatabase. rawQuery("Select * from UsersList order by time DESC",null);
        if(resultSet != null) {
            resultSet.moveToFirst();
            do {
                String name = resultSet.getString(resultSet.getColumnIndex("name"));
                String phoneno = resultSet.getString(resultSet.getColumnIndex("phoneno"));
                contactNameList.add(name);
                contactPhonenoList.add(phoneno);
            }
            while (resultSet.moveToNext());
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.simplerow, contactNameList);

        ListView listView = (ListView) findViewById(R.id.ContactListListView);

        // I need the adaptor as the list of the recent chats
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                /*if (position == 1) {
                    //code specific to first list item
                    Intent myIntent = new Intent(view.getContext(), SingleChatActivity.class);
                    startActivityForResult(myIntent, 0);
                }*/
                Intent intent = new Intent(view.getContext(), SingleChatActivity.class);
                intent.putExtra("dbName", dbName);
                intent.putExtra("name", contactNameList.get(position));
                intent.putExtra("phoneno", contactPhonenoList.get(position));
                //startActivityForResult(intent, 0);
                startActivity(intent);
            }
        });
        mydatabase.close();
    }



    @Override
    public void processFinish(String s) {
        SQLiteDatabase mydatabase = openOrCreateDatabase(dbName,MODE_PRIVATE,null);
        if(s == "") {
            return;
        }
        String rows[] = s.split(";");
        s = "";
        for(int i = 0; i < rows.length; i++)
        {
            //Toast.makeText(ContactListActivity.this, String.valueOf(rows[i]), Toast.LENGTH_LONG).show();
            String []row = rows[i].split("#");
            if(row.length != 4) {
                return;
            }

            String fromPhoneno = row[0].trim();
            String fromName = row[1].trim();
            String fromMsg = row[2].trim();
            String time = row[3].trim();
            // Update the contact list table
            try {
                // update
                mydatabase.execSQL("UPDATE UsersList SET time = "+ String.valueOf(time)+" WHERE phoneno = "+fromPhoneno+";");
            } catch(Exception e) {
                // Insert
                mydatabase.execSQL("INSERT INTO UsersList VALUES("+fromName+","+fromPhoneno+","+time+";");
            }
            try {
                mydatabase.execSQL("INSERT INTO " + fromName + fromPhoneno + " VALUES (1, \"" + fromMsg + "\"," + time + ");");
            } catch(Exception e)
            {
                mydatabase.execSQL("CREATE TABLE " +  fromName + fromPhoneno + " (flag int,msg VARCHAR,time VARCHAR);");
                mydatabase.execSQL("INSERT INTO " + fromName + fromPhoneno + " VALUES (1, \"" + fromMsg + "\"," + time + ");");
            }
        }
        mydatabase.close();
    }
}
