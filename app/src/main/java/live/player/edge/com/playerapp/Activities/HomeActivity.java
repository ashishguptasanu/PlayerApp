package live.player.edge.com.playerapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Objects;
import live.player.edge.com.playerapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    OkHttpClient mOkHttpClient = new OkHttpClient();
    private static final String API_KEY = "";
    Handler handler;
    Runnable runnable;
    ImageView profileImage, imageMenus;
    TextView tvUserName, tvQuizDate, tvQuizPrize;
    Button btnWatchLive;
    String imageUrl;
    SharedPreferences sharedPreferences;
    private DatabaseReference mDatabase;
    OkHttpClient client = new OkHttpClient();
    String userName;
    private static String HOME_API = "/home_api.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initDatabase();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tvQuizDate = findViewById(R.id.tv_quiz_date);
        tvQuizPrize = findViewById(R.id.tv_quiz_prize);
        getUserDetails();
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
        btnWatchLive = findViewById(R.id.btn_watch_live);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("quiz_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(Objects.equals(dataSnapshot.getValue(), String.valueOf(1))){
                    btnWatchLive.setVisibility(View.VISIBLE);
                }else btnWatchLive.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //getLatestResourceUri();
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(runnable, 1000) ;
        profileImage = findViewById(R.id.profile_image);
        tvUserName = findViewById(R.id.tv_username);
        imageMenus = findViewById(R.id.image_menus);
        imageMenus.setOnClickListener(this);
        btnWatchLive.setOnClickListener(this);
        userName = sharedPreferences.getString("display_name","");
        imageUrl = sharedPreferences.getString("photo_url","");
        tvUserName.setText(userName);
        Picasso.with(getApplicationContext()).load(imageUrl).into(profileImage);
    }

    private void initDatabase() {
        /*SQLiteDatabase mydatabase = openOrCreateDatabase("your database name",MODE_PRIVATE,null);
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS TutorialsPoint(Username VARCHAR,Password VARCHAR);");
        mydatabase.execSQL("INSERT INTO TutorialsPoint VALUES('admin','admin');");
        Cursor resultSet = mydatabase.rawQuery("Select * from TutorialsPoint",null);
        resultSet.moveToFirst();
        String value = resultSet.getString(0);
        String[] value2  = resultSet.getColumnNames();
        Log.d("Result:", value + value2);*/
    }

    private void showMenu(){
        PowerMenu powerMenu = new PowerMenu.Builder(getApplicationContext())
                .addItem(new PowerMenuItem("Profile", false))
                .addItem(new PowerMenuItem("Add Referral Code", false))
                .addItem(new PowerMenuItem("Review", false))
                .addItem(new PowerMenuItem("Logout", false))
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setTextColor(getApplicationContext().getResources().getColor(R.color.colorAccent))
                .setSelectedTextColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                .setMenuColor(Color.WHITE)
                .setSelectedEffect(true)
                .setSelectedMenuColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                .setOnMenuItemClickListener(onMenuItemClickListener)

                .build();
        powerMenu.showAsDropDown(imageMenus);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.image_menus:
                showMenu();
                break;
            case R.id.btn_watch_live:
                Intent intent = new Intent(this, PlayerActivity.class);
                startActivity(intent);
                break;
        }
    }
    private  OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            if (Objects.equals(String.valueOf(position), "3")) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
                alertDialog.setMessage("Are you sure you want to logout?");
                alertDialog.setTitle("Logout");
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedPreferences.edit().clear().apply();
                        Intent intent = new Intent(getApplicationContext(), GettingStarted.class);
                        startActivity(intent);
                    }
                });
            }else if(Objects.equals(String.valueOf(position),"0")){
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                profileIntent.putExtra("image_url", imageUrl);
                profileIntent.putExtra("user_name", userName);
                startActivity(profileIntent);
            }
        }
    };
    private void getUserDetails() {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id",sharedPreferences.getString("user_id",""))
                .build();
        Request request = new Request.Builder().url(getResources().getString(R.string.base_url)+HOME_API).addHeader("Token", getResources().getString(R.string.token)).post(requestBody).build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
                         @Override
                         public void onFailure(okhttp3.Call call, IOException e) {
                             System.out.println("Registration Error" + e.getMessage());
                         }
                         @Override
                         public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                             String resp = response.body().string();
                             Log.d("resp", resp);
                             if (response.isSuccessful()) {
                                 JSONObject obj = null;
                                 try {
                                     obj = new JSONObject(resp);
                                     JSONObject obj_response = obj.getJSONObject("response");
                                     JSONObject obj_data = obj_response.getJSONObject("data");
                                     final String quizDate = obj_data.getString("quiz_launch_date");
                                     final String quizTime = obj_data.getString("quiz_prize");
                                     /*String rank = obj_data.getString("rank");
                                     String lives = obj_data.getString("lives");*/
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             tvQuizDate.setText(quizDate);
                                             tvQuizPrize.setText(quizTime);
                                         }
                                     });
                                 } catch(JSONException e){
                                     e.printStackTrace();
                                 }
                             }
                         }
                     }
        );
    }
}
