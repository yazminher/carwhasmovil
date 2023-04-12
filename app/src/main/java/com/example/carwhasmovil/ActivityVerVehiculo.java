package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.carwhasmovil.modelos.RestAPI;
import com.example.carwhasmovil.Spinner.Spinners;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import cz.msebera.android.httpclient.Header;
public class ActivityVerVehiculo extends AppCompatActivity {


    private AsyncHttpClient http;
    private FirebaseAuth mAuth;     // Iniciar Firebase
    private String uid;             // UID del Usuario en Firebase
    private String iduser,id_usuario;          // ID del Usuario en MySQL
    private String URLVehicle;      // URL de Spinner Vehiculo
    private int idUser;
    private String[] iddevehiculo= new String[900]; //IDVEHICULO de la posicion
    private ArrayList<Spinners> lista;
    private String IdVehiculoBD; // Parametro String
    private int id_vehiculo; // Parametro entero
    private Boolean SelectedRow = false;
    private RequestQueue rq;

    ArrayAdapter<Spinners> adp;
    ArrayList ArrayLista;

    ListView Lista;
    Button btnEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_vehiculo);
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        View view = null;
        if (networkInfo != null && networkInfo.isConnected()) {

            Lista = (ListView) findViewById(R.id.lista);
            btnEliminar = (Button) findViewById(R.id.btnEliminar);
            http = new AsyncHttpClient();
            //swipeRefreshLayout = view.findViewById(R.id.perfil_vehiculo);

            rq = Volley.newRequestQueue(getApplicationContext());

            GetUser();

            final int interval = 1500; // 1 Second
            Handler handler = new Handler();
            Runnable runnable = new Runnable(){
                public void run() {
                    ObtenerVehiculos();     // Funcion para cargar Vehiculos en Listview
                    Lista.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    Lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            view.setSelected(true);
                            SelectedRow = true;
                            IdVehiculoBD = iddevehiculo[position];
                        }
                    });
                }
            };

            handler.postAtTime(runnable, System.currentTimeMillis() + interval);
            handler.postDelayed(runnable, interval);

        }else{
            Toast.makeText(getApplicationContext(),"Sin conexion a internet",Toast.LENGTH_SHORT).show();
            view = findViewById(R.id.view);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                            builder.setMessage("Se encuentra fuera de linea, verifique su conexion a internet y vuelva a intentar.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getApplicationContext();
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

        // BOTON ELIMINAR
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(SelectedRow==false){
                    Toast.makeText(getApplicationContext(), "Seleccione un Vehiculo para eliminar", Toast.LENGTH_SHORT).show();
                }
                else{
                    AlertDialog.Builder builder= new AlertDialog.Builder(getApplicationContext());
                    builder.setMessage("Desea eliminar el vehiculo");
                    builder.setTitle("Eliminar");

                    builder.setPositiveButton("SÍ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            eliminar();
                            getApplicationContext(); // FINALIZAR ACTIVIDAD E IR A INICIO
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });
        // Inflate the layout for this fragment
        //return view;

    }

    // OBTENER UID DEL USUARIO EN FIREBASE
    private void GetUser() {

        mAuth = FirebaseAuth.getInstance();            // Iniciar Firebase
        FirebaseUser user = mAuth.getCurrentUser();     // Obtener Usuario Actual

        // Si usuario no existe
        try {
            if (user != null) {
                uid = user.getUid(); // Obtener el UID del Usuario Actual
                SearchUID("https://sitiosweb2021.000webhostapp.com/Carwash/consultarCliente.php?uid='"+uid+"'");

            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }

    // BUSCAR UID DEL USUARIO EN BD
    private void SearchUID(String URL) {
        JsonArrayRequest jsonArrayRequest= new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        idUser = jsonObject.getInt("id_users");
                        iduser = String.valueOf(idUser);
                        URLVehicle = "https://sitiosweb2021.000webhostapp.com/Carwash/consultarVehiculo.php?iduser="+iduser;
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Falló la conexion", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    // OBTENER VEHICULOS DEL USUARIO ACTUAL
    public void ObtenerVehiculos() {

        http.post(URLVehicle, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    ListaVehiculos(new String (responseBody));
                    ListarIDVehiculos(new String (responseBody));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // OBTENER LA LISTA DE VEHICULOS DE LA BD
    private void ListaVehiculos(String URL){
        lista = new ArrayList<Spinners>();
        try {
            JSONArray jsonArreglo = new JSONArray(URL);
            for(int i=0; i<jsonArreglo.length(); i++){
                Spinners m = new Spinners();
                m.setNombre(jsonArreglo.getJSONObject(i).getString("marcamodelo"));
                lista.add(m);
            }

            adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_single_choice, lista);
            Lista.setAdapter(adp);
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // OBTENER EL ID DE LOS VEHICULOS EN UN ARREGLO PARA LA POSICION
    private void ListarIDVehiculos(String URL){
        try {

            JSONArray jsonArreglo = new JSONArray(URL);
            for(int i=0; i<jsonArreglo.length(); i++){
                iddevehiculo[i] = jsonArreglo.getJSONObject(i).getString("idvehi");
            }
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // METODO PARA ELIMINAR VEHICULO DE LA BD
    private void eliminar(){
        System.out.println("ANTES DE LA CONVERSION "+IdVehiculoBD);
        String url = "https://sitiosweb2021.000webhostapp.com/Carwash/eliminar.php";
        JSONObject parametros = new JSONObject();
        try {

            parametros.put("idvehi",IdVehiculoBD);

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
                                Toast.makeText(getApplicationContext(), "SE ELIMINÓ EL VEHICULO", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "No existe el codigo del Vehiculo", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        rq.add(requerimiento);

    }
}