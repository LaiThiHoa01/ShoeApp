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

    public ClientProductRepository(Context context) {
        productDao = AppDatabase.getDatabase(context).productDao();
    }

    public void ensureSeedData() {
        if (productDao.countProducts() >= SEED_PRODUCT_COUNT) {
            return;
        }

        seedBrands();
        seedCategories();
        seedColors();
        seedSizes();
        seedProducts();
        seedVariants();
    }

    public List<com.example.shoeapp.model.Product> getFeaturedProducts() {
        List<com.example.shoeapp.model.Product> products = getAllProducts();
        return products.size() > 4 ? products.subList(0, 4) : products;
    }

    public List<com.example.shoeapp.model.Product> getAllProducts() {
        List<com.example.shoeapp.model.Product> products = new ArrayList<>();
        for (Product product : productDao.getAllProducts()) {
            products.add(toClientProduct(product));
        }
        return products;
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
        insertBrand(1, "Nike", "NIKE");
        insertBrand(2, "adidas", "ADS");
        insertBrand(3, "New Balance", "NB");
        insertBrand(4, "ASICS", "ASC");
        insertBrand(5, "Puma", "PUMA");
        insertBrand(6, "Converse", "CNV");
        insertBrand(7, "Vans", "VNS");
        insertBrand(8, "Biti's", "BTS");
        insertBrand(9, "Ananas", "ANS");
    }

    private void seedCategories() {
        insertCategory(1, "Giày chạy bộ", 1);
        insertCategory(2, "Sneaker hằng ngày", 2);
        insertCategory(3, "Giày bóng rổ", 3);
        insertCategory(4, "Giày tập luyện", 4);
        insertCategory(5, "Giày thời trang", 5);
    }

    private void seedColors() {
        insertColor(1, "Trắng", "#FFFFFF", "CLR-TRANG");
        insertColor(2, "Đen", "#111111", "CLR-DEN");
        insertColor(3, "Xám", "#8A8A8A", "CLR-XAM");
        insertColor(4, "Xanh navy", "#1D3557", "CLR-NAVY");
        insertColor(5, "Kem", "#EEE2C6", "CLR-KEM");
        insertColor(6, "Đỏ", "#C1121F", "CLR-DO");
        insertColor(7, "Xanh lá", "#2D6A4F", "CLR-XANH-LA");
        insertColor(8, "Nâu gum", "#9C6644", "CLR-NAU-GUM");
    }

    private void seedSizes() {
        for (int size = 36; size <= 45; size++) {
            Size entity = new Size();
            entity.id = size - 35;
            entity.name = String.valueOf(size);
            entity.sortOrder = size;
            entity.sizeId = "SIZE-" + size;
            productDao.insertSize(entity);
        }
    }

    private void seedProducts() {
        insertProduct(1, "Nike Air Force 1 '07", "Mẫu sneaker da cổ thấp kinh điển, dễ phối đồ và phù hợp đi hằng ngày.", 2929000, 3239000, 1, 2, "PRD-NIKE-AF1", image(0));
        insertProduct(2, "Nike Pegasus 41", "Giày chạy bộ đệm ReactX, phù hợp chạy hằng ngày và đi bộ đường dài.", 3829000, 4109000, 1, 1, "PRD-NIKE-PEGASUS-41", image(1));
        insertProduct(3, "Nike Dunk Low Retro", "Thiết kế bóng rổ cổ điển chuyển sang phong cách streetwear năng động.", 2929000, 3519000, 1, 5, "PRD-NIKE-DUNK-LOW", image(2));
        insertProduct(4, "Nike Court Vision Low", "Dáng tennis/basketball retro, upper da tổng hợp dễ chăm sóc.", 2069000, 2459000, 1, 2, "PRD-NIKE-COURT-VISION", image(3));
        insertProduct(5, "Nike LeBron Witness 8", "Giày bóng rổ hỗ trợ cổ chân, đệm chắc và bám sân tốt.", 3239000, 3829000, 1, 3, "PRD-NIKE-LEBRON-WITNESS", image(4));
        insertProduct(6, "adidas Samba OG", "Mẫu sneaker biểu tượng với đế gum, phù hợp phong cách casual.", 2700000, 3000000, 2, 5, "PRD-ADIDAS-SAMBA", image(5));
        insertProduct(7, "adidas Ultraboost 5X", "Giày chạy bộ đệm Boost nhẹ, êm và ổn định cho tập luyện.", 5200000, 5800000, 2, 1, "PRD-ADIDAS-ULTRABOOST", image(6));
        insertProduct(8, "adidas Stan Smith", "Sneaker tối giản, form gọn, hợp đi học, đi làm và cuối tuần.", 2500000, 2900000, 2, 2, "PRD-ADIDAS-STAN-SMITH", image(7));
        insertProduct(9, "adidas Gazelle Indoor", "Dáng retro bằng da lộn, nổi bật với đế gum và phối màu cổ điển.", 3000000, 3400000, 2, 5, "PRD-ADIDAS-GAZELLE", image(8));
        insertProduct(10, "adidas Campus 00s", "Form chunky skate-inspired, hợp phối đồ streetwear.", 2800000, 3200000, 2, 5, "PRD-ADIDAS-CAMPUS", image(9));
        insertProduct(11, "New Balance 327", "Sneaker lifestyle đế răng cưa, form nhẹ và phong cách retro running.", 2590000, 2990000, 3, 2, "PRD-NB-327", image(10));
        insertProduct(12, "New Balance 530", "Dáng running Y2K, lưới thoáng và đệm ABZORB thoải mái.", 2890000, 3290000, 3, 2, "PRD-NB-530", image(11));
        insertProduct(13, "New Balance 574 Core", "Mẫu classic dễ mang, đệm EVA nhẹ và bền cho ngày dài.", 2390000, 2790000, 3, 2, "PRD-NB-574", image(12));
        insertProduct(14, "ASICS GEL-Kayano 31", "Giày chạy ổn định cao, hỗ trợ tốt cho runner cần kiểm soát bước chân.", 4690000, 5290000, 4, 1, "PRD-ASICS-KAYANO-31", image(13));
        insertProduct(15, "ASICS GEL-Nimbus 26", "Đệm êm, phù hợp chạy dài và người thích cảm giác mềm dưới chân.", 4390000, 4990000, 4, 1, "PRD-ASICS-NIMBUS-26", image(14));
        insertProduct(16, "ASICS Japan S", "Sneaker thấp cổ lấy cảm hứng từ bóng rổ cổ điển, gọn và dễ phối.", 1690000, 2090000, 4, 2, "PRD-ASICS-JAPAN-S", image(15));
        insertProduct(17, "Puma Suede Classic XXI", "Dòng suede kinh điển, thân giày mềm và kiểu dáng thời trang.", 1990000, 2390000, 5, 5, "PRD-PUMA-SUEDE", image(16));
        insertProduct(18, "Puma RS-X Efekt", "Sneaker chunky nổi bật, đệm tốt cho di chuyển hằng ngày.", 2990000, 3490000, 5, 2, "PRD-PUMA-RSX", image(17));
        insertProduct(19, "Puma TRC Blaze", "Phong cách retro running với phối màu mạnh và phần đế nổi bật.", 2590000, 3190000, 5, 5, "PRD-PUMA-TRC-BLAZE", image(18));
        insertProduct(20, "Converse Chuck 70 High", "Canvas cổ cao kinh điển, đế dày hơn và cảm giác mang chắc chắn.", 1900000, 2200000, 6, 5, "PRD-CONVERSE-CHUCK-70", image(19));
        insertProduct(21, "Vans Old Skool", "Giày skate cổ điển với sọc side stripe đặc trưng và đế waffle.", 1800000, 2100000, 7, 5, "PRD-VANS-OLD-SKOOL", image(20));
        insertProduct(22, "Biti's Hunter X", "Giày Việt nhẹ, đệm êm, phù hợp đi học, đi chơi và di chuyển nhiều.", 1199000, 1499000, 8, 2, "PRD-BITIS-HUNTER-X", image(21));
        insertProduct(23, "Ananas Basas Bumper Gum", "Sneaker canvas đế gum, kiểu dáng tối giản và giá dễ tiếp cận.", 650000, 750000, 9, 5, "PRD-ANANAS-BASAS", image(22));
        insertProduct(24, "Ananas Vintas Saigon 1980s", "Thiết kế retro Việt Nam, hợp phối đồ casual và streetwear.", 720000, 850000, 9, 5, "PRD-ANANAS-VINTAS", image(23));
    }

    private void seedVariants() {
        for (int productId = 1; productId <= SEED_PRODUCT_COUNT; productId++) {
            for (int colorId : colorsForProduct(productId)) {
                for (int sizeId = 1; sizeId <= 10; sizeId++) {
                    int stock = 2 + (productId * colorId + sizeId) % 10;
                    insertVariant(productId, sizeId, colorId, stock);
                }
            }
        }
    }

    private void insertBrand(int id, String name, String prefix) {
        Brand brand = new Brand();
        brand.id = id;
        brand.name = name;
        brand.prefix = prefix;
        brand.logoUrl = "";
        brand.isActive = true;
        productDao.insertBrand(brand);
    }

    private void insertCategory(int id, String name, int sortOrder) {
        Category category = new Category();
        category.id = id;
        category.name = name;
        category.iconUrl = "";
        category.isActive = true;
        category.sortOrder = sortOrder;
        category.createdAt = "2026-06-23";
        productDao.insertCategory(category);
    }

    private void insertColor(int id, String name, String hexcode, String colorId) {
        Color color = new Color();
        color.id = id;
        color.name = name;
        color.hexcode = hexcode;
        color.colorId = colorId;
        productDao.insertColor(color);
    }

    private void insertProduct(int id, String name, String description, double price,
                               double originalPrice, int brandId, int categoryId,
                               String productId, String imageUrl) {
        Product product = new Product();
        product.id = id;
        product.name = name;
        product.description = description;
        product.price = price;
        product.originalPrice = originalPrice;
        product.brandId = brandId;
        product.shoeCategory = categoryId;
        product.addedAt = "2026-06-23";
        product.isDiscontinue = false;
        product.isAvailable = true;
        product.productId = productId;
        productDao.insert(product);
        insertProductImages(id, imageUrl, colorsForProduct(id));
    }

    private void insertProductImages(int productId, String imageUrl, int[] colorIds) {
        for (int index = 0; index < colorIds.length; index++) {
            ProductImg productImg = new ProductImg();
            productImg.productId = productId;
            productImg.colorId = colorIds[index];
            productImg.imgUrl = index == 0 ? imageUrl : image(productId + colorIds[index] + index);
            productImg.sortOrder = 1;
            productImg.isActive = true;
            productImg.isThumbnail = index == 0;
            productDao.insertProductImage(productImg);
        }
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
        float[] ratings = {4.8f, 4.7f, 4.6f, 4.5f, 4.7f, 4.9f, 4.8f, 4.6f, 4.7f, 4.5f, 4.6f, 4.8f};
        return ratings[(id - 1) % ratings.length];
    }

    private int reviewCountFor(int id) {
        int[] counts = {284, 176, 391, 122, 97, 438, 205, 318, 144, 231, 168, 256};
        return counts[(id - 1) % counts.length];
    }

    private int[] colorsForProduct(int productId) {
        int[][] productColors = {
                {1, 2, 5},
                {2, 3, 4},
                {1, 2, 6, 8},
                {1, 2, 3},
                {2, 6, 4},
                {1, 2, 8},
                {2, 3, 7},
                {1, 2, 5},
                {3, 4, 8},
                {2, 5, 7},
                {3, 5, 7},
                {1, 3, 4},
                {2, 3, 5},
                {2, 4, 7},
                {1, 3, 5},
                {1, 2, 6},
                {2, 5, 8},
                {3, 4, 6},
                {2, 6, 8},
                {1, 2, 5, 6},
                {1, 2, 8},
                {2, 4, 7},
                {1, 2, 8},
                {3, 5, 8}
        };
        return productColors[(productId - 1) % productColors.length];
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
}
