package com.example.chatapp;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Model.Message;
import com.example.chatapp.Model.User;
import com.example.chatapp.ViewModel.MessagesAdapter;
import com.example.chatapp.databinding.FragmentChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private String checker = "";

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
    private boolean notify = false;

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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    private void showImagePickDialog()
    {
        String[] options = {"Camera", "Gallery"};
        // dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Image from");
        // set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // item click handle
                if(which == 0)
                {
                    pickFromCamera();
                }
                if(which == 1)
                {
//                    // gallery clicked
//                    if(!checkStoragePermission())
//                    {
//                        requestStoragePermission();
//                    }
//                    else
                    {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }
    private void pickFromGallery()
    {
        // intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera()
    {
        // intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri = getActivity().getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent intent1 = intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }
    private boolean checkStoragePermission()
    {
        // check if storage permission is enabled or not
        // return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission()
    {
        // request runtime storage permission
        //ActivityCompat.requestPermissions(getActivity(), storagePermission, STORAGE_REQUEST_CODE);
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermission()
    {
        // check if camera permission is enabled or not
        // return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission()
    {
        // request runtime camera permission
        // ActivityCompat.requestPermissions(getActivity(),cameraPermission,CAMERA_REQUEST_CODE);
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }
    // handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "Camera & Storage both permissions are necessary....", Toast.LENGTH_SHORT).show();
                    }
                }
                else{ }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccept = grantResults[0] ==  PackageManager.PERMISSION_GRANTED;
                    if(storageAccept) {
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(), "Storage permission necessary...", Toast.LENGTH_SHORT).show();
                    }
                }
                else { }
            }
            break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // image picked from gallery, get uri of image
                image_uri = data.getData();
                // use this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // image picked from camera, get uri of image
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        notify = true;
        // progress dialog
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Sending image.....");
        progressDialog.show();
        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_" + System.currentTimeMillis();
        /* chat node will be created that will contain all images sent via chat */

        // get bitmap from image uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), image_uri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,baos);
        byte[] data = baos.toByteArray(); // convert image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //  image upload
                        progressDialog.dismiss();
                        // get uri of uploaded image
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()) {
                            // add image uri and other info to database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            // setup required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender",senderRoom);
                            hashMap.put("receiver",receiverRoom);
                            hashMap.put("message",downloadUri);
                            hashMap.put("timestamp",timeStamp);
                            hashMap.put("type","image");
                            hashMap.put("isSeen",false);
                            // put this data to firebase
                            databaseReference.child("Chats").push().setValue(hashMap);
                            // send notification
//                            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
//                            database.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    User user = snapshot.getValue(User.class);
//                                    if(notify){
//                                        //sendNotification(hisUid,user.getName(),"Sent you a photo...");
//                                    }
//                                    notify = false;
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // image failed
                        progressDialog.dismiss();
                    }
                });
    }

}