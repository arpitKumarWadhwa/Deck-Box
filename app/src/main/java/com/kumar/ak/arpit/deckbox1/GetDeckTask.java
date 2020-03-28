package com.kumar.ak.arpit.deckbox1;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kumar.ak.arpit.deckbox1.data.DecksContract;
import com.kumar.ak.arpit.deckbox1.data.DecksDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

class GetDeckTask extends AsyncTask<URL, Integer, String> {
    public interface OnTaskCompletedListener {
        void onTaskComplete();
        void onTaskFailed();
    }

    private OnTaskCompletedListener listener;
    private QueryUtils httpHelper = new QueryUtils();
    private DecksDbHelper mDbHelper;

    GetDeckTask(DecksDbHelper mDbHelper){
        this.mDbHelper = mDbHelper;
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener){
        this.listener = listener;
    }

    @Override
    protected String doInBackground(URL... urls) {
        String jsonResponse = "";

        try {
            //Log.e("Splash: ", urls[0].toString());
            jsonResponse = httpHelper.makeHttpRequest(urls[0]);
        } catch (IOException e) {
            listener.onTaskFailed();
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(String jsonResponse) {

        ArrayList<Cards> allCards;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

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
                cv.put(DecksContract.CardsEntry.COLUMN_CARD_CLASS, allCards.get(i).getCardClass());
                //cv.put(DecksContract.CardsEntry.COLUMN_CARD_TEXT, allCards.get(i).getCardText());

                /*JSONObject json = new JSONObject();
                json.put("uniqueArrays", new JSONArray(allCards.get(i).getMechanics()));
                String mechanicsList = json.toString();

                cv.put(DecksContract.CardsEntry.COLUMN_CARD_MECHANICS, mechanicsList);
*/
                long result = db.insert("cards_zhCN", null, cv);

                if (result == -1) {
                    throw new Exception();
                }

            }
            listener.onTaskComplete();
            Log.e("Tas ", "complete");
        } catch (Exception e) {
            listener.onTaskFailed();
        } finally {
            db.close();
        }
    }

    private ArrayList<Cards> parseJson(String jsonResponse) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            //Log.e("jsonEmpty, ", "empty");
            return null;
        }

        //Create an empty ArrayList that we can start adding cards to
        ArrayList<Cards> cards = new ArrayList<>();

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
                String rarityString;
                try {
                    rarityString = cardObject.getString("rarity");
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }

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

                //ArrayList<String> mechanics = new ArrayList<String>();

                String cardClassString = cardObject.getString("cardClass");
                int cardClass = Cards.tellCardClassInInteger(cardClassString);

                /*String cardText;
                try {
                    cardText = cardObject.getString("text");
                    Log.e("fuck", cardText);
                }catch (Exception e){
                    cardText = "NA";
                }


                try {
                    JSONArray mechanicsJSONArray = cardObject.getJSONArray("mechanics");

                    for (int j = 0; j < mechanicsJSONArray.length(); j++) {
                        mechanics.add(mechanicsJSONArray.getString(j));
                    }
                }catch (Exception e){
                    mechanics.add("NA");
                }*/

                Cards tempCard = new Cards(cardID, dbfid, name, cost, rarity, type, 0);
                tempCard.setCardClass(cardClass);
             /*   tempCard.setCardText(cardText);
                tempCard.setMechanics(mechanics);*/

                //Add the new card to the list of cards
                cards.add(tempCard);

            }
        } catch (JSONException e) {
            listener.onTaskFailed();
            e.printStackTrace();
        } finally {
            Log.e("s", String.valueOf(cards.size()));
        }

        return cards;
    }
}


