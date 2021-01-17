package com.choi.firenote;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class AuthActivity extends AppCompatActivity{
//implements GoogleApiClient.OnConnectionFailedListener{

    //private SignInButton mSigninBtn;
    //private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 9001;
    private static GoogleSignInAccount account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();

       if( account!=null){ //자동 로그인 기능
           firebaseWithGoogle(account);
       }


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /*mGoogleApi = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build(); */

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                //Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApi);
                //startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            account = result.getSignInAccount();

            if(result.isSuccess()){
                firebaseWithGoogle(account);
            }else
                Toast.makeText(this,"인증 실패!",Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseWithGoogle(GoogleSignInAccount account){
        AuthCredential credential
                = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        Task<AuthResult> authResultTask
                = mAuth.signInWithCredential(credential);
        authResultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                //FirebaseUser firebaseUser = authResult.getUser();
                //Toast.makeText(AuthActivity.this, firebaseUser.getEmail(),Toast.LENGTH_LONG).show();
                //firebaseUser.getEmail();
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
                finish();
            }
        });
    }
/*
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"인증 실패!",Toast.LENGTH_SHORT).show();
    }
 */
}