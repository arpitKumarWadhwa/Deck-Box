package com.kumar.ak.arpit.deckbox1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DecksDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "deckbox.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    private static final String CARDS_TABLE = "CREATE TABLE " + DecksContract.CardsEntry.TABLE_NAME + " ("
            + DecksContract.CardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.CardsEntry.COLUMN_CARD_DBFID + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_NAME + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_COST + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_RARITY + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_TYPE + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_ID + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_CLASS + " INTEGER);";

    private static final String CARDS_TABLE_ruRU = "CREATE TABLE " + "cards_ruRU" + " ("
            + DecksContract.CardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.CardsEntry.COLUMN_CARD_DBFID + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_NAME + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_COST + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_RARITY + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_TYPE + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_ID + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_CLASS + " INTEGER);";

    private static final String CARDS_TABLE_koKR = "CREATE TABLE " + "cards_koKR" + " ("
            + DecksContract.CardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.CardsEntry.COLUMN_CARD_DBFID + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_NAME + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_COST + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_RARITY + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_TYPE + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_ID + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_CLASS + " INTEGER);";

    private static final String CARDS_TABLE_zhCN = "CREATE TABLE " + "cards_zhCN" + " ("
            + DecksContract.CardsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.CardsEntry.COLUMN_CARD_DBFID + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_NAME + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_COST + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_RARITY + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_TYPE + " INTEGER, "
            + DecksContract.CardsEntry.COLUMN_CARD_ID + " TEXT, "
            + DecksContract.CardsEntry.COLUMN_CARD_CLASS + " INTEGER);";

    private static final String DECKS_TABLE = "CREATE TABLE " + DecksContract.DecksEntry.TABLE_NAME + " ("
            + DecksContract.DecksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DecksContract.DecksEntry.COLUMN_DECK_STRING + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_FORMAT + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_CLASS + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_DECK_NAME + " TEXT, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_0_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_1_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_2_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_3_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_4_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_5_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_6_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_7_COST_CARDS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_MINIONS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_SPELLS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_WEAPONS + " INTEGER, "
            + DecksContract.DecksEntry.COLUMN_NO_OF_HEROES + " INTEGER);";

    public DecksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create the Cards Table
        db.execSQL(CARDS_TABLE);
        db.execSQL(CARDS_TABLE_ruRU);
        db.execSQL(CARDS_TABLE_koKR);
        db.execSQL(CARDS_TABLE_zhCN);
        db.execSQL(DECKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

