package com.example.shoeapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.shoeapp.data.dao.AddressDao;
import com.example.shoeapp.data.dao.CategoryDao;
import com.example.shoeapp.data.dao.CartDao;
import com.example.shoeapp.data.dao.ChatMessageDao;
import com.example.shoeapp.data.dao.OrderDao;
import com.example.shoeapp.data.dao.ProductDao;
import com.example.shoeapp.data.dao.UserDao;
import com.example.shoeapp.data.entity.Brand;
import com.example.shoeapp.data.entity.Cart;
import com.example.shoeapp.data.entity.CartItem;
import com.example.shoeapp.data.entity.Category;
import com.example.shoeapp.data.entity.ChatMessage;
import com.example.shoeapp.data.entity.Color;
import com.example.shoeapp.data.entity.DeliveryAddress;
import com.example.shoeapp.data.entity.Order;
import com.example.shoeapp.data.entity.OrderDetail;
import com.example.shoeapp.data.entity.Product;
import com.example.shoeapp.data.entity.ProductImg;
import com.example.shoeapp.data.entity.ProductReview;
import com.example.shoeapp.data.entity.ProductVariant;
import com.example.shoeapp.data.entity.Promotion;
import com.example.shoeapp.data.entity.PromotionProduct;
import com.example.shoeapp.data.entity.Size;
import com.example.shoeapp.data.entity.TokenType;
import com.example.shoeapp.data.entity.User;

import java.util.List;

@Database(entities = {
        Brand.class,
        Cart.class,
        CartItem.class,
        Category.class,
        ChatMessage.class,
        Color.class,
        Order.class,
        OrderDetail.class,
        Product.class,
        ProductImg.class,
        ProductReview.class,
        ProductVariant.class,
        Promotion.class,
        PromotionProduct.class,
        Size.class,
        TokenType.class,
        User.class,
        DeliveryAddress.class
}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();
    public abstract CategoryDao categoryDao();
    public abstract OrderDao orderDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract AddressDao addressDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "shoeapp.db")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                    prepopulateIfEmpty(INSTANCE);
                }
            }
        }
        return INSTANCE;
    }

    private static void prepopulateIfEmpty(AppDatabase db) {
        if (db.userDao().getAllUsers().isEmpty()) {
            // 1. Thêm Users mẫu
            User admin = new User();
            admin.email = "admin@gmail.com";
            admin.passwordHash = "admin123";
            admin.phoneNumber = "0123456789";
//            admin.address = "123 Đường Admin, TP.HCM";
            admin.role = "ADMIN";
            admin.fullName = "Lại Thị Hoa";
            admin.isActive = true;
            admin.userId = "USR001";
            admin.createdAt = "2026-06-22 12:00:00";
            db.userDao().insert(admin);

            User customer = new User();
            customer.email = "customer@gmail.com";
            customer.passwordHash = "customer123";
            customer.phoneNumber = "0987654321";
//            customer.address = "456 Đường User, Hà Nội";
            customer.role = "CUSTOMER";
            customer.fullName = "Nguyễn Văn A";
            customer.isActive = true;
            customer.userId = "USR002";
            customer.createdAt = "2026-06-22 12:00:00";
            db.userDao().insert(customer);

            int adminId = 1;
            int customerId = 2;
            List<User> users = db.userDao().getAllUsers();
            for (User u : users) {
                if ("ADMIN".equals(u.role)) adminId = u.id;
                else if ("CUSTOMER".equals(u.role)) customerId = u.id;
            }

            // 2. Thêm Categories mẫu
            Category catSneakers = new Category();
            catSneakers.name = "Sneakers";
            catSneakers.isActive = true;
            catSneakers.sortOrder = 1;
            catSneakers.createdAt = "2026-06-22 12:00:00";
            db.categoryDao().insert(catSneakers);

            Category catRunning = new Category();
            catRunning.name = "Running";
            catRunning.isActive = true;
            catRunning.sortOrder = 2;
            catRunning.createdAt = "2026-06-22 12:00:00";
            db.categoryDao().insert(catRunning);

            Category catCasual = new Category();
            catCasual.name = "Casual";
            catCasual.isActive = true;
            catCasual.sortOrder = 3;
            catCasual.createdAt = "2026-06-22 12:00:00";
            db.categoryDao().insert(catCasual);

            int catSneakersId = 1;
            int catRunningId = 2;
            int catCasualId = 3;
            List<Category> categories = db.categoryDao().getAllCategories();
            for (Category c : categories) {
                if ("Sneakers".equals(c.name)) catSneakersId = c.id;
                else if ("Running".equals(c.name)) catRunningId = c.id;
                else if ("Casual".equals(c.name)) catCasualId = c.id;
            }

            // 3. Thêm Brands mẫu
            Brand brandNike = new Brand();
            brandNike.name = "Nike";
            brandNike.logoUrl = "";
            brandNike.isActive = true;
            brandNike.prefix = "NK";
            db.productDao().insertBrand(brandNike);

            Brand brandAdidas = new Brand();
            brandAdidas.name = "Adidas";
            brandAdidas.logoUrl = "";
            brandAdidas.isActive = true;
            brandAdidas.prefix = "AD";
            db.productDao().insertBrand(brandAdidas);

            Brand brandJordan = new Brand();
            brandJordan.name = "Jordan";
            brandJordan.logoUrl = "";
            brandJordan.isActive = true;
            brandJordan.prefix = "JD";
            db.productDao().insertBrand(brandJordan);

            int brandNikeId = 1;
            int brandAdidasId = 2;
            int brandJordanId = 3;
            List<Brand> brands = db.productDao().getAllBrands();
            for (Brand b : brands) {
                if ("Nike".equals(b.name)) brandNikeId = b.id;
                else if ("Adidas".equals(b.name)) brandAdidasId = b.id;
                else if ("Jordan".equals(b.name)) brandJordanId = b.id;
            }

            // 4. Thêm Colors mẫu
            Color colBlack = new Color();
            colBlack.name = "Black";
            colBlack.hexcode = "#000000";
            colBlack.colorId = "COL001";
            db.productDao().insertColor(colBlack);

            Color colWhite = new Color();
            colWhite.name = "White";
            colWhite.hexcode = "#FFFFFF";
            colWhite.colorId = "COL002";
            db.productDao().insertColor(colWhite);

            Color colRed = new Color();
            colRed.name = "Red";
            colRed.hexcode = "#FF0000";
            colRed.colorId = "COL003";
            db.productDao().insertColor(colRed);

            int colBlackId = 1;
            int colWhiteId = 2;
            int colRedId = 3;
            List<Color> colors = db.productDao().getAllColors();
            for (Color c : colors) {
                if ("Black".equals(c.name)) colBlackId = c.id;
                else if ("White".equals(c.name)) colWhiteId = c.id;
                else if ("Red".equals(c.name)) colRedId = c.id;
            }

            // 5. Thêm Sizes mẫu
            Size sz40 = new Size();
            sz40.name = "40";
            sz40.sortOrder = 1;
            sz40.sizeId = "SZ040";
            db.productDao().insertSize(sz40);

            Size sz41 = new Size();
            sz41.name = "41";
            sz41.sortOrder = 2;
            sz41.sizeId = "SZ041";
            db.productDao().insertSize(sz41);

            Size sz42 = new Size();
            sz42.name = "42";
            sz42.sortOrder = 3;
            sz42.sizeId = "SZ042";
            db.productDao().insertSize(sz42);

            int sz40Id = 1;
            int sz41Id = 2;
            int sz42Id = 3;
            List<Size> sizes = db.productDao().getAllSizes();
            for (Size s : sizes) {
                if ("40".equals(s.name)) sz40Id = s.id;
                else if ("41".equals(s.name)) sz41Id = s.id;
                else if ("42".equals(s.name)) sz42Id = s.id;
            }

            // 6. Thêm Products mẫu
            Product p1 = new Product();
            p1.name = "Nike Air Max 270";
            p1.description = "The Nike Air Max 270 delivers elements from the original Air Max models with updated comfort.";
            p1.price = 150.0;
            p1.originalPrice = 170.0;
            p1.brandId = brandNikeId;
            p1.shoeCategory = catSneakersId;
            p1.isAvailable = true;
            p1.isDiscontinue = false;
            p1.productId = "PROD001";
            p1.addedAt = "2026-06-22 12:00:00";
            db.productDao().insert(p1);

            Product p2 = new Product();
            p2.name = "Adidas Ultraboost 22";
            p2.description = "Ultraboost running shoes built in part with Parley Ocean Plastic.";
            p2.price = 180.0;
            p2.originalPrice = 200.0;
            p2.brandId = brandAdidasId;
            p2.shoeCategory = catRunningId;
            p2.isAvailable = true;
            p2.isDiscontinue = false;
            p2.productId = "PROD002";
            p2.addedAt = "2026-06-22 12:00:00";
            db.productDao().insert(p2);

            Product p3 = new Product();
            p3.name = "Air Jordan 1 Retro High";
            p3.description = "Familiar but always fresh, the iconic Air Jordan 1 is remastered for today's sneakerhead culture.";
            p3.price = 170.0;
            p3.originalPrice = 170.0;
            p3.brandId = brandJordanId;
            p3.shoeCategory = catCasualId;
            p3.isAvailable = true;
            p3.isDiscontinue = false;
            p3.productId = "PROD003";
            p3.addedAt = "2026-06-22 12:00:00";
            db.productDao().insert(p3);

            int p1Id = 1, p2Id = 2, p3Id = 3;
            List<Product> products = db.productDao().getAllProducts();
            for (Product p : products) {
                if ("Nike Air Max 270".equals(p.name)) p1Id = p.id;
                else if ("Adidas Ultraboost 22".equals(p.name)) p2Id = p.id;
                else if ("Air Jordan 1 Retro High".equals(p.name)) p3Id = p.id;
            }

            // 7. Thêm ProductVariants mẫu (để có số lượng tồn kho hiển thị)
            ProductVariant pv1 = new ProductVariant();
            pv1.productId = p1Id;
            pv1.sizeId = sz40Id;
            pv1.colorId = colBlackId;
            pv1.stock = 15;
            pv1.isDiscontinueVariant = false;
            db.productDao().insertProductVariant(pv1);

            ProductVariant pv2 = new ProductVariant();
            pv2.productId = p1Id;
            pv2.sizeId = sz41Id;
            pv2.colorId = colWhiteId;
            pv2.stock = 8;
            pv2.isDiscontinueVariant = false;
            db.productDao().insertProductVariant(pv2);

            ProductVariant pv3 = new ProductVariant();
            pv3.productId = p2Id;
            pv3.sizeId = sz41Id;
            pv3.colorId = colBlackId;
            pv3.stock = 20;
            pv3.isDiscontinueVariant = false;
            db.productDao().insertProductVariant(pv3);

            ProductVariant pv4 = new ProductVariant();
            pv4.productId = p3Id;
            pv4.sizeId = sz42Id;
            pv4.colorId = colRedId;
            pv4.stock = 5;
            pv4.isDiscontinueVariant = false;
            db.productDao().insertProductVariant(pv4);

            // 8. Thêm ProductReview mẫu
            ProductReview rev1 = new ProductReview();
            rev1.productId = p1Id;
            rev1.userId = customerId;
            rev1.rating = 5;
            rev1.content = "Giày rất êm và ôm chân!";
            rev1.createdAt = "2026-06-22 12:00:00";
            db.productDao().insertReview(rev1);

            ProductReview rev2 = new ProductReview();
            rev2.productId = p2Id;
            rev2.userId = customerId;
            rev2.rating = 4;
            rev2.content = "Chất lượng tốt, giao hàng nhanh.";
            rev2.createdAt = "2026-06-22 12:00:00";
            db.productDao().insertReview(rev2);

            // 9. Thêm Orders mẫu
            Order o1 = new Order();
            o1.userId = customerId;
            o1.createdAt = "2026-06-22 12:00:00";
            o1.shippingFee = 5.0;
            o1.subTotal = 150.0;
            o1.grandTotal = 155.0;
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
            o2.subTotal = 350.0;
            o2.grandTotal = 355.0;
            o2.shippingAddress = "456 Đường User, Hà Nội";
            o2.phoneNumber = "0987654321";
            o2.orderStatus = "SHIPPED";
            o2.paymentMethod = "MOMO";
            o2.paymentStatus = "PAID";
            o2.orderNote = "";
            o2.shippingStatus = "SHIPPING";
            o2.ordersId = "ORD-20260622-002";
            db.orderDao().insert(o2);

            int o1Id = 1, o2Id = 2;
            List<Order> orders = db.orderDao().getAllOrders();
            for (Order o : orders) {
                if ("ORD-20260622-001".equals(o.ordersId)) o1Id = o.id;
                else if ("ORD-20260622-002".equals(o.ordersId)) o2Id = o.id;
            }

            // OrderDetails cho o1
            OrderDetail od1 = new OrderDetail();
            od1.orderId = o1Id;
            od1.productId = p1Id;
            od1.colorId = colBlackId;
            od1.sizeId = sz40Id;
            od1.quantity = 1;
            od1.unitPrice = 150.0;
            od1.subtotal = 150.0;
            od1.orderDetailId = "ORDDET-001";
            db.orderDao().insertDetail(od1);

            // OrderDetails cho o2
            OrderDetail od2 = new OrderDetail();
            od2.orderId = o2Id;
            od2.productId = p2Id;
            od2.colorId = colBlackId;
            od2.sizeId = sz41Id;
            od2.quantity = 1;
            od2.unitPrice = 180.0;
            od2.subtotal = 180.0;
            od2.orderDetailId = "ORDDET-002";
            db.orderDao().insertDetail(od2);

            OrderDetail od3 = new OrderDetail();
            od3.orderId = o2Id;
            od3.productId = p3Id;
            od3.colorId = colRedId;
            od3.sizeId = sz42Id;
            od3.quantity = 1;
            od3.unitPrice = 170.0;
            od3.subtotal = 170.0;
            od3.orderDetailId = "ORDDET-003";
            db.orderDao().insertDetail(od3);
        }
    }
}

