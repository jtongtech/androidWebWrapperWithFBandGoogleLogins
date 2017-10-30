package auth.quickstart.firebase.google.com.facebook_google_signin_template;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.iid.FirebaseInstanceId;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_USER = "auth.quickstart.firebase.google.com.facebook_google_signin_template.USER";
    public static final String EXTRA_EMAIL= "auth.quickstart.firebase.google.com.facebook_google_signin_template.EMAIL";
    public static final String EXTRA_FCM_TOKEN= "auth.quickstart.firebase.google.com.facebook_google_signin_template.FCM";
    SignInButton button;
    WaitListBtn waitListBtn;
    FirebaseAuth mAuth;
    private final static int RC_SIGN_IN = 2;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth.AuthStateListener mAuthListener;
    private CallbackManager callbackManager;


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "auth.quickstart.firebase.google.com.facebook_google_signin_template",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (SignInButton) findViewById(R.id.googleBtn);
        LoginButton facebook_btn = (LoginButton) findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        callbackManager = CallbackManager.Factory.create();
        mAuth = FirebaseAuth.getInstance();
        final String fcm_token = FirebaseInstanceId.getInstance().getToken();
//        Log.e("Firebase toke is", FirebaseInstanceId.getInstance().getToken());

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){
//                    Log.e("Current User", firebaseAuth.getCurrentUser().toString());
//                    Go to your webwrapper
                    Intent intent = new Intent(MainActivity.this, SecondPage.class);
                    String user = firebaseAuth.getCurrentUser().getUid();
                    String email = firebaseAuth.getCurrentUser().getEmail();
                    if (email != null) {
                        // User is signed in
                        email = firebaseAuth.getCurrentUser().getEmail();

                        // If the above were null, iterate the provider data
                        // and set with the first non null data
                        for (UserInfo userInfo : firebaseAuth.getCurrentUser().getProviderData()) {
                            if (email == null && userInfo.getEmail() != null) {
                                email = userInfo.getEmail();
                            }
                        }
                    }
                    if (email == null){
                        email = firebaseAuth.getCurrentUser().getEmail();;
                    }
                    intent.putExtra(EXTRA_USER, user);
                    intent.putExtra(EXTRA_EMAIL,email);
                    intent.putExtra(EXTRA_FCM_TOKEN,fcm_token);
                    startActivity(intent);
                }

            }
        };




        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText( MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        facebook_btn.setReadPermissions("email", "public_profile");
        // Other app specific specialization

        // Callback registration
        facebook_btn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText( MainActivity.this, "You are logged in with FB", Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
                Log.e("Firebase toke in", FirebaseInstanceId.getInstance().getToken());
                startActivity(new Intent(MainActivity.this, SecondPage.class));


            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText( MainActivity.this, "You canceled FB login", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText( MainActivity.this, "Auth went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
//                            startActivity(new Intent(MainActivity.this, SecondPage.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

//    GoogleSignInOptions gso = new
//            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build();

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText( MainActivity.this, "Auth went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private class WaitListBtn {
    }
}