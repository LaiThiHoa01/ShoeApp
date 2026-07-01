package com.example.shoeapp.user;

import android.content.Context;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductImg;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.entity.Size;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ClientProductRepository {


    private final ProductDao productDao;
    private final AppDatabase db;

    public ClientProductRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        productDao = db.productDao();
    }

    public void ensureSeedData() {
        // Dữ liệu mẫu đã được khởi tạo bên trong DatabaseSeeder (sự kiện onCreate của AppDatabase)
        // Nên hàm này được để trống để tránh trùng lặp dữ liệu.
    }

    public List<com.example.shoeapp.model.Product> getFeaturedProducts() {
        // Lấy 4 sản phẩm mới nhất (không phụ thuộc vào order_detail)
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        try {
            List<Product> products = productDao.getProductsActiveLimited(4);
            android.util.Log.d("FEATURED", "getProductsActiveLimited(4) returned: " + products.size());
            for (Product p : products) {
                result.add(toClientProduct(p));
            }
        } catch (Exception e) {
            android.util.Log.e("FEATURED", "Error: " + e.getMessage());
        }

        // Fallback nếu DB chưa có data
        if (result.isEmpty()) {
            android.util.Log.w("FEATURED", "No products found — DB may be empty");
        }

        android.util.Log.d("FEATURED", "Returning " + result.size() + " products");
        return result;
    }

    public List<com.example.shoeapp.model.Product> getAllProducts() {
        List<com.example.shoeapp.model.Product> products = new ArrayList<>();
        for (Product product : productDao.getAllProductsActive()) {
            products.add(toClientProduct(product));
        }
        return products;
    }

    public List<com.example.shoeapp.model.Product> getProductsByPromotion(int promotionId) {
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        com.example.shoeapp.data.entity.Promotion promo = productDao.getPromotionById(promotionId);
        
        if (promo == null) {
            return result;
        }

        if ("CATEGORY".equals(promo.targetType) && promo.categoryId != null) {
            List<Product> products = productDao.getProductsByCategoryActive(promo.categoryId);
            for (Product p : products) {
                result.add(toClientProduct(p));
            }
        } else if ("BRAND".equals(promo.targetType) && promo.brandId != null) {
            List<Product> products = productDao.getProductsByBrandActive(promo.brandId);
            for (Product p : products) {
                result.add(toClientProduct(p));
            }
        } else {
            // "PRODUCTS" targetType
            List<com.example.shoeapp.data.entity.PromotionProduct> pps = productDao.getProductsByPromotion(promotionId);
            for (com.example.shoeapp.data.entity.PromotionProduct pp : pps) {
                Product p = productDao.getProductById(pp.productId);
                if (p != null && p.isAvailable && !p.isDiscontinue) {
                    result.add(toClientProduct(p));
                }
            }
        }
        
        return result;
    }

    public Product getProductById(int id) {
        return productDao.getProductById(id);
    }

    public String getBrandName(int brandId) {
        String brand = productDao.getBrandName(brandId);
        return brand == null ? "" : brand;
    }

    public String getCategoryName(int categoryId) {
        String category = productDao.getCategoryName(categoryId);
        return category == null ? "" : category;
    }

    public int getStock(int productId) {
        return productDao.getProductStock(productId);
    }

    public String getThumbnailUrl(int productId) {
        String url = productDao.getThumbnailUrl(productId);
        return url == null ? "" : url;
    }

    public List<ProductColorOption> getAvailableColors(int productId) {
        return productDao.getAvailableColors(productId);
    }

    public List<ProductSizeOption> getAvailableSizes(int productId, int colorId) {
        return productDao.getAvailableSizes(productId, colorId);
    }

    public ProductVariant getVariant(int productId, int colorId, int sizeId) {
        return productDao.getVariant(productId, colorId, sizeId);
    }

    public String getImageUrl(int productId, int colorId) {
        String url = productDao.getImageUrl(productId, colorId);
        return url == null ? getThumbnailUrl(productId) : url;
    }

    public List<com.example.shoeapp.data.entity.Category> getCategories() {
        return productDao.getAllCategories();
    }

    public List<com.example.shoeapp.data.entity.Brand> getBrands() {
        return productDao.getAllBrands();
    }

    public List<com.example.shoeapp.data.entity.ProductReview> getReviewsByProduct(int productId) {
        return productDao.getReviewsByProduct(productId);
    }

    public com.example.shoeapp.data.entity.User getUserById(Context context, int userId) {
        return AppDatabase.getDatabase(context).userDao().getUserById(userId);
    }

    public List<com.example.shoeapp.model.Product> getProductsByCategory(int categoryId) {
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        for (com.example.shoeapp.data.entity.Product p : productDao.getProductsByCategoryActive(categoryId)) {
            result.add(toClientProduct(p));
        }
        return result;
    }

    public List<com.example.shoeapp.model.Product> searchProducts(String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        List<com.example.shoeapp.model.Product> all = getAllProducts();
        if (kw.isEmpty()) return all;
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        for (com.example.shoeapp.model.Product p : all) {
            if (p.getName().toLowerCase().contains(kw) || p.getBrand().toLowerCase().contains(kw)) {
                result.add(p);
            }
        }
        return result;
    }

    public com.example.shoeapp.model.Product toClientProduct(Product product) {
        String brand = getBrandName(product.brandId);
        String category = getCategoryName(product.shoeCategory);
        return new com.example.shoeapp.model.Product(
                product.id,
                product.name,
                brand + " - " + category,
                category,
                product.price,
                product.originalPrice,
                getStock(product.id),
                com.example.shoeapp.Helper.Helpers.isProductNew(product.addedAt),
                Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45),
                ratingFor(product.id),
                reviewCountFor(product.id),
                R.drawable.ic_shoe,
                getThumbnailUrl(product.id));
    }



    private float ratingFor(int id) {
        Float avg = productDao.getAverageRating(id);
        return avg != null && avg > 0 ? avg : 0f;
    }

    private int reviewCountFor(int id) {
        return productDao.getReviewCount(id);
    }

    public String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(price)) + " đ";
    }


}
