package com.kumar.ak.arpit.deckbox1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DeckListAdapter extends ArrayAdapter<Cards>{

    ArrayList<Cards> res;
    Context context;

    public DeckListAdapter(@NonNull Context context, ArrayList<Cards> deckList) {
        super(context, R.layout.layout1, deckList);
        res = deckList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.layout1, parent, false);
        }

            TextView manaCostTextView = listItemView.findViewById(R.id.mana_cost);
            String cost = String.valueOf(res.get(position).getCost());

            //Log.e("rarity: ", String.valueOf(res.get(position).getRarity()));

            switch (res.get(position).getRarity()) {
                case 1:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorCommon));
                    break;
                case 2:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorRare));
                    break;
                case 3:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorEpic));
                    break;
                case 4:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorLegendary));
                    break;
                default:
                    manaCostTextView.setBackgroundColor(parent.getResources().getColor(R.color.colorCommon));
            }

            //Adjusting the cost to the center of the mana cost box
            if (cost.length() == 1) {
                cost = " " + cost;
            }

            manaCostTextView.setText(cost);

            TextView nameTextView = listItemView.findViewById(R.id.card_name);
            nameTextView.setText(res.get(position).getName());

            TextView quantityTextView = listItemView.findViewById(R.id.card_quantity);
            quantityTextView.setText(String.valueOf(res.get(position).getQuantity()));


            final ImageView cardTileImageView = listItemView.findViewById(R.id.card_tile);
            GlideApp.with(context).load(res.get(position).getCardTileUrl()).into(cardTileImageView);

        return listItemView;
    }

}

