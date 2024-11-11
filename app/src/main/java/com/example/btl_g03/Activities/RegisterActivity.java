package com.example.btl_g03.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.view.WindowCompat;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.btl_g03.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText edt_emai, edt_pas, edt_repas;
    FirebaseAuth auth;
    Button btn_regist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        edt_emai = findViewById(R.id.edt_email_dk);
        edt_pas = findViewById(R.id.edt_pass_dk);
        edt_repas = findViewById(R.id.edt_repass_dk);
        btn_regist = findViewById(R.id.btn_register);
        btn_regist.setOnClickListener(v -> {
            String email = edt_emai.getText().toString();
            String pass = edt_pas.getText().toString();
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Dang ky thanh cong", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("user_email", email);
                        intent.putExtra("password", pass);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Dang ky that bai", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}