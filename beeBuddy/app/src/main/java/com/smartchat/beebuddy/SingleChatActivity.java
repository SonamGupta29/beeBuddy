package com.smartchat.beebuddy;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

public class SingleChatActivity extends AppCompatActivity  implements AsyncResponse{

    public String dbName = null;
    public String name = null;
    public String phoneno = null;
    public String tableName = null;
    public SQLiteDatabase mydatabase = null;
    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    public static int counter = 0;




    public boolean isTableExists(String tableName) {
        boolean isExist = false;
        Cursor cursor = mydatabase.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isExist = true;
            }
            cursor.close();
        }
        return isExist;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        // To set the text in the center
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        // Get values from the intent

        Intent intent = getIntent();
        dbName = intent.getStringExtra("dbName").toString();
        name = intent.getStringExtra("name").toString();
        phoneno = intent.getStringExtra("phoneno").toString().trim();

        tableName = name + phoneno;

        //((TextView)findViewById(R.id.action_bar_title)).setText(name);

        // Set the label
        ((TextView)findViewById(R.id.meLbl)).setText(name);
        // TO display the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Toast.makeText(SingleChatActivity.this, dbName, Toast.LENGTH_LONG).show();
        //Toast.makeText(SingleChatActivity.this, tableName, Toast.LENGTH_LONG).show();

        mydatabase = openOrCreateDatabase(dbName, MODE_PRIVATE, null);



        if(!isTableExists(tableName))
        {
            mydatabase.execSQL("CREATE TABLE " + tableName + " (flag int,msg VARCHAR,time VARCHAR);");
            mydatabase.execSQL("INSERT INTO " + tableName + " VALUES(1,'hi there ??',"+ String.valueOf(System.currentTimeMillis()) +");");
            mydatabase.execSQL("INSERT INTO " + tableName + " VALUES(0,'heyyyyy',"+ String.valueOf(System.currentTimeMillis()) +");");
            mydatabase.execSQL("INSERT INTO " + tableName + " VALUES(1,'hw r you ',"+ String.valueOf(System.currentTimeMillis()) +");");
        }

        ////////////////////////////////////
        initControls();
        ////////////////////////////////////

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean check2DOkeyword( String messageText)
    {

        return messageText.startsWith("#TODO");
    }

    public int checkAddKeyword( String messageText )
    {
        return messageText.indexOf("ADD");
    }

    public int checkShowKeyword( String messageText)
    {
        return messageText.indexOf("SHOW");
    }

    public int checkRemoveKeyword( String messageText)
    {
        return messageText.indexOf("REMOVE");
    }

    public static int  getCount()
    {
        return ++counter;
    }



    private void initControls() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);

        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView companionLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);

        SharedPreferences sp = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        final String myname=sp.getString("name",null);
        final String myphoneno=sp.getString("phoneno",null);
        companionLabel.setText(myname);

        loadDummyHistory();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString().trim();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }



                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(false);
                displayMessage(chatMessage);

                ///////////////////////////////////////////////////////////////////////////////////
                mydatabase.execSQL("INSERT INTO " + tableName + " (flag, msg, time) VALUES (0, \""
                        + messageText+"\", "+ String.valueOf(System.currentTimeMillis()) +");");

                mydatabase.execSQL("UPDATE UsersList SET time = "+ String.valueOf(System.currentTimeMillis())+" WHERE phoneno = "+phoneno+";");


                if( check2DOkeyword(messageText) ) /// messageText Contains 2DO in the text
                {
                    // check for 2DO table

                    if( !isTableExists("TODO") )
                    {
                        mydatabase.execSQL("CREATE TABLE TODO (srno int , item VARCHAR);");
                    }
                    // if "ADD" after 2DO
                    if( checkAddKeyword(messageText) != -1 ) // ADD present in the messageText
                    {
                        String item = messageText.substring(checkAddKeyword(messageText)+3).trim();
                        String srno = Integer.toString(getCount());
                        mydatabase.execSQL("INSERT INTO TODO ( srno , item ) VALUES ( " +srno+",\""+item+"\" );");

                        ChatMessage msg = new ChatMessage();
                        msg.setId(1);
                        msg.setMe(true);
                        msg.setMessage("ADDED TO TODO LIST");
                        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                        displayMessage(msg);;

                        mydatabase.execSQL("INSERT INTO " + tableName + " (flag, msg, time) VALUES (1, \""
                                + "Added to TODO List "+"\", "+ String.valueOf(System.currentTimeMillis()) +");");

                        //displayMessage(msg);


                    }
                    else if( checkShowKeyword(messageText) != -1 ) // Show keywrod present
                    {
                        Cursor resultSet = mydatabase.rawQuery("Select * from TODO ;",null);
                        resultSet.moveToFirst();
                        int count = 1;
                        String showList = "";
                        if(resultSet != null) {
                            do {


                                // Toast.makeText(SingleChatActivity.this, resultSet.getString(1), Toast.LENGTH_LONG).show();
                                if( resultSet.getCount() > 0 )
                                {
                                    showList+=resultSet.getString(0);
                                    showList+=" ";
                                    showList+=resultSet.getString(1);
                                    showList+="\n";
                                }
                                else
                                {
                                    ChatMessage msg = new ChatMessage();
                                    msg.setId(count++);
                                    msg.setMe(true);
                                    msg.setMessage("TODO List Empty !!");
                                    msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));

                                    displayMessage(msg);
                                }


                            } while (resultSet.moveToNext());
                        }
                        if( showList != "" ) {


                            ChatMessage msg = new ChatMessage();
                            msg.setId(count++);
                            msg.setMe(true);
                            showList = showList.substring(0, showList.length() - 1);
                            msg.setMessage(showList);
                            msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));

                            displayMessage(msg);
                        }

                        //  displayMessage(chatMessage);
                    }
                    else if( checkRemoveKeyword(messageText) != -1 )// remove present
                    {

                        String index = messageText.substring(messageText.indexOf("REMOVE")+6,messageText.length()).trim();
                        mydatabase.execSQL("DELETE FROM TODO WHERE srno = "+index+" ;");

                        ChatMessage msg = new ChatMessage();
                        msg.setId(1);
                        msg.setMe(true);
                        msg.setMessage("REMOVED from TODO List");
                        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                        displayMessage(msg);;

                        mydatabase.execSQL("INSERT INTO " + tableName + " (flag, msg, time) VALUES (1, \""
                                + "REMOVED from TODO List "+"\", "+ String.valueOf(System.currentTimeMillis()) +");");

                    }


                    // if "SHOW" after 2DO

                }
                else
                {
                    HashMap postData = new HashMap();
                    postData.put("fromphoneno", myphoneno);
                    postData.put("fromname", myname);
                    postData.put("tophoneno",phoneno );
                    postData.put("toname", name);
                    postData.put("msg", messageText);
                    postData.put("time", String.valueOf(System.currentTimeMillis()));
                    sendData(postData);
                    ///////////////////////////////////////////////////////////////////////////////////


                }
                messageET.setText("");


            }
        });
    }

    public void sendData(HashMap postData)
    {
        PostResponseAsyncTask task = new PostResponseAsyncTask(this, postData);
        // change proxy settings if required and enable the below lines
        Properties sysProperties = System.getProperties();
        sysProperties.put("proxyHost", "http://proxy.iiit.ac.in");
        sysProperties.put("proxyPort", "8080");
        sysProperties.put("proxySet", "true");
        //String myurl = "http://"+txt_ip.getText().toString().trim()+"/user_control2.php";
        //String myurl="http://10.0.2.2/user_control.php";
        String myurl="http://10.2.130.233/user_control.php";
        task.execute(myurl);
    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void loadDummyHistory(){

        chatHistory = new ArrayList<ChatMessage>();
        chatHistory.clear();
        //mydatabase = openOrCreateDatabase(dbName, MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from "+tableName+" order by time ASC",null);
        resultSet.moveToFirst();
        int count = 1;
        if(resultSet != null) {
            do {
                //Toast.makeText(SingleChatActivity.this, resultSet.getString(1), Toast.LENGTH_LONG).show();
                ChatMessage msg = new ChatMessage();
                msg.setId(count++);
                if (resultSet.getString(0).equals("0"))
                    msg.setMe(false);
                else
                    msg.setMe(true);
                msg.setMessage(resultSet.getString(1));
                msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatHistory.add(msg);
            } while (resultSet.moveToNext());
        }

        /*
        msg.setId(1);
        msg.setMe(true);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(true);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg1);
        ChatMessage msg2 = new ChatMessage();
        msg2.setId(1);
        msg2.setMe(false);
        msg2.setMessage("Mast Doing");
        msg2.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg2);
        ChatMessage msg3 = new ChatMessage();
        msg3.setId(2);
        msg3.setMe(false);
        msg3.setMessage("Lode Lge");
        msg3.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg3);*/

        adapter = new ChatAdapter(SingleChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for(int i=0; i < chatHistory.size() ; i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }

    }

    @Override
    public void processFinish(String s) {

    }
}

