package com.example.chatapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class chatFragment extends HandlerUser {

    static public User userReceive;
    static public User userSend;
    FragmentChatBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String senderRoom, receiverRoom;
    List<Message> messageList;
    MessagesAdapter adapter;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<Intent> activityResultLauncherGallery;

    // permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    // images pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    //  permissions array
    String[] cameraPermission;
    String[] storagePermission;
    // image picked will be same in this uri
    Uri image_uri = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userReceive = (User) getArguments().getSerializable("user");
            userSend = new User();
            // init permission arrays
            cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Glide.with(getContext()).load(userReceive.getImageUri()).into(binding.image);
        binding.receiveName.setText(userReceive.getName());
        binding.receiveStt.setText(userReceive.getStatus());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userSend.setUid(auth.getUid());

        messageList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        binding.messageAdapter.setLayoutManager(linearLayoutManager);
        adapter = new MessagesAdapter(messageList, getContext());
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
                Message messages = new Message(message, userSend.getUid(), date.getTime(), false);
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
        binding.imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show image pick dialog
                showImagePickDialog();
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(getView()).navigate(R.id.homeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onActivityResult(ActivityResult result) {

                Bundle extras = result.getData().getExtras();
                Uri imageUri;
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(imageBitmap,
                        imageBitmap.getHeight(), imageBitmap.getWidth(), false).copy(
                        Bitmap.Config.RGB_565, true
                ));

                Bitmap bm = result1.get();
                imageUri = saveImage(bm, getContext());
                try {
                    sendImageMessage(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        activityResultLauncherGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent data = result.getData();
                    if (data != null){
                        Uri selectedImageUri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                            WeakReference<Bitmap> result1 = new WeakReference<>(Bitmap.createScaledBitmap(bitmap,
                                    bitmap.getHeight(), bitmap.getWidth(), false).copy(
                                    Bitmap.Config.RGB_565, true
                            ));

                            Bitmap bm = result1.get();
                            Uri imageUri = saveImage(bm, getContext());
                            sendImageMessage(imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });


        return root;
    }

    private Uri saveImage(Bitmap image, Context context) {
        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "captured_image.jpg");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.example.chatapp" + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Image from");
        // set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // item click handle
                if (which == 0) {
                    pickFromCamera();
                }
                if (which == 1) {
                    {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncherGallery.launch(intent);
    }

    private void pickFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        activityResultLauncher.launch(takePictureIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendImageMessage(Object o) throws IOException {

        DatabaseReference reference = database.getReference().child("users").child(auth.getUid());
        String message = o.toString();
        Date date = new Date();
        Message message1 = new Message(message, userSend.getUid(), date.getTime(), true);
        database = FirebaseDatabase.getInstance();
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .push()
                .setValue(message1).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .push()
                                .setValue(message1).addOnCompleteListener(new OnCompleteListener<Void>() {
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


}