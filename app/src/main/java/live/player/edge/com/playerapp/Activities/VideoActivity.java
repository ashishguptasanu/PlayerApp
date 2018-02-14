package live.player.edge.com.playerapp.Activities;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

import live.player.edge.com.playerapp.R;

public class VideoActivity extends AppCompatActivity {
    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_video);


        videoView = (VideoView)this.findViewById(R.id.video_view);

        //add controls to a MediaPlayer like play, pause.
        /*MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);*/

        //Set the path of Video or URI
        videoView.setVideoURI(Uri.parse("rtsp://93.115.28.144:1935/live/myStream"));

        //
        videoView.start();
        //Set the focus
        videoView.requestFocus();

    }
}
