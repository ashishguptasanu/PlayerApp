package live.player.edge.com.playerapp.Activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bambuser.broadcaster.BroadcastPlayer;
import com.bambuser.broadcaster.PlayerState;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.github.krtkush.lineartimer.LinearTimer;
import io.github.krtkush.lineartimer.LinearTimerView;
import live.player.edge.com.playerapp.Adapters.CommentAdapter;
import live.player.edge.com.playerapp.Models.Comments;
import live.player.edge.com.playerapp.Models.Questions;
import live.player.edge.com.playerapp.Models.Status;
import live.player.edge.com.playerapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayerActivity extends AppCompatActivity implements LinearTimer.TimerListener, View.OnClickListener {
    SurfaceView mVideoSurface;
    TextView mPlayerStatusTextView;
    BroadcastPlayer mBroadcastPlayer;
    MediaController mMediaController = null;
    OkHttpClient mOkHttpClient = new OkHttpClient();
    String uri;
    TextView tvLiveStatus;
    EditText edtComment;
    DatabaseReference mDatabase;
    RecyclerView recyclerViewComments;
    LinearLayoutManager linearLayoutManager;
    CommentAdapter commentAdapter;
    List<Comments> commentsList = new ArrayList<>();
    VideoView videoView;
    CardView cardQuestion;
    TextView tvQuestion, tvTick;
    ImageView imageAnswerStatus;
    Button btnOption1, btnOption2, btnOption3;
    LinearTimer linearTimer;
    OkHttpClient client = new OkHttpClient();
    List<Questions> questions = new ArrayList<>();
    DatabaseReference databaseReference;
    boolean isOptionSelected = false;
    private static String QUIZ_URL = "/available_quiz.php";
    private static String POST_ANSWER_URL = "/post_answer.php";
    private static final String APPLICATION_ID = "CaeKICW1agdVn9C1KIOWsw";
    String selectedOptionId = " ";
    LinearTimerView linearTimerView;

    BroadcastPlayer.Observer mBroadcastPlayerObserver = new BroadcastPlayer.Observer() {
        @Override
        public void onStateChange(PlayerState playerState) {
            if (mPlayerStatusTextView != null)
                mPlayerStatusTextView.setText("Status: " + playerState);
            if(Objects.equals(playerState.name(), "COMPLETED")){
                tvLiveStatus.setVisibility(View.GONE);
            }
            if (playerState == PlayerState.PLAYING || playerState == PlayerState.PAUSED || playerState == PlayerState.COMPLETED) {
                if (mMediaController == null && mBroadcastPlayer != null && !mBroadcastPlayer.isTypeLive()) {
                    mMediaController = new MediaController(PlayerActivity.this);
                    mMediaController.setAnchorView(mVideoSurface);
                    mMediaController.setMediaPlayer(mBroadcastPlayer);
                }
                if (mMediaController != null) {
                    mMediaController.setEnabled(true);
                    mMediaController.show();
                }
            } else if (playerState == PlayerState.ERROR || playerState == PlayerState.CLOSED) {
                if (mMediaController != null) {
                    mMediaController.setEnabled(false);
                    mMediaController.hide();
                }
                mMediaController = null;
            }

        }
        @Override
        public void onBroadcastLoaded(boolean live, int width, int height) {
        }
    };
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
        databaseReference = FirebaseDatabase.getInstance().getReference();
        getQuiz();
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        videoView = this.findViewById(R.id.video_view);
        /*MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);*/

        videoView.setVideoURI(Uri.parse("rtsp://93.115.28.144:1935/live/myStream"));
        Log.d("Buffer", String.valueOf(videoView.getBufferPercentage()));
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
        imageAnswerStatus = findViewById(R.id.image_answer_status);
        linearTimerView = (LinearTimerView)
                findViewById(R.id.linearTimer);
        tvTick = findViewById(R.id.tv_tick);
        tvTick.setText("10");
        linearTimer = new LinearTimer.Builder()
                .linearTimerView(linearTimerView)
                .timerListener(this)
                .duration(10 * 1000)
                .build();

        /*mVideoSurface =  findViewById(R.id.VideoSurfaceView);
        mPlayerStatusTextView =  findViewById(R.id.PlayerStatusTextView);*/
        tvLiveStatus = findViewById(R.id.tv_live_status);
        tvLiveStatus.setVisibility(View.GONE);
        /*if(getIntent().getExtras() !=null && getIntent().getExtras().containsKey("resource_uri")){
            uri = getIntent().getStringExtra("resource_uri");
        }*/



        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("Prepared", "True");


            }
        });
        /*Timer timer = new Timer();
        TimerTask timerTask;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                animateCard();
            }
        };
        timer.schedule(timerTask, 0, 10000);*/
        animateLiveStatus();
        getComments();
        initializeComments();
    }
    @Override
    protected void onPause() {
        super.onPause();
        /*mOkHttpClient.dispatcher().cancelAll();
        mVideoSurface = null;
        if (mBroadcastPlayer != null)
            mBroadcastPlayer.close();
        mBroadcastPlayer = null;
        if (mMediaController != null)
            mMediaController.hide();
        mMediaController = null;*/
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*if (ev.getActionMasked() == MotionEvent.ACTION_UP && mBroadcastPlayer != null && mMediaController != null) {
            PlayerState state = mBroadcastPlayer.getState();
            if (state == PlayerState.PLAYING ||
                    state == PlayerState.BUFFERING ||
                    state == PlayerState.PAUSED ||
                    state == PlayerState.COMPLETED) {
                if (mMediaController.isShowing())
                    mMediaController.hide();
                else
                    mMediaController.show();
            } else {
                mMediaController.hide();
            }
        }*/
        return false;
    }
    @Override
    protected void onResume() {
        super.onResume();
        /*mVideoSurface = findViewById(R.id.VideoSurfaceView);
        mPlayerStatusTextView.setText("Loading latest broadcast");
        if(getIntent().getExtras() !=null && getIntent().getExtras().containsKey("resource_uri")){
            uri = getIntent().getStringExtra("resource_uri");
        }
        initPlayer(uri);
        //*/
    }
    void initPlayer(String resourceUri) {
        /*animateLiveStatus();
        getComments();
        initializeComments();
        if (resourceUri == null) {
            if (mPlayerStatusTextView != null)
                mPlayerStatusTextView.setText("Could not get info about latest broadcast");
            return;
        }
        if (mVideoSurface == null) {
            // UI no longer active
            return;
        }
        if (mBroadcastPlayer != null)
            mBroadcastPlayer.close();
        mBroadcastPlayer = new BroadcastPlayer(this, resourceUri, APPLICATION_ID, mBroadcastPlayerObserver);
        mBroadcastPlayer.setSurfaceView(mVideoSurface);
        mBroadcastPlayer.load();*/


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
                Log.d("Clicked", "1");
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Log.d("Clicked", "2");
                    if (edtComment.getText().toString().length() > 0) {
                        sendCommentToFirebase("Nitin", edtComment.getText().toString());


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
        Log.d("Tick", String.valueOf(10 - (tickUpdateInMillis/1000)));
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
                             Log.d("resp",resp);

                             if (response.isSuccessful()) {
                                 JSONObject obj = null;
                                 try {
                                     obj = new JSONObject(resp);
                                     JSONObject obj_response=obj.getJSONObject("Response");
                                     JSONObject obj_data=obj_response.getJSONObject("data");
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
                                         Log.d("Size List", String.valueOf(questions.size()));
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
                Log.d("Status", String.valueOf(dataSnapshot) + dataSnapshot.getKey());
                Status status = dataSnapshot.getValue(Status.class);
                assert status != null;
                Log.d("Status",status.question_status + " " + status.question_answer + " " + status.questionId);
                if(Objects.equals(status.question_status, "1")){
                    Log.d("Changed", "Question Status");
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
                    btnOption1.setOnClickListener(null);
                    btnOption1.setOnClickListener(null);
                    tvQuestion.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getQuestion());
                    btnOption1.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption1());
                    btnOption2.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption2());
                    btnOption3.setText(questions.get(Integer.parseInt(dataSnapshot.getKey()) - 1).getOption3());
                    linearTimerView.setVisibility(View.GONE);
                    tvTick.setVisibility(View.GONE);
                    Log.d("Selected Answer", String.valueOf(selectedOptionId));
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
                        imageAnswerStatus.setBackgroundResource(R.mipmap.false_symbol);
                        if(Objects.equals(selectedOptionId, "1")){
                            btnOption1.setBackground(getResources().getDrawable(R.drawable.answer_false));
                        }else if(Objects.equals(selectedOptionId, "2")){
                            btnOption2.setBackground(getResources().getDrawable(R.drawable.answer_false));
                        }else if(Objects.equals(selectedOptionId, "3")){
                            btnOption3.setBackground(getResources().getDrawable(R.drawable.answer_false));
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
                        }
                    }, 5000);
                }



                //Log.d("Snapshot", String.valueOf(dataSnapshot) + dataSnapshot.child("question_status").getValue());

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
                if(!isOptionSelected){
                    btnOption1.setBackground(getResources().getDrawable(R.drawable.button_selected));
                    isOptionSelected = true;
                    sendSelectedAnswer("1");
                }
                break;
            case R.id.btn_option2:
                if(!isOptionSelected){
                    btnOption2.setBackground(getResources().getDrawable(R.drawable.button_selected));
                    isOptionSelected = true;
                    sendSelectedAnswer("2");
                }
                break;
            case R.id.btn_option3:
                if(!isOptionSelected){
                    btnOption3.setBackground(getResources().getDrawable(R.drawable.button_selected));
                    isOptionSelected = true;
                    sendSelectedAnswer("3");
                }
                break;
        }
    }

    private void sendSelectedAnswer(String optionId) {
        selectedOptionId = optionId;
        RequestBody requestBody = new MultipartBody.Builder()
                .addFormDataPart("test","test")
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
                            /* if (response.isSuccessful()) {
                                 JSONObject obj = null;
                                 try {
                                     obj = new JSONObject(resp);
                                     JSONObject obj_response=obj.getJSONObject("Response");
                                     JSONObject obj_data=obj_response.getJSONObject("data");

                                 } catch (JSONException e) {
                                     e.printStackTrace();
                                 }
                             }*/
                         }
                     }
        );
    }
}

