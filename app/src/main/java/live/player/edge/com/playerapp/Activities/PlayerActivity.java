package live.player.edge.com.playerapp.Activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import io.github.krtkush.lineartimer.LinearTimer;
import io.github.krtkush.lineartimer.LinearTimerView;
import live.player.edge.com.playerapp.Adapters.CommentAdapter;
import live.player.edge.com.playerapp.Models.Comments;
import live.player.edge.com.playerapp.Models.Questions;
import live.player.edge.com.playerapp.Models.Status;
import live.player.edge.com.playerapp.R;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PlayerActivity extends AppCompatActivity implements LinearTimer.TimerListener, View.OnClickListener {
    TextView tvLiveStatus;
    EditText edtComment;
    DatabaseReference mDatabase;
    RecyclerView recyclerViewComments;
    LinearLayoutManager linearLayoutManager;
    CommentAdapter commentAdapter;
    List<Comments> commentsList = new ArrayList<>();
    VideoView videoView;
    CardView cardQuestion;
    TextView tvQuestion, tvTick, tvEliminated, tvPlayerCount;
    ImageView imageAnswerStatus;
    Button btnOption1, btnOption2, btnOption3;
    LinearTimer linearTimer;
    OkHttpClient client = new OkHttpClient();
    List<Questions> questions = new ArrayList<>();
    DatabaseReference databaseReference;
    boolean isOptionSelected = false, isElemenated = false;
    private static String QUIZ_URL = "/available_quiz.php";
    private static String POST_ANSWER_URL = "/leader_board.php";
    //private static final String APPLICATION_ID = "CaeKICW1agdVn9C1KIOWsw";
    String selectedOptionId = " ";
    LinearTimerView linearTimerView;
    SharedPreferences sharedPreferences;
    int selectedQuestionId, quizId, initQuestionId=0;
    int onCreateStatus=0, onPauseStatus=0, onResumeStatus=0, onStopStatus=0;
    @SuppressLint({"ResourceType", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT > 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_player);
        tvPlayerCount = findViewById(R.id.tv_player_count);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("live_user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int live_users = Integer.parseInt(dataSnapshot.getValue().toString());
                tvPlayerCount.setText(String.valueOf(live_users));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        databaseReference.child("live_user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onCreateStatus == 0){
                    int live_users = Integer.parseInt(dataSnapshot.getValue().toString());
                    live_users = live_users + 1;
                    databaseReference.child("live_user").setValue(live_users);
                    onCreateStatus = 1;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getQuiz();
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        videoView = this.findViewById(R.id.video_view);
        /*MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);*/
        //rtsp://93.115.28.144:1935/live/myStream
        //rtmp://rtmp.streamaxia.com/streamaxia/ef6a64c7cf098e0cTJfAWYfbiLQXPgyK14U3tcmTw9I3
        videoView.setVideoURI(Uri.parse("rtsp://93.115.28.144:1935/live/myStream"));
        videoView.start();
        videoView.requestFocus();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        recyclerViewComments = findViewById(R.id.recycler_comments);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewComments.setLayoutManager(linearLayoutManager);
        commentAdapter = new CommentAdapter(getApplicationContext(), commentsList);
        recyclerViewComments.setAdapter(commentAdapter);
        cardQuestion = findViewById(R.id.card_question);
        tvQuestion = findViewById(R.id.tv_question);
        btnOption1 = findViewById(R.id.btn_option1);
        btnOption2 = findViewById(R.id.btn_option2);
        btnOption3 = findViewById(R.id.btn_option3);
        btnOption1.setOnClickListener(this);
        btnOption2.setOnClickListener(this);
        btnOption3.setOnClickListener(this);
        tvEliminated = findViewById(R.id.tv_eliminated);
        imageAnswerStatus = findViewById(R.id.image_answer_status);
        linearTimerView = findViewById(R.id.linearTimer);
        tvTick = findViewById(R.id.tv_tick);
        tvTick.setText("10");
        mDatabase.child("quiz_status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(Objects.equals(dataSnapshot.getValue(), String.valueOf(2))){
                    Toast.makeText(getApplicationContext(),"QUIZ ENDED", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), QuizResults.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        linearTimer = new LinearTimer.Builder()
                .linearTimerView(linearTimerView)
                .timerListener(this)
                .duration(10 * 1000)
                .build();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        /*mVideoSurface =  findViewById(R.id.VideoSurfaceView);
        mPlayerStatusTextView =  findViewById(R.id.PlayerStatusTextView);*/
        tvLiveStatus = findViewById(R.id.tv_live_status);
        tvLiveStatus.setVisibility(View.GONE);
        /*if(getIntent().getExtras() !=null && getIntent().getExtras().containsKey("resource_uri")){
            uri = getIntent().getStringExtra("resource_uri");
        }*/
        /*Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                animateCard();
            }
        };
        timer.schedule(timerTask, 0, 10000);*/
        //animateLiveStatus();
        getComments();
        initializeComments();
    }
    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.child("live_user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onPauseStatus == 0){

                    Log.d("live user:", dataSnapshot.getValue().toString());
                    int live_users = Integer.parseInt(dataSnapshot.getValue().toString());
                    live_users = live_users - 1;
                    databaseReference.child("live_user").setValue(live_users);
                    onPauseStatus = 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.child("live_user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onResumeStatus == 0){
                    int live_users = Integer.parseInt(dataSnapshot.getValue().toString());
                    live_users = live_users + 1;
                    databaseReference.child("live_user").setValue(live_users);
                    onResumeStatus = 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void getComments() {
        databaseReference.child("comments").child("1").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comments comments = dataSnapshot.getValue(Comments.class);
                commentsList.add(comments);
                commentAdapter.notifyDataSetChanged();
                recyclerViewComments.smoothScrollToPosition(commentAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeComments() {
        edtComment = findViewById(R.id.edt_comment);
        edtComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (edtComment.getText().toString().length() > 0) {
                        sendCommentToFirebase(sharedPreferences.getString("display_name",""), edtComment.getText().toString());
                    }
                }
                return false;
            }
        });
    }

    private void sendCommentToFirebase(String username, String comment) {
        Comments comments = new Comments(username, comment);
        DatabaseReference newRef =  mDatabase.child("comments").child("1").push();
        newRef.setValue(comments);
        edtComment.setText("");
        edtComment.setHint("Enter your Comment..");
    }

    private void animateLiveStatus() {
        tvLiveStatus.setVisibility(View.VISIBLE);
        ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(tvLiveStatus, View.ALPHA, 1, 0);
        scaleAnim.setDuration(500);
        scaleAnim.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnim.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnim.start();
    }
    private void animateCard(int a1, int a2, int duration){
        final ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(cardQuestion, View.ALPHA, a1, a2);
        scaleAnim.setDuration(duration);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scaleAnim.start();
            }
        });
    }


    @Override
    public void animationComplete() {
        animateCard(1, 0, 100);
        linearTimer.resetTimer();
    }

    @Override
    public void timerTick(long tickUpdateInMillis) {
        btnOption1.setOnClickListener(this);
        btnOption2.setOnClickListener(this);
        btnOption3.setOnClickListener(this);
        tvTick.setText(String.valueOf(10 - (tickUpdateInMillis/1000)));
    }

    @Override
    public void onTimerReset() {
        btnOption1.setBackground(getResources().getDrawable(R.drawable.buttonshape));
        btnOption2.setBackground(getResources().getDrawable(R.drawable.buttonshape));
        btnOption3.setBackground(getResources().getDrawable(R.drawable.buttonshape));
        isOptionSelected = false;
    }
    private void getQuiz() {
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("test","test")
                .build();
        Request request = new Request.Builder().url(getResources().getString(R.string.base_url)+QUIZ_URL).addHeader("Token", getResources().getString(R.string.token)).post(requestBody).build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
                         @Override
                         public void onFailure(okhttp3.Call call, IOException e) {
                             System.out.println("Registration Error" + e.getMessage());
                         }
                         @Override
                         public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                             String resp = response.body().string();
                             if (response.isSuccessful()) {
                                 JSONObject obj = null;
                                 try {
                                     obj = new JSONObject(resp);
                                     JSONObject obj_response=obj.getJSONObject("Response");
                                     JSONObject obj_data=obj_response.getJSONObject("data");
                                     quizId = obj_data.getInt("QuizId");
                                     sharedPreferences.edit().putString("quiz_id", String.valueOf(quizId)).apply();
                                     JSONArray questionArray = obj_data.getJSONArray("QuizQuestions");
                                     for(int i=0; i<questionArray.length(); i++){
                                         JSONObject questionObject = questionArray.getJSONObject(i);
                                         String questionId = questionObject.getString("QuizQuestionId");
                                         String questionQuiz = questionObject.getString("QuizQuestion");
                                         String option1 = questionObject.getString("QuestionOptionA");
                                         String option2 = questionObject.getString("QuestionOptionB");
                                         String option3 = questionObject.getString("QuestionOptionC");
                                         Questions question = new Questions(Integer.parseInt(questionId), questionQuiz, option1, option2, option3);
                                         questions.add(question);}
                                     postQuestions(questions);
                                 } catch (JSONException e) {
                                     e.printStackTrace();
                                 }
                             }
                         }
                     }
        );
    }

    private void postQuestions(final List<Questions> questions) {
        databaseReference.child("quiz").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Snapshot", String.valueOf(dataSnapshot) + s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Status status = dataSnapshot.getValue(Status.class);
                assert status != null;
                if(initQuestionId < 2)
                    if(Objects.equals(status.question_status, "1")){
                        selectedQuestionId = Integer.parseInt(status.questionId);
                        linearTimerView.setVisibility(View.VISIBLE);
                        tvTick.setVisibility(View.VISIBLE);
                        tvTick.setVisibility(View.VISIBLE);
                        tvQuestion.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getQuestion());
                        btnOption1.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption1());
                        btnOption2.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption2());
                        btnOption3.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption3());
                        linearTimer.startTimer();
                        animateCard(0, 1, 1000);
                    }
                    else if(Objects.equals(status.question_status, "2")){
                        btnOption1.setOnClickListener(null);
                        btnOption2.setOnClickListener(null);
                        btnOption3.setOnClickListener(null);
                        tvQuestion.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getQuestion());
                        btnOption1.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption1());
                        btnOption2.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption2());
                        btnOption3.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption3());
                        linearTimerView.setVisibility(View.GONE);
                        tvTick.setVisibility(View.GONE);
                        if(isElemenated){
                            imageAnswerStatus.setVisibility(View.GONE);
                            tvEliminated.setVisibility(View.VISIBLE);
                            if(Objects.equals(status.question_answer, "2")){
                                btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }else if(Objects.equals(status.question_answer, "3")){
                                btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }else if(Objects.equals(status.question_answer, "1")){
                                btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }
                        }
                        if(Objects.equals(status.question_answer, selectedOptionId)){
                            imageAnswerStatus.setBackgroundResource(R.mipmap.true_symbol);
                            if(Objects.equals(selectedOptionId, "1")){
                                btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }else if(Objects.equals(selectedOptionId, "2")){
                                btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }else if(Objects.equals(selectedOptionId, "3")){
                                btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_true));
                            }

                        }else{
                            isElemenated = true;
                            tvEliminated.setVisibility(View.VISIBLE);
                            if(Objects.equals(selectedOptionId, "1")){
                                imageAnswerStatus.setBackgroundResource(R.mipmap.false_symbol);
                                btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_false));
                                if(Objects.equals(status.question_answer, "2")){
                                    btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }else if(Objects.equals(status.question_answer, "3")){
                                    btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }
                            }else if(Objects.equals(selectedOptionId, "2")){
                                imageAnswerStatus.setBackgroundResource(R.mipmap.false_symbol);
                                btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_false));
                                if(Objects.equals(status.question_answer, "1")){
                                    btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }else if(Objects.equals(status.question_answer, "3")){
                                    btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }
                            }else if(Objects.equals(selectedOptionId, "3")){
                                imageAnswerStatus.setBackgroundResource(R.mipmap.false_symbol);
                                btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_false));
                                if(Objects.equals(status.question_answer, "1")){
                                    btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }else if(Objects.equals(status.question_answer, "2")){
                                    btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_true));
                                }
                            }
                        }
                        imageAnswerStatus.setVisibility(View.VISIBLE);
                        animateCard(0, 1, 1000);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animateCard(1,0, 100);
                                btnOption1.setBackground(getResources().getDrawable(R.drawable.buttonshape));
                                btnOption2.setBackground(getResources().getDrawable(R.drawable.buttonshape));
                                btnOption3.setBackground(getResources().getDrawable(R.drawable.buttonshape));
                                isOptionSelected = false;
                                selectedOptionId = " ";
                                imageAnswerStatus.setVisibility(View.GONE);
                                tvEliminated.setVisibility(View.GONE);
                            }
                        }, 5000);
                    }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_option1:
                if(!isElemenated){
                    if(!isOptionSelected){
                        btnOption1.setBackground(getResources().getDrawable(R.drawable.button_selected));
                        isOptionSelected = true;
                        sendSelectedAnswer("1");
                    }}
                else{
                    tvEliminated.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_option2:
                if(!isElemenated){
                    if(!isOptionSelected){
                        btnOption2.setBackground(getResources().getDrawable(R.drawable.button_selected));
                        isOptionSelected = true;
                        sendSelectedAnswer("2");
                    }}
                else{
                    tvEliminated.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_option3:
                if(!isElemenated){
                    if(!isOptionSelected){
                        btnOption3.setBackground(getResources().getDrawable(R.drawable.button_selected));
                        isOptionSelected = true;
                        sendSelectedAnswer("3");
                    }}
                else{
                    tvEliminated.setVisibility(View.VISIBLE);

                }
                break;
        }
    }
    private void sendSelectedAnswer(String optionId) {
        selectedOptionId = optionId;
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("quiz_id", String.valueOf(quizId))
                .addFormDataPart("user_id",sharedPreferences.getString("user_id", ""))
                .addFormDataPart("question_id", String.valueOf(selectedQuestionId))
                .addFormDataPart("user_answer",selectedOptionId)
                .build();
        Request request = new Request.Builder().url(getResources().getString(R.string.base_url)+POST_ANSWER_URL).addHeader("Token", getResources().getString(R.string.token)).post(requestBody).build();
        okhttp3.Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback() {
                         @Override
                         public void onFailure(okhttp3.Call call, IOException e) {
                             System.out.println("Registration Error" + e.getMessage());
                         }
                         @Override
                         public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                             String resp = response.body().string();
                             Log.d("resp",resp);

                         }
                     }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.child("live_user").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(onStopStatus == 1){
                    Log.d("live user:", dataSnapshot.getValue().toString());
                    int live_users = Integer.parseInt(dataSnapshot.getValue().toString());
                    live_users = live_users - 1;
                    databaseReference.child("live_user").setValue(live_users);
                    onStopStatus = 1;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

