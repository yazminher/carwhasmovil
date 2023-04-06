package com.example.carwhasmovil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityPass extends AppCompatActivity {
    EditText txtMail;
    Button btnEnviar;
    AwesomeValidation awesomeValidation;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        firebaseAuth = FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(this,R.id.txtMail, Patterns.EMAIL_ADDRESS,R.string.invalid_mail);

        txtMail = (EditText) findViewById(R.id.txtMail);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restablecerContra();
            }
        });
    }

    private void restablecerContra() {

        if(TextUtils.isEmpty(txtMail.getText())){
            Toast.makeText(this, "Ingresa una direccion de correo electronico valido", Toast.LENGTH_LONG).show();
        }else{
            String emailAddress = txtMail.getText().toString();

            if(awesomeValidation.validate()){
                firebaseAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Hemos enviado un correo para restablecer su contraseña", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        });
            }

        }
    }

}