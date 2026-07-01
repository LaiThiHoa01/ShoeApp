package com.example.shoeapp.user;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.DeliveryAddress;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.repo.AddressRepository;
import com.example.shoeapp.user.adapter.AddressAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AddressBookActivity extends AppCompatActivity {
    private AddressRepository addressRepository;
    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private AppDatabase db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Địa chỉ");
        }
        db = AppDatabase.getDatabase(this);
        addressRepository = new AddressRepository(this);

        int loggedInUserId = SessionManager.getUserId(this);

        if (loggedInUserId == -1) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        User user = db.userDao().getUserById(loggedInUserId);

        if (user == null) {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        this.currentUserId = user.userId;

        recyclerView = findViewById(R.id.address_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AddressAdapter(new ArrayList<>(), this::setDefaultAddress, this::deleteAddress);
        recyclerView.setAdapter(adapter);

        if (currentUserId != null) {
            loadAddresses();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin User trong Database!", Toast.LENGTH_SHORT).show();
        }

        MaterialButton btnAdd = findViewById(R.id.btn_add_address);
        btnAdd.setOnClickListener(v -> showAddAddressDialog());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadAddresses() {
        List<DeliveryAddress> list = addressRepository.getAddresses(currentUserId);
        adapter.setAddresses(list);
    }

    private void setDefaultAddress(DeliveryAddress address) {
        addressRepository.setDefault(currentUserId, address);
        loadAddresses();
        Toast.makeText(this, "Đã đổi địa chỉ mặc định", Toast.LENGTH_SHORT).show();
    }

    private void deleteAddress(DeliveryAddress address) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ?")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    addressRepository.delete(address);
                    loadAddresses();
                    Toast.makeText(this, "Đã xóa địa chỉ", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm địa chỉ mới");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.activity_profile, null);
        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Số điện thoại");
        final EditText inputAddress = new EditText(this);
        inputAddress.setHint("Địa chỉ đầy đủ");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputPhone);
        layout.addView(inputAddress);
        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            DeliveryAddress newAddress = new DeliveryAddress();
            newAddress.userId = currentUserId;
            newAddress.phoneNumber = inputPhone.getText().toString();
            newAddress.address = inputAddress.getText().toString();

            if(adapter.getItemCount() == 0) {
                newAddress.isDefault = true;
            } else {
                newAddress.isDefault = false;
            }

            new Thread(() -> {
                addressRepository.addAddress(
                        currentUserId,
                        inputPhone.getText().toString().trim(),
                        inputAddress.getText().toString().trim(),
                        adapter.getItemCount() == 0
                );
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                });
            }).start();
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}