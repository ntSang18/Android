package com.example.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.example.chatapp.databinding.FragmentSettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class SettingFragment extends Fragment {

    User user = new User();
    Uri rsImage;
    FragmentSettingBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncherGallery;

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
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        DatabaseReference reference = database.getReference().child("users").child(auth.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user.setName(snapshot.child("name").getValue().toString());
                user.setImageUri(snapshot.child("imageUri").getValue().toString());

                binding.name.setText(user.getName());
                Glide.with(getContext()).load(user.getImageUri()).into(binding.image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SharedPreferences preferences = getContext().getSharedPreferences("user.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();

        Glide.with(getContext()).load(user.getImageUri()).into(binding.image);
        binding.name.setText(user.getName());
        binding.email.setText(preferences.getString("email", ""));
        binding.password.setText(preferences.getString("password", ""));

        binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reference.child("status").setValue("Offline").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseAuth.getInstance().signOut();
                        Navigation.findNavController(getView()).navigate(R.id.loginFragment);
                    }
                });

            }
        });

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.name.getText().toString().equals(user.getName())) {
                    reference.child("name").setValue(binding.name.getText().toString());
                }
                if(!user.getImageUri().equals(rsImage.toString())){
                    StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                    if (rsImage != null){
                        storageReference.putFile(rsImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            reference.child("imageUri").setValue(uri.toString());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(preferences.getString("email", ""), preferences.getString("password", ""));
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                user.updateEmail(binding.email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        reference.child("email").setValue(binding.email.getText().toString());
                                        ed.putString("email", binding.email.getText().toString());
                                        ed.apply();

                                        user.updatePassword(binding.password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ed.putString("password", binding.password.getText().toString());
                                                ed.apply();
                                            }
                                        });
                                    }
                                });

                            }
                        });

                Toast.makeText(getContext(), "Update completed!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncherGallery.launch(intent);
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(getView()).navigate(R.id.homeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);

        activityResultLauncherGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data != null){
                        Uri selectedImageUri = data.getData();
                        binding.image.setImageURI(selectedImageUri);
                        rsImage = selectedImageUri;
                    }
                }
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference().child("users").child(FirebaseAuth.getInstance().getUid());
            reference.child("status").setValue("Online");
        }
    }
}