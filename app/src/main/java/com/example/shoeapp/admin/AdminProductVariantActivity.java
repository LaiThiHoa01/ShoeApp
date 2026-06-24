package com.example.shoeapp.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.adapter.AdminProductVariantAdapter;
import com.example.shoeapp.admin.viewmodel.AdminProductVariantViewModel;
import com.example.shoeapp.admin.viewmodel.VariantDisplayItem;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.Size;

import java.util.ArrayList;
import java.util.List;

public class AdminProductVariantActivity extends AppCompatActivity
        implements AdminProductVariantAdapter.OnVariantActionListener,
        AdminProductVariantViewModel.OnStateChangedListener {

    private ImageButton btnBack;
    private TextView tvProductName;
    private RecyclerView recyclerView;
    private Button btnAdd;
    private Button btnSave;

    private AdminProductVariantAdapter adapter;
    private AdminProductVariantViewModel viewModel;
    private AppDatabase db;
    private int productId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_variant);

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Không tìm thấy mã sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getDatabase(this);
        setupEdgeToEdge();
        bindViews();
        setupRecyclerView();
        setupViewModel();
        loadProductInfo();
    }

    private void setupEdgeToEdge() {
        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.variant_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        btnBack = findViewById(R.id.variant_btn_back);
        tvProductName = findViewById(R.id.variant_product_name);
        recyclerView = findViewById(R.id.variant_recycler);
        btnAdd = findViewById(R.id.variant_btn_add);
        btnSave = findViewById(R.id.variant_btn_save);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            // Force keyboard to hide and clear focus to trigger any active text inputs
            View currentFocus = getCurrentFocus();
            if (currentFocus != null) {
                currentFocus.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }
            viewModel.saveChanges(db);
        });
        btnAdd.setOnClickListener(v -> showAddVariantDialog());
    }

    private void setupRecyclerView() {
        adapter = new AdminProductVariantAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new AdminProductVariantViewModel();
        viewModel.setListener(this);
        viewModel.loadVariants(db, productId);
    }

    private void loadProductInfo() {
        Product p = db.productDao().getProductById(productId);
        if (p != null) {
            tvProductName.setText(p.name);
        }
    }

    // ── Adapter Listeners ───────────────────────────────────────────────────

    @Override
    public void onAdjustStock(int position, int delta) {
        viewModel.adjustStock(position, delta);
    }

    // ── ViewModel Listeners ──────────────────────────────────────────────────

    @Override
    public void onStateChanged(List<VariantDisplayItem> items) {
        adapter.submitList(items);
    }

    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveSuccess() {
        Toast.makeText(this, "Đã lưu thay đổi biến thể thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // ── Dialog Thêm biến thể mới ─────────────────────────────────────────────

    private void showAddVariantDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_variant, null);
        Spinner spinnerColor = dialogView.findViewById(R.id.dialog_variant_spinner_color);
        Spinner spinnerSize = dialogView.findViewById(R.id.dialog_variant_spinner_size);
        EditText inputStock = dialogView.findViewById(R.id.dialog_variant_input_stock);

        // Load colors & sizes
        List<Color> colors = db.productDao().getAllColors();
        List<Size> sizes = db.productDao().getAllSizes();

        if (colors.isEmpty() || sizes.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu màu sắc hoặc kích thước để chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> colorNames = new ArrayList<>();
        for (Color c : colors) {
            colorNames.add(c.name);
        }

        List<String> sizeNames = new ArrayList<>();
        for (Size s : sizes) {
            sizeNames.add("Size " + s.name);
        }

        // Tạo adapter sử dụng custom dark-theme layout của dự án
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, colorNames);
        colorAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerColor.setAdapter(colorAdapter);

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, sizeNames);
        sizeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerSize.setAdapter(sizeAdapter);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    int colorIndex = spinnerColor.getSelectedItemPosition();
                    int sizeIndex = spinnerSize.getSelectedItemPosition();

                    int colorId = colors.get(colorIndex).id;
                    int sizeId = sizes.get(sizeIndex).id;

                    String stockStr = inputStock.getText().toString().trim();
                    int stock = 0;
                    if (!stockStr.isEmpty()) {
                        try {
                            stock = Integer.parseInt(stockStr);
                        } catch (NumberFormatException ignored) {}
                    }

                    viewModel.addVariant(db, productId, colorId, sizeId, stock);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
