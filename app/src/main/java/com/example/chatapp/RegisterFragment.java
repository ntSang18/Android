package com.example.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.util.PatternsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.Model.User;
import com.example.chatapp.databinding.FragmentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;


public class RegisterFragment extends Fragment {

    FirebaseAuth auth = FirebaseAuth.getInstance();

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
        FragmentRegisterBinding binding = FragmentRegisterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.signInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.loginFragment);
            }
        });


        binding.signup.setEnabled(false);

        binding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")
                        && !binding.name.getText().toString().equals("") && !binding.rePass.getText().toString().equals("")) {
                    binding.signup.setEnabled(true);
                } else {
                    binding.signup.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")
                        && !binding.name.getText().toString().equals("") && !binding.rePass.getText().toString().equals("")) {
                    binding.signup.setEnabled(true);
                } else {
                    binding.signup.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")
                        && !binding.name.getText().toString().equals("") && !binding.rePass.getText().toString().equals("")) {
                    binding.signup.setEnabled(true);
                } else {
                    binding.signup.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        binding.rePass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")
                        && !binding.name.getText().toString().equals("") && !binding.rePass.getText().toString().equals("")) {
                    binding.signup.setEnabled(true);
                } else {
                    binding.signup.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (binding.password.getText().toString().length() < 6) {
                    binding.password.setError("Password must be at least 6 characters");
                    return;
                }

                if (!binding.rePass.getText().toString().equals(binding.password.getText().toString())) {
                    binding.rePass.setError("Password incorrect");
                    return;
                }

                if (!PatternsCompat.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
                    binding.email.setError("Please enter a valid email");
                    return;
                }

                Bundle bundle = new Bundle();
                String image = "https://firebasestorage.googleapis.com/v0/b/messenger-9b343.appspot.com/o/profile.png?alt=media&token=01a2be09-ec0c-4e51-9358-399d22a7b3e1";
                User user = new User(auth.getUid(), binding.name.getText().toString(), binding.email.getText().toString(), image, "Offline", 0);
                bundle.putSerializable("user", user);
                bundle.putString("password", binding.password.getText().toString());
                Navigation.findNavController(view).navigate(R.id.otpFragment, bundle);
            }
        });

        return root;
    }
}