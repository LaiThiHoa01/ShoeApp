package com.example.shoeapp.data.repo;

import android.content.Context;

import com.example.shoeapp.R;
import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.PromotionProduct;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.data.entity.ProductReview;
import com.example.shoeapp.data.entity.User;
import com.example.shoeapp.admin.DateUtils;
import java.util.HashSet;
import java.util.Set;

public class ProductRepository {


    private final ProductDao productDao;
    private final AppDatabase db;

    public ProductRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        productDao = db.productDao();
    }

    public void ensureSeedData() {
        // Dữ liệu mẫu đã được khởi tạo bên trong DatabaseSeeder (sự kiện onCreate của AppDatabase)
        // Nên hàm này được để trống để tránh trùng lặp dữ liệu.
    }

    /**
     * L\u1ea5y s\u1ea3n ph\u1ea9m n\u1ed5i b\u1eadt theo c\u01a1 ch\u1ebf k\u1ebft h\u1ee3p:
     * - \u01acu ti\u00ean: 2 s\u1ea3n ph\u1ea9m b\u00e1n ch\u1ea1y nh\u1ea5t + 2 s\u1ea3n ph\u1ea9m rating cao nh\u1ea5t (tr\u00e1nh tr\u00f9ng l\u1eabp)
     * - Fallback: n\u1ebfu ch\u01b0a c\u00f3 \u0111\u1ee7 d\u1eef li\u1ec7u, b\u1ed5 sung b\u1eb1ng s\u1ea3n ph\u1ea9m m\u1edbi nh\u1ea5t
     */
    public List<com.example.shoeapp.model.Product> getFeaturedProducts() {
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();

        try {
            // B\u01b0\u1edbc 1: L\u1ea5y 2 s\u1ea3n ph\u1ea9m b\u00e1n ch\u1ea1y nh\u1ea5t
            List<Product> topSelling = productDao.getTopSellingProductsActive(2);
            for (Product p : topSelling) {
                if (addedIds.add(p.id)) {
                    result.add(toClientProduct(p));
                }
            }

            // B\u01b0\u1edbc 2: L\u1ea5y 4 s\u1ea3n ph\u1ea9m rating cao nh\u1ea5t, ch\u1ecdn nh\u1eefng c\u00e1i ch\u01b0a c\u00f3 trong list
            List<Product> topRated = productDao.getTopRatedProductsActive(4);
            for (Product p : topRated) {
                if (result.size() >= 4) break;
                if (addedIds.add(p.id)) {
                    result.add(toClientProduct(p));
                }
            }

            // B\u01b0\u1edbc 3: Fallback n\u1ebfu ch\u01b0a \u0111\u1ee7 4 s\u1ea3n ph\u1ea9m (ch\u01b0a c\u00f3 \u0111\u01a1n h\u00e0ng ho\u1eb7c review) \u2192 d\u00f9ng s\u1ea3n ph\u1ea9m m\u1edbi nh\u1ea5t
            if (result.size() < 4) {
                List<Product> newest = productDao.getProductsActiveLimited(8);
                for (Product p : newest) {
                    if (result.size() >= 4) break;
                    if (addedIds.add(p.id)) {
                        result.add(toClientProduct(p));
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("FEATURED", "Error loading featured products: " + e.getMessage());
        }

        android.util.Log.d("FEATURED", "Returning " + result.size() + " featured products");
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
        Promotion promo = productDao.getPromotionById(promotionId);
        
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
            List<PromotionProduct> pps = productDao.getProductsByPromotion(promotionId);
            for (PromotionProduct pp : pps) {
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

    public List<Category> getCategories() {
        return productDao.getAllCategories();
    }

    public List<Brand> getBrands() {
        return productDao.getAllBrands();
    }

    public List<ProductReview> getReviewsByProduct(int productId) {
        return productDao.getReviewsByProduct(productId);
    }

    public User getUserById(Context context, int userId) {
        return AppDatabase.getDatabase(context).userDao().getUserById(userId);
    }

    public List<com.example.shoeapp.model.Product> getProductsByCategory(int categoryId) {
        List<com.example.shoeapp.model.Product> result = new ArrayList<>();
        for (Product p : productDao.getProductsByCategoryActive(categoryId)) {
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
                DateUtils.isProductNew(product.addedAt),
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
