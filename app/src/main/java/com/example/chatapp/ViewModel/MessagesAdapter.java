package com.example.chatapp.ViewModel;

import static com.example.chatapp.chatFragment.userReceive;
import static com.example.chatapp.chatFragment.userSend;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.net.URL;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter {

    private List<Message> messageList;
    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;
    Context context;

    public MessagesAdapter(List<Message> messageList, Context context){
        this.messageList = messageList;
        this.context = context;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sender_item, parent, false);
            return new SenderViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receiver_item, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder.getClass()==SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            if (message.isType()){

                viewHolder.txtMessage.setVisibility(View.GONE);
                viewHolder.SenderImageview.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getMessage()).into(viewHolder.SenderImageview);
            }
            else{
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.SenderImageview.setVisibility(View.GONE);
                viewHolder.txtMessage.setText(message.getMessage());
            }

            Glide.with(context).load(userSend.getImageUri()).into(viewHolder.image);
        }
        else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            if (message.isType()){

                viewHolder.txtMessage.setVisibility(View.GONE);
                viewHolder.ReceiverImageview.setVisibility(View.VISIBLE);
                Glide.with(context).load(message.getMessage()).into(viewHolder.ReceiverImageview);
            }
            else {
                viewHolder.txtMessage.setVisibility(View.VISIBLE);
                viewHolder.ReceiverImageview.setVisibility(View.GONE);
                viewHolder.txtMessage.setText(message.getMessage());
            }
            Glide.with(context).load(userReceive.getImageUri()).into(viewHolder.image);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(message.getSenderID())){
            return ITEM_SEND;
        }
        else {
            return ITEM_RECEIVE;
        }
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView txtMessage;
        ImageView SenderImageview;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            SenderImageview = itemView.findViewById(R.id.messageIv);
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView txtMessage;
        ImageView ReceiverImageview;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            ReceiverImageview = itemView.findViewById(R.id.messageIv);
        }
    }

    public static boolean validateHTTP_URI(String uri) {
        final URL url;
        try {
            url = new URL(uri);
        } catch (Exception e1) {
            return false;
        }
        return "http".equals(url.getProtocol());
    }
}
