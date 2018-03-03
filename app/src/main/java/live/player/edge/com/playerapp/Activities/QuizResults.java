package live.player.edge.com.playerapp.Activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import live.player.edge.com.playerapp.Adapters.CommentAdapter;
import live.player.edge.com.playerapp.Adapters.WinnerAdapter;
import live.player.edge.com.playerapp.Models.Questions;
import live.player.edge.com.playerapp.Models.Winners;
import live.player.edge.com.playerapp.R;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class QuizResults extends AppCompatActivity {
    private static String LEADERBOARD_URL = "/leader_score_board.php";
    OkHttpClient client = new OkHttpClient();
    SharedPreferences sharedPreferences;
    String ammount;
    List<Winners> winnersList = new ArrayList<>();
    RecyclerView recyclerView;
    GridLayoutManager gridLayoutManager;
    WinnerAdapter winnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getQuizResult();
    }

    private void getQuizResult() {
        initViews();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("quiz_id",sharedPreferences.getString("quiz_id",""))
                .build();
        Request request = new Request.Builder().url(getResources().getString(R.string.base_url)+LEADERBOARD_URL).addHeader("Token", getResources().getString(R.string.token)).post(requestBody).build();
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
                                     ammount = obj_data.getString("prize_per_winner");
                                     //Log.d("QUIZID", String.valueOf(quizId));
                                     JSONArray questionArray = obj_data.getJSONArray("winners");
                                     for (int i = 0; i < questionArray.length(); i++) {
                                         JSONObject questionObject = questionArray.getJSONObject(i);
                                         String winnerId = questionObject.getString("winnerId");
                                         String winnerName = questionObject.getString("winnerName");
                                         String winnerImg = questionObject.getString("winnerImg");
                                         Winners winners = new Winners(winnerId, ammount, winnerImg, winnerName);
                                         winnersList.add(winners);
                                     }
                                     Log.d("Size List", String.valueOf(winnersList.size()));
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             recyclerView.setAdapter(winnerAdapter);
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

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_winners);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        winnerAdapter = new WinnerAdapter(getApplicationContext(), winnersList);
    }
}
