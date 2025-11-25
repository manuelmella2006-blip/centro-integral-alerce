// GestionUsuariosActivity.java
package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.data.Usuario;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GestionUsuariosActivity extends AppCompatActivity {

    private static final String TAG = "GestionUsuariosActivity";
    private boolean usuariosCargados = false; // Bandera para controlar carga duplicada

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAgregarUsuario;
    private UsuarioAdapter usuarioAdapter;
    private List<Usuario> listaUsuarios;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_usuarios);

        // Verificar permisos
        if (!UserSession.getInstance().puede("gestionar_usuarios")) {
            AlertManager.showErrorToast(this, "❌ No tienes permisos para gestionar usuarios");
            finish();
            return;
        }

        initViews();
        setupFirebase();
        cargarUsuarios(); // Carga inicial
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestión de Usuarios");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerViewUsuarios);
        progressBar = findViewById(R.id.progressBar);
        fabAgregarUsuario = findViewById(R.id.fabAgregarUsuario);

        listaUsuarios = new ArrayList<>();
        usuarioAdapter = new UsuarioAdapter(listaUsuarios, this::onUsuarioClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(usuarioAdapter);

        fabAgregarUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(GestionUsuariosActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void cargarUsuarios() {
        progressBar.setVisibility(View.VISIBLE);
        listaUsuarios.clear();

        db.collection("usuarios")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    usuariosCargados = true; // Marcar como cargados

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Usuario usuario = document.toObject(Usuario.class);
                            usuario.setUid(document.getId()); // Guardar el ID del documento
                            listaUsuarios.add(usuario);
                        }
                        usuarioAdapter.notifyDataSetChanged();

                        if (listaUsuarios.isEmpty()) {
                            AlertManager.showInfoSnackbar(
                                    AlertManager.getRootView(this),
                                    "No hay usuarios registrados"
                            );
                        }
                    } else {
                        Log.e(TAG, "Error cargando usuarios: ", task.getException());
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "Error al cargar usuarios: " + task.getException().getMessage()
                        );
                    }
                });
    }

    private void onUsuarioClick(Usuario usuario) {
        // Mostrar opciones: Editar o Eliminar
        CharSequence[] opciones = {"Editar Usuario", "Eliminar Usuario", "Cancelar"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Opciones para: " + usuario.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar
                            editarUsuario(usuario);
                            break;
                        case 1: // Eliminar
                            eliminarUsuario(usuario);
                            break;
                        case 2: // Cancelar
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void editarUsuario(Usuario usuario) {
        Intent intent = new Intent(this, EditarUsuarioActivity.class);
        intent.putExtra("USUARIO_UID", usuario.getUid());
        intent.putExtra("USUARIO_NOMBRE", usuario.getNombre());
        intent.putExtra("USUARIO_EMAIL", usuario.getEmail());
        intent.putExtra("USUARIO_ROL", usuario.getRolId());
        startActivity(intent);
    }

    private void eliminarUsuario(Usuario usuario) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar al usuario: " + usuario.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    eliminarUsuarioConfirmado(usuario);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuarioConfirmado(Usuario usuario) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("usuarios").document(usuario.getUid())
                .delete()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        AlertManager.showSuccessSnackbar(
                                AlertManager.getRootView(this),
                                "Usuario eliminado correctamente"
                        );
                        cargarUsuarios(); // Recargar la lista
                    } else {
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "Error al eliminar usuario: " + task.getException().getMessage()
                        );
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Solo recargar si ya se cargaron los usuarios previamente
        // Esto evita la carga duplicada cuando la actividad se crea por primera vez
        if (usuariosCargados) {
            cargarUsuarios();
        }
    }
}