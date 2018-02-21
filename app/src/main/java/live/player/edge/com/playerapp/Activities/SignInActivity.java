package live.player.edge.com.playerapp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import live.player.edge.com.playerapp.R;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 1;
    private static final String EMAIL = "email";
    private static final String POST_URL = "/sign_up.php";
    GoogleSignInOptions gso;
    GoogleApiClient mGoogleApiClient;
    SignInButton signInButton;
    SharedPreferences sharedPreferences;
    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_signin);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();

            }
        });

    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d("Results:", String.valueOf(result));
            Log.d("Result", "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfolly, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                sharedPreferences.edit().putString("display_name",acct.getDisplayName()).apply();
                sharedPreferences.edit().putString("photo_url", acct.getPhotoUrl().toString()).apply();
                sharedPreferences.edit().putInt("login_status", 1).apply();
                /*mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));*/
                //Similarly you can get the email and photourl using acct.getEmail() and  acct.getPhotoUrl()
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                if(acct.getPhotoUrl() != null)
                    intent.putExtra("photo_url", acct.getPhotoUrl().toString());
                    intent.putExtra("user_name", acct.getDisplayName());
                    Log.d("Photo_url",acct.getDisplayName() + "=" + acct.getGivenName());
                    postSignInData(acct.getId(), acct.getDisplayName(), acct.getEmail(), String.valueOf(acct.getPhotoUrl()));
                startActivity(intent);
            }

        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("Connection Result:", String.valueOf(connectionResult));
    }
    private void postSignInData(String userId, String username, String email, String imageUrl){
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id",userId)
                .addFormDataPart("user_name",username)
                .addFormDataPart("user_email",email)
                .addFormDataPart("img_url",imageUrl)
                .build();
        Request request = new Request.Builder().url(getResources().getString(R.string.base_url)+POST_URL).addHeader("Token", getResources().getString(R.string.token)).post(requestBody).build();
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

                             /*if (response.isSuccessful()) {
                                 JSONObject obj = null;
                                 try {
                                     obj = new JSONObject(resp);
                                     JSONObject obj_response=obj.getJSONObject("Response");
                                     JSONObject obj_data=obj_response.getJSONObject("data");
                                     final String msgFinal = obj_data.getString("type");
                                     if(Objects.equals(msgFinal, "Success")){
                                         orderId = obj_data.getString("order_id");
                                         String lastId = obj_data.getString("last_id");
                                         if(filePaths.size() > 0){
                                             sendFile(FILE_URL, lastId);
                                         }
                                         Intent intent = new Intent(getApplicationContext(), SummaryOnlineConsultation.class);
                                         intent.putExtra("fee",fee);
                                         intent.putExtra("mode",modeVideoConsultation.getText().toString());
                                         sharedPreferences.edit().putInt("visa_id",102).apply();
                                         sharedPreferences.edit().putString("order_id",orderId).apply();
                                         intent.putExtra("order_id", orderId);
                                         startActivity(intent);
                                     }else{
                                         runOnUiThread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 showToast("Unsuccessful");
                                             }
                                         });
                                     }
                                 } catch (JSONException e) {
                                     e.printStackTrace();
                                 }
                             }*/
                         }
                     }
        );
    }
}
