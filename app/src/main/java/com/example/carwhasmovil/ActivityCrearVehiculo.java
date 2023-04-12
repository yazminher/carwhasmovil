package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ActivityCrearVehiculo extends AppCompatActivity {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

/*    private FragmentRegistroAutomovilBinding binding;
    private SlideShowViewModel slideshowViewModel;*/

    private Spinner spmarca,sp_anio, sptipoaceite;
    private String ItemMarcaModelo, ItemAnio, ItemTAceite;
    private Button btnguardar;

    private AsyncHttpClient http;
    private FirebaseAuth mAuth;

    private String uid,id_usuario;
    private int idUser;
    private RequestQueue rq;

/*
    public RegistroVehiculoFragment() {
        // Required empty public constructor
    }


     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilUsuarioFragment.

    // TODO: Rename and change types and number of parameters

*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_vehiculo);

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        View view = null;
        if (networkInfo != null && networkInfo.isConnected()) {
            http = new AsyncHttpClient();

            spmarca = (Spinner) findViewById(R.id.sp_model);
            sp_anio = (Spinner) findViewById(R.id.sp_anio);
            sptipoaceite = (Spinner) findViewById(R.id.sp_taceite);

            btnguardar = (Button) findViewById(R.id.btnAgg);

            rq = Volley.newRequestQueue(this);

            ObtenerMarcas_Modelos();   // Lista de Modelos en Spinner Marca y modelo
            ObtenerAnio(); // Lista de Año en Spinner Años
            ObtenerAceites();   // Lista de Aceites en Spinner Tipo de Aceite
            GetUser();          // Obtener ID del usuario en MySQL

            btnguardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InsertarVehiculo();    // Insertar en MySQL datos de Vehiculo del Usuario
                }
            });
        }else{
            Toast.makeText(getApplicationContext(),"Sin conexion a internet",Toast.LENGTH_SHORT).show();
            view = findViewById(R.id.view);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                            builder.setMessage("Verifique su conexion a internet");
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
        //return view;
    }

    // OBTENER LA LISTA DE MODELOS DE LA BD
    private void ListaModelos(String URL){
        ArrayList<Spinners> lista = new ArrayList<Spinners>();
        try {
            JSONObject jsonRespuesta = new JSONObject(URL);
            JSONArray jsonArreglo = jsonRespuesta.getJSONArray("datos");
            for(int i=0; i<jsonArreglo.length(); i++){
                Spinners a = new Spinners();
                a.setId(jsonArreglo.getJSONObject(i).getInt("idmarca"));
                a.setNombre(jsonArreglo.getJSONObject(i).getString("nombre"));
                lista.add(a);
            }

            ArrayAdapter<Spinners> adp = new ArrayAdapter<Spinners>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, lista);
            spmarca.setAdapter(adp);

            spmarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemMarcaModelo = (String) spmarca.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // OBTENER MARCAS DE LA BD
    public void ObtenerMarcas_Modelos() {
        String URL = RestAPI.ApiPostMarcaModelo;    // URL de recurso PHP

        http.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    ListaModelos(new String (responseBody));

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // OBTENER Y LISTAR AÑOS
    public void ObtenerAnio() {

        try {
            String[] anio = new String[32];
            ArrayList<String> Anios;

            for(int i=0; i<32; i++){
                anio[i] = (i+1990)+"";
            }

            Anios = new ArrayList<>(Arrays.asList(anio));
            ArrayAdapter adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Anios);
            sp_anio.setAdapter(adp);


            sp_anio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemAnio = (String) sp_anio.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // OBTENER LA LISTA DE MODELOS DE LA BD
    private void ListaAceites(String URL){
        ArrayList<Spinners> lista = new ArrayList<Spinners>();
        try {
            JSONObject jsonRespuesta = new JSONObject(URL);
            JSONArray jsonArreglo = jsonRespuesta.getJSONArray("datos");
            for(int i=0; i<jsonArreglo.length(); i++){
                Spinners a = new Spinners();
                a.setId(jsonArreglo.getJSONObject(i).getInt("tpact_id"));
                a.setNombre(jsonArreglo.getJSONObject(i).getString("tpact_nombre"));
                lista.add(a);
            }

            ArrayAdapter<Spinners> adp = new ArrayAdapter<Spinners>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, lista);
            sptipoaceite.setAdapter(adp);

            sptipoaceite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemTAceite = (String) sptipoaceite.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // OBTENER TIPOS DE ACEITE DE LA BD
    public void ObtenerAceites() {
        String URL = RestAPI.ApiPostAceite;   // URL de recurso PHP

        http.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    ListaAceites(new String (responseBody));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
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
                System.out.println("UID"+uid);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error si no entra al TRY: "+ e, Toast.LENGTH_LONG).show();
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
                        id_usuario = String.valueOf(idUser);
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

        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    // GUARDAR VEHICULO EN LA BASE DE DATOS
    private void InsertarVehiculo() {

        String url = RestAPI.ApiPostCrearVehiculo;    // URL del RestAPI

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Datos insertados", Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Error en Response", "onResponse: " + error.getMessage().toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                HashMap<String, String> parametros = new HashMap<String, String>();
                parametros.put("marcamodelo", ItemMarcaModelo);
                parametros.put("anio", ItemAnio);
                parametros.put("taceite", ItemTAceite);
                parametros.put("iduser", id_usuario);
                return parametros;

            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(stringRequest);
    }

/*    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }*/



}