package com.example.shoeapp.admin;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.admin.adapter.ItemSelectProductAdapter;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.data.entity.PromotionProduct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class AdminAddPromotionActivity extends AppCompatActivity {

    private EditText etName, etSubtitle, etDescription, etBannerUrl, etVoucherCode, etQuantity, etDiscountValue, etStartDate, etEndDate, etMaxDiscount;
    private RadioGroup rgDiscountType, rgTargetType;
    private SwitchCompat switchIsActive;
    private Spinner spCategory, spBrand;
    private View layoutCategorySelect, layoutProductsSelect, layoutBrandSelect;
    private Button btnSelectProducts;
    private TextView tvSelectedProducts;
    private ProductDao productDao;
    
    private List<Category> categories;
    private List<Brand> brands;
    private List<Product> allProducts = new ArrayList<>();
    private Set<Integer> selectedProductIds = new HashSet<>();
    
    private int editingPromotionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_add_promotion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_add_promo_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        productDao = AppDatabase.getDatabase(this).productDao();

        initViews();
        loadCategories();
        loadBrands();
        loadProducts();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> savePromotion());
        
        btnSelectProducts.setOnClickListener(v -> showProductSelectionDialog());
        
        editingPromotionId = getIntent().getIntExtra("promotion_id", -1);
        if (editingPromotionId != -1) {
            ((TextView) findViewById(R.id.tv_header_title)).setText("Sửa Khuyến mãi");
            loadPromotionData();
        }
        
        rgTargetType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutCategorySelect.setVisibility(View.GONE);
            layoutProductsSelect.setVisibility(View.GONE);
            layoutBrandSelect.setVisibility(View.GONE);
            if (checkedId == R.id.rb_target_category) {
                layoutCategorySelect.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.rb_target_brand) {
                layoutBrandSelect.setVisibility(View.VISIBLE);
            } else {
                layoutProductsSelect.setVisibility(View.VISIBLE);
            }
        });
        
        rgDiscountType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_percentage) {
                etMaxDiscount.setVisibility(View.VISIBLE);
            } else {
                etMaxDiscount.setVisibility(View.GONE);
                etMaxDiscount.setText("");
            }
        });
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etSubtitle = findViewById(R.id.et_subtitle);
        etDescription = findViewById(R.id.et_description);
        etBannerUrl = findViewById(R.id.et_banner_url);
        etVoucherCode = findViewById(R.id.et_voucher_code);
        etQuantity = findViewById(R.id.et_quantity);
        etDiscountValue = findViewById(R.id.et_discount_value);
        etStartDate = findViewById(R.id.et_start_date);
        etEndDate = findViewById(R.id.et_end_date);
        etMaxDiscount = findViewById(R.id.et_max_discount);
        
        rgDiscountType = findViewById(R.id.rg_discount_type);
        rgTargetType = findViewById(R.id.rg_target_type);
        switchIsActive = findViewById(R.id.switch_is_active);
        spCategory = findViewById(R.id.sp_category);
        spBrand = findViewById(R.id.sp_brand);
        
        layoutCategorySelect = findViewById(R.id.layout_category_select);
        layoutBrandSelect = findViewById(R.id.layout_brand_select);
        layoutProductsSelect = findViewById(R.id.layout_products_select);
        
        btnSelectProducts = findViewById(R.id.btn_select_products);
        tvSelectedProducts = findViewById(R.id.tv_selected_products);
    }
    
    private void loadCategories() {
        Executors.newSingleThreadExecutor().execute(() -> {
            categories = productDao.getAllCategories();
            runOnUiThread(() -> {
                if (categories != null && !categories.isEmpty()) {
                    String[] categoryNames = new String[categories.size()];
                    for (int i = 0; i < categories.size(); i++) {
                        categoryNames[i] = categories.get(i).name;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
                    spCategory.setAdapter(adapter);
                }
            });
        });
    }
    
    private void loadBrands() {
        Executors.newSingleThreadExecutor().execute(() -> {
            brands = productDao.getAllBrands();
            runOnUiThread(() -> {
                if (brands != null && !brands.isEmpty()) {
                    String[] brandNames = new String[brands.size()];
                    for (int i = 0; i < brands.size(); i++) {
                        brandNames[i] = brands.get(i).name;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brandNames);
                    spBrand.setAdapter(adapter);
                }
            });
        });
    }
    
    private void loadProducts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            allProducts = productDao.getAllProducts();
        });
    }

    private void loadPromotionData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Promotion promo = productDao.getPromotionById(editingPromotionId);
            if (promo != null) {
                List<PromotionProduct> pps = productDao.getProductsByPromotion(editingPromotionId);
                runOnUiThread(() -> {
                    etName.setText(promo.name);
                    etSubtitle.setText(promo.subtitle != null ? promo.subtitle : "");
                    etDescription.setText(promo.description != null ? promo.description : "");
                    etBannerUrl.setText(promo.bannerUrl != null ? promo.bannerUrl : "");
                    etVoucherCode.setText(promo.voucherCode != null ? promo.voucherCode : "");
                    etQuantity.setText(String.valueOf(promo.quantity));
                    etDiscountValue.setText(String.valueOf(promo.discountValue));
                    etStartDate.setText(promo.startDate != null ? promo.startDate : "");
                    etEndDate.setText(promo.endDate != null ? promo.endDate : "");
                    switchIsActive.setChecked(promo.isActive);

                    if ("PERCENTAGE".equals(promo.discountType)) {
                        rgDiscountType.check(R.id.rb_percentage);
                        etMaxDiscount.setVisibility(View.VISIBLE);
                        if (promo.maxDiscountAmount != null) {
                            etMaxDiscount.setText(String.valueOf(promo.maxDiscountAmount));
                        }
                    } else {
                        rgDiscountType.check(R.id.rb_fixed_amount);
                        etMaxDiscount.setVisibility(View.GONE);
                    }

                    if ("CATEGORY".equals(promo.targetType)) {
                        rgTargetType.check(R.id.rb_target_category);
                        layoutCategorySelect.setVisibility(View.VISIBLE);
                        if (categories != null) {
                            for (int i = 0; i < categories.size(); i++) {
                                if (promo.categoryId != null && categories.get(i).id == promo.categoryId) {
                                    spCategory.setSelection(i);
                                    break;
                                }
                            }
                        }
                    } else if ("BRAND".equals(promo.targetType)) {
                        rgTargetType.check(R.id.rb_target_brand);
                        layoutBrandSelect.setVisibility(View.VISIBLE);
                        if (brands != null) {
                            for (int i = 0; i < brands.size(); i++) {
                                if (promo.brandId != null && brands.get(i).id == promo.brandId) {
                                    spBrand.setSelection(i);
                                    break;
                                }
                            }
                        }
                    } else {
                        rgTargetType.check(R.id.rb_target_products);
                        layoutProductsSelect.setVisibility(View.VISIBLE);
                        if (pps != null) {
                            for (PromotionProduct pp : pps) {
                                selectedProductIds.add(pp.productId);
                            }
                            updateSelectedProductsText();
                        }
                    }
                });
            }
        });
    }
    
    private void showProductSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_select_products);
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
        
        EditText etSearch = dialog.findViewById(R.id.et_search_product);
        RecyclerView rvProducts = dialog.findViewById(R.id.rv_select_products);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_selection);
        ImageButton btnClose = dialog.findViewById(R.id.btn_close_dialog);
        
        ItemSelectProductAdapter adapter = new ItemSelectProductAdapter();
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
        
        adapter.setProductList(allProducts);
        // Create a copy of the selected set so user can cancel without affecting main state
        Set<Integer> tempSelectedIds = new HashSet<>(selectedProductIds);
        adapter.setSelectedProductIds(tempSelectedIds);
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                if (query.isEmpty()) {
                    adapter.setProductList(allProducts);
                } else {
                    List<Product> filtered = new ArrayList<>();
                    for (Product p : allProducts) {
                        if (p.name.toLowerCase().contains(query)) {
                            filtered.add(p);
                        }
                    }
                    adapter.setProductList(filtered);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        btnConfirm.setOnClickListener(v -> {
            selectedProductIds = new HashSet<>(adapter.getSelectedProductIds());
            updateSelectedProductsText();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void updateSelectedProductsText() {
        tvSelectedProducts.setText("Đã chọn: " + selectedProductIds.size() + " sản phẩm");
    }

    private void savePromotion() {
        String name = etName.getText().toString().trim();
        String subtitle = etSubtitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String bannerUrl = etBannerUrl.getText().toString().trim();
        String voucherCode = etVoucherCode.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String discountValStr = etDiscountValue.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        
        if (name.isEmpty() || voucherCode.isEmpty() || quantityStr.isEmpty() || discountValStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        Promotion p = new Promotion();
        p.name = name;
        p.slug = name.toLowerCase().replace(" ", "-");
        p.subtitle = subtitle;
        p.description = description;
        p.bannerUrl = bannerUrl;
        p.voucherCode = voucherCode;
        p.quantity = Integer.parseInt(quantityStr);
        p.discountValue = Double.parseDouble(discountValStr);
        p.startDate = startDate;
        p.endDate = endDate;
        p.isActive = switchIsActive.isChecked();
        
        if (rgDiscountType.getCheckedRadioButtonId() == R.id.rb_percentage) {
            p.discountType = "PERCENTAGE";
            String maxDiscountStr = etMaxDiscount.getText().toString().trim();
            if (!maxDiscountStr.isEmpty()) {
                p.maxDiscountAmount = Double.parseDouble(maxDiscountStr);
            } else {
                p.maxDiscountAmount = null;
            }
        } else {
            p.discountType = "FIXED_AMOUNT";
            p.maxDiscountAmount = null;
        }
        
        if (rgTargetType.getCheckedRadioButtonId() == R.id.rb_target_category) {
            p.targetType = "CATEGORY";
            int selectedPos = spCategory.getSelectedItemPosition();
            if (categories != null && selectedPos >= 0 && selectedPos < categories.size()) {
                p.categoryId = categories.get(selectedPos).id;
            }
        } else if (rgTargetType.getCheckedRadioButtonId() == R.id.rb_target_brand) {
            p.targetType = "BRAND";
            int selectedPos = spBrand.getSelectedItemPosition();
            if (brands != null && selectedPos >= 0 && selectedPos < brands.size()) {
                p.brandId = brands.get(selectedPos).id;
            }
        } else {
            p.targetType = "PRODUCTS";
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            long promoId;
            if (editingPromotionId != -1) {
                p.id = editingPromotionId;
                productDao.updatePromotion(p);
                productDao.deleteProductsByPromotion(editingPromotionId);
                promoId = editingPromotionId;
            } else {
                promoId = productDao.insertPromotionReturnId(p);
            }
            
            if ("PRODUCTS".equals(p.targetType)) {
                for (int productId : selectedProductIds) {
                    PromotionProduct pp = new PromotionProduct();
                    pp.promotionId = (int) promoId;
                    pp.productId = productId;
                    productDao.insertPromotionProduct(pp);
                }
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
