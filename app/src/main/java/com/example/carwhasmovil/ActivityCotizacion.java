package com.example.carwhasmovil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ActivityCotizacion extends AppCompatActivity {
    private AsyncHttpClient http;
    //private SlideShowViewModel slideshowViewModel;
    //private FragmentCotizacionBinding binding;
    private FirebaseAuth mAuth;
    private RequestQueue rq;

    // USUARIO
    private String uid; // UID del Usuario Firebase
    private String iduser;  // ID del Usuario tabla USERS

    //VEHICULOS
    private int idUser; // ID del Usuario tabla CREARVEHICULO
    private String[] iddevehiculo= new String[900]; //IDVEHICULO de la posicion
    private String IdVehiculoBD,vehiculo; // ID del Vehiculo tabla CREARVEHICULO

    //SPINNERS
    Spinner sp_vehiculos,sp_servicios,sp_ubicacion;
    private String ItemVehiculo, ItemServicios, ItemUbicacion;
    private ArrayList<Spinners> lista;
    ArrayAdapter<Spinners> adp;
    private ArrayAdapter adapter2;

    //UBICACION
    private int seleccionar; // Opcion spinner ubicacion
    String Latitud,Longitud;
    private String[] contenido; // Array opciones Spinner
    private boolean isFirstTime = true;
    boolean retorno;
    TextView textViewUbicacion;

    //FECHA Y HORA
    private int dia, mes, anio, hora, minutos;
    EditText txtFecha,txtHora;

    //BOTONES
    Button btnGuardar;
    ImageButton btnFecha,btnHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cotizacion);
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        View view = null;
        if (networkInfo != null && networkInfo.isConnected()) {

            sp_vehiculos = (Spinner) findViewById(R.id.sp_vehiculos);
            sp_servicios = (Spinner) findViewById(R.id.sp_servicios);
            sp_ubicacion = (Spinner) findViewById(R.id.sp_ubicacion);

            btnFecha = (ImageButton) findViewById(R.id.btnFecha);
            btnHora = (ImageButton) findViewById(R.id.btnHora);
            btnGuardar = (Button) findViewById(R.id.btnGuardar);

            txtFecha = (EditText) findViewById(R.id.txtFecha);
            txtHora = (EditText) findViewById(R.id.txtHora);

            textViewUbicacion = (TextView) findViewById(R.id.textViewUbicacion);

            http = new AsyncHttpClient();
            rq = Volley.newRequestQueue(getApplicationContext());

            txtFecha.setInputType(InputType.TYPE_NULL);
            txtHora.setInputType(InputType.TYPE_NULL);

            GetUser(); // Funcion para obtener UID y ID del usuario

            final int interval = 1500; // 1 Second
            Handler handler = new Handler();
            Runnable runnable = new Runnable(){
                public void run() {
                    ObtenerVehiculos();     // Funcion para cargar Vehiculos en Spinner
                    ObtenerServicios();     // Funcion para cargar Servicios en Spinner
                }
            };

            handler.postAtTime(runnable, System.currentTimeMillis() + interval);
            handler.postDelayed(runnable, interval);

            // SI SERVICIO ES CAMBIO DE ACEITE
            sp_servicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemServicios = (String) sp_servicios.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner

                    String CA = "Cambio de Aceite";

                    if (ItemServicios.equals(CA)) {
                        AlertDialog.Builder alerta = new AlertDialog.Builder(getApplicationContext());
                        alerta.setMessage("Unicamente se hace en centro de servicio")
                                .setCancelable(false)
                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        sp_ubicacion.setSelection(adapter2.getPosition("Centro de Servicio"));
                                        sp_ubicacion.setEnabled(false);
                                    }
                                });
                        AlertDialog titulo = alerta.create();
                        titulo.setTitle("Aviso");
                        titulo.show();

                    }
                    else{
                        sp_ubicacion.setEnabled(true);

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            // ESCOGER UBICACION EN EL SPINNER
            contenido = new String[]{"Seleccione","Centro de Servicio", "A Domicilio"};
            ArrayList<String> ubicacion = new ArrayList<>(Arrays.asList(contenido));
            adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ubicacion);
            sp_ubicacion.setAdapter(adapter2);
            sp_ubicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isFirstTime){
                        isFirstTime = true;
                    }
                    if (contenido[position] == "A Domicilio") {
                        seleccionar = 0;

                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("opcion", seleccionar);
                        startActivity(intent);


                    } else if (contenido[position] == "Centro de Servicio") {
                        seleccionar = 1;

                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        intent.putExtra("opcion", seleccionar);
                        startActivity(intent);
                    }
                    ItemUbicacion = (String) sp_ubicacion.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner


                }


                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            btnFecha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Calendar c = Calendar.getInstance();
                    dia = c.get(Calendar.DAY_OF_MONTH);
                    mes = c.get(Calendar.MONTH);
                    anio = c.get(Calendar.YEAR);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getApplicationContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                            txtFecha.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
                        }
                    },anio,mes,dia);
                    datePickerDialog.show();
                }
            });

            btnHora.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Calendar c = Calendar.getInstance();
                    hora = c.get(Calendar.HOUR_OF_DAY);
                    minutos = c.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            txtHora.setText(hourOfDay+":"+minute);
                        }
                    }, hora,minutos,false);
                    timePickerDialog.show();
                }
            });

            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    validar();
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
                            builder.setMessage("Se encuentra fuera de linea, verifique su conexion a internet y vuelva a intentar.");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getApplicationContext().finish();
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
       // return view;

    }

    // OBTENER UID DEL USUARIO EN FIREBASE
    private void GetUser() {

        mAuth = FirebaseAuth.getInstance();            // Iniciar Firebase
        FirebaseUser user = mAuth.getCurrentUser();     // Obtener Usuario Actual

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
                        idUser = jsonObject.getInt("id_users"); // Obtiene ID del Usuario
                        iduser = String.valueOf(idUser); // Convierte el ID a String
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

        http.get("https://sitiosweb2021.000webhostapp.com/Carwash/consultarVehiculo.php?iduser='"+iduser+"'", new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                System.out.println("ON SUCCESS");
                if(statusCode == 200){
                    ListaVehiculos(new String (responseBody));
                    //ListarIDVehiculos(new String (responseBody));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                System.out.println("ERROR HTTP");
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

            adp = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, lista);
            sp_vehiculos.setAdapter(adp);

            sp_vehiculos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemVehiculo = (String) sp_vehiculos.getAdapter().getItem(position).toString();
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

    // OBTENER EL ID DE LOS VEHICULOS EN UN ARREGLO PARA LA POSICION
    private void ListarIDVehiculos(String URL){
        try {

            JSONArray jsonArreglo = new JSONArray(URL);
            for(int i=0; i<jsonArreglo.length(); i++){
                iddevehiculo[i] = jsonArreglo.getJSONObject(i).getString("idvehi");
                vehiculo = jsonArreglo.getJSONObject(i).getString("marcamodelo");
                System.out.println("VEHICULO: "+vehiculo);
            }
        }
        catch (Exception e1){
            e1.printStackTrace();
        }
    }

    // OBTENER SERVICIOS DE LA BD
    public void ObtenerServicios() {
        String URL = RestAPI.ApiPostServicios;    // URL de recurso PHP

        http.post(URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    ListaServicios(new String (responseBody));

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    // OBTENER LA LISTA DE SERVICIOS DE LA BD EN EL SPINNER
    private void ListaServicios(String URL){
        lista = new ArrayList<Spinners>();
        try {
            JSONObject jsonRespuesta = new JSONObject(URL);
            JSONArray jsonArreglo = jsonRespuesta.getJSONArray("datos");
            for(int i=0; i<jsonArreglo.length(); i++){
                Spinners a = new Spinners();
                a.setId(jsonArreglo.getJSONObject(i).getInt("idServicio"));
                a.setNombre(jsonArreglo.getJSONObject(i).getString("servicio"));
                lista.add(a);
            }

            adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, lista);
            sp_servicios.setAdapter(adp);

            sp_servicios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ItemServicios = (String) sp_servicios.getAdapter().getItem(position).toString();   // El elemento seleccionado del Spinner
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

    // PEDIR PERMISOS PARA LA UBICACION
    public void Permisos(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
    }

    // OBTENER UBICACION
    @SuppressLint("MissingPermission")
    private void locationStart() {
        LocationManager mlocManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        //Local.setCotizacionFragment(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);

    }

    // PERMISO UBICACION ACTIVO
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    // OBTENER UBICACION MEDIANTE LA LONGITUD Y LATITUD
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // OBTENER LONGITUD Y LATITUD
    public class Localizacion implements LocationListener {
        cotizacion_Fragment cotizacionFragment;

        public cotizacion_Fragment getCotizacionFragment() {
            return cotizacionFragment;
        }

        public void setCotizacionFragment(cotizacion_Fragment cotizacionFragment) {
            this.cotizacionFragment = cotizacionFragment;
        }

        @Override
        public void onLocationChanged(Location loc) {

            loc.getLatitude();
            loc.getLongitude();

            Latitud =  ""+loc.getLatitude();
            Longitud =  ""+loc.getLongitude();
            this.cotizacionFragment.setLocation(loc);

        }
    }

    // VALIDACION DE CAMPOS VACIOS
    public boolean validar(){
        retorno= true;
        String fecha= txtFecha.getText().toString();
        String hora= txtHora.getText().toString();

        if(ItemUbicacion=="Seleccione"){
            textViewUbicacion.setError("DEBE SELECCIONAR UNA UBICACION");
            txtFecha.setError(null);
            txtHora.setError(null);
        }
        else if(fecha.isEmpty()){
            txtFecha.setError("DEBE SELECCIONAR UNA FECHA");
            textViewUbicacion.setError(null);
            txtHora.setError(null);
            retorno = false;
        }
        else if(hora.isEmpty()){
            txtHora.setError("DEBE SELECCIONAR UNA HORA");
            textViewUbicacion.setError(null);
            txtFecha.setError(null);
            retorno = false;
        }
        else
        {
            textViewUbicacion.setError(null);
            txtHora.setError(null);
            txtFecha.setError(null);
            guardarCotizacion();
        }
        return retorno;
    }

    // GUARDAR COTIZACION EN LA BASE DE DATOS
    private void guardarCotizacion(){
        String URL = RestAPI.ApiPostCotizacion;
        StringRequest stringRequest= new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Toast.makeText(getApplicationContext(), "Operacion Exitosa", Toast.LENGTH_SHORT).show();
                createNotification();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros=new HashMap<String,String>();
                String FechaHora= txtFecha.getText().toString()+" "+txtHora.getText().toString()+":00";

                parametros.put("vehiculo", ItemVehiculo);
                parametros.put("servicio", ItemServicios);
                parametros.put("ubicacion", ItemUbicacion);
                parametros.put("fecha", FechaHora);
                parametros.put("estado", "Aprobado");
                parametros.put("iduser",iduser);
                return parametros;

            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(stringRequest);

    }

    private void createNotification(){
        String id="mensaje";
        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),id);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(id, "nuevo", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setAutoCancel(true).setWhen(System.currentTimeMillis())
                .setContentTitle("Cotización de Servicio").setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentText("Su cotización ha sido aprobada con exito.")
                .setColor(Color.BLUE)
                .setContentIntent(sendNotification())
                .setContentInfo("nuevo");
        Random random = new Random();
        int id_notification = random.nextInt(8000);

        assert notificationManager != null;
        notificationManager.notify(id_notification,builder.build());
    }

    public PendingIntent sendNotification(){
        Intent intent = new Intent(getApplicationContext().getApplicationContext(), MainActivity.class);
        intent.putExtra("color", "rojo");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(getApplicationContext(),0,intent,0);
    }

    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }*/

}