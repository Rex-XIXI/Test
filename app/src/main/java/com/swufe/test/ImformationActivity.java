package com.swufe.test;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImformationActivity extends AppCompatActivity implements Runnable{

    TextView show;
    Handler handler;
    EditText input;
    HashMap<String,String> map= new HashMap<>();
    private List<Result> resultsList = new ArrayList<Result>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imformation);

        SharedPreferences sp=getSharedPreferences("mydata", Activity.MODE_PRIVATE);


        show=findViewById(R.id.imformation_show);

        Log.i("imformation","hello");

        Thread t = new Thread(this);
        //判断本周内是否更新过数据,以决定是否开启子线程
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);

        int update_date=sp.getInt("update_date",0);
        Log.i("date", ""+today);
        Log.i("date", ""+update_date);

        if(update_date+6<today){t.start();}else{
            Log.i("update", "本周内已更新过数据，不再更新");
        }
        int size=sp.getInt("size",0);
        for(int i=0;i<size;i++){
            map.put(sp.getString("title_"+i,""),sp.getString("href_"+i,""));
            Log.i("data", "onCreate: 标题"+sp.getString("title_"+i,""));
        }

        handler=new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what==1){
                    Bundle bdn= (Bundle) msg.obj;
                    show.setText(bdn.getStringArrayList("titles").get(0));
                    Toast.makeText(ImformationActivity.this, "公告数据已更新", Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

    }

    @Override
    public void run() {
        Log.i("run","run()....");

        ArrayList<String> titlestr =new ArrayList<String>();
        ArrayList<String> hrefstr =new ArrayList<String>();

        //获取网络数据
        Bundle bdn=new Bundle();
        Document doc = null;

        for(int i =1;i<56;i++){
            try {
               doc = Jsoup.connect("https://it.swufe.edu.cn/index/tzgg/"+i+".htm").get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("doc", "第"+i+"页");

            Elements lists = doc.getElementsByTag("ul");
            Element list =lists.get(17);

            Elements titles =list.getElementsByTag("li");
            String href;
            for(Element title:titles) {
                href = title.select("a").attr("abs:href");
                //Log.i("title", "run: " + title.text() + href);
                titlestr.add(title.text());
                hrefstr.add(href);
            }
        }
        try {
            doc = Jsoup.connect("https://it.swufe.edu.cn/index/tzgg.htm").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("doc", "文档标题："+doc.title());

        Elements lists = doc.getElementsByTag("ul");
        Element list =lists.get(17);

        Elements titles =list.getElementsByTag("li");
        String href;
        for(Element title:titles){
            href=title.select("a").attr("abs:href");
            Log.i("title", "run: "+title.text()+href);
            //data.put(title.text(),href);
            titlestr.add(title.text());
            hrefstr.add(href);

        }

        for(int i=0;i<titlestr.size();i++){
            Log.i("data", "标题："+ titlestr.get(i) +"链接："+ hrefstr.get(i));
        }

        bdn.putStringArrayList("titles",titlestr);
        bdn.putStringArrayList("href",hrefstr);
        Message msg = handler.obtainMessage(1);
        msg.obj= bdn;
        handler.sendMessage(msg);

        //通过SharedPreferences存储更新日期及数据
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SharedPreferences sp=getSharedPreferences("mydata", Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor=sp.edit();
        editor.putInt("update_date",day);
        editor.putInt("size",titlestr.size());
        for(int i=0;i<titlestr.size();i++){
            editor.putString("title_"+i,titlestr.get(i));
            editor.putString("href_"+i,hrefstr.get(i));
        }
        editor.commit();

    }

    public HashMap<String,String> search(String keyword,HashMap<String,String> data){
        HashMap<String,String> result = new HashMap<>();
        String href;
        for(Map.Entry<String,String>entry:data.entrySet()) {
            if(entry.getKey().contains(keyword)){
                href=entry.getValue();
                Result res= new Result(entry.getKey(),href);
                resultsList.add(res);
                result.put(entry.getKey(),href);
                Log.i("search", " title:"+entry.getKey()+"href:"+href);

            }
        }
        return result;
    }

    public void onSearch(View bdn){
        input=findViewById(R.id.imformation_input);
        String keyword= String.valueOf(input.getText());
        Log.i("search", "keyword "+keyword);
        if(keyword.length()>0){
            if(search(keyword,map).isEmpty()){
                Toast.makeText(ImformationActivity.this, "无匹配结果", Toast.LENGTH_SHORT).show();
            }else{
                //show.setText("存在匹配结果");
                ResultAdapter adapter = new ResultAdapter(ImformationActivity.this, R.layout.result_item, resultsList);
                ListView listView = findViewById(R.id.resultlist);
                listView.setAdapter(adapter);

            }
        }else{
            Toast.makeText(ImformationActivity.this, "关键词为空", Toast.LENGTH_SHORT).show();;
        }
    }


}
class ResultAdapter extends ArrayAdapter {
    private final int resourceId;
    public ResultAdapter(Context context, int textViewResourceId, List<Result> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         Result result= (Result) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象
        TextView title = (TextView) view.findViewById(R.id.itemtitle);//获取该布局内的文本视图
        TextView href= (TextView) view.findViewById(R.id.itemhref);
        title.setText(result.getTitle());
        href.setText(result.getHref());
        return view;
    }
}

class Result{
    String title;
    String href;
    Result(String title,String href){
        this.title=title;
        this.href=href;
    }

    public String getTitle() {
        return title;
    }

    public String getHref() {
        return href;
    }
}
