package com.example.chatapp.View;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HandlerUser extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference().child("users").child(FirebaseAuth.getInstance().getUid());
            reference.child("status").setValue("Online");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference().child("users").child(FirebaseAuth.getInstance().getUid());
            reference.child("status").setValue("Offline");
        }
    }
}
