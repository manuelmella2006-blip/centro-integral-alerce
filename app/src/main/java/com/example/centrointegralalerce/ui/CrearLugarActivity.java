package com.example.centrointegralalerce.ui;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CrearLugarActivity extends AppCompatActivity {

    private EditText etNombreLugar;
    private Button btnGuardarLugar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_lugar);

        etNombreLugar = findViewById(R.id.etNombreLugar);
        btnGuardarLugar = findViewById(R.id.btnGuardarLugar);
        db = FirebaseFirestore.getInstance();

        btnGuardarLugar.setOnClickListener(v -> {
            String nombre = etNombreLugar.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingrese un nombre", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> lugar = new HashMap<>();
            lugar.put("nombre", nombre);
            lugar.put("activo", true);

            db.collection("lugares").add(lugar)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "Lugar agregado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }
}
