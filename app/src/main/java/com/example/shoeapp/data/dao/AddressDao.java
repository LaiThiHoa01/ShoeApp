package com.example.shoeapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shoeapp.data.entity.DeliveryAddress;

import java.util.List;

@Dao
public interface AddressDao {
    @Insert
    void insertAddress(DeliveryAddress address);
    @Query("SELECT * FROM delivery_addresses WHERE userId = :userId ORDER BY isDefault DESC")
    List<DeliveryAddress> getUserAddresses(String userId);

    @Query("UPDATE delivery_addresses SET isDefault = 0 WHERE userId = :userId")
    void clearAllDefaults(String userId);

    @Update
    void updateAddress(DeliveryAddress address);

    @Query("SELECT * FROM delivery_addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    DeliveryAddress getDefaultAddress(String userId);
}