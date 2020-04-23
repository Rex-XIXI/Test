package com.swufe.test;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RateListActivity extends ListActivity implements Runnable{

    String listwait[]={"wait..."};
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rate_list);

        Thread t=new Thread(this);
        t.start();

        handler=new Handler(){
            List<String> data=new ArrayList<String>();
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==2){
                    data= (List<String>) msg.obj;
                    Log.i("thread", "handleMessage: data更新");
                    ListAdapter adapter =new ArrayAdapter<String>(RateListActivity.this,android.R.layout.simple_list_item_1,data);
                    setListAdapter(adapter);
                }
                super.handleMessage(msg);
            }
        };


        ListAdapter adapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,listwait);
        setListAdapter(adapter);

    }

    @Override
    public void run() {
        Log.i("run","run()....");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //获取网络数据
        List<String> relist=new ArrayList<String>();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.usd-cny.com/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("doc", "文档标题："+doc.title());

        Elements tables = doc.getElementsByTag("table");
        Elements tds=tables.get(0).getElementsByTag("td");
        for(int i=0;i<tds.size();i+=5){
            Element td1=tds.get(i);
            Element td2=tds.get(i+1);

            String str1 =td1.text();
            String str2 =td2.text();
            //由于原网站存在数据缺失，增加判断条件跳过缺失部分防止报错
            if(str2.length()<2){continue;}
            Float val=100f/Float.parseFloat(str2);
            relist.add(str1+"-->"+val);
            Log.i("data", str1+"-->"+val);
            }

        Message msg = handler.obtainMessage(2);
        msg.obj=relist;
        handler.sendMessage(msg);
    }

}
