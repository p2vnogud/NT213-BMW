package com.example.bmw_lab5;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Khai báo các widget trong layout
        Button btnLogin = findViewById(R.id.btnLogin);
        EditText etEmail = findViewById(R.id.etInput);
        EditText etPassword = findViewById(R.id.etPassword);

        // Kiểm tra trạng thái người dùng đã đăng nhập hay chưa
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Chuyển sang màn hình UserInfo nếu đã đăng nhập
            fetchUsernameAndNavigate(currentUser.getUid());
        }

        // Xử lý nút đăng nhập
        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Kiểm tra đầu vào
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xác thực người dùng với Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                fetchUsernameAndNavigate(user.getUid());
                            }
                        } else {
                            // Đăng nhập thất bại
                            Toast.makeText(MainActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Chuyển đến màn hình đăng ký
        findViewById(R.id.tvGoToRegister).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterForm.class);
            startActivity(intent);
        });
    }

    /**
     * Hàm lấy username từ Firestore và chuyển sang màn hình UserInfo.
     */
    private void fetchUsernameAndNavigate(String userId) {
        DocumentReference userRef = firestore.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Lấy username từ Firestore
                String username = documentSnapshot.getString("name");
                if (username == null || username.isEmpty()) {
                    username = "Người dùng";
                }
                // Chuyển sang màn hình UserInfo
                Intent intent = new Intent(MainActivity.this, UserInfo.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Lỗi khi lấy dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
