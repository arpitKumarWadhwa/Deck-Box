package com.kumar.ak.arpit.deckbox1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kumar.ak.arpit.deckbox1.data.DecksContract;
import com.kumar.ak.arpit.deckbox1.data.DecksDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeckListActivity extends AppCompatActivity {

    private int deckId; //Id of deck as in database
    DeckDecoder deckDecoder = new DeckDecoder();
    QueryUtils httpHelper = new QueryUtils();
    ListView deckListListView;
    DeckListAdapter adapter;

    ArrayList<Cards> deckList = new ArrayList<Cards>();

    //Create Deck List Manager
    DeckListManager dlm = new DeckListManager();

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        deckId = getIntent().getIntExtra("deckId", -1);
        String deckCode = getIntent().getStringExtra("deckString");
        String deckName = getIntent().getStringExtra("deckName");
        int deckPosition = getIntent().getIntExtra("deckPosition", 0);

        toolbar.setTitle(deckName);
        setSupportActionBar(toolbar);

        dlm.setDeckString(deckCode);
        dlm.setDeckName(deckName);
        dlm.setId(deckId);

        try {
            ArrayList<ArrayList<Integer>> dbfIdList;
            dbfIdList = deckDecoder.decode(deckCode);

            //Find all 1-quantity cards by their dbfIds
            ArrayList<Integer> dbf1 = dbfIdList.get(0);
            //Log.e("1-quantity cards: ", String.valueOf(dbf1.size()));
            for (int i = 0; i < dbf1.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(DeckListActivity.this, dbf1.get(i));
                tempCard.setQuantity(1);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }

            //Find all 2-quantity cards by their dbfIds
            ArrayList<Integer> dbf2 = dbfIdList.get(1);
            //Log.e("2-quantity cards: ", String.valueOf(dbf2.size()));
            for (int i = 0; i < dbf2.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(DeckListActivity.this, dbf2.get(i));
                tempCard.setQuantity(2);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }
        } catch (IOException e) {
            //Log.e("Deck Decoding Error: ", e.toString());
        }

        /*
        DownloadImageTask task = new DownloadImageTask();
        task.execute(deckList);
        task.setOnTaskCompleteListener(new DownloadImageTask.OnTaskCompleteListener() {
            @Override
            public void onTaskComplete(ArrayList<Cards> cardsList) {
                //Sort deck by mana cost
                cardsList = dlm.sortByMana(deckList);

                ListView deckListListView = findViewById(R.id.deck_list);
                DeckListAdapter adapter = new DeckListAdapter(DeckListActivity.this, cardsList);
                deckListListView.setAdapter(adapter);
            }
        });
        */

        //Sort deck by mana cost
        deckList = dlm.sortByMana(deckList);
        deckList = dlm.sortLexicographically(deckList);

        deckListListView = findViewById(R.id.deck_list);
        adapter = new DeckListAdapter(DeckListActivity.this, deckList);
        deckListListView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deck_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Delete Deck
        if (id == R.id.action_delete) {
            DecksDbHelper mDbHelper = new DecksDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String selection = DecksContract.DecksEntry._ID + "=?";
            String selectionArgs[] = new String[]{String.valueOf(deckId)};

            Log.e("List:, ", String.valueOf(deckId));

            int r = db.delete(
                    DecksContract.DecksEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            Log.e("Deleted: ", String.valueOf(r));

            db.close();

            finish();
        } else if (id == R.id.action_copy) {

            try {
                ClipboardManager myClipboard;
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip;
                String text = dlm.getDeckString();
                String annotatedDeckString = getAnnotatedDeckString(text);
                myClip = ClipData.newPlainText("text", annotatedDeckString);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(this, "Deck copied to clipboard", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Deck could not be copied", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_edit_deck_name) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View v = this.getLayoutInflater().inflate(R.layout.edit_deck_name_dialog, null);
            builder.setView(v);
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText editText = v.findViewById(R.id.new_name);
                    String newDeckName = editText.getText().toString();
                    updateName(newDeckName);  //Updates the deck name in the database
                    toolbar.setTitle(newDeckName);
                    setSupportActionBar(toolbar);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.setCancelable(false);

            builder.create();
            builder.show();
        }
        else if(id == R.id.action_share_deck){
            shareDeck();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateName(String newName) {
        DecksDbHelper mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = DecksContract.DecksEntry._ID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(deckId)};

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_NAME, newName);

        int r = db.update(
                DecksContract.DecksEntry.TABLE_NAME,
                cv,
                selection,
                selectionArgs
        );

        //Log.e("Update: ", String.valueOf(r));
    }

    public String getAnnotatedDeckString(String deckString) {
        StringBuilder builder = new StringBuilder();
        DeckDecoder dd = new DeckDecoder();
        String deckName = dlm.getDeckName();
        String playableClass = dd.getPlayableClass(deckString);
        String format = dd.getFormat(deckString);

        //Header
        builder.append("### ");
        builder.append(deckName);
        builder.append("\n");
        builder.append("# ");
        builder.append("Class: ");
        builder.append(playableClass);
        builder.append("\n");
        builder.append("# ");
        builder.append("Format: ");
        builder.append(format);
        builder.append("\n");
        builder.append("#");
        builder.append("\n");

        //Generate the deck list
        for (int i = 0; i < deckList.size(); i++) {
            builder.append("# ");
            builder.append(String.valueOf(deckList.get(i).getQuantity()));
            builder.append("x ");
            builder.append("(");
            builder.append(String.valueOf(deckList.get(i).getCost()));
            builder.append(") ");
            builder.append(deckList.get(i).getName());
            builder.append("\n");
        }
        builder.append("#");
        builder.append("\n");

        //Add the deck string
        builder.append(deckString.trim());
        builder.append("\n");

        builder.append("#");
        builder.append("\n");

        //Add Note
        builder.append("# ");
        builder.append("To use this deck, copy it to your clipboard and create a new deck in Hearthstone");
        builder.append("\n");

        //Credits
        builder.append("#");
        builder.append("\n");
        builder.append("# ");
        builder.append("Generated by Deck Box");

        return builder.toString();
    }

    public void shareDeck() {

        String deck = getAnnotatedDeckString(dlm.getDeckString());

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        //newIntent.setPackage("com.whatsapp");
        newIntent.putExtra(Intent.EXTRA_TEXT, deck);

        startActivity(newIntent);
    }
}
