package com.example.shoeapp.data.repo;

import android.content.Context;

import com.example.shoeapp.data.AppDatabase;
import com.example.shoeapp.data.entity.DeliveryAddress;

import java.util.List;

public class AddressRepository {
    private final AppDatabase db;

    public AddressRepository(Context context) {
        db = AppDatabase.getDatabase(context);
    }

    public List<DeliveryAddress> getAddresses(String userId) {
        return db.addressDao().getUserAddresses(userId);
    }

    public void addAddress(String userId, String phone, String address, boolean isDefault) {
        DeliveryAddress item = new DeliveryAddress();
        item.userId = userId;
        item.phoneNumber = phone;
        item.address = address;
        item.isDefault = isDefault;
        db.addressDao().insertAddress(item);
    }

    public void setDefault(String userId, DeliveryAddress address) {
        db.addressDao().clearAllDefaults(userId);
        address.isDefault = true;
        db.addressDao().updateAddress(address);
    }

    public void delete(DeliveryAddress address) {
        db.addressDao().deleteAddress(address);
    }

    public DeliveryAddress getDefault(String userId) {
        return db.addressDao().getDefaultAddress(userId);
    }
}