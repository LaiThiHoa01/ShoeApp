package com.example.shoeapp.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductImg;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.shoeapp.user.ImageLoader;

public class AdminAddProductActivity extends BaseAdminActivity {

    private EditText etName, etPrice, etOriginalPrice, etDescription, etProductImageUrl;
    private Spinner  spinnerBrand, spinnerCategory;
    private CheckBox cbIsAvailable;
    private ImageView ivProductPreview;
    private TextView tvTitle;
    private com.google.android.material.button.MaterialButton btnSave;
    private AppDatabase db;

    private List<Brand>    brands     = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    private boolean isEditMode = false;
    private int productId = -1;
    private Product editingProduct;

    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        etProductImageUrl.setText(uri.toString());
                        ImageLoader.load(uri.toString(), ivProductPreview, R.drawable.ic_shoe);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);
        db = AppDatabase.getDatabase(this);

        androidx.activity.EdgeToEdge.enable(this);
        View root = findViewById(R.id.admin_add_product_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        loadSpinnerData();

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId != -1) {
            isEditMode = true;
            tvTitle.setText("Sửa sản phẩm");
            btnSave.setText("Cập nhật sản phẩm");
            loadProductData();
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_select_image).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
        btnSave.setOnClickListener(v -> saveProduct());

        etProductImageUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                ImageLoader.load(s.toString().trim(), ivProductPreview, R.drawable.ic_shoe);
            }
        });
    }

    private void bindViews() {
        etName            = findViewById(R.id.et_product_name);
        etPrice           = findViewById(R.id.et_product_price);
        etOriginalPrice   = findViewById(R.id.et_product_original_price);
        etDescription     = findViewById(R.id.et_product_description);
        etProductImageUrl = findViewById(R.id.et_product_image_url);
        spinnerBrand      = findViewById(R.id.spinner_brand);
        spinnerCategory   = findViewById(R.id.spinner_category);
        cbIsAvailable     = findViewById(R.id.cb_is_available);
        ivProductPreview  = findViewById(R.id.iv_product_preview);
        tvTitle           = findViewById(R.id.tv_add_product_title);
        btnSave           = findViewById(R.id.btn_save_product);
    }

    private void loadSpinnerData() {
        brands     = db.productDao().getAllBrands();
        categories = db.categoryDao().getAllCategories();

        if (brands == null) {
            brands = new ArrayList<>();
        }
        if (categories == null) {
            categories = new ArrayList<>();
        }

        List<String> brandNames = new ArrayList<>();
        for (Brand b : brands) {
            if (b != null && b.name != null) {
                brandNames.add(b.name);
            }
        }

        List<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            if (c != null && c.name != null) {
                categoryNames.add(c.name);
            }
        }

        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_selected, brandNames);
        brandAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerBrand.setAdapter(brandAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_selected, categoryNames);
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(categoryAdapter);
        if (brands.isEmpty()) {
            Toast.makeText(this,
                    "Chưa có thương hiệu nào. Vui lòng thêm brand trước.",
                    Toast.LENGTH_LONG).show();
        }
        if (categories.isEmpty()) {
            Toast.makeText(this,
                    "Chưa có danh mục nào. Vui lòng thêm category trước.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadProductData() {
        editingProduct = db.productDao().getProductById(productId);
        if (editingProduct != null) {
            etName.setText(editingProduct.name);
            etPrice.setText(String.valueOf(editingProduct.price));
            etOriginalPrice.setText(String.valueOf(editingProduct.originalPrice));
            etDescription.setText(editingProduct.description);
            cbIsAvailable.setChecked(editingProduct.isAvailable);

            for (int i = 0; i < brands.size(); i++) {
                if (brands.get(i).id == editingProduct.brandId) {
                    spinnerBrand.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).id == editingProduct.shoeCategory) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            ProductImg thumbnail = db.productDao().getThumbnail(editingProduct.id);
            if (thumbnail != null) {
                etProductImageUrl.setText(thumbnail.imgUrl);
                ImageLoader.load(thumbnail.imgUrl, ivProductPreview, R.drawable.ic_shoe);
            }
        }
    }

    private void saveProduct() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String originalPriceStr = etOriginalPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageUrl = etProductImageUrl.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Tên sản phẩm không được trống");
            etName.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá bán");
            etPrice.requestFocus();
            return;
        }
        if (brands.isEmpty() || categories.isEmpty()) {
            Toast.makeText(this,
                    "Cần có ít nhất 1 thương hiệu và 1 danh mục",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> existing = db.productDao().getAllProducts();
        for (Product p : existing) {
            if (isEditMode && p.id == editingProduct.id) {
                continue;
            }
            if (name.equalsIgnoreCase(p.name)) {
                etName.setError("Tên sản phẩm đã tồn tại");
                etName.requestFocus();
                return;
            }
        }

        double price;
        double originalPrice;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Giá bán không hợp lệ");
            etPrice.requestFocus();
            return;
        }

        try {
            originalPrice = originalPriceStr.isEmpty() ? price : Double.parseDouble(originalPriceStr);
        } catch (NumberFormatException e) {
            etOriginalPrice.setError("Giá gốc không hợp lệ");
            etOriginalPrice.requestFocus();
            return;
        }

        String finalImageUrl = imageUrl;
        if (selectedImageUri != null && imageUrl.startsWith("content://")) {
            String localPath = saveImageToInternalStorage(selectedImageUri);
            if (localPath != null) {
                finalImageUrl = localPath;
            }
        }

        int selectedBrandId    = brands.get(spinnerBrand.getSelectedItemPosition()).id;
        int selectedCategoryId = categories.get(spinnerCategory.getSelectedItemPosition()).id;

        if (isEditMode) {
            editingProduct.name = name;
            editingProduct.description = description;
            editingProduct.price = price;
            editingProduct.originalPrice = originalPrice;
            editingProduct.brandId = selectedBrandId;
            editingProduct.shoeCategory = selectedCategoryId;
            editingProduct.isAvailable = cbIsAvailable.isChecked();

            db.productDao().update(editingProduct);

            ProductImg thumbnail = db.productDao().getThumbnail(editingProduct.id);
            if (!finalImageUrl.isEmpty()) {
                if (thumbnail != null) {
                    thumbnail.imgUrl = finalImageUrl;
                    db.productDao().updateProductImg(thumbnail);
                } else {
                    ProductImg img = new ProductImg();
                    img.productId = editingProduct.id;
                    img.colorId = null;
                    img.imgUrl = finalImageUrl;
                    img.sortOrder = 1;
                    img.isActive = true;
                    img.isThumbnail = true;
                    db.productDao().insertProductImg(img);
                }
            } else if (thumbnail != null) {
                db.productDao().deleteProductImg(thumbnail);
            }

            Toast.makeText(this, "Đã cập nhật sản phẩm \"" + name + "\"", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Product product         = new Product();
            product.name            = name;
            product.description     = description;
            product.price           = price;
            product.originalPrice   = originalPrice;
            product.brandId         = selectedBrandId;
            product.shoeCategory    = selectedCategoryId;
            product.isAvailable     = cbIsAvailable.isChecked();
            product.isDiscontinue   = false;
            product.addedAt         = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            product.productId       = "PRD" + System.currentTimeMillis();

            long insertedId = db.productDao().insertProduct(product);
            int insertedProductId = (int) insertedId;

            if (insertedProductId != -1 && !finalImageUrl.isEmpty()) {
                ProductImg img = new ProductImg();
                img.productId = insertedProductId;
                img.colorId = null;
                img.imgUrl = finalImageUrl;
                img.sortOrder = 1;
                img.isActive = true;
                img.isThumbnail = true;
                db.productDao().insertProductImg(img);
            }

            Toast.makeText(this, "Đã thêm sản phẩm \"" + name + "\"", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;

            String filename = "prod_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getFilesDir(), filename);

            try (InputStream in = is; FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
