package com.example.btl_g03.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_g03.Models.User;
import com.example.btl_g03.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<User> list;

    public UserAdapter(List<User> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);
        String fullName = user.getFullName();
        String phoneNumber = user.getPhoneNumber();
        String address = user.getAddress() + "";

        holder.textView.setText("FullName: "+fullName);
        holder.tv2.setText("PhoneNumber: " + phoneNumber);
        holder.tv3.setText("Address: " + address);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView,tv2,tv3;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txt_FullName);
            tv2=itemView.findViewById(R.id.txt_PhoneNumber);
            tv3 =itemView.findViewById(R.id.txt_Address);
        }
    }
}
