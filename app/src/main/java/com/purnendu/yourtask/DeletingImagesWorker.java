package com.purnendu.yourtask;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Objects;
import Model.DataForDeserializing;


public class DeletingImagesWorker extends Worker {

    private static final String FailureDeleting = "Failure";
    private static final String Success = "Successfully deleted";
    Context context;
    WorkerParameters workerParameters;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private StorageReference storageReference;
    FirebaseStorage storage;
    private DatabaseReference reference;


    public DeletingImagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.workerParameters = workerParams;
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser != null && (Objects.requireNonNull(mUser).isEmailVerified())) {
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference().child("images/" + mUser.getEmail() + "/");
            reference = FirebaseDatabase.getInstance().getReference().child("TaskNotes").child(mUser.getUid());
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        deletingUnnecessaryImageFromStorage();
        return Result.success();
    }

    private void deletingUnnecessaryImageFromStorage() {

        ArrayList<String> imageUrlList = new ArrayList<>();
        if (storageReference == null)
            return;
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrlList.add(uri.toString());
                        }
                    });
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (imageUrlList.isEmpty())
                            return;
                        removingImagesWhichIsNotPresent(imageUrlList);
                    }
                }, 60000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("FailureDeleting", "onFailure: " + "with exception " + e);
            }
        });
    }

    private void removingImagesWhichIsNotPresent(ArrayList<String> imageUrlList) {

        if (reference == null)
            return;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DataForDeserializing data = snapshot.getValue(DataForDeserializing.class);
                    if (data != null) {
                        imageUrlList.remove(data.getImageUrl());
                    }
                }

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (imageUrlList.isEmpty())
                            return;
                        deletingFromFirebaseStorage(imageUrlList);
                    }
                }, 60000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.d(FailureDeleting, "onFailure: " + "with exception " + error.getMessage());
            }

        });
    }

    private void deletingFromFirebaseStorage(ArrayList<String> imageUrlList) {

        if (storage == null)
            return;
        for (String imageUrl : imageUrlList) {
            storage.getReferenceFromUrl(imageUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(Success, "onSuccess: Deleted successfully");
                }
            });
        }
        imageUrlList.clear();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.d("onStopped", "onStopped: OnStoppedCalled");
    }
}
