package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.PatternsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatapp.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

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
        FragmentLoginBinding binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(getView()).navigate(R.id.registerFragment);
            }
        });

        binding.login.setEnabled(false);

        binding.email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")) {
                    binding.login.setEnabled(true);
                } else {
                    binding.login.setEnabled(false);
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
                if (!binding.email.getText().toString().equals("") && !binding.password.getText().toString().equals("")) {

                    binding.login.setEnabled(true);
                } else {

                    binding.login.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        SharedPreferences preferences = getContext().getSharedPreferences("user.txt", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();

        binding.email.setText(preferences.getString("email", ""));
        binding.password.setText(preferences.getString("pass", ""));

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PatternsCompat.EMAIL_ADDRESS.matcher(binding.email.getText().toString()).matches()) {
                    binding.email.setError("Please enter a valid email");
                    return;
                }

                if (binding.password.getText().toString().length() < 6) {
                    binding.password.setError("Please enter a valid password");
                    return;
                }

                auth.signInWithEmailAndPassword(binding.email.getText().toString(), binding.password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    database.getReference().child("users").child(task.getResult().getUser().getUid()).child("status")
                                            .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.getResult().getValue().toString().equals("Offline")) {
                                                        ed.putString("email", binding.email.getText().toString());
                                                        ed.putString("password", binding.password.getText().toString());
                                                        ed.apply();

                                                        Navigation.findNavController(getView()).navigate(R.id.homeFragment);
                                                    } else {
                                                        Toast.makeText(getContext(), "Your account is currently logged in somewhere else!", Toast.LENGTH_LONG).show();
                                                        FirebaseAuth.getInstance().signOut();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(getContext(), "Username or Password does not match!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        return root;
    }
}