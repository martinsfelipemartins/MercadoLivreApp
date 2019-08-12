package br.com.digitalhouse.mercadolivreapp.tairo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    // Botões customizados
    private Button btnLoginGoogle;
    private Button btnLoginFacebook;

    //Botões Google e Facebook default
    private SignInButton btnGoogleDefault;
    private LoginButton btnfacebookDefault;

    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    private static final int RESULT_GOOGLE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnGoogleDefault = findViewById(R.id.btnLoginGoogleDefault);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);
        //btnfacebookDefault = findViewById(R.id.btnLoginFacebookDefault);


        btnLoginGoogle.setOnClickListener(view -> {
            GoogleSignInOptions options = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

             googleSignInClient = GoogleSignIn.getClient(this, options);
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RESULT_GOOGLE);
        });

        btnGoogleDefault.setSize(SignInButton.SIZE_WIDE);
        btnGoogleDefault.setOnClickListener(view -> {
            GoogleSignInOptions options = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, options);
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent, RESULT_GOOGLE);
        });

        // Verifica se usuário já esta logado
       /* GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            gotoHome(account.getEmail());
        }*/




        callbackManager = CallbackManager.Factory.create();

        btnLoginFacebook.setOnClickListener(view -> {
           LoginManager
                   .getInstance()
                   .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));

           LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
               @Override
               public void onSuccess(LoginResult loginResult) {
                   getFacebookProfile(loginResult);
               }

               @Override
               public void onCancel() {
                   Log.i("TAG", "onCancel: ");
               }

               @Override
               public void onError(FacebookException error) {
                   Log.i("TAG", "error: " + error.getMessage());
               }
           });
       });

    }

    private void getFacebookProfile(LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (result, response) -> {
            try {
                Log.i("RESAULTS : ", result.toString());

                gotoHome(result.getString("email"));
            } catch (Exception e) {
                Log.e("TAG", "Erro ao buscar profile : ", e);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RESULT_GOOGLE) {

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Log.w("TAG", "signInResult:success user=" + account.getDisplayName() + account.getEmail());
                gotoHome(account.getEmail());

            } catch (ApiException e) {
                Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
                e.printStackTrace();
            }
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void gotoHome(String email) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
        finish();
    }
}
