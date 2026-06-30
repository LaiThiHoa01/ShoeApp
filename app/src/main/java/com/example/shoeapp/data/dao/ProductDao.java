package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import com.example.shoeapp.data.entity.*;
import com.example.shoeapp.data.model.ProductColorOption;
import com.example.shoeapp.data.model.ProductSizeOption;
import java.util.List;

@Dao
public interface ProductDao {

    // ── Insert with Ignore (for seed data / base operations) ─────────────
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertBrand(Brand brand);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertColor(Color color);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertSize(Size size);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductVariant(ProductVariant variant);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertVariant(ProductVariant variant);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProductVariants(List<ProductVariant> variants);

    @androidx.room.Transaction
    default void updateProductVariantsTransaction(List<ProductVariant> variants) {
        insertProductVariants(variants);
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductImg(ProductImg img);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertProductImage(ProductImg productImg);

    // ── Product ──────────────────────────────────────
    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE is_available = 1 AND is_discontinue = 0")
    List<Product> getAllProductsActive();

    @Query("SELECT * FROM product WHERE is_available = 1 AND is_discontinue = 0 ORDER BY id DESC LIMIT :limit")
    List<Product> getProductsActiveLimited(int limit);

    @Query("SELECT p.* FROM product p INNER JOIN category c ON p.shoe_category = c.id WHERE p.is_available = 1 AND p.is_discontinue = 0 AND c.is_active = 1 ORDER BY p.id DESC LIMIT :limit")
    List<Product> getNewestProducts(int limit);

    @Query("SELECT * FROM product WHERE shoe_category = :categoryId")
    List<Product> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM product WHERE shoe_category = :categoryId AND is_available = 1 AND is_discontinue = 0 ORDER BY id DESC")
    List<Product> getProductsByCategoryActive(int categoryId);

    @Query("SELECT * FROM product WHERE brand_id = :brandId AND is_available = 1 AND is_discontinue = 0 ORDER BY id DESC")
    List<Product> getProductsByBrandActive(int brandId);

    @Query("SELECT * FROM product WHERE id = :id LIMIT 1")
    Product getProductById(int id);

    @Query("SELECT COUNT(*) FROM product")
    int countProducts();

    // ── Queries for Admin Dashboard ───────────────────

    /**
     * Lấy danh sách sản phẩm bán chạy nhất dựa trên số lượng trong đơn hàng
     */
    @Query("SELECT p.* FROM product p INNER JOIN order_detail od ON p.id = od.product_id GROUP BY p.id ORDER BY SUM(od.quantity) DESC LIMIT :limit")
    List<Product> getTopSellingProducts(int limit);

    @Query("SELECT p.* FROM product p INNER JOIN category c ON p.shoe_category = c.id INNER JOIN order_detail od ON p.id = od.product_id WHERE p.is_available = 1 AND p.is_discontinue = 0 AND c.is_active = 1 GROUP BY p.id ORDER BY SUM(od.quantity) DESC LIMIT :limit")
    List<Product> getTopSellingProductsActive(int limit);

    // ── Brand ─────────────────────────────────────────
    @Update
    void updateBrand(Brand brand);

    @Delete
    void deleteBrand(Brand brand);

    @Query("SELECT * FROM brand")
    List<Brand> getAllBrands();

    @Query("SELECT * FROM brand WHERE id = :id LIMIT 1")
    Brand getBrandById(int id);

    @Query("SELECT name FROM brand WHERE id = :brandId LIMIT 1")
    String getBrandName(int brandId);

    @Query("SELECT * FROM brand WHERE is_active = 1 ORDER BY id ASC")
    List<Brand> getAllBrandsActive();

    // ── Color ─────────────────────────────────────────
    @Update
    void updateColor(Color color);

    @Delete
    void deleteColor(Color color);

    @Query("SELECT * FROM color")
    List<Color> getAllColors();

    @Query("SELECT * FROM color WHERE id = :id LIMIT 1")
    Color getColorById(int id);

    // ── Size ──────────────────────────────────────────
    @Update
    void updateSize(Size size);

    @Delete
    void deleteSize(Size size);

    @Query("SELECT * FROM size ORDER BY sort_order ASC")
    List<Size> getAllSizes();

    @Query("SELECT * FROM size WHERE id = :id LIMIT 1")
    Size getSizeById(int id);

    // ── ProductImg ────────────────────────────────────
    @Update
    void updateProductImg(ProductImg img);

    @Delete
    void deleteProductImg(ProductImg img);

    @Query("SELECT * FROM product_img WHERE product_id = :productId")
    List<ProductImg> getImagesByProduct(int productId);

    @Query("SELECT * FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 LIMIT 1")
    ProductImg getThumbnail(int productId);

    @Query("SELECT img_url FROM product_img WHERE product_id = :productId AND is_thumbnail = 1 AND is_active = 1 ORDER BY sort_order ASC LIMIT 1")
    String getThumbnailUrl(int productId);

    @Query("SELECT img_url FROM product_img WHERE product_id = :productId AND color_id = :colorId AND is_active = 1 ORDER BY is_thumbnail DESC, sort_order ASC LIMIT 1")
    String getImageUrl(int productId, int colorId);

    // ── Category ──────────────────────────────────────
    @Query("SELECT name FROM category WHERE id = :categoryId LIMIT 1")
    String getCategoryName(int categoryId);

    @Query("SELECT * FROM category WHERE is_active = 1 ORDER BY sort_order ASC")
    List<Category> getAllCategories();

    // ── ProductVariant ────────────────────────────────
    @Update
    void updateProductVariant(ProductVariant variant);

    @Delete
    void deleteProductVariant(ProductVariant variant);

    @Query("SELECT * FROM product_variant WHERE product_id = :productId")
    List<ProductVariant> getVariantsByProduct(int productId);

    @Query("SELECT COALESCE(SUM(stock), 0) FROM product_variant WHERE product_id = :productId AND is_discontinue_variant = 0")
    int getProductStock(int productId);

    @Query("SELECT DISTINCT c.id AS id, c.name AS name, c.hexcode AS hexcode FROM color c INNER JOIN product_variant pv ON pv.color_id = c.id WHERE pv.product_id = :productId AND pv.stock > 0 AND pv.is_discontinue_variant = 0 ORDER BY c.id ASC")
    List<ProductColorOption> getAvailableColors(int productId);

    @Query("SELECT s.id AS id, s.name AS name, COALESCE(pv.stock, 0) AS stock FROM size s INNER JOIN product_variant pv ON pv.size_id = s.id WHERE pv.product_id = :productId AND pv.color_id = :colorId AND pv.is_discontinue_variant = 0 ORDER BY s.sort_order ASC")
    List<ProductSizeOption> getAvailableSizes(int productId, int colorId);

    @Query("SELECT * FROM product_variant WHERE product_id = :productId AND color_id = :colorId AND size_id = :sizeId AND is_discontinue_variant = 0 LIMIT 1")
    ProductVariant getVariant(int productId, int colorId, int sizeId);

    // ── Promotion ─────────────────────────────────────
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    void insertPromotion(Promotion promotion);
    
    @Insert
    long insertPromotionReturnId(Promotion promotion);

    @Update
    void updatePromotion(Promotion promotion);

    @Delete
    void deletePromotion(Promotion promotion);

    @Query("SELECT * FROM promotion WHERE id = :id")
    Promotion getPromotionById(int id);

    @Query("SELECT * FROM promotion ORDER BY id DESC")
    List<Promotion> getAllPromotions();

    // Lấy 3 promotion mới nhất, sắp xếp thứ tự theo còn nhiều voucher hơn. Dù cho promotion có ở trạng thái inactive vẫn phải hiển thị
    @Query("SELECT * FROM (SELECT * FROM promotion ORDER BY id DESC LIMIT 3) ORDER BY quantity DESC")
    List<Promotion> getBannerPromotions();

    // Lấy danh sách đang active thật sự
    @Query("SELECT * FROM promotion WHERE is_active = 1 AND quantity > 0 AND date(start_date) <= date('now', 'localtime') AND date(end_date) >= date('now', 'localtime')")
    List<Promotion> getActivePromotions();

    @Query("SELECT * FROM promotion WHERE UPPER(voucher_code) = UPPER(:code) AND is_active = 1 AND quantity > 0 AND date(start_date) <= date('now', 'localtime') AND date(end_date) >= date('now', 'localtime') LIMIT 1")
    Promotion getActivePromotionByVoucher(String code);

    // ── PromotionProduct ──────────────────────────────
    @Insert
    void insertPromotionProduct(PromotionProduct pp);

    @Delete
    void deletePromotionProduct(PromotionProduct pp);
    
    @Query("DELETE FROM promotion_product WHERE promotion_id = :promoId")
    void deleteProductsByPromotion(int promoId);
    
    @androidx.room.Transaction
    default void updatePromotionWithProducts(Promotion promotion, java.util.List<PromotionProduct> products) {
        updatePromotion(promotion);
        deleteProductsByPromotion(promotion.id);
        for (PromotionProduct pp : products) {
            insertPromotionProduct(pp);
        }
    }

    @Query("SELECT * FROM promotion_product WHERE promotion_id = :promotionId")
    List<PromotionProduct> getProductsByPromotion(int promotionId);
    
    @Query("DELETE FROM promotion_product WHERE promotion_id = :promotionId")
    void deletePromotionProductsByPromotion(int promotionId);

    // ── ProductReview ─────────────────────────────────
    @Insert
    void insertReview(ProductReview review);

    @Update
    void updateReview(ProductReview review);

    @Delete
    void deleteReview(ProductReview review);

    @Query("SELECT * FROM product_review WHERE product_id = :productId ORDER BY created_at DESC")
    List<ProductReview> getReviewsByProduct(int productId);

    @Query("SELECT AVG(rating) FROM product_review WHERE product_id = :productId")
    Float getAverageRating(int productId);

    @Query("SELECT COUNT(*) FROM product_review WHERE product_id = :productId")
    int getReviewCount(int productId);

    @Query("SELECT COUNT(*) FROM brand")
    int countBrands();

    @Query("SELECT COUNT(*) FROM category")
    int countCategories();

    @Query("SELECT COUNT(*) FROM color")
    int countColors();

    @Query("SELECT COUNT(*) FROM size")
    int countSizes();

    @Query("SELECT COUNT(*) FROM product_variant")
    int countVariants();

    @Query("DELETE FROM order_detail")
    void deleteAllOrderDetails();

    @Query("DELETE FROM orders")
    void deleteAllOrders();

    @Query("DELETE FROM product_variant")
    void deleteAllVariants();

    @Query("DELETE FROM product_img")
    void deleteAllImages();

    @Query("DELETE FROM product_review")
    void deleteAllReviews();

    @Query("DELETE FROM product")
    void deleteAllProducts();

    @Query("DELETE FROM brand")
    void deleteAllBrands();

    @Query("DELETE FROM category")
    void deleteAllCategories();

    @Query("DELETE FROM color")
    void deleteAllColors();

    @Query("DELETE FROM size")
    void deleteAllSizes();

    @Query("DELETE FROM promotion")
    void deleteAllPromotions();

    @Query("DELETE FROM promotion_product")
    void deleteAllPromotionProducts();
}
