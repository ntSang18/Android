package com.example.chatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Model.User;
import com.example.chatapp.ViewModel.UserAdapter;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends HandlerUser {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    User user = new User();
    UserAdapter userAdapter;
    List<User> userList;

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
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // TSang: Get list user
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        binding.onlines.setAdapter(userAdapter);
        binding.onlines.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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
                    Picasso.get().load(snapshot.child("imageUri").getValue().toString()).into(binding.imgView);
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

        }

        return root;
    }


}