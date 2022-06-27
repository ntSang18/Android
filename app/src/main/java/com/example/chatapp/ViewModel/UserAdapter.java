package com.example.chatapp.ViewModel;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> listUserOnline;
    private Context context;


    public UserAdapter(List<User> listUserOnline, Context context) {
        this.listUserOnline = listUserOnline;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_online, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = listUserOnline.get(position);
        if(user == null){
            return;
        }
        holder.userName.setText(user.getName());
        Glide.with(context).load(user.getImageUri()).into(holder.userImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("user", user);
                Navigation.findNavController(holder.itemView).navigate(R.id.chatFragment, bundle);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listUserOnline.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView userImage;
        public TextView userName;

        public ViewHolder(@NonNull View view) {
            super(view);
            userImage = view.findViewById(R.id.UserImage);
            userName = view.findViewById(R.id.userName);
        }
    }
}
