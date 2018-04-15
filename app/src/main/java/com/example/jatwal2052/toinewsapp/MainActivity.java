package com.example.jatwal2052.toinewsapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> urll = new ArrayList<>();
    ArrayAdapter arrayAdapter ;
    SQLiteDatabase database;

    public class DownloadTask extends AsyncTask<String ,Void ,String>{

        @Override
        protected String doInBackground(String... params) {

            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try{

                url = new URL(params[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1){
                    char s = (char) data;
                    result += s;
                    data = reader.read();
                }

                JSONObject jsonObject = new JSONObject(result);
                for (int i = 0;i < jsonObject.getJSONArray("articles").length();i++ ) {
                    String titles = jsonObject.getJSONArray("articles").getJSONObject(i).getString("title");
                    String urls =  jsonObject.getJSONArray("articles").getJSONObject(i).getString("url");
                    String content = "";
//                    url = new URL(urls);
//                    httpURLConnection = (HttpURLConnection) url.openConnection();
//                    inputStream = httpURLConnection.getInputStream();
//                    reader = new InputStreamReader(inputStream);
//                    data = reader.read();
//
//                    while (data != -1){
//                        char s = (char) data;
//                        content += s;
//                        data = reader.read();
//                    }
                    Log.i("titles url",titles + urls);
                    String sql = "INSERT INTO article (title,url ) VALUES (?,?)";
                    SQLiteStatement sqLiteStatement =database.compileStatement(sql);

                    sqLiteStatement.bindString(1,titles);
                   sqLiteStatement.bindString(2,urls);
                    sqLiteStatement.execute();
                }


            }
            catch(Exception e){
                e.printStackTrace();
                return "failed";
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            update();
        }
    }

   void update(){

        Cursor c = database.rawQuery("SELECT * FROM article",null);
       int urlindex = c.getColumnIndex("url");
       int titleindex = c.getColumnIndex("title");
              if(c.moveToFirst()){
               list.clear();
               urll.clear();
            do{
                list.add(c.getString(titleindex));
                 urll.add(c.getString(urlindex));
//
            }while(c.moveToNext());
//
           arrayAdapter.notifyDataSetChanged();
}
        }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(arrayAdapter);

        database = this.openOrCreateDatabase("Article",MODE_PRIVATE,null);
        database.execSQL("DROP TABLE article");
        database.execSQL("CREATE TABLE article (id INTEGER PRIMARY KEY,title VARCHAR,url VARCHAR)");

       // update();

        DownloadTask downloadTask = new DownloadTask();
        try {
            downloadTask.execute("https://newsapi.org/v1/articles?source=the-times-of-india&sortBy=latest&apiKey=275f44680f6d4277a911f66940960ec7").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent j = new Intent(getApplicationContext(),Main2Activity.class);
                j.putExtra("url",urll.get(position));
                startActivity(j);

            }
        });

    }



}
