package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView emailText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        emailText = findViewById(R.id.loginEmail);
        passwordText = findViewById(R.id.loginPassword);

        TextView forgetPass = findViewById(R.id.forgetPassword);
        forgetPass.setPaintFlags(forgetPass.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Log.d(Manager.getInstance().TAG, "getCurrentUser() not Null.");
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        findViewById(R.id.loginSignIn).setOnClickListener(v -> SignIn());
        findViewById(R.id.loginSignUp).setOnClickListener(v -> SignUp());
    }

    private void SignIn() {
        mAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(Manager.getInstance().TAG, "signInWithEmail:success");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(Manager.getInstance().TAG, "signInWithEmail:failure");
                        Toast.makeText(this, "이메일 또는 비밀번호를 확인해주세요.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void SignUp() {
        Intent intent = new Intent(this, SingUpActivity.class);
        startActivity(intent);
    }
}