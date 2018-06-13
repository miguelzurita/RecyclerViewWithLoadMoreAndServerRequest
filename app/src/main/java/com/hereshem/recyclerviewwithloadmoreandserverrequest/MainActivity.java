package com.hereshem.recyclerviewwithloadmoreandserverrequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hereshem.lib.recycler.MultiLayoutAdapter;
import com.hereshem.lib.recycler.MultiLayoutAdapter.TypeHolderLayout;
import com.hereshem.lib.recycler.MyRecyclerView;
import com.hereshem.lib.recycler.MyViewHolder;
import com.hereshem.lib.recycler.RecyclerViewAdapter;
import com.hereshem.lib.server.MyDataQuery;
import com.hereshem.lib.utils.Preferences;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int start = 0;
    MyRecyclerView recycler;
    List<Object> items = new ArrayList<>();
    List<TypeHolderLayout> holders = new ArrayList<>();

    public static class Events {
        public String date, title, summary;
        public Events(JSONObject jObj){
            date = jObj.optString("Date");
            title = jObj.optString("Title");
            summary = jObj.optString("Summary");
        }
        public static List<Events> parseJSON(JSONArray jArr){
            List<Events> list = new ArrayList<>();
            for (int i = 0; i < jArr.length(); i++) {
                list.add(new Events(jArr.optJSONObject(i)));
            }
            return list;
        }
        public static List<Events> parseJSON(String jsonArrayString){
            try{
                return parseJSON(new JSONArray(jsonArrayString));
            }catch (Exception e){e.printStackTrace();}
            return new ArrayList<>();
        }
    }

    public static class VHolder extends MyViewHolder<Events> {
        TextView date, title, summary;
        public VHolder(View v) {
            super(v);
            date = v.findViewById(R.id.date);
            title = v.findViewById(R.id.title);
            summary = v.findViewById(R.id.summary);
        }

        @Override
        public void bindView(Events c){
            date.setText(c.date);
            title.setText(c.title);
            summary.setText(c.summary);
        }
    }

    public static class TVHolder extends MyViewHolder<String> {
        TextView title;
        public TVHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
        }
        public void bindView(String c){
            title.setText(c);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerViewAdapter adapter1 = new RecyclerViewAdapter(this, items, VHolder.class, R.layout.row_contact);

        holders.add(new TypeHolderLayout(Events.class, VHolder.class, R.layout.row_contact));
        holders.add(new TypeHolderLayout(String.class, TVHolder.class, R.layout.row_simple));

        MultiLayoutAdapter adapter2 = new MultiLayoutAdapter(this, items, holders);

        recycler = findViewById(R.id.recycler);
        recycler.setAdapter(adapter2);
        recycler.setOnItemClickListener(new MyRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(MainActivity.this, "Recycler Item Clicked " + position, Toast.LENGTH_SHORT).show();
            }
        });
        recycler.setOnLoadMoreListener(new MyRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData();
            }
        });
        loadData();
    }

    private void loadData() {
        new MyDataQuery(this) {
            @Override
            public void onSuccess(String table_name, String result) {
                List<Events> data = Events.parseJSON(result);
                if (table_name.equals("0")) {
                    items.clear();
                }
                if (data.size() > 0) {
                    for (int i = 0; i < data.size(); i++) {
                        if(i%5 == 4){
                            items.add((i+1)+"th Item");
                        }
                        items.add(data.get(i));
                    }
                    //items.addAll(data);
                    recycler.loadComplete();
                    start += data.size();
                } else {
                    recycler.hideLoadMore();
                }
            }

            @Override
            public String onDbQuery(String table, HashMap<String, String> params) {
                if(table.equals("0")){
                    return new Preferences(getApplicationContext()).getPreferences("data_downloaded");
                }
                return super.onDbQuery(table, params);
            }

            @Override
            public void onDbSave(String table, String response) {
                if(table.equals("0")){
                    new Preferences(getApplicationContext()).setPreferences("data_downloaded", response);
                }
            }
        }
        .setUrl("http://dl.mantraideas.com/apis/events.json")
        .setMethod(MyDataQuery.Method.GET)
        .setIdentifier(start+"")
        .execute();
    }
}
