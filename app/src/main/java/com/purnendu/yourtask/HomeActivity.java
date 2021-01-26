package com.purnendu.yourtask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import Model.Data;

public class HomeActivity extends AppCompatActivity {
    Toolbar toolbar_home;
    FloatingActionButton fab_btn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    private   FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar_home=findViewById(R.id.toolbar_home);
        fab_btn=findViewById(R.id.fab_btn);
        setSupportActionBar(toolbar_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Your Task");
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        String uID=mUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("TaskNotes").child(uID);
        mDatabase.keepSynced(true);
        fab_btn.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog= new AlertDialog.Builder(HomeActivity.this);
            LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
            View myView= inflater.inflate(R.layout.custom_input_field,null);
            alertDialog.setView(myView);
            Dialog dialog=alertDialog.create();
           dialog.show();
            EditText heading,notes;
            Button save;
            heading=myView.findViewById(R.id.heading);
            notes=myView.findViewById(R.id.notes);
            save=myView.findViewById(R.id.save);
            save.setOnClickListener(v13 -> {
                String mheading=heading.getText().toString().trim();
                String mnotes=notes.getText().toString().trim();
                if(TextUtils.isEmpty(mheading))
                {
                    heading.setError("Required Field");
                    return;
                }
                if(TextUtils.isEmpty(mnotes))
                {
                    notes.setError("Required Field");
                    return;
                }
                String id=mDatabase.push().getKey();
                String date= DateFormat.getDateInstance().format(new Date());
                Data data=new Data(mheading,mnotes,date,id);
                mDatabase.child(id).setValue(data);
                dialog.dismiss();
                Toast.makeText(HomeActivity.this, "Data inserted ", Toast.LENGTH_SHORT).show();

            });


        });


        //For showing data from Firebase
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(HomeActivity.this);
       // recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        FirebaseRecyclerOptions<Data>options=new FirebaseRecyclerOptions.Builder<Data>().setQuery(mDatabase, snapshot -> new Data(Objects.requireNonNull(snapshot.child("title").getValue()).toString(),
                Objects.requireNonNull(snapshot.child("note").getValue()).toString(),
                Objects.requireNonNull(snapshot.child("date").getValue()).toString(),
                Objects.requireNonNull(snapshot.child("id").getValue()).toString())).build();
       adapter=new FirebaseRecyclerAdapter<Data,ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Data data) {
                String title=data.getTitle();
                String note=data.getNote();
                viewHolder.setTitle(title);
                viewHolder.setNotes(note);
                viewHolder.setDate(data.getDate());
                String postKey=getRef(i).getKey();

                viewHolder.cardView.setOnClickListener(v -> {
                    EditText title_update,notes_update;
                    Button update,delete;
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(HomeActivity.this);
                    LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
                    View myView= inflater.inflate(R.layout.update_field,null);
                    alertDialog.setView(myView);
                    Dialog dialog=alertDialog.create();
                    dialog.show();
                    title_update=myView.findViewById(R.id.title_update);
                    notes_update=myView.findViewById(R.id.notes_update);
                    update=myView.findViewById(R.id.update);
                    delete=myView.findViewById(R.id.delete);
                    title_update.setText(title);
                    notes_update.setText(note);
                    update.setOnClickListener(v12 -> {
                        String mtitle=title_update.getText().toString().trim();
                        String mnote=notes_update.getText().toString().trim();
                        if(TextUtils.isEmpty(mtitle))
                        {
                          title_update.setError("Required Field");
                            return;
                        }
                        if(TextUtils.isEmpty(mnote))
                        {
                            notes_update.setError("Required Field");
                            return;
                        }
                        String date= DateFormat.getDateInstance().format(new Date());
                        Data data1 =new Data(mtitle,mnote,date,postKey);
                        mDatabase.child(postKey).setValue(data1);
                        Toast.makeText(HomeActivity.this, "Data inserted ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                    delete.setOnClickListener(v1 -> {
                        mDatabase.child(postKey).removeValue();
                        Toast.makeText(HomeActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });


                });
            }
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_card_view, parent, false);
                return new ViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
}
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu)
        {
            mAuth.signOut();
            Intent intent=new Intent(HomeActivity.this,RegistrationActivity.class);
            finish();
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
        super.onBackPressed();
    }
}