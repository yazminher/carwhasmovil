package com.example.carwhasmovil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.carwhasmovil.modelos.RestAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ActivityRegistrarUsuario extends AppCompatActivity {


    TextView txtlogin;
    EditText txtNom, txtApellido,txtTelefono,txtEmail,txtPass;
    Button btnRegistrar;
    Boolean retorno;
    AwesomeValidation awesomeValidation;
    FirebaseAuth firebaseAuth;
    private String uid; // UID del Usuario
    // Array de Paises en Spinner
    private ArrayList<String> Paises;
    ArrayAdapter adp;
    private Spinner sp_paisap;
    private static final String DEFAULT_LOCAL = "Honduras";
    String nombre,apellido,celular, mail, pass, elemento;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_usuario);
        firebaseAuth = FirebaseAuth.getInstance();
        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);
        awesomeValidation.addValidation(this,R.id.txtEmail, Patterns.EMAIL_ADDRESS,R.string.invalid_mail);
        awesomeValidation.addValidation(this,R.id.txtPass, ".{6,}",R.string.invalid_password);

        txtlogin = (TextView)findViewById(R.id.txtlogin);
        txtNom = (EditText) findViewById(R.id.txtNombre);
        txtApellido = (EditText) findViewById(R.id.txtApellido);
        txtTelefono = (EditText) findViewById(R.id.txtTelefono);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPass = (EditText) findViewById(R.id.txtPass);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        sp_paisap = (Spinner) findViewById(R.id.Pais); //Elemento del Spinner de Paises

        String[] paises = getResources().getStringArray(R.array.paises);
        Paises = new ArrayList<>(Arrays.asList(paises));

        adp = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Paises);
        sp_paisap.setAdapter(adp);
        sp_paisap.setSelection(adp.getPosition(DEFAULT_LOCAL));

        sp_paisap.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                elemento = (String) sp_paisap.getAdapter().getItem(position);   // El elemento seleccionado del Spinner
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        txtlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityRegistrarUsuario.this, ActivityLogin.class);
                startActivity(intent);
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validar();
                nombre = txtNom.getText().toString().trim();
                apellido= txtApellido.getText().toString().trim();
                celular = txtTelefono.getText().toString().trim();
                mail= txtEmail.getText().toString().trim();
                pass = txtPass.getText().toString().trim();

                if (awesomeValidation.validate()){
                    firebaseAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                user.sendEmailVerification();
                                InsertEmail();
                                onCreateDialog();
                                CleanScreen();
                            }else{
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                dameToastdeError(errorCode);
                            }
                        }
                    });
                }else{
                    Toast.makeText(ActivityRegistrarUsuario.this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // VALIDAR CAMPOS VACIOS
    public boolean validar(){
        retorno= true;

        String nom = txtNom.getText().toString();
        String ape= txtApellido.getText().toString();
        String tel = txtTelefono.getText().toString();

        if(nom.isEmpty()){
            txtNom.setError("DEBE INGRESAR EL NOMBRE");
            retorno = false;
        }
        if(ape.isEmpty()){
            txtApellido.setError("DEBE INGRESAR EL APELLIDO");
            retorno = false;
        }
        if(tel.isEmpty()){
            txtTelefono.setError("DEBE INGRESAR EL TELEFONO");
            retorno = false;
        }

        return retorno;
    }

    // POSIBLES ERRORES DEL AWESOMEVALIDATION
    private void dameToastdeError(String error) {

        switch (error) {

            case "ERROR_INVALID_CUSTOM_TOKEN":
                Toast.makeText(ActivityRegistrarUsuario.this, "El formato del token personalizado es incorrecto. Por favor revise la documentación", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Toast.makeText(ActivityRegistrarUsuario.this, "El token personalizado corresponde a una audiencia diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Toast.makeText(ActivityRegistrarUsuario.this, "La credencial de autenticación proporcionada tiene un formato incorrecto o ha caducado.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_EMAIL":
                Toast.makeText(ActivityRegistrarUsuario.this, "La dirección de correo electrónico está mal formateada.", Toast.LENGTH_LONG).show();
                txtEmail.setError("La dirección de correo electrónico está mal formateada.");
                txtEmail.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Toast.makeText(ActivityRegistrarUsuario.this, "La contraseña no es válida o el usuario no tiene contraseña.", Toast.LENGTH_LONG).show();
                txtPass.setError("la contraseña es incorrecta ");
                txtPass.requestFocus();
                txtPass.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Toast.makeText(ActivityRegistrarUsuario.this, "Las credenciales proporcionadas no corresponden al usuario que inició sesión anteriormente..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Toast.makeText(ActivityRegistrarUsuario.this,"Esta operación es sensible y requiere autenticación reciente. Inicie sesión nuevamente antes de volver a intentar esta solicitud.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Toast.makeText(ActivityRegistrarUsuario.this, "Ya existe una cuenta con la misma dirección de correo electrónico pero diferentes credenciales de inicio de sesión. Inicie sesión con un proveedor asociado a esta dirección de correo electrónico.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(ActivityRegistrarUsuario.this, "La dirección de correo electrónico ya está siendo utilizada por otra cuenta..   ", Toast.LENGTH_LONG).show();
                txtEmail.setError("La dirección de correo electrónico ya está siendo utilizada por otra cuenta.");
                txtEmail.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Toast.makeText(ActivityRegistrarUsuario.this, "Esta credencial ya está asociada con una cuenta de usuario diferente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_DISABLED":
                Toast.makeText(ActivityRegistrarUsuario.this, "La cuenta de usuario ha sido inhabilitada por un administrador..", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Toast.makeText(ActivityRegistrarUsuario.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_USER_NOT_FOUND":
                Toast.makeText(ActivityRegistrarUsuario.this, "No hay ningún registro de usuario que corresponda a este identificador. Es posible que se haya eliminado al usuario.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Toast.makeText(ActivityRegistrarUsuario.this, "La credencial del usuario ya no es válida. El usuario debe iniciar sesión nuevamente.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Toast.makeText(ActivityRegistrarUsuario.this, "Esta operación no está permitida. Debes habilitar este servicio en la consola.", Toast.LENGTH_LONG).show();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(ActivityRegistrarUsuario.this, "La contraseña proporcionada no es válida..", Toast.LENGTH_LONG).show();
                txtPass.setError("La contraseña no es válida, debe tener al menos 6 caracteres");
                txtPass.requestFocus();
                break;

        }

    }

    // OBTENER UID DEL FIREBASE
    private void GetUID() {

        firebaseAuth = FirebaseAuth.getInstance(); // Iniciar Firebase
        FirebaseUser user = firebaseAuth.getCurrentUser();  // Obtener Usuario Actual

        // Si usuario no existe
        try {
            if (user != null) {
                uid = user.getUid(); // Obtener el UID del Usuario Actual
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }

    // INSERTAR DATOS EN LA BD
    private void InsertEmail() {

        GetUID();   // Obtener funcion UID para almacenarlo

        ProgressDialog progressDialog=new ProgressDialog( this);

        String url = RestAPI.ApiPostRegistrar;    // URL del RestAPI


        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equalsIgnoreCase("Datos insertados")){
                    Toast.makeText(ActivityRegistrarUsuario.this, "Datos insertados", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(ActivityRegistrarUsuario.this, "No se puede insertar", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ActivityRegistrarUsuario.this, "No se puede insertar "+error.toString(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> parametros = new HashMap<String, String>();

                parametros.put("uid", uid);
                parametros.put("correo", mail);
                parametros.put("nombre", nombre);
                parametros.put("apellido", apellido);
                parametros.put("celular", celular);
                parametros.put("pais", elemento);
                return parametros;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // CUADRO DE DIALOGO PARA LA VERIFICACION DEL EMAIL
    private void onCreateDialog() {
        String mail= txtEmail.getText().toString();
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegistrarUsuario.this);
        builder.setMessage("Hemos enviado un enlace de verificacion a "+mail+". Por favor revise su bandeja de entrada o carpeta de correo no deseado");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // Create the AlertDialog object and return it
        AlertDialog titulo =builder.create();
        titulo.show();
    }

    // LIMPIAR CAJAS DE TEXTO
    private void CleanScreen() {
        txtNom.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtPass.setText("");
        sp_paisap.setSelection(adp.getPosition(DEFAULT_LOCAL));
    }
}

