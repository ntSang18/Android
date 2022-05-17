package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapp.Model.User;
import com.example.chatapp.databinding.FragmentOptBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.mukesh.OnOtpCompletionListener;


import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class OtpFragment extends Fragment {

    public static String otp_code;
    private User user;
    private String password;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
            password = getArguments().getString("password");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentOptBinding binding = FragmentOptBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.email.setText(user.getEmail());
        binding.email.setEnabled(false);

        binding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.email.setEnabled(true);
            }
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.send.setText("Re-send");
                binding.send.setEnabled(false);
                binding.email.setEnabled(false);
                binding.otpView.setText("");

                new CountDownTimer(30000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        binding.countdown.setText("" + millisUntilFinished / 1000);

                    }

                    public void onFinish() {
                        binding.countdown.setText("√");
                        binding.send.setEnabled(true);
                    }

                }.start();

                try {
                    sendMail(binding.email.getText().toString());
                } catch (Exception e) {

                }

            }
        });

        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                if(otp.equals(otp_code)){
                    user.setEmail(binding.email.getText().toString());
                    auth.createUserWithEmailAndPassword(user.getEmail(), password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        DatabaseReference reference = database.getReference().child("users").child(auth.getUid());
                                        reference.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Navigation.findNavController(getView()).navigate(R.id.homeFragment);
                                                }else {
                                                    Toast.makeText(getContext(), "Error in Creating a new User", Toast.LENGTH_SHORT).show();
                                                    binding.otpView.setText("");
                                                }
                                            }
                                        });
                                    }else {
                                        Toast.makeText(getContext(), "Error in Creating a new User", Toast.LENGTH_SHORT).show();
                                        binding.otpView.setText("");
                                    }
                                }
                            });

                }
                else {
                    Toast.makeText(getContext(), "Invalid code", Toast.LENGTH_SHORT).show();
                    binding.otpView.setText("");
                }
            }
        });

        return root;
    }

    public static void sendMail(String recepient) {

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "465");
            String myAccountEmail = "chat.app.vn@gmail.com";
            String password = "Qwerty2581";
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(myAccountEmail, password);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            mimeMessage.setSubject("Verification");
            otp_code = getRandomNumberString();
            mimeMessage.setText(otp_code);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(mimeMessage);
                    } catch (MessagingException e) {

                    }
                }
            });

            thread.start();

        } catch (Exception e) {

        }

    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
}