package com.example.majorprojectticketbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    EditText name,email,mno,pass,dob;
    Button signup;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name=findViewById(R.id.editTextText3);
        email=findViewById(R.id.editTextText4);
        mno=findViewById(R.id.editTextText5);
        pass=findViewById(R.id.editTextText6);
        signup=findViewById(R.id.button3);
        dob=findViewById(R.id.editTextDate2);
        auth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();

        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid=true;
                if(name.getText().toString().trim().isEmpty())
                {
                    name.setError("Empty!");
                    isValid=false;
                }

                if(!emailValid(email.getText().toString()))
                {
                    email.setError("Email is incorrect!");
                    isValid=false;
                }

                if(!mobileValid(mno.getText().toString()))
                {
                    mno.setError("10 digit Mobile Number!");
                    isValid=false;
                }

                if(!passwordValid(pass.getText().toString()))
                {
                    pass.setError("Length should be atleast 7!");
                    isValid=false;
                }

                if(!dobValid(dob.getText().toString()))
                {
                    isValid=false;
                }

                if(isValid==true)
                {
                    auth.createUserWithEmailAndPassword(email.getText().toString(),pass.getText().toString())
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful())
                                {
                                    FirebaseUser user=auth.getCurrentUser();
                                    if(user!=null)
                                    {
                                        user.sendEmailVerification().addOnCompleteListener(emailTask ->{
                                            if(emailTask.isSuccessful())
                                            {
                                                Toast.makeText(SignUp.this,"Verification email sent",Toast.LENGTH_SHORT).show();
                                                Intent i=new Intent(SignUp.this, Otp.class);
                                                i.putExtra("name",name.getText().toString());
                                                i.putExtra("email",email.getText().toString());
                                                i.putExtra("mno",mno.getText().toString());
                                                i.putExtra("dob",dob.getText().toString());
                                                startActivity(i);
                                            }
                                            else
                                            {
                                                Log.e(TAG, "Error in sending email verification", emailTask.getException());
                                                Toast.makeText(SignUp.this, "Error in sending email.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                else
                                {
                                    Log.e(TAG, "Sign Up Failed", task.getException());
                                    Toast.makeText(SignUp.this, "Sign Up Failed!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else
                {
                    Toast.makeText(SignUp.this, "Error!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean emailValid(String email)
    {
        return email!= null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean mobileValid(String mobile)
    {
        String pattern="^\\d{10}$";
        return mobile!=null && mobile.matches(pattern);
    }

    private boolean passwordValid(String pass)
    {
        String pattern="^.{7,}$";
        return pass!=null && pass.matches(pattern);
    }

    private boolean dobValid(String date)
    {
        String pattern = "^\\d{2}/\\d{2}/\\d{4}$";
        if (date==null || !date.matches(pattern))
        {
            dob.setError("Enter dob in the format dd/mm/yyyy");
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate birthDate = LocalDate.parse(date, formatter);
            LocalDate currentDate = LocalDate.now();
            int age = Period.between(birthDate, currentDate).getYears();
            if (age < 18) {
                dob.setError("You must be 18 years or older!");
                return false;
            }
        } catch (DateTimeParseException e) {
            dob.setError("Invalid date format!");
            return false;
        }
        return true;
    }
}