package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ActivityPerfil extends AppCompatActivity {

    EditText ttnombre,ttapellidos, ttemail,tttelefono, ttpais;
    private FirebaseAuth mAuth;
    ImageView img;
    ImageButton btn_camara;
    Button btnAgg;
    byte [] Foto;
    View v;
    MotionEvent event;
    Boolean retorno;
    String correo, nombre, apellido,celular,pais;

    private RequestQueue rq;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAMARA = 100;


    private static Locale FilenameUtils;

    private String uid; // UID del Usuario
    private int id_users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            ttnombre = (EditText) view.findViewById(R.id.ttnombre);
            ttpais = (EditText) view.findViewById(R.id.ttpais);
            ttapellidos = (EditText) view.findViewById(R.id.ttapelllidos);
            tttelefono = (EditText) view.findViewById(R.id.tttelefono);
            ttemail = (EditText) view.findViewById(R.id.ttemail);

            img = (ImageView) view.findViewById(R.id.img);

            btn_camara = (ImageButton) view.findViewById(R.id.btn_camara);
            btnAgg = (Button) view.findViewById(R.id.btnAgg);


            rq = Volley.newRequestQueue(getContext());

            ttpais.setEnabled(false);
            ttemail.setEnabled(false);






            btn_camara.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    permisos();


                }
            });


            btnAgg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validar();
                    actualizar();

                }
            });


            GetUser();  ///cargar usuario


        }else{
            Toast.makeText(getApplication(),"Sin conexion a internet",Toast.LENGTH_SHORT).show();
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplication());
                            builder.setMessage("Se encuentra fuera de linea, verifique su conexion a internet y vuelva a intentar.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getApplication();
                                }
                            });
                            // Create the AlertDialog object and return it
                            AlertDialog titulo =builder.create();
                            titulo.show();

                            break;
                    }
                    return false;
                }
            });
        }

        return view;

    }

    // VALIDAR CAJAS DE TEXTO
    public boolean validar(){
        retorno= true;
        nombre = ttnombre.getText().toString().trim();
        apellido = ttapellidos.getText().toString().trim();
        celular = tttelefono.getText().toString().trim();


        if(nombre.isEmpty()){
            ttnombre.setError("DEBE INGRESAR EL NOMBRE");
            retorno = false;
        }
        if(apellido.isEmpty()){
            ttapellidos.setError("DEBE INGRESAR EL APELLIDO");
            retorno = false;
        }
        if(celular.isEmpty()){
            tttelefono.setError("DEBE INGRESAR EL TELEFONO");
            retorno = false;
        }
        return retorno;
    }

    // OBTENER UID DEL FIREBASE
    private void GetUser() {

        mAuth = FirebaseAuth.getInstance(); // Iniciar Firebase
        FirebaseUser user = mAuth.getCurrentUser();  // Obtener Usuario Actual

        // Si usuario no existe
        try {
            if (user != null) {
                uid = user.getUid(); // Obtener el UID del Usuario Actual
                SearchUID("https://sitiosweb2021.000webhostapp.com/Carwash/consultarCliente.php?uid='"+uid+"'");
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplication(), "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }

    // BUSCAR UID EN LA TABLA DE USUARIO DE LA BD
    private void SearchUID(String URL) {
        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        id_users = jsonObject.getInt("id_users");
                        ttnombre.setText(jsonObject.getString("nombre"));
                        ttapellidos.setText(jsonObject.getString("apellido"));
                        ttemail.setText(jsonObject.getString("correo"));
                        tttelefono.setText(jsonObject.getString("celular"));
                        ttpais.setText(jsonObject.getString("pais"));
                        //Traemos foto tipo Blob de BD como String Base64 y decodificamos a ByteArray
                        byte[] decodedString = Base64.decode(jsonObject.getString("foto"), 0);
                        //de ByteArray lo convertimos a Bitmap
                        Bitmap bitmapfotoBD = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        //Seteamos dicho Bitmap en el ImageView
                        img.setImageBitmap(bitmapfotoBD);
                    } catch (JSONException e) {
                        Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue= Volley.newRequestQueue(this.getApplication());
        requestQueue.add(jsonArrayRequest);
    }

    // PETICION PERMISOS DE LA GALERIA Y ACCESO A CAMARA
    private void permisos() {
        if(ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED  &&
                ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getApplication(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PETICION_ACCESO_CAMARA);
        }
        else
        {

            dispatchTakePictureIntent1();
        }
    }

    // RESPUESTA DE PERMISOS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PETICION_ACCESO_CAMARA) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                dispatchTakePictureIntent1();

            }
        }
        else {
            Toast.makeText(getApplication(), "Se necesitan permisos de acceso", Toast.LENGTH_LONG).show();
        }
    }

    // CAPTURAR FOTOGRAFIA DE LA CAMARA
    private void dispatchTakePictureIntent1() {
        Intent takePictureIntent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent1.resolveActivity(getApplication().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent1, REQUEST_IMAGE_CAPTURE);
        }
    }

    // CAPTURAR RESULTADO FOTOGRAFIA DE LA CAMARA Y GALERIA
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(imageBitmap);
        }

        else if (resultCode == getActivity().RESULT_OK) {
            Uri path=data.getData();
            img.setImageURI(path);

        }

    }

    //METODO ACTUALIZAR DATOS DEL USUARIO
    private void actualizar(){

        System.out.println("ANTES DE LA CONVERSION "+id_users);
        String url = "https://sitiosweb2021.000webhostapp.com/Carwash/actualizarUsuario.php";
        JSONObject parametros = new JSONObject();
        try {

            parametros.put("uid",uid);
            parametros.put("correo", ttemail.getText().toString());
            parametros.put("nombre", ttnombre.getText().toString());
            parametros.put("apellido", ttapellidos.getText().toString());
            parametros.put("celular", tttelefono.getText().toString());
            parametros.put("pais", ttpais.getText().toString());
            parametros.put("foto",GetStringImage(img));
            parametros.put("id_users",id_users);


        }catch (JSONException e){
            e.printStackTrace();
        }
        JsonObjectRequest requerimiento = new JsonObjectRequest(Request.Method.POST,
                url, parametros,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String resu = response.get("resultado").toString();
                            if (resu.equals("1")) {
                                Toast.makeText(getApplication(), "SE ACTUALIZÓ EL PERFIL", Toast.LENGTH_SHORT).show();
                            } else {
                                System.out.println("ID EN ELSE: "+id_users);
                                Toast.makeText(getApplication(), "Error al actualizar" , Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplication(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        rq.add(requerimiento);


    }

    // OBTENER STRING DE LA IMAGEN
    public static String GetStringImage(ImageView ObjImagen) {
        Bitmap bitmap = ((BitmapDrawable)ObjImagen.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] imagebyte = stream.toByteArray();
        String encode = Base64.encodeToString(imagebyte, Base64.DEFAULT);
        return encode;

    }
}