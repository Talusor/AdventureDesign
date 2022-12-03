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
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "이메일 형식을 확인해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.length() < 5) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(Manager.getInstance().TAG, "createUserWithEmail:success");
                    } else {
                        Log.d(Manager.getInstance().TAG, "createUserWithEmail:failure");
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            Toast.makeText(this, "이미 존재하는 이메일입니다.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}