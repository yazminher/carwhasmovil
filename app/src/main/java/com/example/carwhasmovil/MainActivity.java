package com.example.carwhasmovil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private String uid;
    Button btnCerrarSesion, btnAgregarVehiculos, btnVerVehiculo, btnCotizacion, btnHistorialLabados, btnHistorialAceite, btnPerfilUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCerrarSesion = (Button) findViewById(R.id.tabCerrarSesion);
        btnAgregarVehiculos = (Button) findViewById(R.id.btnAgregarVehiculo);
        btnVerVehiculo = (Button) findViewById(R.id.btnVehiculos);
        btnCotizacion = (Button) findViewById(R.id.btnCotizacion);

        btnHistorialLabados = (Button) findViewById(R.id.btnLavados);
        btnHistorialAceite = (Button) findViewById(R.id.btnAceite);
        btnPerfilUsuario = (Button) findViewById(R.id.btnPerfil);

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });

        btnAgregarVehiculos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityCrearVehiculo.class);
                startActivity(intent);
            }
        });

        btnVerVehiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityVerVehiculo.class);
                startActivity(intent);
            }
        });

        btnCotizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityCotizacion.class);
                startActivity(intent);
            }
        });

        btnHistorialLabados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityHistorialLavados.class);
                startActivity(intent);
            }
        });

        btnHistorialAceite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityHistorialAceite.class);
                startActivity(intent);
            }
        });

        btnPerfilUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ActivityPerfil.class);
                startActivity(intent);
            }
        });


    }

    private void GetUser() {

        mAuth = FirebaseAuth.getInstance();            // Iniciar Firebase
        FirebaseUser user = mAuth.getCurrentUser();     // Obtener Usuario Actual

        try {
            if (user != null) {
                uid = user.getUid(); // Obtener el UID del Usuario Actual
            }
        }
        catch (Exception e) {
            Toast.makeText(this, "Error: "+ e, Toast.LENGTH_LONG).show();
        }
    }
}
