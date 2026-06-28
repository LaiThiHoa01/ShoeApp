package com.example.shoeapp.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.authentication.SessionManager;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.User;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddUserBottomSheet extends BottomSheetDialogFragment {

    public interface OnUserSavedListener {
        void onUserSaved();
    }

    private static final String ARG_USER_ID = "arg_user_id";

    private OnUserSavedListener listener;
    private AppDatabase db;
    private int editUserId = -1;
    private User editUser;
    private boolean isActive = true;
    private String selectedRole = "CUSTOMER";

    private EditText etFullName, etEmail, etPhone, etPassword;
    private TextView tvActive, tvInactive;
    private TextView tvRoleCustomer, tvRoleAdmin;
    private TextView tvTitle;
    private com.google.android.material.button.MaterialButton btnCreateUser;

    public static AddUserBottomSheet newInstance(int userId, OnUserSavedListener listener) {
        AddUserBottomSheet fragment = new AddUserBottomSheet();
        fragment.listener = listener;
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddUserBottomSheet newInstance(OnUserSavedListener listener) {
        AddUserBottomSheet fragment = new AddUserBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getDatabase(requireContext());

        if (getArguments() != null) {
            editUserId = getArguments().getInt(ARG_USER_ID, -1);
        }

        // Khởi tạo các View
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etPhone = view.findViewById(R.id.et_phone);
        etPassword = view.findViewById(R.id.et_password);
        tvActive = view.findViewById(R.id.status_active);
        tvInactive = view.findViewById(R.id.status_inactive);
        tvRoleCustomer = view.findViewById(R.id.role_customer);
        tvRoleAdmin = view.findViewById(R.id.role_admin);
        btnCreateUser = view.findViewById(R.id.btn_create_user);

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dismiss());

        // Sự kiện chuyển đổi Trạng thái
        tvActive.setOnClickListener(v -> updateStatusUI(true));
        tvInactive.setOnClickListener(v -> updateStatusUI(false));

        // Sự kiện chuyển đổi Vai trò
        tvRoleCustomer.setOnClickListener(v -> updateRoleUI("CUSTOMER"));
        tvRoleAdmin.setOnClickListener(v -> updateRoleUI("ADMIN"));

        // Nạp dữ liệu cũ nếu chỉnh sửa
        if (editUserId != -1) {
            loadUserData();
            int currentAdminId = SessionManager.getUserId(requireContext());
            if (editUserId == currentAdminId) {
                tvActive.setEnabled(false);
                tvInactive.setEnabled(false);
                tvRoleCustomer.setEnabled(false);
                tvRoleAdmin.setEnabled(false);
                tvActive.setAlpha(0.5f);
                tvInactive.setAlpha(0.5f);
                tvRoleCustomer.setAlpha(0.5f);
                tvRoleAdmin.setAlpha(0.5f);
            }
        } else {
            updateStatusUI(true);
            updateRoleUI("CUSTOMER");
        }

        btnCreateUser.setOnClickListener(v -> saveUser());

        // Tự động focus vào ô Họ và tên và hiện bàn phím ảo
        etFullName.requestFocus();
        etFullName.postDelayed(() -> {
            if (getContext() != null) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                        getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etFullName, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);
    }

    private void updateRoleUI(String role) {
        this.selectedRole = role;
        if (getContext() == null) return;
        int colorActive = ContextCompat.getColor(getContext(), R.color.status_success);
        int colorInactive = ContextCompat.getColor(getContext(), R.color.text_dark_tertiary);

        if ("CUSTOMER".equals(role)) {
            tvRoleCustomer.setBackgroundResource(R.drawable.bg_status_active_dark);
            tvRoleCustomer.setTextColor(colorActive);
            
            tvRoleAdmin.setBackgroundResource(R.drawable.bg_card_dark);
            tvRoleAdmin.setTextColor(colorInactive);
        } else {
            tvRoleAdmin.setBackgroundResource(R.drawable.bg_status_active_dark);
            tvRoleAdmin.setTextColor(colorActive);
            
            tvRoleCustomer.setBackgroundResource(R.drawable.bg_card_dark);
            tvRoleCustomer.setTextColor(colorInactive);
        }
    }

    private void updateStatusUI(boolean active) {
        this.isActive = active;
        if (getContext() == null) return;
        int colorActive = ContextCompat.getColor(getContext(), R.color.status_success);
        int colorInactive = ContextCompat.getColor(getContext(), R.color.text_dark_tertiary);

        if (active) {
            tvActive.setBackgroundResource(R.drawable.bg_status_active_dark);
            tvActive.setTextColor(colorActive);
            
            tvInactive.setBackgroundResource(R.drawable.bg_card_dark);
            tvInactive.setTextColor(colorInactive);
        } else {
            tvInactive.setBackgroundResource(R.drawable.bg_status_active_dark);
            tvInactive.setTextColor(colorActive);
            
            tvActive.setBackgroundResource(R.drawable.bg_card_dark);
            tvActive.setTextColor(colorInactive);
        }
    }

    private void loadUserData() {
        new Thread(() -> {
            editUser = db.userDao().getUserById(editUserId);
            if (editUser != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTitle.setText("Chỉnh sửa thành viên");
                    etFullName.setText(editUser.fullName);
                    etEmail.setText(editUser.email);
                    etPhone.setText(editUser.phoneNumber);
                    etPassword.setHint("Để trống nếu giữ nguyên...");
                    btnCreateUser.setText("Cập nhật thông tin");
                    updateStatusUI(editUser.isActive);
                    updateRoleUI(editUser.role != null ? editUser.role : "CUSTOMER");
                });
            }
        }).start();
    }

    private void saveUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên không được để trống");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }
        if (phone.isEmpty()) {
            etPhone.setError("Số điện thoại không được để trống");
            return;
        }
        if (editUserId == -1 && password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống khi thêm mới");
            return;
        }

        new Thread(() -> {
            // Kiểm tra trùng email
            User existingUser = db.userDao().getUserByEmail(email);
            if (existingUser != null && (editUserId == -1 || existingUser.id != editUserId)) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> etEmail.setError("Địa chỉ email đã tồn tại"));
                }
                return;
            }

            if (editUserId == -1) {
                // Thêm mới
                User newUser = new User();
                newUser.fullName = fullName;
                newUser.email = email;
                newUser.phoneNumber = phone;
                newUser.passwordHash = password; // Lưu thuần túy theo cấu trúc dự án hiện tại
                newUser.role = selectedRole;
                newUser.isActive = isActive;
                newUser.userId = "USR-" + System.currentTimeMillis();
                newUser.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                
                db.userDao().insert(newUser);
            } else if (editUser != null) {
                // Chỉnh sửa
                editUser.fullName = fullName;
                editUser.email = email;
                editUser.phoneNumber = phone;
                editUser.role = selectedRole;
                editUser.isActive = isActive;
                if (!password.isEmpty()) {
                    editUser.passwordHash = password;
                }
                db.userDao().update(editUser);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onUserSaved();
                    }
                    dismiss();
                });
            }
        }).start();
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }
}
