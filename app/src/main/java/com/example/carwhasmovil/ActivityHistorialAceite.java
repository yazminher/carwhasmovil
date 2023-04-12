package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.carwhasmovil.modelos.Historial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ActivityHistorialAceite extends AppCompatActivity {

    private AsyncHttpClient http;
    private FirebaseAuth mAuth;     // Iniciar Firebase
    private String uid,iduser;             // UID del Usuario en Firebase
    private int idUser;          // ID del Usuario en MySQL
    private String URLQuotation;      // URL de Lista Cotizacion Vehiculo

    RecyclerView recycler;

    ArrayList<Historial> historials;
    ArrayList<Historial> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_aceite);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        View view = null;
        if (networkInfo != null && networkInfo.isConnected()) {
            http = new AsyncHttpClient();

            items = new ArrayList<>();

            // Obtener el Recycler
            recycler = (RecyclerView) findViewById(R.id.reciclador);

            // Usar un administrador para LinearLayout
            recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

            GetUser();

            final int interval = 2000; // 1 Second
            Handler handler = new Handler();
            Runnable runnable = new Runnable(){
                public void run() {
                    // Inicializar Cotizaciones
                    ObtenerCotizacion();
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
                            builder.setMessage("Verifique su conexion a internet y vuelva a intentar.");
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
                        URLQuotation = "https://sitiosweb2021.000webhostapp.com/Carwash/HistorialAceite.php?iduser="+iduser;

                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }


    // Obtener Cotizaciones de Usuario con MySQL
    public void ObtenerCotizacion() {

        http.post(URLQuotation, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    ListaCotizacion(new String (responseBody));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // OBTENER LA LISTA DE COTIZACIONES DE LA BD
    private void ListaCotizacion(String URL){
        historials = new ArrayList<Historial>();
        try {
            JSONArray jsonArreglo = new JSONArray(URL);
            for(int i=0; i<jsonArreglo.length(); i++){
                Historial h = new Historial();
                h.setVehiculo(jsonArreglo.getJSONObject(i).getString("vehiculo"));
                h.setServicio(jsonArreglo.getJSONObject(i).getString("servicio"));
                h.setUbicacion(jsonArreglo.getJSONObject(i).getString("ubicacion"));
                h.setFecha(jsonArreglo.getJSONObject(i).getString("fecha"));
                h.setEstado(jsonArreglo.getJSONObject(i).getString("estado"));
                historials.add(h);
            }

            HistorialList();

            // Crear un nuevo adaptador
            ActivityAdaptadorHistorial adapter = new ActivityAdaptadorHistorial(items);
            recycler.setAdapter(adapter);
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // RELLENAR EL LISTVIEW
    private void HistorialList() {

        for (int i = 0;  i < historials.size(); i++){
            items.add(new Historial(
                    historials.get(i).getVehiculo(),
                    historials.get(i).getServicio(),
                    historials.get(i).getUbicacion(),
                    historials.get(i).getFecha(),
                    historials.get(i).getEstado()));
        }
    }
}