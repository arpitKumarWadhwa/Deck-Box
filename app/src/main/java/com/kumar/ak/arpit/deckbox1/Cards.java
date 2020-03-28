package com.kumar.ak.arpit.deckbox1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kumar.ak.arpit.deckbox1.data.DecksContract;
import com.kumar.ak.arpit.deckbox1.data.DecksDbHelper;

import java.net.URL;
import java.util.ArrayList;

public class Cards {
    private static final int RARITY_UNKNOWN = -1;
    private static final int RARITY_FREE = 0; //FREE AND SET=CORE
    private static final int RARITY_COMMON = 1; //COMMON
    private static final int RARITY_RARE = 2; //RARE
    private static final int RARITY_EPIC = 3; //EPIC
    private static final int RARITY_LEGENDARY = 4; //LEGENDARY

    private static final int TYPE_UNKNOWN = -1;
    private static final int TYPE_HERO = 0; //HERO
    private static final int TYPE_MINION = 1; //MINION
    private static final int TYPE_SPELL = 2; //SPELL
    private static final int TYPE_WEAPON = 3; //WEAPON

    public static final int CARD_CLASS_INVALID = -1;
    public static final int CARD_CLASS_WARRIOR = 0;
    public static final int CARD_CLASS_HUNTER = 1;
    public static final int CARD_CLASS_PALADIN = 2;
    public static final int CARD_CLASS_ROGUE = 3;
    public static final int CARD_CLASS_DRUID = 4;
    public static final int CARD_CLASS_SHAMAN = 5;
    public static final int CARD_CLASS_MAGE = 6;
    public static final int CARD_CLASS_PRIEST = 7;
    public static final int CARD_CLASS_WARLOCK = 8;
    public static final int CARD_CLASS_DEMON_HUNTER = 10;
    public final static int CARD_CLASS_NEUTRAL = 9;

    private String cardId; //id
    private int dbfid; //For generating deck codes. dbfid
    private String name; //name
    private int cost; //Mana Cost. cost
    private int rarity;//rarity
    private int type; //Type of card i.e hero/minion/spell/weapon. type
    private int quantity; //Quantity of the card in a deck. Does not go in a database.
    private int cardClass;
    private String cardText;
    private ArrayList<String> mechanics;
    private URL cardTileUrl; //The card tile image used for generating in-game style decklist(layout1)

    public String getCardId() {
        return cardId;
    }

    public int getDbfid() {
        return dbfid;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getRarity() {
        return rarity;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getType() {
        return type;
    }

    public URL getCardTileUrl() {
        return cardTileUrl;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCardTileUrl(URL cardTileUrl) {
        this.cardTileUrl = cardTileUrl;
    }

    public Cards() {
    }

    public int getCardClass() {
        return cardClass;
    }

    public void setCardClass(int cardClass) {
        this.cardClass = cardClass;
    }

    public String getCardText() {
        return cardText;
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
    }

    public ArrayList<String> getMechanics() {
        return mechanics;
    }

    public void setMechanics(ArrayList<String> mechanics) {
        this.mechanics = mechanics;
    }

    //TODO: Remove the quantity parameter from the constructor
    public Cards(String cardId, int dbfid, String name, int cost, int rarity, int type, int quantity) {
        this.cardId = cardId;
        this.dbfid = dbfid;
        this.name = name;
        this.cost = cost;
        this.rarity = rarity;
        this.type = type;
        this.quantity = quantity;
    }

    public static Cards getCardByDbfid(Context context, int dbf) {
        Cards tempCard = new Cards();
        DecksDbHelper cardsDbHelper = new DecksDbHelper(context);
        SQLiteDatabase db = cardsDbHelper.getReadableDatabase();

        String selection = DecksContract.CardsEntry.COLUMN_CARD_DBFID + "=?";
        String selectionArgs[] = new String[]{String.valueOf(dbf)};

        Cursor c = db.query(
                DecksContract.CardsEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();

        try {
            tempCard.cardId = c.getString(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_ID));
            tempCard.name = c.getString(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_NAME));
            tempCard.cost = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_COST));
            tempCard.rarity = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_RARITY));
            tempCard.type = c.getInt(c.getColumnIndex(DecksContract.CardsEntry.COLUMN_CARD_TYPE));
            tempCard.dbfid = dbf;
        } catch (Exception e) {
            //Log.e("Cards: ", e.toString());
        } finally {
            c.close();
            db.close();
        }

        return tempCard;
    }

    //Converts the rarity of a card in String to an int
    public static int tellRarityInInteger(String rarityString) {
        switch (rarityString) {
            case DecksContract.CardsEntry.RARITY_FREE:
                return RARITY_FREE;
            case DecksContract.CardsEntry.RARITY_COMMON:
                return RARITY_COMMON;
            case DecksContract.CardsEntry.RARITY_RARE:
                return RARITY_RARE;
            case DecksContract.CardsEntry.RARITY_EPIC:
                return RARITY_EPIC;
            case DecksContract.CardsEntry.RARITY_LEGENDARY:
                return RARITY_LEGENDARY;
            default:
                return RARITY_UNKNOWN;
        }
    }

    //Converts the type of a card in String to an int
    public static int tellTypeInInteger(String typeString) {
        switch (typeString) {
            case DecksContract.CardsEntry.TYPE_HERO:
                return TYPE_HERO;
            case DecksContract.CardsEntry.TYPE_MINION:
                return TYPE_MINION;
            case DecksContract.CardsEntry.TYPE_SPELL:
                return TYPE_SPELL;
            case DecksContract.CardsEntry.TYPE_WEAPON:
                return TYPE_WEAPON;
            default:
                return TYPE_UNKNOWN;
        }

    }

    public static int tellCardClassInInteger(String cardClass){
        switch (cardClass){
            case "WARRIOR":
                return CARD_CLASS_WARRIOR;
            case "HUNTER":
                return CARD_CLASS_HUNTER;
            case "PALADIN":
                return CARD_CLASS_PALADIN;
            case "ROGUE":
                return CARD_CLASS_ROGUE;
            case "DRUID":
                return CARD_CLASS_DRUID;
            case "SHAMAN":
                return CARD_CLASS_SHAMAN;
            case "MAGE":
                return CARD_CLASS_MAGE;
            case "PRIEST":
                return CARD_CLASS_PRIEST;
            case "WARLOCK":
                return CARD_CLASS_WARLOCK;
            case "NEUTRAL":
                return CARD_CLASS_NEUTRAL;
            case "DEMONHUNTER":
                return CARD_CLASS_DEMON_HUNTER;
                default:
                    return CARD_CLASS_INVALID;
        }
    }

}

