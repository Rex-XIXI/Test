package com.swufe.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

public class RateActivity extends AppCompatActivity implements Runnable{
    EditText rmb;
    TextView show;

    float dollarrate=0.1f;
    float eurorate=0.2f;
    float wonrate=0.3f;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //根据id查找到控件
        rmb=findViewById(R.id.rate_input);
        show=findViewById(R.id.rate_output);

        //通过SharePreferences获取配置的汇率变量值
        SharedPreferences sp=getSharedPreferences("myrate", Activity.MODE_PRIVATE);

        dollarrate=sp.getFloat("dollar_rate",0.1f);
        eurorate=sp.getFloat("euro_rate",0.1f);
        wonrate=sp.getFloat("won_rate",0.1f);

        //判断今日是否更新过数据,以决定是否开启子线程
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);

        int update_date=sp.getInt("update_date",0);
        Log.i("date", ""+today);
        Log.i("date", ""+update_date);

        Thread t = new Thread(this);
        if(update_date!=today){t.start();}else{
            Log.i("update", "今日已更新过汇率，不再更新");
        }

        //通过Handler实现不同线程间消息的传递
        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==1){
                    Bundle bdn= (Bundle) msg.obj;
                    dollarrate = bdn.getFloat("dolrate");
                    eurorate = bdn.getFloat("eurorate");
                    wonrate = bdn.getFloat("wonrate");
                    Log.i("update", "handleMessage: dolrate:" + dollarrate);
                    Log.i("update", "handleMessage: eurorate:" + eurorate);
                    Log.i("update", "handleMessage: wonrate:" + wonrate);
                    Toast.makeText(RateActivity.this, "汇率已更新", Toast.LENGTH_SHORT).show();

                }
                super.handleMessage(msg);
            }
        };
    }


    public void convert(View btn){

        String str=rmb.getText().toString();
        float value=0;
        float output=0;

        if(str.length()>0){
            value=Float.parseFloat(str);
        }else{
            Toast.makeText(this,"请输入数额",Toast.LENGTH_SHORT).show();
        }

        if(btn.getId()==R.id.rate_dol){
            output=value*(dollarrate);
        }else if(btn.getId()==R.id.rate_euro){
            output=value*(eurorate);
        }else{
            output=value*wonrate;
        }
        DecimalFormat  df  = new DecimalFormat("######0.00");
        show.setText(df.format(output));
    }

    public void openCfg(View btn){
        openCfg();
    }

    private void openCfg() {
        Intent config = new Intent(this, ConfigActivity.class);
        config.putExtra("cfg_dol_rate", dollarrate);
        config.putExtra("cfg_euro_rate", eurorate);
        config.putExtra("cfg_won_rate", wonrate);

        startActivityForResult(config, 1);
    }

    private void openList() {
        //Intent list = new Intent(this,RateListActivity.class);
        //startActivity(list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==1&&resultCode==2){
            Bundle bdn=data.getExtras();
            dollarrate=bdn.getFloat("newDollar",0.1f);
            eurorate=bdn.getFloat("newEuro",0.1f);
            wonrate=bdn.getFloat("newWon",0.1f);

            SharedPreferences sp=getSharedPreferences("myrate", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putFloat("dollar_rate",dollarrate);
            editor.putFloat("euro_rate",eurorate);
            editor.putFloat("won_rate",wonrate);
            editor.commit();
            Log.i("save","新汇率已保存");

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rate,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_rate_config){
            openCfg();
        }else if(item.getItemId()==R.id.menu_rate_list){
            openList();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        Log.i("run","run()....");

        //获取网络数据
        Bundle bdn=new Bundle();
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


            if(str1.equals("美元")){
                Log.i("test", str1+"-->"+val.toString());
                bdn.putFloat("dolrate",val);
            }else if(str1.equals("欧元")){
                Log.i("test", str1+"-->"+val.toString());
                bdn.putFloat("eurorate",val);
            }else if(str1.equals("韩币")){
                Log.i("test", str1+"-->"+val.toString());
                bdn.putFloat("wonrate",val);
            }
        }

        Message msg = handler.obtainMessage(1);
        msg.obj= bdn;
        handler.sendMessage(msg);

        //通过SharedPreferences存储更新日期
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SharedPreferences sp=getSharedPreferences("myrate", Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor=sp.edit();
        editor.putInt("update_date",day);
        editor.commit();
    }

}
