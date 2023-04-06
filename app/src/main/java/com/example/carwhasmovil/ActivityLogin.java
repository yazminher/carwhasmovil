package com.example.carwhasmovil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class ActivityLogin extends AppCompatActivity {

    TextView txtRegistrarse, txtRecuperar;
    EditText txtCorreo, txtContrasenia;
    Button btnLogin;
    boolean retorno;
    AwesomeValidation awesomeValidation;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(this,R.id.txtCorreo, Patterns.EMAIL_ADDRESS,R.string.invalid_mail);
        awesomeValidation.addValidation(this, R.id.txtContrasenia, ".{6,}", R.string.invalid_password);



        txtRegistrarse = (TextView)findViewById(R.id.txtRegistrarse);
        txtRecuperar = (TextView)findViewById(R.id.txtRecuperar);
        txtCorreo= (EditText) findViewById(R.id.txtCorreo);
        txtContrasenia = (EditText) findViewById(R.id.txtContrasenia);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        txtRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivityRegistrarUsuario.class);
                startActivity(intent);
            }
        });

        txtRecuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogin.this, ActivityPass.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mail = txtCorreo.getText().toString();
                String pass = txtContrasenia.getText().toString();

                if(awesomeValidation.validate()){

                    firebaseAuth.signInWithEmailAndPassword(mail,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                //Validar la Persistencia de datos
                                FirebaseUser user = firebaseAuth.getCurrentUser();


                                //Metodo para ir a la pantalla Principal
                                if(!user.isEmailVerified()){
                                    ///si user devuelve un valor false, enviamos un mensaje al usuario con un Toast
                                    //donde le coomunicamos que todavia no ha ido a verificar su correo con el enlace que le enviamos
                                    clean();
                                    Toast.makeText(ActivityLogin.this, "Correo electronico no verificado", Toast.LENGTH_LONG).show();
                                }else{
                                    // si User nos devuelve un valor true significa que el correo esta verificado
                                    // Accede a la aplicacion
                                    //Intent dashboardActivity = new Intent(ActivityLogin.this, MainActivity.class);
                                    //startActivity(dashboardActivity);
                                    Inicio();
                                }

                            }else{
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                dameToastdeError(errorCode);
                            }
                        }
                    });
                }
            }
        });
    }

    // Limpiar cajas de texto
    private void clean() {
        txtCorreo.setText("");
        txtContrasenia.setText("");
    }

    // Iniciar la app e ir a inicio
    private void Inicio(){
        Intent i = new Intent(this, MainActivity.class);
        //Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    // Posibles errores del AwesomeValidation
    private void dameToastdeError(String error) {

        switch (error) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(ActivityLogin.this, "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(ActivityLogin.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(ActivityLogin.this, "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(ActivityLogin.this, "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();
                txtCorreo.setError("La dirección de correo electrónico está mal formateada.");
                txtCorreo.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(ActivityLogin.this, "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                txtContrasenia.setError("la contraseña es incorrecta ");
                txtContrasenia.requestFocus();
                txtContrasenia.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(ActivityLogin.this, "Las credenciales proporcionadas no corresponden al usuario que inició sesión anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(ActivityLogin.this,"Esta operación es sensible y requiere autenticación reciente. Inicie sesión nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(ActivityLogin.this, "Ya existe una cuenta con la misma dirección de correo electrónico pero diferentes credenciales de inicio de sesión. Inicie sesión con un proveedor asociado a esta dirección de correo electrónico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(ActivityLogin.this, "La dirección de correo electrónico ya está siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                txtCorreo.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                txtCorreo.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(ActivityLogin.this, "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(ActivityLogin.this, "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(ActivityLogin.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(ActivityLogin.this, "No hay ningún registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(ActivityLogin.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(ActivityLogin.this, "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(ActivityLogin.this, "La contraseña proporcionada no es válida..", Toast.LENGTH_LONG).show();
                txtContrasenia.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                txtContrasenia.requestFocus();
                break;

        }

    }

    // Ir a inicio si encuentra sesion activa
    protected void onStart() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){ //si no es null el usuario ya esta logueado
            //mover al usuario al dashboard
            if(user.isEmailVerified()){
                Intent dashboardActivity = new Intent(ActivityLogin.this, MainActivity.class);
                startActivity(dashboardActivity);
            }
        }
        super.onStart();
    }
}
