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
    private static final int SEED_PRODUCT_COUNT = 24;
    private final ProductDao productDao;
    private final AppDatabase db;

    // ID arrays populated during seeding — indexed from 1
    private final int[] brandIds    = new int[10];
    private final int[] categoryIds = new int[7];
    private final int[] colorIds    = new int[10];
    private final int[] sizeIds     = new int[11];
    private final int[] productIds  = new int[25];

    public ClientProductRepository(Context context) {
        db = AppDatabase.getDatabase(context);
        productDao = db.productDao();
    }

    public void ensureSeedData() {
        if (productDao.countVariants() < 100) {
            productDao.deleteAllOrderDetails();
            productDao.deleteAllOrders();
            productDao.deleteAllVariants();
            productDao.deleteAllImages();
            productDao.deleteAllReviews();
            productDao.deleteAllProducts();
            productDao.deleteAllBrands();
            productDao.deleteAllCategories();
            productDao.deleteAllColors();
            productDao.deleteAllSizes();
            productDao.deleteAllPromotionProducts();
            productDao.deleteAllPromotions();

            seedBrands();
            seedCategories();
            seedColors();
            seedSizes();
            seedProducts();
            seedVariants();
            try {
                seedMockOrders();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Luôn kiểm tra riêng: Nếu bảng promotion rỗng thì seed
        java.util.List<com.example.shoeapp.data.entity.Promotion> activePromotions = productDao.getActivePromotions();
        if (activePromotions.isEmpty()) {
            seedPromotions();
        }

        new Thread(() -> {
            try {
                java.util.List<com.example.shoeapp.data.entity.Category> cats = productDao.getAllCategories();
                if (cats != null) {
                    for (com.example.shoeapp.data.entity.Category c : cats) {
                        if (c.iconUrl != null && c.iconUrl.trim().endsWith(".avif")) {
                            c.iconUrl = "https://res.cloudinary.com/dnmowplwi/image/upload/v1768911723/AIR_JORDAN_1_LOW_nocz0l.jpg";
                            db.categoryDao().update(c);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void seedPromotions() {
        com.example.shoeapp.data.entity.Promotion p1 = new com.example.shoeapp.data.entity.Promotion();
        p1.name = "SALE ĐÓN HÈ MÁT MẺ";
        p1.slug = "sale-don-he-mat-me";
        p1.discountType = "PERCENTAGE";
        p1.discountValue = 20.0;
        p1.startDate = "2026-06-01";
        p1.endDate = "2026-07-30";
        p1.isActive = true;
        p1.subtitle = "Giảm giá lên đến 20% cho toàn bộ sưu tập Hè";
        p1.bannerUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=800"; 
        p1.voucherCode = "SUMMER20";
        p1.description = "Nhập mã SUMMER20 để được giảm 20% cho tất cả các sản phẩm giày chạy bộ. Nhanh tay kẻo lỡ!";
        p1.quantity = 100;
        p1.targetType = "CATEGORY";
        p1.categoryId = categoryIds[1]; // Giày chạy bộ

        com.example.shoeapp.data.entity.Promotion p2 = new com.example.shoeapp.data.entity.Promotion();
        p2.name = "BACK TO SCHOOL";
        p2.slug = "back-to-school";
        p2.discountType = "FIXED_AMOUNT";
        p2.discountValue = 100000.0;
        p2.startDate = "2026-08-01";
        p2.endDate = "2026-09-15";
        p2.isActive = true;
        p2.subtitle = "Giảm ngay 100K cho học sinh, sinh viên";
        p2.bannerUrl = "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&q=80&w=800"; 
        p2.voucherCode = "B2SCHOOL100K";
        p2.description = "Mã B2SCHOOL100K giảm trực tiếp 100.000đ cho các dòng giày đặc biệt dành riêng cho mùa tựu trường.";
        p2.quantity = 50;
        p2.targetType = "PRODUCTS";

        com.example.shoeapp.data.entity.Promotion p3 = new com.example.shoeapp.data.entity.Promotion();
        p3.name = "FLASH SALE CUỐI TUẦN";
        p3.slug = "flash-sale-cuoi-tuan";
        p3.discountType = "PERCENTAGE";
        p3.discountValue = 50.0;
        p3.startDate = "2026-06-25";
        p3.endDate = "2026-06-30";
        p3.isActive = true;
        p3.subtitle = "Ưu đãi chớp nhoáng giảm tới 50%";
        p3.bannerUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&q=80&w=800"; 
        p3.voucherCode = "FLASH50";
        p3.description = "Sale sốc cuối tuần lên đến 50%. Chỉ áp dụng cho một số sản phẩm nhất định trong thời gian ngắn.";
        p3.quantity = 10;
        p3.targetType = "PRODUCTS"; 

        productDao.insertPromotion(p1);
        productDao.insertPromotion(p2);
        productDao.insertPromotion(p3);

        java.util.List<com.example.shoeapp.data.entity.Promotion> promos = productDao.getActivePromotions();
        
        if (promos.size() >= 3) {
            java.util.List<com.example.shoeapp.data.entity.Product> activeProducts = productDao.getAllProductsActive();
            if (activeProducts.size() >= 3) {
                com.example.shoeapp.data.entity.PromotionProduct pp1 = new com.example.shoeapp.data.entity.PromotionProduct();
                pp1.promotionId = promos.get(0).id;
                pp1.productId = activeProducts.get(0).id;
                productDao.insertPromotionProduct(pp1);

                com.example.shoeapp.data.entity.PromotionProduct pp2 = new com.example.shoeapp.data.entity.PromotionProduct();
                pp2.promotionId = promos.get(1).id;
                pp2.productId = activeProducts.get(1).id;
                productDao.insertPromotionProduct(pp2);

                com.example.shoeapp.data.entity.PromotionProduct pp3 = new com.example.shoeapp.data.entity.PromotionProduct();
                pp3.promotionId = promos.get(2).id;
                pp3.productId = activeProducts.get(2).id;
                productDao.insertPromotionProduct(pp3);
            }
        }
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
                product.id <= 6,
                Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44, 45),
                ratingFor(product.id),
                reviewCountFor(product.id),
                R.drawable.ic_shoe,
                getThumbnailUrl(product.id));
    }

    private void seedBrands() {
        brandIds[1] = (int) insertBrand("Nike", "NIKE");
        brandIds[2] = (int) insertBrand("adidas", "ADS");
        brandIds[3] = (int) insertBrand("New Balance", "NB");
        brandIds[4] = (int) insertBrand("ASICS", "ASC");
        brandIds[5] = (int) insertBrand("Puma", "PUMA");
        brandIds[6] = (int) insertBrand("Converse", "CNV");
        brandIds[7] = (int) insertBrand("Vans", "VNS");
        brandIds[8] = (int) insertBrand("Biti's", "BTS");
        brandIds[9] = (int) insertBrand("Ananas", "ANS");
    }

    private void seedCategories() {
        categoryIds[1] = (int) insertCategory("Giày chạy bộ", 1);
        categoryIds[2] = (int) insertCategory("Sneaker hằng ngày", 2);
        categoryIds[3] = (int) insertCategory("Giày bóng rổ", 3);
        categoryIds[4] = (int) insertCategory("Giày tập luyện", 4);
        categoryIds[5] = (int) insertCategory("Giày thời trang", 5);
    }

    private void seedColors() {
        colorIds[1] = (int) insertColor("Trắng", "#FFFFFF", "CLR-TRANG");
        colorIds[2] = (int) insertColor("Đen", "#111111", "CLR-DEN");
        colorIds[3] = (int) insertColor("Xám", "#8A8A8A", "CLR-XAM");
        colorIds[4] = (int) insertColor("Xanh navy", "#1D3557", "CLR-NAVY");
        colorIds[5] = (int) insertColor("Kem", "#EEE2C6", "CLR-KEM");
        colorIds[6] = (int) insertColor("Đỏ", "#C1121F", "CLR-DO");
        colorIds[7] = (int) insertColor("Xanh lá", "#2D6A4F", "CLR-XANH-LA");
        colorIds[8] = (int) insertColor("Nâu gum", "#9C6644", "CLR-NAU-GUM");
    }

    private void seedSizes() {
        for (int size = 36; size <= 45; size++) {
            sizeIds[size - 35] = (int) insertSize(String.valueOf(size), size, "SIZE-" + size);
        }
    }

    private void seedProducts() {
        productIds[1]  = insertProductSeed("Nike Air Force 1 '07", "Mẫu sneaker da cổ thấp kinh điển, dễ phối đồ và phù hợp đi hằng ngày.", 2929000, 3239000, brandIds[1], categoryIds[2], "PRD-NIKE-AF1", image(0));
        productIds[2]  = insertProductSeed("Nike Pegasus 41", "Giày chạy bộ đệm ReactX, phù hợp chạy hằng ngày và đi bộ đường dài.", 3829000, 4109000, brandIds[1], categoryIds[1], "PRD-NIKE-PEGASUS-41", image(1));
        productIds[3]  = insertProductSeed("Nike Dunk Low Retro", "Thiết kế bóng rổ cổ điển chuyển sang phong cách streetwear năng động.", 2929000, 3519000, brandIds[1], categoryIds[5], "PRD-NIKE-DUNK-LOW", image(2));
        productIds[4]  = insertProductSeed("Nike Court Vision Low", "Dáng tennis/basketball retro, upper da tổng hợp dễ chăm sóc.", 2069000, 2459000, brandIds[1], categoryIds[2], "PRD-NIKE-COURT-VISION", image(3));
        productIds[5]  = insertProductSeed("Nike LeBron Witness 8", "Giày bóng rổ hỗ trợ cổ chân, đệm chắc và bám sân tốt.", 3239000, 3829000, brandIds[1], categoryIds[3], "PRD-NIKE-LEBRON-WITNESS", image(4));
        productIds[6]  = insertProductSeed("adidas Samba OG", "Mẫu sneaker biểu tượng với đế gum, phù hợp phong cách casual.", 2700000, 3000000, brandIds[2], categoryIds[5], "PRD-ADIDAS-SAMBA", image(5));
        productIds[7]  = insertProductSeed("adidas Ultraboost 5X", "Giày chạy bộ đệm Boost nhẹ, êm và ổn định cho tập luyện.", 5200000, 5800000, brandIds[2], categoryIds[1], "PRD-ADIDAS-ULTRABOOST", image(6));
        productIds[8]  = insertProductSeed("adidas Stan Smith", "Sneaker tối giản, form gọn, hợp đi học, đi làm và cuối tuần.", 2500000, 2900000, brandIds[2], categoryIds[2], "PRD-ADIDAS-STAN-SMITH", image(7));
        productIds[9]  = insertProductSeed("adidas Gazelle Indoor", "Dáng retro bằng da lộn, nổi bật với đế gum và phối màu cổ điển.", 3000000, 3400000, brandIds[2], categoryIds[5], "PRD-ADIDAS-GAZELLE", image(8));
        productIds[10] = insertProductSeed("adidas Campus 00s", "Form chunky skate-inspired, hợp phối đồ streetwear.", 2800000, 3200000, brandIds[2], categoryIds[5], "PRD-ADIDAS-CAMPUS", image(9));
        productIds[11] = insertProductSeed("New Balance 327", "Sneaker lifestyle đế răng cưa, form nhẹ và phong cách retro running.", 2590000, 2990000, brandIds[3], categoryIds[2], "PRD-NB-327", image(10));
        productIds[12] = insertProductSeed("New Balance 530", "Dáng running Y2K, lưới thoáng và đệm ABZORB thoải mái.", 2890000, 3290000, brandIds[3], categoryIds[2], "PRD-NB-530", image(11));
        productIds[13] = insertProductSeed("New Balance 574 Core", "Mẫu classic dễ mang, đệm EVA nhẹ và bền cho ngày dài.", 2390000, 2790000, brandIds[3], categoryIds[2], "PRD-NB-574", image(12));
        productIds[14] = insertProductSeed("ASICS GEL-Kayano 31", "Giày chạy ổn định cao, hỗ trợ tốt cho runner cần kiểm soát bước chân.", 4690000, 5290000, brandIds[4], categoryIds[1], "PRD-ASICS-KAYANO-31", image(13));
        productIds[15] = insertProductSeed("ASICS GEL-Nimbus 26", "Đệm êm, phù hợp chạy dài và người thích cảm giác mềm dưới chân.", 4390000, 4990000, brandIds[4], categoryIds[1], "PRD-ASICS-NIMBUS-26", image(14));
        productIds[16] = insertProductSeed("ASICS Japan S", "Sneaker thấp cổ lấy cảm hứng từ bóng rổ cổ điển, gọn và dễ phối.", 1690000, 2090000, brandIds[4], categoryIds[2], "PRD-ASICS-JAPAN-S", image(15));
        productIds[17] = insertProductSeed("Puma Suede Classic XXI", "Dòng suede kinh điển, thân giày mềm và kiểu dáng thời trang.", 1990000, 2390000, brandIds[5], categoryIds[5], "PRD-PUMA-SUEDE", image(16));
        productIds[18] = insertProductSeed("Puma RS-X Efekt", "Sneaker chunky nổi bật, đệm tốt cho di chuyển hằng ngày.", 2990000, 3490000, brandIds[5], categoryIds[2], "PRD-PUMA-RSX", image(17));
        productIds[19] = insertProductSeed("Puma TRC Blaze", "Phong cách retro running với phối màu mạnh và phần đế nổi bật.", 2590000, 3190000, brandIds[5], categoryIds[5], "PRD-PUMA-TRC-BLAZE", image(18));
        productIds[20] = insertProductSeed("Converse Chuck 70 High", "Canvas cổ cao kinh điển, đế dày hơn và cảm giác mang chắc chắn.", 1900000, 2200000, brandIds[6], categoryIds[5], "PRD-CONVERSE-CHUCK-70", image(19));
        productIds[21] = insertProductSeed("Vans Old Skool", "Giày skate cổ điển với sọc side stripe đặc trưng và đế waffle.", 1800000, 2100000, brandIds[7], categoryIds[5], "PRD-VANS-OLD-SKOOL", image(20));
        productIds[22] = insertProductSeed("Biti's Hunter X", "Giày Việt nhẹ, đệm êm, phù hợp đi học, đi chơi và di chuyển nhiều.", 1199000, 1499000, brandIds[8], categoryIds[2], "PRD-BITIS-HUNTER-X", image(21));
        productIds[23] = insertProductSeed("Ananas Basas Bumper Gum", "Sneaker canvas đế gum, kiểu dáng tối giản và giá dễ tiếp cận.", 650000, 750000, brandIds[9], categoryIds[5], "PRD-ANANAS-BASAS", image(22));
        productIds[24] = insertProductSeed("Ananas Vintas Saigon 1980s", "Thiết kế retro Việt Nam, hợp phối đồ casual và streetwear.", 720000, 850000, brandIds[9], categoryIds[5], "PRD-ANANAS-VINTAS", image(23));
    }

    private void seedVariants() {
        int[][] colorGroups = {
                {colorIds[1], colorIds[2], colorIds[5]},
                {colorIds[2], colorIds[3], colorIds[4]},
                {colorIds[1], colorIds[2], colorIds[6], colorIds[8]},
                {colorIds[1], colorIds[2], colorIds[3]},
                {colorIds[2], colorIds[6], colorIds[4]},
                {colorIds[1], colorIds[2], colorIds[8]},
                {colorIds[2], colorIds[3], colorIds[7]},
                {colorIds[1], colorIds[2], colorIds[5]},
                {colorIds[3], colorIds[4], colorIds[8]},
                {colorIds[2], colorIds[5], colorIds[7]},
                {colorIds[3], colorIds[5], colorIds[7]},
                {colorIds[1], colorIds[3], colorIds[4]},
                {colorIds[2], colorIds[3], colorIds[5]},
                {colorIds[2], colorIds[4], colorIds[7]},
                {colorIds[1], colorIds[3], colorIds[5]},
                {colorIds[1], colorIds[2], colorIds[6]},
                {colorIds[2], colorIds[5], colorIds[8]},
                {colorIds[3], colorIds[4], colorIds[6]},
                {colorIds[2], colorIds[6], colorIds[8]},
                {colorIds[1], colorIds[2], colorIds[5], colorIds[6]},
                {colorIds[1], colorIds[2], colorIds[8]},
                {colorIds[2], colorIds[4], colorIds[7]},
                {colorIds[1], colorIds[2], colorIds[8]},
                {colorIds[3], colorIds[5], colorIds[8]}
        };
        for (int i = 1; i <= SEED_PRODUCT_COUNT; i++) {
            int pid = productIds[i];
            if (pid == 0) continue;
            int[] cols = colorGroups[(i - 1) % colorGroups.length];
            for (int colorId : cols) {
                if (colorId == 0) continue;
                for (int s = 1; s <= 10; s++) {
                    int sid = sizeIds[s];
                    if (sid == 0) continue;
                    int stock = 2 + (i * colorId + s) % 10;
                    insertVariant(pid, sid, colorId, stock);
                }
            }
        }
    }

    private long insertBrand(String name, String prefix) {
        Brand brand = new Brand();
        brand.name = name;
        brand.prefix = prefix;
        brand.logoUrl = "";
        brand.isActive = true;
        return productDao.insertBrand(brand);
    }

    private long insertCategory(String name, int sortOrder) {
        Category category = new Category();
        category.name = name;
        category.iconUrl = "";
        category.isActive = true;
        category.sortOrder = sortOrder;
        category.createdAt = "2026-06-23";
        return productDao.insertCategory(category);
    }

    private long insertColor(String name, String hexcode, String colorIdStr) {
        Color color = new Color();
        color.name = name;
        color.hexcode = hexcode;
        color.colorId = colorIdStr;
        return productDao.insertColor(color);
    }

    private long insertSize(String name, int sortOrder, String sizeIdStr) {
        Size entity = new Size();
        entity.name = name;
        entity.sortOrder = sortOrder;
        entity.sizeId = sizeIdStr;
        return productDao.insertSize(entity);
    }

    private int insertProductSeed(String name, String description, double price,
                                  double originalPrice, int brandId, int categoryId,
                                  String productIdStr, String imageUrl) {
        Product product = new Product();
        product.name = name;
        product.description = description;
        product.price = price;
        product.originalPrice = originalPrice;
        product.brandId = brandId;
        product.shoeCategory = categoryId;
        product.addedAt = "2026-06-23";
        product.isDiscontinue = false;
        product.isAvailable = true;
        product.productId = productIdStr;
        int pid = (int) productDao.insertProduct(product);
        // Insert thumbnail image
        if (pid > 0) {
            ProductImg img = new ProductImg();
            img.productId = pid;
            img.colorId = colorIds[1]; // màu đầu tiên làm thumbnail
            img.imgUrl = imageUrl;
            img.sortOrder = 1;
            img.isActive = true;
            img.isThumbnail = true;
            productDao.insertProductImage(img);
        }
        return pid;
    }

    private void insertVariant(int productId, int sizeId, int colorId, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.productId = productId;
        variant.sizeId = sizeId;
        variant.colorId = colorId;
        variant.stock = stock;
        variant.isDiscontinueVariant = false;
        productDao.insertVariant(variant);
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

    private String image(int index) {
        String[] images = {
                "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1608231387042-66d1773070a5?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1579338559194-a162d19bf842?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1607522370275-f14206abe5d3?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1556906781-9a412961c28c?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1552346154-21d32810aba3?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1587563871167-1ee9c731aefb?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1560769629-975ec94e6a86?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1543508282-6319a3e2621f?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1539185441755-769473a23570?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1460353581641-37baddab0fa2?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1515955656352-a1fa3ffcd111?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1533867617858-e7b97e060509?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1514989940723-e8e51635b782?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1521093470119-a3acdc43374a?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1542219550-76864b1bc385?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1520256862855-398228c41684?auto=format&fit=crop&w=900&q=80",
                "https://images.unsplash.com/photo-1575537302964-96cd47c06b1b?auto=format&fit=crop&w=900&q=80"
        };
        return images[index % images.length];
    }

    private void seedMockOrders() {
        int customerId = 1; // Default to Admin user if no customer exists to avoid FK crash
        List<User> users = db.userDao().getAllUsers();
        if (!users.isEmpty()) {
            customerId = users.get(0).id;
            for (User u : users) {
                if ("CUSTOMER".equals(u.role)) {
                    customerId = u.id;
                    break;
                }
            }
        }

        Order o1 = new Order();
        o1.userId = customerId;
        o1.createdAt = "2026-06-22 12:00:00";
        o1.shippingFee = 5.0;
        o1.subTotal = 2929000.0;
        o1.grandTotal = 2934000.0;
        o1.shippingAddress = "456 Đường User, Hà Nội";
        o1.phoneNumber = "0987654321";
        o1.orderStatus = "PROCESSING";
        o1.paymentMethod = "COD";
        o1.paymentStatus = "UNPAID";
        o1.orderNote = "Giao giờ hành chính";
        o1.shippingStatus = "PENDING";
        o1.ordersId = "ORD-20260622-001";
        db.orderDao().insert(o1);

        Order o2 = new Order();
        o2.userId = customerId;
        o2.createdAt = "2026-06-22 14:00:00";
        o2.shippingFee = 5.0;
        o2.subTotal = 6758000.0;
        o2.grandTotal = 6763000.0;
        o2.shippingAddress = "456 Đường User, Hà Nội";
        o2.phoneNumber = "0987654321";
        o2.orderStatus = "SHIPPED";
        o2.paymentMethod = "ZALOPAY";
        o2.paymentStatus = "PAID";
        o2.orderNote = "";
        o2.shippingStatus = "SHIPPING";
        o2.ordersId = "ORD-20260622-002";
        db.orderDao().insert(o2);

        Order o3 = new Order();
        o3.userId = customerId;
        o3.createdAt = "2026-06-23 09:30:00";
        o3.shippingFee = 0.0;
        o3.subTotal = 3829000.0;
        o3.grandTotal = 3829000.0;
        o3.shippingAddress = "789 Đường Lê Lợi, TP.HCM";
        o3.phoneNumber = "0909090909";
        o3.orderStatus = "DELIVERED";
        o3.paymentMethod = "ZALOPAY";
        o3.paymentStatus = "PAID";
        o3.orderNote = "Gọi trước khi giao";
        o3.shippingStatus = "DELIVERED";
        o3.ordersId = "ORD-20260623-003";
        db.orderDao().insert(o3);

        Order o4 = new Order();
        o4.userId = customerId;
        o4.createdAt = "2026-06-24 15:45:00";
        o4.shippingFee = 5.0;
        o4.subTotal = 2929000.0;
        o4.grandTotal = 2934000.0;
        o4.shippingAddress = "123 Nguyễn Huệ, TP.HCM";
        o4.phoneNumber = "0911111111";
        o4.orderStatus = "CANCELLED";
        o4.paymentMethod = "COD";
        o4.paymentStatus = "UNPAID";
        o4.orderNote = "Hủy do đổi ý";
        o4.shippingStatus = "CANCELLED";
        o4.ordersId = "ORD-20260624-004";
        db.orderDao().insert(o4);

        Order o5 = new Order();
        o5.userId = customerId;
        o5.createdAt = "2026-06-25 10:00:00";
        o5.shippingFee = 5.0;
        o5.subTotal = 6758000.0;
        o5.grandTotal = 6763000.0;
        o5.shippingAddress = "321 Trần Hưng Đạo, Đà Nẵng";
        o5.phoneNumber = "0922222222";
        o5.orderStatus = "PROCESSING";
        o5.paymentMethod = "COD";
        o5.paymentStatus = "UNPAID";
        o5.orderNote = "";
        o5.shippingStatus = "PENDING";
        o5.ordersId = "ORD-20260625-005";
        db.orderDao().insert(o5);

        int o1Id = 1, o2Id = 2, o3Id = 3, o4Id = 4, o5Id = 5;
        List<Order> orders = db.orderDao().getAllOrders();
        for (Order o : orders) {
            if ("ORD-20260622-001".equals(o.ordersId)) o1Id = o.id;
            else if ("ORD-20260622-002".equals(o.ordersId)) o2Id = o.id;
            else if ("ORD-20260623-003".equals(o.ordersId)) o3Id = o.id;
            else if ("ORD-20260624-004".equals(o.ordersId)) o4Id = o.id;
            else if ("ORD-20260625-005".equals(o.ordersId)) o5Id = o.id;
        }

        OrderDetail od1 = new OrderDetail();
        od1.orderId = o1Id;
        od1.productId = 1;
        od1.colorId = 2; // Black
        od1.sizeId = 5; // Size 40
        od1.quantity = 1;
        od1.unitPrice = 2929000.0;
        od1.subtotal = 2929000.0;
        od1.orderDetailId = "ORDDET-001";
        db.orderDao().insertDetail(od1);

        OrderDetail od2 = new OrderDetail();
        od2.orderId = o2Id;
        od2.productId = 2;
        od2.colorId = 2; // Black
        od2.sizeId = 6; // Size 41
        od2.quantity = 1;
        od2.unitPrice = 3829000.0;
        od2.subtotal = 3829000.0;
        od2.orderDetailId = "ORDDET-002";
        db.orderDao().insertDetail(od2);

        OrderDetail od3 = new OrderDetail();
        od3.orderId = o2Id;
        od3.productId = 3;
        od3.colorId = 6; // Red
        od3.sizeId = 7; // Size 42
        od3.quantity = 1;
        od3.unitPrice = 2929000.0;
        od3.subtotal = 2929000.0;
        od3.orderDetailId = "ORDDET-003";
        db.orderDao().insertDetail(od3);

        OrderDetail od4 = new OrderDetail();
        od4.orderId = o3Id;
        od4.productId = 2;
        od4.colorId = 2; // Black
        od4.sizeId = 6; // Size 41
        od4.quantity = 1;
        od4.unitPrice = 3829000.0;
        od4.subtotal = 3829000.0;
        od4.orderDetailId = "ORDDET-004";
        db.orderDao().insertDetail(od4);

        OrderDetail od5 = new OrderDetail();
        od5.orderId = o4Id;
        od5.productId = 1;
        od5.colorId = 2; // Black
        od5.sizeId = 5; // Size 40
        od5.quantity = 1;
        od5.unitPrice = 2929000.0;
        od5.subtotal = 2929000.0;
        od5.orderDetailId = "ORDDET-005";
        db.orderDao().insertDetail(od5);

        OrderDetail od6 = new OrderDetail();
        od6.orderId = o5Id;
        od6.productId = 1;
        od6.colorId = 1; // White
        od6.sizeId = 6; // Size 41
        od6.quantity = 1;
        od6.unitPrice = 2929000.0;
        od6.subtotal = 2929000.0;
        od6.orderDetailId = "ORDDET-006";
        db.orderDao().insertDetail(od6);

        OrderDetail od7 = new OrderDetail();
        od7.orderId = o5Id;
        od7.productId = 2;
        od7.colorId = 2; // Black
        od7.sizeId = 6; // Size 41
        od7.quantity = 1;
        od7.unitPrice = 3829000.0;
        od7.subtotal = 3829000.0;
        od7.orderDetailId = "ORDDET-007";
        db.orderDao().insertDetail(od7);
    }
}
