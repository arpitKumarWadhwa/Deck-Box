package com.kumar.ak.arpit.deckbox1;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kumar.ak.arpit.deckbox1.data.DecksContract;
import com.kumar.ak.arpit.deckbox1.data.DecksDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static android.view.View.GONE;

public class SplashScreenActivity extends AppCompatActivity {

    //private boolean isTaskCompleted = false;

    private static QueryUtils httpHelper = new QueryUtils();
    String CARDS_URL = "https://api.hearthstonejson.com/v1/latest/enUS/cards.collectible.json";
    String CARDS_URL_ruRU = "https://api.hearthstonejson.com/v1/latest/ruRU/cards.collectible.json";
    String CARDS_URL_koKR = "https://api.hearthstonejson.com/v1/latest/koKR/cards.collectible.json";
    String CARDS_URL_zhCN = "https://api.hearthstonejson.com/v1/latest/zhCN/cards.collectible.json";
    String CARDS_URL_zhTW = "https://api.hearthstonejson.com/v1/latest/zhTW/cards.collectible.json";

    //ArrayList<Cards> allCards = new ArrayList<Cards>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

            //setUpDatabase(CARDS_URL);
            setUpDatabase(CARDS_URL_ruRU);
            //setUpDatabase(CARDS_URL_koKR);
            //setUpDatabase(CARDS_URL_zhCN);


       /*     Intent i = new Intent(this, DeckBoxActivity.class);
            startActivity(i);

            finish();*/


    }

    private boolean doesCardsDatabaseExist() {
        DecksDbHelper cardsDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = cardsDbHelper.getReadableDatabase();

        Cursor c = db.query(
                "cards",
                null,
                null,
                null,
                null,
                null,
                null
        );
        if (c.getCount() > 0) {
            c.close();
            //Log.e("Insertion: ", "return");
            return true;
        }

        return false;
    }

    private void setUpDatabase(String stringUrl) {
        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        URL url = httpHelper.createUrl(stringUrl);
        GetDeckTask task = new GetDeckTask(mDbHelper);
        task.setOnTaskCompletedListener(new GetDeckTask.OnTaskCompletedListener() {
            @Override
            public void onTaskComplete() {
                Intent i = new Intent(SplashScreenActivity.this, DeckBoxActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onTaskFailed() {
                finish();
            }
        });

        task.execute(url);
    }

   /*
    private static ArrayList<Cards> parseJson(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            //Log.e("jsonEmpty, ", "empty");
            return null;
        }

        //Create an empty ArrayList that we can start adding cards to
        ArrayList<Cards> cards = new ArrayList<Cards>();

        try {
            // Create a JSONObject from the JSON response string
            JSONArray baseJsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < baseJsonArray.length(); i++) {

                //Get a card object
                JSONObject cardObject = baseJsonArray.getJSONObject(i);

                //Get card ID
                String cardID = cardObject.getString("id");

                //Get dbfid
                int dbfid = cardObject.getInt("dbfId");

                //Get name
                String name = cardObject.getString("name");

                //Get rarity
                String rarityString = cardObject.getString("rarity");

                //Convert rairty to an integer constant
                int rarity = Cards.tellRarityInInteger(rarityString);

                //Get type
                String typeString = cardObject.getString("type");

                //Convert type to an integer constant
                int type = Cards.tellTypeInInteger(typeString);

                int cost;

                try {
                    //Get card cost
                    cost = cardObject.getInt("cost");
                } catch (JSONException e) {
                    cost = -1; //Non valid mana cost for heroes
                }

                Cards tempCard = new Cards(cardID, dbfid, name, cost, rarity, type, 0);

                //Add the new card to the list of cards
                cards.add(tempCard);

            }
        } catch (JSONException e) {
            //Log.e("Main Activity: ", e.toString());
        }

        return cards;
    }

    private class GetDeckTask extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String jsonResponse = "";

            try {
                //Log.e("SPlash: ", urls[0].toString());
                jsonResponse = httpHelper.makeHttpRequest(urls[0]);
            } catch (IOException e) {
                Toast.makeText(SplashScreenActivity.this, "Error connecting to server. Closing app.", Toast.LENGTH_SHORT).show();
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {

            DecksDbHelper mHelper = new DecksDbHelper(SplashScreenActivity.this);
            SQLiteDatabase db = mHelper.getWritableDatabase();

            allCards = parseJson(jsonResponse);

            try {
                //Insert all cards into database
                for (int i = 0; i < allCards.size(); i++) {
                    ContentValues cv = new ContentValues();
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_ID, allCards.get(i).getCardId());
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_DBFID, allCards.get(i).getDbfid());
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_NAME, allCards.get(i).getName());
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_COST, allCards.get(i).getCost());
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_RARITY, allCards.get(i).getRarity());
                    cv.put(DecksContract.CardsEntry.COLUMN_CARD_TYPE, allCards.get(i).getType());

                    long result = 0;

                    result = db.insert(DecksContract.CardsEntry.TABLE_NAME, null, cv);

                    if (result == -1) {
                        //Log.e("Insert Error: ", String.valueOf(result));
                    }

                }
            } catch (Exception e) {
                //Log.e("Insert Error: ", e.toString());
            } finally {
                db.close();
                isCompleted = true;
            }
        }
    }
    */
}
