package com.example.shoeapp.user;

import com.example.shoeapp.R;
import com.example.shoeapp.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClientProductSamples {
    private ClientProductSamples() {
    }

    public static List<Product> featured() {
        return all().subList(0, 4);
    }

    public static List<Product> all() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1, "Air Phantom Pro", "NovaSole - Sneakers", "Sneakers", 189.99, 249.99, 45, true, Arrays.asList(7, 8, 9, 10, 11), 4.8f, 234, R.drawable.ic_shoe));
        products.add(new Product(2, "Urban Stride X", "StreetFlex - Sneakers", "Sneakers", 149.99, 199.99, 30, false, Arrays.asList(7, 8, 9, 10), 4.6f, 189, R.drawable.ic_shoe));
        products.add(new Product(3, "Blaze Runner", "SwiftKick - Running", "Running", 219.99, 279.99, 60, true, Arrays.asList(6, 7, 8, 9, 10), 4.9f, 412, R.drawable.ic_shoe));
        products.add(new Product(4, "Shadow Force", "DarkLine - Sneakers", "Sneakers", 259.99, 319.99, 8, false, Arrays.asList(8, 9, 10, 11), 4.7f, 311, R.drawable.ic_shoe));
        products.add(new Product(5, "Cloud Walker", "FeatherStep - Casual", "Casual", 99.99, 129.99, 12, false, Arrays.asList(7, 8, 9, 10), 4.3f, 97, R.drawable.ic_shoe));
        products.add(new Product(6, "Apex Boost", "ProStride - Running", "Running", 179.99, 229.99, 25, true, Arrays.asList(7, 8, 9, 10, 11, 12), 4.5f, 156, R.drawable.ic_shoe));
        products.add(new Product(7, "Midnight Classic", "DarkLine - Casual", "Casual", 129.99, 169.99, 18, false, Arrays.asList(7, 8, 9, 10, 11), 4.4f, 88, R.drawable.ic_shoe));
        products.add(new Product(8, "Storm Elite", "NovaSole - Training", "Training", 199.99, 249.99, 22, false, Arrays.asList(8, 9, 10, 11, 12), 4.6f, 143, R.drawable.ic_shoe));
        return products;
    }
}
