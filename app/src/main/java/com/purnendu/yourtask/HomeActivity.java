package com.purnendu.yourtask;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import Model.Data;

public class HomeActivity extends AppCompatActivity implements NetworkChangeCallBack {
    private ImageView nothing_found;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter<?, ?> adapter;
    private View createNoteDialogBox;
    private Uri pickedImageUri = null;
    private MyReceiver MyReceiver = null;
    private ProgressDialog progressDialog;
    private UploadTask uploadTask = null;
    private WorkManager workManager;
    private WorkRequest workRequest;

    //opening gallery and selecting image
    /*ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (createNoteDialogBox != null) {
                                ShapeableImageView selectedImage = createNoteDialogBox.findViewById(R.id.selectedImage);
                                selectedImage.setVisibility(View.VISIBLE);
                                selectedImage.setImageURI(uri);
                                pickedImageUri = uri;
                            }
                        }

                    }
                }
            });*/

    ActivityResultLauncher<PickVisualMediaRequest> photoPicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    if (createNoteDialogBox != null) {
                        ShapeableImageView selectedImage = createNoteDialogBox.findViewById(R.id.selectedImage);
                        selectedImage.setVisibility(View.VISIBLE);
                        selectedImage.setImageURI(uri);
                        pickedImageUri = uri;
                    }
                }
                else {
                    Toast.makeText(HomeActivity.this, "Image Pick cancelled", Toast.LENGTH_SHORT).show();
                }
            });




    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //WorkManager for deleting unnecessary images from FirebaseStorage
        workManager = WorkManager.getInstance(getApplicationContext());
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresStorageNotLow(false)
                .build();
        workRequest = new PeriodicWorkRequest.Builder(DeletingImagesWorker.class, 12, TimeUnit.HOURS).setConstraints(constraints).build();
        workManager.enqueue(workRequest);

        //Creating ProgressDialog
        progressDialog = new ProgressDialog(HomeActivity.this, R.style.MyGravity);
        progressDialog.setCancelable(false);
        Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        //Initialising elements
        Toolbar toolbar_home = findViewById(R.id.toolbar_home);
        FloatingActionButton fab_btn = findViewById(R.id.fab_btn);
        nothing_found = findViewById(R.id.nothing_found);

        //Custom ActionBar
        setSupportActionBar(toolbar_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Your Task");

        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        if (mUser == null) {
            Toast.makeText(HomeActivity.this, "User is null", Toast.LENGTH_SHORT).show();
            return;
        }
        String uID = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TaskNotes").child(uID);
        mDatabase.keepSynced(true);

        //Floating action button for creating task
        fab_btn.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
            createNoteDialogBox = inflater.inflate(R.layout.custom_input_field, null);
            alertDialog.setView(createNoteDialogBox);
            Dialog dialog = alertDialog.create();
            Objects.requireNonNull(Objects.requireNonNull(dialog.getWindow())).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.show();
            dialog.setCancelable(false);
            EditText heading, notes;
            ImageButton close;
            ImageView saveButton, imageUpload;
            ShapeableImageView selectedImage;

            //Initialising component of Dialog
            heading = createNoteDialogBox.findViewById(R.id.heading);
            notes = createNoteDialogBox.findViewById(R.id.notes);
            imageUpload = createNoteDialogBox.findViewById(R.id.imageUpload);
            saveButton = createNoteDialogBox.findViewById(R.id.saveButton);
            close = createNoteDialogBox.findViewById(R.id.close);
            selectedImage = createNoteDialogBox.findViewById(R.id.selectedImage);

            //Close button on click
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickedImageUri = null;
                    dialog.dismiss();
                }
            });

            //Selecting image button
            imageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Opening ButtonSheet
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(createNoteDialogBox.getContext());
                    @SuppressLint("InflateParams") View view = LayoutInflater.from(createNoteDialogBox.getContext()).inflate(R.layout.image_upload_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();
                    bottomSheetDialog.setCancelable(true);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);
                    bottomSheetDialog.setDismissWithAnimation(true);

                    //Selecting Image from phone
                    imageCapture(view, bottomSheetDialog, dialog);
                }
            });

            //Saving button
            saveButton.setOnClickListener(v13 -> {
                String mHeading = heading.getText().toString().trim();
                String mNotes = notes.getText().toString().trim();

                //Validating
                if ((TextUtils.isEmpty(mHeading)) && (TextUtils.isEmpty(mNotes))) {
                    pickedImageUri = null;
                    dialog.dismiss();
                    return;
                } else {
                    if ((TextUtils.isEmpty(mHeading))) {
                        heading.setError("Required Field");
                        pickedImageUri = null;
                        return;
                    }
                    if ((TextUtils.isEmpty(mNotes))) {
                        notes.setError("Required Field");
                        pickedImageUri = null;
                        return;
                    }

                }

                //If pickedImageUri is not null
                if ((pickedImageUri != null) && !Uri.EMPTY.equals(pickedImageUri)) {

                    //Checking File size
                    Cursor returnCursor = getContentResolver().query(pickedImageUri, null, null, null, null);
                    int sizeIndex = Objects.requireNonNull(returnCursor).getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    double size = returnCursor.getDouble(sizeIndex);
                    double sizeMb = size / (1024 * 1024);
                    returnCursor.close();
                    if (sizeMb > 1) {
                        Toast.makeText(HomeActivity.this, "File size is too big", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Checking connection
                    if (!NetworkUtility.getConnectivityStatus(HomeActivity.this)) {
                        Toast.makeText(HomeActivity.this, "Connection not available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //Uploading in FirebaseStorage
                    String phNo=mUser.getPhoneNumber();
                    String email=mUser.getEmail();
                    String providerName=(mUser.getProviderData()).get(1).getProviderId();

                    StorageReference reference = null;
                    if(email!=null && providerName.equals("password"))
                    {
                        reference = storageReference.child("images/" + email + "/" + pickedImageUri.getLastPathSegment());
                    }

                    if(email!=null && providerName.equals("google.com"))
                    {
                        reference = storageReference.child("images/" + email + "/" + pickedImageUri.getLastPathSegment());
                    }

                    if(phNo!=null && providerName.equals("phone"))
                    {
                        reference = storageReference.child("images/" + phNo + "/" + pickedImageUri.getLastPathSegment());
                    }
                    if(reference==null)
                        return;
                    progressDialog.show();

                    uploadTask = reference.putFile(pickedImageUri);
                    StorageReference finalReference = reference;
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            finalReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String id = mDatabase.push().getKey();
                                    if (id != null && uri != null && !Uri.EMPTY.equals(uri)) {
                                        Data data = new Data(mHeading, mNotes, ServerValue.TIMESTAMP, id, uri.toString());
                                        mDatabase.child(id).setValue(data);
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                        pickedImageUri = null;
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                    pickedImageUri=null;
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                            pickedImageUri=null;
                        }
                    }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onPaused(@NonNull UploadTask.TaskSnapshot snapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(HomeActivity.this, "Uploading paused and then canceled", Toast.LENGTH_SHORT).show();
                            if (!snapshot.getTask().isSuccessful())
                            {
                                snapshot.getTask().cancel();
                            }
                            dialog.dismiss();
                            pickedImageUri=null;
                        }
                    });
                } else {
                    String id = mDatabase.push().getKey();
                    if (id != null) {
                        Data data = new Data(mHeading, mNotes, ServerValue.TIMESTAMP, id);
                        mDatabase.child(id).setValue(data);
                        dialog.dismiss();
                    }
                }
            });

            selectedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedImage.setVisibility(View.GONE);
                    pickedImageUri = null;
                }
            });
        });


        //Firebase RecyclerView For showing data from Firebase
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        FirebaseRecyclerOptions<DataSnapshot> options = new FirebaseRecyclerOptions.Builder<DataSnapshot>().setQuery(mDatabase.orderByChild("date"),
                new SnapshotParser<DataSnapshot>() {
                    @NonNull
                    public DataSnapshot parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return snapshot;
                    }
                }).build();
        adapter = new FirebaseRecyclerAdapter<DataSnapshot, ViewHolder>(options) {

            @Override
            public void onDataChanged() {

                if (getItemCount() == 0)
                    nothing_found.setVisibility(View.VISIBLE);
                else
                    nothing_found.setVisibility(View.INVISIBLE);

                layoutManager.setReverseLayout(recyclerView.canScrollVertically(1));


                super.onDataChanged();
            }
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull DataSnapshot dataSnapshot) {

                String title = dataSnapshot.child("title").getValue(String.class);
                String note = dataSnapshot.child("note").getValue(String.class);
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                viewHolder.setTitle(title);
                viewHolder.setNotes(note);
                viewHolder.setImage(imageUrl, HomeActivity.this);

                Long time = null;
                if (dataSnapshot.child("date").getValue(Long.class) != null)
                    time = dataSnapshot.child("date").getValue(Long.class);
                if (time != null) {
                    Date date = new Date(time);
                    @SuppressLint("SimpleDateFormat") Format format = new SimpleDateFormat("dd MMM yyyy");
                    viewHolder.setDate(format.format(date));
                }
                String postKey = getRef(i).getKey();
                viewHolder.setPostKey(postKey);

                //OnClick of cardView
                viewHolder.cardView.setOnClickListener(v -> {
                    EditText title_update, notes_update;
                    Button update, delete;

                    //Creating dialog
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                    View myView = inflater.inflate(R.layout.update_field, null);
                    alertDialog.setView(myView);
                    Dialog dialog = alertDialog.create();
                    Objects.requireNonNull(Objects.requireNonNull(dialog.getWindow())).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.show();

                    //Initialising component of dialog
                    title_update = myView.findViewById(R.id.title_update);
                    notes_update = myView.findViewById(R.id.notes_update);
                    update = myView.findViewById(R.id.update);
                    delete = myView.findViewById(R.id.delete);

                    //Setting up things
                    title_update.setText(title);
                    notes_update.setText(note);

                    //Updating
                    update.setOnClickListener(v12 -> {

                        if (postKey == null)
                            return;


                        String mtitle = title_update.getText().toString().trim();
                        String mnote = notes_update.getText().toString().trim();

                        //Validating
                        if (TextUtils.isEmpty(mtitle)) {
                            title_update.setError("Required Field");
                            return;
                        }
                        if (TextUtils.isEmpty(mnote)) {
                            notes_update.setError("Required Field");
                            return;
                        }

                        Data data1;
                        //If there already image
                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            data1 = new Data(mtitle, mnote, ServerValue.TIMESTAMP, postKey, imageUrl);
                        }
                        //If no image there
                        else {
                            data1 = new Data(mtitle, mnote, ServerValue.TIMESTAMP, postKey);
                        }
                        mDatabase.child(postKey).setValue(data1);
                        dialog.dismiss();
                        Toast.makeText(HomeActivity.this, "Note Updated ", Toast.LENGTH_SHORT).show();
                    });

                    //Deleting things
                    delete.setOnClickListener(v1 -> {

                        if (imageUrl != null && !imageUrl.isEmpty()) {

                            //Connection check
                            if (!NetworkUtility.getConnectivityStatus(HomeActivity.this)) {
                                Toast.makeText(HomeActivity.this, "Connection not available", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            //Deleting from FirebaseStorage
                            StorageReference photoRef = storage.getReferenceFromUrl(imageUrl);
                            progressDialog.show();
                            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (postKey == null)
                                        return;
                                    mDatabase.child(postKey).removeValue();
                                    Toast.makeText(HomeActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    progressDialog.dismiss();
                                    int errorCode = ((StorageException) exception).getErrorCode();
                                    if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                        if (postKey == null)
                                            return;
                                        mDatabase.child(postKey).removeValue();
                                        progressDialog.dismiss();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(HomeActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            if (postKey == null)
                                return;
                            mDatabase.child(postKey).removeValue();
                            Toast.makeText(HomeActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

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
        //Receiver for checking Connection
        MyReceiver = new MyReceiver();
        MyReceiver.setCallBack(HomeActivity.this);
        broadcastIntent();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        unregisterReceiver(MyReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu) {
            mAuth.signOut();
            workManager.cancelWorkById(workRequest.getId());
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("About");
            TextView textView = new TextView(this);
            textView.setText(R.string.dialogContent);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            builder.setView(textView);
            builder.setCancelable(true);
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Opening gallery and selecting image
    private void imageCapture(View buttonSheetView, BottomSheetDialog bottomSheetDialog, Dialog dialog) {

        if (buttonSheetView != null && createNoteDialogBox != null) {

            //Initializing  component of ButtonSheet Dialog
            ImageView selectImage = buttonSheetView.findViewById(R.id.selectImage);

            //Selection of image from storage
            selectImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();

                    photoPicker.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            });

        }
    }

    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @Override
    public void whenNotConnected() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
            Toast.makeText(HomeActivity.this, "Connection not available", Toast.LENGTH_SHORT).show();
        }

        if (uploadTask != null) {
            if (uploadTask.isInProgress()) {
                if (!uploadTask.isComplete()) {
                    uploadTask.cancel();
                } else {
                    uploadTask.getSnapshot().getStorage().delete();
                }

            }
        }

    }

    @Override
    public void whenConnected() {
        //Do things when connected
    }
}