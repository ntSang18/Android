package com.example.chatapp;

import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Model.Message;
import com.example.chatapp.Model.User;
import com.example.chatapp.ViewModel.MessagesAdapter;
import com.example.chatapp.databinding.FragmentChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class chatFragment extends Fragment {

    static public User userReceive;
    static public User userSend;
    FragmentChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderRoom, receiverRoom;
    List<Message> messageList;
    MessagesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userReceive = (User) getArguments().getSerializable("user");
            userSend = new User();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Picasso.get().load(userReceive.getImageUri()).into(binding.image);
        binding.receiveName.setText(userReceive.getName());
        binding.receiveStt.setText(userReceive.getStatus());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userSend.setUid(auth.getUid());

        messageList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        binding.messageAdapter.setLayoutManager(linearLayoutManager);
        adapter = new MessagesAdapter(messageList);
        binding.messageAdapter.setAdapter(adapter);

        senderRoom = userSend.getUid() + userReceive.getUid();
        receiverRoom = userReceive.getUid() + userSend.getUid();

        DatabaseReference reference = database.getReference().child("users").child(auth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userSend.setImageUri(snapshot.child("imageUri").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String message = binding.edtMessage.getText().toString();
                if (message.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter valid message", Toast.LENGTH_SHORT).show();
                    return;
                }
                binding.edtMessage.setText("");
                Date date = new Date();
                Message messages = new Message(message, userSend.getUid(), date.getTime());
                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .push()
                                        .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }
                        });

                // set recentActivity for user
                LocalDateTime now = LocalDateTime.now();
                ZonedDateTime zdt = now.atZone(ZoneId.of("America/Los_Angeles"));
                long millis = zdt.toInstant().toEpochMilli();
                reference.child("recentActivity").setValue(millis);
                DatabaseReference referenceReceive = database.getReference().child("users").child(userReceive.getUid());
                referenceReceive.child("recentActivity").setValue(millis);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(getView()).navigate(R.id.homeFragment);
            }
        });


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}