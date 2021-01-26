package com.purnendu.yourtask;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public TextView title,notes,date;
    public CardView cardView;

    public ViewHolder(View itemView) {
        super(itemView);
        title=itemView.findViewById(R.id.title);
        notes=itemView.findViewById(R.id.notes);
        date=itemView.findViewById(R.id.date);
        cardView=itemView.findViewById(R.id.cardView);
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
}