package com.danieldonato.olxclone.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.danieldonato.olxclone.R;
import com.danieldonato.olxclone.adapter.AdapterAnuncios;
import com.danieldonato.olxclone.helper.ConfiguracaoFirebase;
import com.danieldonato.olxclone.helper.RecyclerItemClickListener;
import com.danieldonato.olxclone.model.Anuncio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autentcacao;
    private List<Anuncio> anuncios = new ArrayList<>();
    private RecyclerView recyclerAnunciosPublicos;
    private Button buttonRegiao, ButtonCategoria;
    private AdapterAnuncios adapterAnuncios;
    private DatabaseReference anunciosPublicosRef;
    private AlertDialog alertDialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);
        inicializarComponentes();

        autentcacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios");

        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(anuncios, this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);

        recuperarAnunciosPublicos();

        recyclerAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Anuncio anuncio = anuncios.get(position);
                                Intent i = new Intent(AnunciosActivity.this, DetalhesProdutoActivity.class);
                                i.putExtra("anuncioSelecionado", anuncio);
                                startActivity(i);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            }
                        }
                ));
    }

    public void filtrarPorEstado(View view) {
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione o estado desejado");

        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        final Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.estados);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, estados
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapter);
        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filtroEstado = spinnerEstado.getSelectedItem().toString();
                recuperarAnunciosPorEstado();
                filtrandoPorEstado = true;
            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    public void recuperarAnunciosPorEstado() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        alertDialog.show();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot categoria : dataSnapshot.getChildren()) {
                    for(DataSnapshot ds : categoria.getChildren()) {
                        Anuncio anuncio = ds.getValue(Anuncio.class);
                        anuncios.add(anuncio);
                    }
                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void filtrarPorCategoria(View view) {
        if(!filtrandoPorEstado) {
            Toast.makeText(this, "Escolha primeiro um filtro por estado",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
        dialogEstado.setTitle("Selecione a categoria desejado");

        View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);

        final Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);
        String[] estados = getResources().getStringArray(R.array.categorias);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, estados
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
        dialogEstado.setView(viewSpinner);

        dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                recuperarAnunciosPorCategoria();
            }
        });

        dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = dialogEstado.create();
        dialog.show();
    }

    public void recuperarAnunciosPorCategoria() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        alertDialog.show();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria);
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anuncios.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Anuncio anuncio = ds.getValue(Anuncio.class);
                    anuncios.add(anuncio);
                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void recuperarAnunciosPublicos() {
        alertDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anúncios")
                .setCancelable(false)
                .build();
        alertDialog.show();
        anuncios.clear();
        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot estados : dataSnapshot.getChildren()) {
                    for(DataSnapshot categoria : estados.getChildren()) {
                        for(DataSnapshot ds : categoria.getChildren()) {
                            Anuncio anuncio = ds.getValue(Anuncio.class);
                            anuncios.add(anuncio);
                        }
                    }
                }
                Collections.reverse(anuncios);
                adapterAnuncios.notifyDataSetChanged();
                alertDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //chamado apenas uma vez para montar o menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) { //chamado toda vez que o menu for aparecer
        if( autentcacao.getCurrentUser() == null ){
            menu.setGroupVisible(R.id.group_deslogado, true);
        }else {
            menu.setGroupVisible(R.id.gruop_logado, true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_cadastrar:
                startActivity(new Intent(getApplicationContext(), CadastroActivity.class));
                break;
            case R.id.menu_sair:
                autentcacao.signOut();
                invalidateOptionsMenu(); //vai recarregar os menus
                break;
            case R.id.menu_anuncios:
                startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes() {
        recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);
    }
}
