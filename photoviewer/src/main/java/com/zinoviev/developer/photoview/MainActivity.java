package com.zinoviev.developer.photoview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import com.zinoviev.developer.photoviewer.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "MainActivity.class";

    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
    List<String> photoUrls = new ArrayList<String>();
    List<String> textUrls = new ArrayList<String>();

    ProgressDialog progress;

    ImageLoader imageLoader = ImageLoader.getInstance(); // Получили экземпляр

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File cacheDir = StorageUtils.getCacheDirectory(getBaseContext(), true);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(480, 800) // width, height
                .threadPoolSize(3)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(8 * 1024 * 1024)) // 16 Mb
                .discCacheExtraOptions(1024, 1024, Bitmap.CompressFormat.JPEG, 100, null)
                .discCache(new UnlimitedDiscCache(cacheDir))
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        imageLoader.init(config); // Проинициализировали конфигом по умолчанию

        if (!isOnline()) {

            // This is using code:
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Включить WiFi?");

            alert.setPositiveButton("Включить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
                    wm.setWifiEnabled(true);

                    while (!isOnline()) {
                        isOnline();
                    }
                    createRequest();
                }
            });
            alert.setNegativeButton("Выйти", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    finish();
                }
            });
            alert.show();

        } else {
            createRequest();
        }
    }

    /*
    * create & add request for Valley queue
     */
    private void createRequest() {

        Intent intent = getIntent();
        String tag = intent.getStringExtra("tag");
        String photoUrl = "https://api.vk.com/method/photos.search?q="+ tag +"&sort=1&count=1000&v=5.7";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET,
                photoUrl,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(String.valueOf(response));
                    JSONObject jsonResult = jsonObject.getJSONObject("response");
                    JSONArray jArray = jsonResult.getJSONArray("items");

                    for (int i = 0; i < jArray.length(); i++) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        // Retrieve JSON Objects
                        map.put("photo_604", jArray.getJSONObject(i).getString("photo_604"));
                        map.put("text", jArray.getJSONObject(i).getString("text"));
                        map.put("owner_id", jArray.getJSONObject(i).getString("owner_id"));
                        // Set the JSON Objects into the array
                        try{
                            arrayList.add(map);
                        }catch (Exception e) {
                            Log.e(LOG_TAG, "arrayList.add(map): ERROR");
                            e.printStackTrace();
                        }
                    }
                    onDataUpdated();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "onErrorResponse");
            }
        });
        progress = ProgressDialog.show(this, "Идет загрузка...",
                "подождите", true);
        queue.add(jsObjRequest);
    }

    /*
     * method create array from parser result
     * call in Valley Response Listener
     */
    private void onDataUpdated() {
        for(HashMap<String, String> item: arrayList) {
            photoUrls.add(item.get("photo_604"));
            textUrls.add(item.get("text"));
        }

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(MainActivity.this, "" + textUrls.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        progress.dismiss();
    }

    /*
     * GridView adapter
     */
    class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return photoUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SquareImageView imageView = new SquareImageView(mContext);

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            if (convertView == null) {
                imageLoader.displayImage(photoUrls.get(position), imageView, options); // Запустили асинхронный показ картинки
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            } else {
                imageView = (SquareImageView) convertView;
            }

            /*imageView.setImageUrl(photoUrls.get(position));*/
            return imageView;
        }
    }

    /*
     * check if WiFi available
     */
    public boolean isOnline() {
        // check if an internet connection is present
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && cm.getActiveNetworkInfo().isAvailable() && cm
                .getActiveNetworkInfo().isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
