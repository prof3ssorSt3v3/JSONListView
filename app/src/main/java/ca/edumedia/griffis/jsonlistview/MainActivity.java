package ca.edumedia.griffis.jsonlistview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ca.edumedia.griffis.jsonlistview.model.DataItem;
import ca.edumedia.griffis.jsonlistview.services.MyService;
import ca.edumedia.griffis.jsonlistview.utils.NetworkHelper;

import static android.R.attr.process;

public class MainActivity extends AppCompatActivity {

    public static final Uri JSON_URL = Uri.parse("http://jsonplaceholder.typicode.com/posts");
    public static final String TAG = "TAG";

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra(MyService.SERVICE_PAYLOAD)){
                //looks like we got some data!!!
                DataItem[] dataItems = (DataItem[]) intent.getParcelableArrayExtra(MyService.SERVICE_PAYLOAD);
                Log.i(TAG, "Received the DataItems");
                //pass the data to the interface
                processTheFrackenJson(dataItems);

            }else if(intent.hasExtra(MyService.SERVICE_EXCEPTION)){
                //bad things happened. We bow our head in shame
                //Toast the user
                Log.i(TAG, MyService.SERVICE_EXCEPTION);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //we need to start the service and send it the URL
        if(NetworkHelper.hasNetworkAccess(this)){
            //we can start the service
            Intent intent = new Intent(this, MyService.class);
            intent.setData(JSON_URL);
            startService(intent);
            Log.i(TAG, "Service Started");
        }else{
            //Much Shame. Very Toast.
            Log.i(TAG, "No Network Access");
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(br, new IntentFilter(MyService.SERVICE_ACTION));
    }

    protected void processTheFrackenJson(DataItem[] dataItems){
        ListView postList = (ListView) findViewById(R.id.post_list);
        ArrayList<String> titles = new ArrayList<String>();
        for (DataItem item: dataItems ) {
            String t = item.getTitle();
            titles.add( t );
            Log.i(TAG, "processTheFrackenJson: "+ t);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.post_view, R.id.text_title, titles);
        postList.setAdapter(adapter);
        //The titles are now being displayed in the ListView
        //add click listeners to the items
        postList.setOnItemClickListener( new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                // adapterView is the container for the Views that will be loaded into the ListView
                // view is the textview that we loaded each name into
                // i is the position in the list
                // l is the row id of the item that was clicked.
                String item = adapterView.getItemAtPosition(i).toString();
                String msg = item + " @ position " + i;
                Toast toast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
