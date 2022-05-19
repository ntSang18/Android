package com.example.chatapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Model.User;
import com.example.chatapp.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class HomeFragment extends Fragment {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    User user = new User();

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

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null){
            getChildFragmentManager().beginTransaction().replace(R.id.homepage, new LoginFragment()).commit();
        }else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
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