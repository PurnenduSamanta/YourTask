package com.purnendu.yourtask;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import Model.Data;

public class HomeActivity extends AppCompatActivity {
    Toolbar toolbar_home;
    FloatingActionButton fab_btn;
    ImageView nothing_found;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private   FirebaseRecyclerAdapter<?,?> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar_home=findViewById(R.id.toolbar_home);
        fab_btn=findViewById(R.id.fab_btn);
        nothing_found=findViewById(R.id.nothing_found);
        setSupportActionBar(toolbar_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Your Task");
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser=mAuth.getCurrentUser();
        if(mUser==null)
        {
            Toast.makeText(HomeActivity.this, "User is null", Toast.LENGTH_SHORT).show();
            return;
        }
        String uID=mUser.getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("TaskNotes").child(uID);
        mDatabase.keepSynced(true);
        fab_btn.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog= new AlertDialog.Builder(HomeActivity.this);
            LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
            View myView= inflater.inflate(R.layout.custom_input_field,null);
            alertDialog.setView(myView);
            Dialog dialog=alertDialog.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            dialog.setCancelable(false);
            EditText heading,notes;
            ImageButton saveButton,close;
            heading=myView.findViewById(R.id.heading);
            notes=myView.findViewById(R.id.notes);
            saveButton=myView.findViewById(R.id.saveButton);
            close=myView.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            saveButton.setOnClickListener(v13 -> {
                String mheading=heading.getText().toString().trim();
                String mnotes=notes.getText().toString().trim();
                if((TextUtils.isEmpty(mheading))&& (TextUtils.isEmpty(mnotes)))
                {
                    dialog.dismiss();
                    return;
                }
                else
                {
                    if((TextUtils.isEmpty(mheading)))
                    {
                        heading.setError("Required Field");
                        return;
                    }
                    if((TextUtils.isEmpty(mnotes)))
                    {
                        notes.setError("Required Field");
                        return;
                    }

                }
                String id=mDatabase.push().getKey();
                if(id!=null)
                {
                    Data data=new Data(mheading,mnotes, ServerValue.TIMESTAMP,id);
                    mDatabase.child(id).setValue(data);
                    dialog.dismiss();
                    Toast.makeText(HomeActivity.this, "Inserted", Toast.LENGTH_SHORT).show();
                }
            });


        });


        //For showing data from Firebase
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(HomeActivity.this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        FirebaseRecyclerOptions<DataSnapshot>options=new FirebaseRecyclerOptions.Builder<DataSnapshot>().setQuery(mDatabase.orderByChild("date"),
                new SnapshotParser<DataSnapshot>() {
                    @NonNull
                    public DataSnapshot parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return snapshot;
                    }
                }).build();
        adapter=new FirebaseRecyclerAdapter<DataSnapshot, ViewHolder>(options) {

           @Override
           public void onDataChanged() {
                if(getItemCount()==0)
                    nothing_found.setVisibility(View.VISIBLE);
                else
                    nothing_found.setVisibility(View.INVISIBLE);
               super.onDataChanged();
           }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull DataSnapshot dataSnapshot) {

                String title=dataSnapshot.child("title").getValue(String.class);
                String note=dataSnapshot.child("note").getValue(String.class);
                long time=0;
                 if(dataSnapshot.child("date").getValue(Long.class)!=null)
                     time =dataSnapshot.child("date").getValue(Long.class);
                viewHolder.setTitle(title);
                viewHolder.setNotes(note);
                Date date = new Date(time);
                @SuppressLint("SimpleDateFormat") Format format = new SimpleDateFormat("dd MMM yyyy");
                viewHolder.setDate(format.format(date));

                String postKey=getRef(i).getKey();

                viewHolder.cardView.setOnClickListener(v -> {
                    EditText title_update,notes_update;
                    Button update,delete;
                    AlertDialog.Builder alertDialog= new AlertDialog.Builder(HomeActivity.this);
                    LayoutInflater inflater=LayoutInflater.from(HomeActivity.this);
                    View myView= inflater.inflate(R.layout.update_field,null);
                    alertDialog.setView(myView);
                    Dialog dialog=alertDialog.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
                        // String date= DateFormat.getDateInstance().format(new Date());
                        Data data1 =new Data(mtitle,mnote,ServerValue.TIMESTAMP,postKey);
                        assert postKey != null;
                        mDatabase.child(postKey).setValue(data1);
                        Toast.makeText(HomeActivity.this, "Data Updated ", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    });
                    delete.setOnClickListener(v1 -> {
                        assert postKey != null;
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
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu)
        {
            mAuth.signOut();
            Intent intent=new Intent(HomeActivity.this,RegistrationActivity.class);
            finish();
            startActivity(intent);
            return true;
        }

        else if(item.getItemId()==R.id.about)
        {
            AlertDialog.Builder builder =new AlertDialog.Builder(this);
            builder.setTitle("About");
            TextView textView=new TextView(this);
            textView.setText(R.string.dialogContent);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            builder.setView(textView);
            builder.setCancelable(true);
            builder.show();
            return true;

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