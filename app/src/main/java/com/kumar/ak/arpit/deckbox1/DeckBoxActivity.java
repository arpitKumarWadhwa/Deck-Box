package com.kumar.ak.arpit.deckbox1;

import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.kumar.ak.arpit.deckbox1.data.DecksContract;
import com.kumar.ak.arpit.deckbox1.data.DecksDbHelper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

//TODO: Remember the user's format preference by storing it in SharedPreferences.


//This activity load a eck every time a change is made in the list of decks.
//The decks are filtered out using a filterIconPointer which keeps cycling
//between the values 0-2 depending on user filter choice.
public class DeckBoxActivity extends AppCompatActivity {

    ClipboardManager myClipboard;
    String deckString;
    DecksDbHelper mDbHelper = new DecksDbHelper(this);
    static ArrayList<DeckListManager> decks; //Manages all the decks stored by user
    ArrayList<DeckListManager> filteredDecks; //Filtered decks based on user's search query
    ListView deckBoxListView; //The ListView which displays all the decks saved by user
    DecksAdapter adapter;
    DeckDecoder deckDecoder = new DeckDecoder();

    //A variable selectedDeckPosition is used to remember which deck the user
    //was on when going into the DeckListActivity. This information is used to
    //scroll back to that deck when returning back to this activity.
    //This is handled in onResume().
    int selectedDeckPosition = 0;

    //Dialog for updating the app
    AlertDialog.Builder builder;
    AlertDialog updateDialog;

    //These are the icons which allow the user to filter the decks based
    //on format. The filterIconPointer is used to cycle through the filterIcons[].
    int[] filterIcons = new int[]{R.drawable.all_icon, R.drawable.standard_icon, R.drawable.wild_icon};
    static int filterIconPointer;

    //Handles the increment and cycling of filterIconPointer
    void incrementFilterIconPointer() {
        filterIconPointer = (filterIconPointer + 1) % 3;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_box);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        filterIconPointer = 0;

        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        decks = loadDecks(filterIconPointer); //Load all saved decks

        deckBoxListView = findViewById(R.id.deck_box);
        adapter = new DecksAdapter(this, decks);
        deckBoxListView.setAdapter(adapter);

        if (selectedDeckPosition >= deckBoxListView.getCount()) {
            selectedDeckPosition = deckBoxListView.getCount() - 1;
        }
        deckBoxListView.setSelection(selectedDeckPosition);

        registerForContextMenu(deckBoxListView);

        deckBoxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeckListManager deck = (DeckListManager) parent.getItemAtPosition(position);
                String deckString = deck.getDeckString();

                Intent i = new Intent(getBaseContext(), DeckListActivity.class);
                i.putExtra("deckId", deck.getId());
                i.putExtra("deckString", deckString);
                i.putExtra("deckName", deck.getDeckName());
                //Save the selected deck position in the list view so that it can be scrolled back to on return
                selectedDeckPosition = position;
                startActivity(i);
            }
        });

        //Get the deck string from the clipboard
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ClipData data = myClipboard.getPrimaryClip();
                    ClipData.Item item = data.getItemAt(0);
                    Log.e("DeckBoxActivity: ", String.valueOf(data.getItemCount()));
                    deckString = item.getText().toString();

                    DeckListManager newDeck = insertDeck(deckString);

                    if (newDeck != null) {
                        adapter.add(newDeck);
                        adapter.notifyDataSetChanged();
                        deckBoxListView.setSelection(deckBoxListView.getCount() - 1);
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    //Deck Searching is also implemented here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deck_box, menu);
        final MenuItem filterItem = menu.findItem(R.id.action_filter_by_format);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItem.setIcon(filterIcons[filterIconPointer]);
                if (newText.isEmpty()) {
                    adapter.clear();
                    decks = loadDecks(filterIconPointer);
                    adapter.addAll(decks);
                    adapter.notifyDataSetChanged();
                    return true;
                } else {
                    decks = loadDecks(filterIconPointer);
                    handleSearch(newText);
                    adapter.clear();
                    adapter.addAll(filteredDecks);
                    adapter.notifyDataSetChanged();
                    decks = loadDecks(filterIconPointer);
                    return true;
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Delete All Decks
        if (id == R.id.action_delete_all) {
            DecksDbHelper mDbHelper = new DecksDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            db.delete(
                    DecksContract.DecksEntry.TABLE_NAME,
                    null,
                    null
            );

            db.close();

            adapter.clear();
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_share_all_decks) {
            shareAllDecks();
        } else if (id == R.id.action_import_all_decks) {
            importAllDecksFromClipboard();
        } else if (id == R.id.action_update) {
            builder = new AlertDialog.Builder(this);
            View v = this.getLayoutInflater().inflate(R.layout.update_dialog, null);
            builder.setView(v);
            updateDialog = builder.create();
            updateDialog.setCancelable(false);
            updateDialog.show();
            updateApp();
        } else if (id == R.id.action_filter_by_format) {
            incrementFilterIconPointer();
            item.setIcon(filterIcons[filterIconPointer]);

            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_deck_box, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_deck_name) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            final View v = this.getLayoutInflater().inflate(R.layout.edit_deck_name_dialog, null);
            builder.setView(v);
            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText editText = v.findViewById(R.id.new_name);
                    String newDeckName = editText.getText().toString();
                    updateName(newDeckName, adapter.getItem(position).getId());  //Updates the deck name in the database

                    adapter.clear();
                    decks = loadDecks(filterIconPointer);
                    adapter.addAll(decks);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.setCancelable(false);

            builder.create();
            builder.show();
        } else if (id == R.id.action_copy) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DeckListManager dlm = adapter.getItem(position);
            try {
                ClipboardManager myClipboard;
                myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                ClipData myClip;
                String annotatedDeckString = getAnnotatedDeckString(dlm);
                myClip = ClipData.newPlainText("text", annotatedDeckString);
                myClipboard.setPrimaryClip(myClip);

                Toast.makeText(this, "Deck copied to clipboard", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Deck could not be copied", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_delete) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DecksDbHelper mDbHelper = new DecksDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String selection = DecksContract.DecksEntry._ID + "=?";
            String selectionArgs[] = new String[]{String.valueOf(adapter.getItem(position).getId())};

            int r = db.delete(
                    DecksContract.DecksEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );

            //Log.e("Deleted: ", String.valueOf(r));

            db.close();
            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_share_deck) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            final int position = info.position;

            DeckListManager dlm = adapter.getItem(position);
            shareDeck(dlm);
        }
        return super.onContextItemSelected(item);
    }

    //Inserts the deck in the database and returns the same deck back as a DeckListManager object.
    public DeckListManager insertDeck(String deckString) {
        String deckName = deckDecoder.getDeckName(deckString);

        deckString = deckDecoder.prepareDeckString(deckString);
        mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String format = deckDecoder.getFormat(deckString);
        String playableClass = deckDecoder.getPlayableClass(deckString);


        if (deckName == null) {
            deckName = playableClass + " Deck"; //eg. If class is Rogue, and string doesn't contain deck name then deck name will be "Rogue Deck"
        }


        if (format == "Invalid Format") {
            return null;
        }

        DeckListManager deck = new DeckListManager(deckString);
        deck.setPlayableClass(playableClass);
        deck.setFormat(format);
        deck.setDeckName(deckName);

        ContentValues cv = new ContentValues();
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_STRING, deckString);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_FORMAT, format);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_CLASS, playableClass);
        cv.put(DecksContract.DecksEntry.COLUMN_DECK_NAME, deckName);
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_0_COST_CARDS, deck.getNoOf0CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_1_COST_CARDS, deck.getNoOf1CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_2_COST_CARDS, deck.getNoOf2CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_3_COST_CARDS, deck.getNoOf3CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_4_COST_CARDS, deck.getNoOf4CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_5_COST_CARDS, deck.getNoOf5CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_6_COST_CARDS, deck.getNoOf6CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_7_COST_CARDS, deck.getNoOf7CostCards());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_MINIONS, deck.getNoOfMinions());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_SPELLS, deck.getNoOfSpells());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_WEAPONS, deck.getNoOfWeapons());
        cv.put(DecksContract.DecksEntry.COLUMN_NO_OF_HEROES, deck.getNoOfHeroes());

        long result = db.insert(DecksContract.DecksEntry.TABLE_NAME, null, cv);

        if (result == -1) {
            Log.e("DeckBoxActivity: ", "Deck Insertion Error");
        }

        db.close();

        db = mDbHelper.getReadableDatabase();

        Cursor c = db.query(
                DecksContract.DecksEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        c.moveToLast();
        deck.setId(c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID)));

        c.close();
        db.close();

        return deck;
    }

    //Loads all the decks from the database based on the user's format prefernce
    public ArrayList<DeckListManager> loadDecks(int filterIconPointer) {
        ArrayList<DeckListManager> decks = new ArrayList<DeckListManager>();

        mDbHelper = new DecksDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String format = "";

        switch (filterIconPointer) {
            case 0: //All Decks
                format = "All";
                break;
            case 1: //Standard Format
                format = DecksContract.DecksEntry.FORMAT_STANDARD;
                break;
            case 2: //Wild Format
                format = DecksContract.DecksEntry.FORMAT_WILD;
                break;
        }

        String selection = DecksContract.DecksEntry.COLUMN_DECK_FORMAT + "=?";
        String selectionArgs[] = new String[1];

        if(format.equals("All")){
            selectionArgs = null;
            selection = null;
        }else{
            selectionArgs[0] = format;
        }

        Cursor c = db.query(
                DecksContract.DecksEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (c.getCount() == 0)
            return decks;
        else {
            while (c.moveToNext()) {
                DeckListManager deck = new DeckListManager(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_STRING)));
                deck.setFormat(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_FORMAT)));
                deck.setPlayableClass(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_CLASS)));
                deck.setId(c.getInt(c.getColumnIndex(DecksContract.DecksEntry._ID)));
                deck.setDeckName(c.getString(c.getColumnIndex(DecksContract.DecksEntry.COLUMN_DECK_NAME)));

                decks.add(deck);
            }

            c.close();
            db.close();

            return decks;
        }
    }

    //Shares all decks as a String to whatsapp/email, etc
    //Can be accessed through Options Menu
    public void shareAllDecks() {
        StringBuilder share = new StringBuilder();

        for (int i = 0; i < decks.size(); i++) {
            share.append("### "); //Indicates the line where Deck Name is written
            share.append(decks.get(i).getDeckName());
            share.append("\n"); //New Line
            share.append(decks.get(i).getDeckString());
            share.append("\n\n"); //2 New Lines
        }

        String allDecks = share.toString().trim();

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        //newIntent.setPackage("com.whatsapp");
        newIntent.putExtra(Intent.EXTRA_TEXT, allDecks);

        startActivity(newIntent);
    }

    //Shares a single deck to whatsapp/email, etc.
    //Can be accessed through Context Menu
    public void shareDeck(DeckListManager dlm) {


        String deck = getAnnotatedDeckString(dlm);

        Intent newIntent = new Intent(Intent.ACTION_SEND);
        newIntent.setType("text/plain");
        //newIntent.setPackage("com.whatsapp");
        newIntent.putExtra(Intent.EXTRA_TEXT, deck);

        startActivity(newIntent);
    }

    private void setUpDatabase() {
        String CARDS_URL = "https://api.hearthstonejson.com/v1/latest/enUS/cards.collectible.json";

        QueryUtils httpHelper = new QueryUtils();
        URL url = httpHelper.createUrl(CARDS_URL);
        GetDeckTask task = new GetDeckTask(mDbHelper);
        task.setOnTaskCompletedListener(new GetDeckTask.OnTaskCompletedListener() {
            @Override
            public void onTaskComplete() {
                updateDialog.dismiss();
            }

            @Override
            public void onTaskFailed() {
                Toast.makeText(DeckBoxActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                updateDialog.dismiss();
            }
        });
        task.execute(url);
    }

    private void importAllDecksFromClipboard() {
        DeckDecoder dd = new DeckDecoder();
        try {
            ClipData data = myClipboard.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            String clipboardData = item.getText().toString();

            if (clipboardData == "") {
                throw new Exception();
            }

            Log.e("Prepared data ", clipboardData);

            ArrayList<String> deckStrings = new ArrayList<String>(Arrays.asList(clipboardData.split("\n\n")));

            Log.e("Size ", String.valueOf(deckStrings.size()));

            for (int i = 0; i < deckStrings.size(); i++) {
                DeckListManager newDeck = insertDeck(deckStrings.get(i).trim());
            }

            adapter.clear();
            decks = loadDecks(filterIconPointer);
            adapter.addAll(decks);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("Error ", e.toString());
            Toast.makeText(this, "No decks found in clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateName(String newName, int deckId) {
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

    //Annotates the deck string with the full deck list
    public String getAnnotatedDeckString(DeckListManager dlm) {
        String deckString = dlm.getDeckString();
        StringBuilder builder = new StringBuilder();
        DeckDecoder dd = new DeckDecoder();
        String deckName = dlm.getDeckName();
        String playableClass = dd.getPlayableClass(deckString);
        String format = dd.getFormat(deckString);

        ArrayList<Cards> deckList = new ArrayList<Cards>();
        QueryUtils httpHelper = new QueryUtils();

        try {
            ArrayList<ArrayList<Integer>> dbfIdList;
            dbfIdList = deckDecoder.decode(deckString);

            //Find all 1-quantity cards by their dbfIds
            ArrayList<Integer> dbf1 = dbfIdList.get(0);
            //Log.e("1-quantity cards: ", String.valueOf(dbf1.size()));
            for (int i = 0; i < dbf1.size(); i++) {
                Cards tempCard = new Cards();
                tempCard = Cards.getCardByDbfid(this, dbf1.get(i));
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
                tempCard = Cards.getCardByDbfid(this, dbf2.get(i));
                tempCard.setQuantity(2);

                String imageUrl = "https://art.hearthstonejson.com/v1/tiles/" + tempCard.getCardId() + ".jpg";
                URL url = httpHelper.createUrl(imageUrl);
                tempCard.setCardTileUrl(url);

                deckList.add(tempCard);
            }
        } catch (IOException e) {
            //Log.e("Deck Decoding Error: ", e.toString());
        }

        //Sort deck by mana cost
        deckList = dlm.sortByMana(deckList);
        deckList = dlm.sortLexicographically(deckList);

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

    //Only updates the cards in the database that were changed and/or
    //added to the game by Blizzard. This operation is still carried out even
    //if there are no changes. In this case the old db is deleted and then replaced with
    //the same one. Forcefully shutting the app when this function is running will leave
    //the app in an unusable state.
    //TODO: Remove this functionality and handle updates through Play Store
    private void updateApp() {
        //Clear the cards database so that it can be repopulated again with newer data
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int result = db.delete(
                DecksContract.CardsEntry.TABLE_NAME,
                null,
                null
        );

        try {
            setUpDatabase();
        } catch (Exception e) {
            Toast.makeText(DeckBoxActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            updateDialog.dismiss();
        }
    }

    //This is the function that actually searches the database for all the
    //stored decks based on the format preference set by user.
    //NOTE: Enter "class:<classname>" in the search bar to filter the decks
    //based on class. eg. "class: Druid" will return all Druid decks.
    //This function ignores the case. i.e Druid is same as DRuiD.
    private void handleSearch(String query) {
        filteredDecks = new ArrayList<DeckListManager>();

        query = query.toLowerCase();
        query = query.trim();

        for (int i = 0; i < decks.size(); i++) {
            if (decks.get(i).getDeckName().toLowerCase().contains(query)) {
                filteredDecks.add(decks.get(i));
            }
        }


        if (query.length() > 6) {
            if (query.substring(0, 6).compareTo("class:") == 0) {
                String classSearch = query.substring(6);
                classSearch = classSearch.trim();
                for (int i = 0; i < decks.size(); i++) {
                    if (decks.get(i).getPlayableClass().toLowerCase().equalsIgnoreCase(classSearch)) {
                        filteredDecks.add(decks.get(i));
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Inside OnResume", "yes");

        adapter.clear();

        decks = loadDecks(filterIconPointer); //Load all saved decks

        deckBoxListView = findViewById(R.id.deck_box);
        adapter = new DecksAdapter(this, decks);
        deckBoxListView.setAdapter(adapter);

        if (selectedDeckPosition >= deckBoxListView.getCount()) {
            selectedDeckPosition = deckBoxListView.getCount() - 1;
        }
        deckBoxListView.setSelection(selectedDeckPosition);
    }

}
