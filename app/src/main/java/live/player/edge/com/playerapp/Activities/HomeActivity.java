package live.player.edge.com.playerapp.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;
import live.player.edge.com.playerapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {
    OkHttpClient mOkHttpClient = new OkHttpClient();
    private static final String API_KEY = "cwnka4f079cro6nfqolllkf36";
    Handler handler;
    Runnable runnable;
    ImageView profileImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        View mContentView = findViewById(R.id.content_fullscreen);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                getLatestResourceUri();
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(runnable, 1000) ;

        profileImage = findViewById(R.id.profile_image);
        if(getIntent().getExtras() != null){
            String imageUrl = getIntent().getStringExtra("photo_url");
            Picasso.with(getApplicationContext()).load(imageUrl).into(profileImage);
        }
    }
    void getLatestResourceUri() {
        Request request = new Request.Builder()
                .url("https://api.irisplatform.io/broadcasts")
                .addHeader("Accept", "application/vnd.bambuser.v1+json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .get()
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                /*runOnUiThread(new Runnable() { @Override public void run() {
                    if (mPlayerStatusTextView != null)
                        mPlayerStatusTextView.setText("Http exception: " + e);
                }});*/
            }
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                String body = response.body().string();
                String resourceUri = null;
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray results = json.getJSONArray("results");
                    JSONObject latestBroadcast = results.optJSONObject(0);
                    resourceUri = latestBroadcast.optString("resourceUri");
                    String status = latestBroadcast.getString("type");
                    final String uri = resourceUri;
                    if(Objects.equals(status, "live")){
                        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                        intent.putExtra("resource_uri",uri);
                        startActivity(intent);
                    }
                } catch (Exception ignored) {}
            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}