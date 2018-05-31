package live.player.edge.com.playerapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import live.player.edge.com.playerapp.R;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView imgProfileImage;
    String userName;
    EditText edtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        imgProfileImage = findViewById(R.id.img_profile_image);
        userName = getIntent().getStringExtra("user_name");
        edtUserName = findViewById(R.id.edt_user_name);
        edtUserName.setText(userName);
         Log.d("UserName:",userName);
        Picasso.with(getApplicationContext()).load(getIntent().getStringExtra("image_url")).into(imgProfileImage);
    }
}
