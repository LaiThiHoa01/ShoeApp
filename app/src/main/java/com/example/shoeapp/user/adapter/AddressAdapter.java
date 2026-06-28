package com.example.shoeapp.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoeapp.R;
import com.example.shoeapp.data.entity.DeliveryAddress;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    private List<DeliveryAddress> addresses;
    private final OnAddressClickListener listener;
    private final OnAddressDeleteListener deleteListener;

    public interface OnAddressClickListener {
        void onDefaultClick(DeliveryAddress address);
    }

    public interface OnAddressDeleteListener {
        void onDeleteClick(DeliveryAddress address);
    }

    public AddressAdapter(
            List<DeliveryAddress> addresses,
            OnAddressClickListener listener,
            OnAddressDeleteListener deleteListener
    ) {
        this.addresses = addresses;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setAddresses(List<DeliveryAddress> newAddresses) {
        this.addresses = newAddresses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryAddress addr = addresses.get(position);

        holder.txtPhone.setText(addr.phoneNumber);
        holder.txtFull.setText(addr.address);
        holder.radioDefault.setChecked(addr.isDefault);

        holder.itemView.setOnClickListener(v -> {
            if (!addr.isDefault) {
                listener.onDefaultClick(addr);
            }
        });

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(addr));
    }

    @Override
    public int getItemCount() {
        return addresses == null ? 0 : addresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPhone;
        TextView txtFull;
        RadioButton radioDefault;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPhone = itemView.findViewById(R.id.item_address_phone);
            txtFull = itemView.findViewById(R.id.item_address_full);
            radioDefault = itemView.findViewById(R.id.item_address_radio);
            deleteButton = itemView.findViewById(R.id.item_address_delete);
        }
    }
}