package com.example.chatapp.ViewModel;

import static com.example.chatapp.chatFragment.userReceive;
import static com.example.chatapp.chatFragment.userSend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Model.Message;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter {

    private List<Message> messageList;
    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessagesAdapter(List<Message> messageList){
        this.messageList = messageList;
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
            viewHolder.txtMessage.setText(message.getMessage());
            Picasso.get().load(userSend.getImageUri()).into(viewHolder.image);
        }
        else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.txtMessage.setText(message.getMessage());
            Picasso.get().load(userReceive.getImageUri()).into(viewHolder.image);
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

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            txtMessage = itemView.findViewById(R.id.txtMessage);
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView txtMessage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            txtMessage = itemView.findViewById(R.id.txtMessage);
        }
    }
}
