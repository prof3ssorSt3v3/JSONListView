package ca.edumedia.griffis.jsonlistview.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import ca.edumedia.griffis.jsonlistview.model.DataItem;
import ca.edumedia.griffis.jsonlistview.utils.HttpHelper;
import ca.edumedia.griffis.jsonlistview.utils.NetworkHelper;

import static android.content.ContentValues.TAG;

/**
 * Created by griffis on 2017-12-06.
 */

public class MyService extends IntentService {

    public final static String SERVICE_ACTION = "some-custom-event";
    public final static String SERVICE_EXCEPTION = "IOException";
    public static final String SERVICE_PAYLOAD = "payload";
    public static final String TAG = "TAG";

    public MyService() {
        super("MyService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri = Uri.parse( intent.getData().toString() );
        //fetch the data using this URL
        //return the data through a broadcast after it is retrieved

        Intent returningIntent = new Intent(SERVICE_ACTION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getApplicationContext());

        if(NetworkHelper.hasNetworkAccess(getApplicationContext())){
            //we can make the call
            String address = uri.toString();
            Log.i(TAG, "About to try to download the post data");
            try {
                String response = HttpHelper.downloadUrl(address, "user", "pass");
                //this is what we want to send back to the Activity
                //But NOT as a String.
                //As a Parcelable Array of DataItems
                Gson gson = new Gson();
                DataItem[] dataItems = gson.fromJson(response, DataItem[].class);
                returningIntent.putExtra(SERVICE_PAYLOAD, dataItems);
                Log.i(TAG, "onHandleIntent: GOT THE JSON. Broadcasting");
                bm.sendBroadcast(returningIntent);

            } catch (IOException e){
                //we failed to fetch the URL
                //broadcast our shame to the user
                Log.i(TAG, SERVICE_EXCEPTION);
                returningIntent.putExtra(SERVICE_EXCEPTION, "Unable to fetch the JSON data.");
                bm.sendBroadcast(returningIntent);
            }
        }else{
            //we have no connection
            //broadcast our shame to the user
            Log.i(TAG, SERVICE_EXCEPTION);
            returningIntent.putExtra(SERVICE_EXCEPTION, "Unable to connect to the network.");
            bm.sendBroadcast(returningIntent);
        }
    }
}
