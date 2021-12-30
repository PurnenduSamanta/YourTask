package com.purnendu.yourtask;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView title, notes, date;
    public CardView cardView;
    public ImageView image;
    private String postKey;
    private DatabaseReference mDatabase;

    public ViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        notes = itemView.findViewById(R.id.notes);
        date = itemView.findViewById(R.id.date);
        cardView = itemView.findViewById(R.id.cardView);
        image = itemView.findViewById(R.id.image);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        if (mUser == null)
            return;
        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TaskNotes").child(uId);
    }

    public void setTitle(String string) {
        title.setText(string);
    }


    public void setNotes(String string) {
        notes.setText(string);
    }

    public void setDate(String string) {
        date.setText(string);
    }

    public void setImage(String imageUrl, Context context) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            image.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {

                            if (Objects.requireNonNull(e.getMessage()).equals("HTTP 403")) {
                                if (postKey == null)
                                    return;
                                if (postKey.isEmpty())
                                    return;
                                mDatabase.child(postKey).removeValue();
                            } else {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            image.setVisibility(View.GONE);
                        }
                    });
        } else {
            image.setVisibility(View.GONE);
        }

    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }
}