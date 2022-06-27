package com.example.chatapp;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.example.chatapp.ViewModel.UserAdapter;
import com.example.chatapp.ViewModel.UserChatAdapter;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class HomeFragment extends HandlerUser {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    User user = new User();
    UserAdapter userAdapter;
    UserChatAdapter userAdapterChat;
    ArrayList<User> userList;
    ArrayList<User> userListChat;
    FragmentHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // TSang: Get list user
        userList = new ArrayList<>();
        userListChat = new ArrayList<>();
        userAdapter = new UserAdapter(userList, getContext());
        userAdapterChat = new UserChatAdapter(userListChat, getContext());
        binding.onlines.setAdapter(userAdapter);
        binding.chats.setAdapter(userAdapterChat);
        binding.onlines.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.chats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        // End

        if(auth.getCurrentUser() == null){
            getChildFragmentManager().beginTransaction().replace(R.id.homepage, new LoginFragment()).commit();
        }else {
            DatabaseReference reference = database.getReference().child("users").child(auth.getUid());
            reference.child("status").setValue("Online");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user.setName(snapshot.child("name").getValue().toString());
                    user.setImageUri(snapshot.child("imageUri").getValue().toString());
                    Glide.with(getContext()).load(snapshot.child("imageUri").getValue().toString()).into(binding.imgView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            // TSang: Get list user
            reference = database.getReference().child("users");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (!dataSnapshot.getKey().equals(auth.getUid())){
                            User user = dataSnapshot.getValue(User.class);
                            user.setUid(dataSnapshot.getKey());

                            userList.add(user);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    DatabaseReference reference = database.getReference().child("chats");
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            userListChat.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                for (User user : userList){
                                    if(dataSnapshot.getKey().contains(user.getUid()) && dataSnapshot.getKey().contains(auth.getUid()) && !userListChat.contains(user))
                                        userListChat.add(user);
                                }
                            }
                            Collections.sort(userListChat, new Comparator<User>() {
                                @Override
                                public int compare(User o1, User o2) {
                                    return Long.compare(o2.getRecentActivity(), o1.getRecentActivity());
                                }
                            });

                            userAdapterChat.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // End get list user

            binding.imgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Navigation.findNavController(getView()).navigate(R.id.settingFragment);
                }
            });

            binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    filter(s);
                    return true;
                }
            });

        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
                System.exit(0);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


        return root;
    }

    private void filter(String newText) {
        ArrayList<User> list = new ArrayList<>();

        for (User user : userListChat) {
            if (user.getName().toLowerCase().contains(newText.toLowerCase())) {
                list.add(user);
            }
        }
        userAdapterChat.filterList(list);
    }

}