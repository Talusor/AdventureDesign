package kr.cnu.ai.lth.adventuredesign;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SingUpActivity extends AppCompatActivity {
    TextView emailText, nameText, passwordText, passwordConfirmText;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        emailText = findViewById(R.id.loginEmail);
        nameText = findViewById(R.id.loginName);
        passwordText = findViewById(R.id.loginPassword);
        passwordConfirmText = findViewById(R.id.loginPasswordConfirm);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.loginSignUp).setOnClickListener(v -> SignUp());
    }

    private void SignUp() {
        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String passwordConfirm = passwordConfirmText.getText().toString();

        if (name.isEmpty()) {
            nameText.requestFocus();
            Toast.makeText(this, "유저 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            emailText.requestFocus();
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            passwordText.requestFocus();
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordConfirm.isEmpty()) {
            passwordConfirmText.requestFocus();
            Toast.makeText(this, "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.requestFocus();
            Toast.makeText(this, "이메일 형식을 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 5) {
            passwordText.requestFocus();
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_LONG).show();
                        Map<String, Object> data = new HashMap<>();
                        data.put("name", name);

                        db.collection("user").document(task.getResult().getUser().getUid()).set(data);

                        Log.d(Manager.getInstance().TAG, "createUserWithEmail:success");
                        finish();
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