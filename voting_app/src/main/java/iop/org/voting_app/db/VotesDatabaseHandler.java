package iop.org.voting_app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import iop_sdk.governance.vote.Vote;


/**
 * Created by mati on 21/12/16.
 */

public class VotesDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "VOTING";
    private static final int DATABASE_VERSION = 2;

    // Contacts table name
    private static final String TABLE_VOTES = "votes";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_GENESIS_HASH = "genesis_hash";
    private static final String KEY_VOTE_TYPE = "vote_type";
    private static final String KEY_VOTING_POWER = "voting_power";
    private static final String KEY_LOCKED_OUTPUT_HASH = "locked_output_hash";
    private static final String KEY_LOCKED_OUTPUT_INDEX = "locked_output_index";
    private static final String KEY_IS_VOTE_LOCKED = "is_vote_locked";

    // positions
    private static final int KEY_POS_ID =                                           0;
    private static final int KEY_POS_GENESIS_HASH =                                 1;
    private static final int KEY_POS_VOTE_TYPE =                                    2;
    private static final int KEY_POS_VOTING_POWER =                                 3;
    private static final int KEY_POS_LOCKED_OUTPUT_HASH =                           4;
    private static final int KEY_POS_LOCKED_OUTPUT_INDEX =                          5;
    private static final int KEY_POS_IS_VOTE_LOCKED =                               6;


    public VotesDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_VOTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_GENESIS_HASH + " TEXT,"
                + KEY_VOTE_TYPE + " TEXT,"
                + KEY_VOTING_POWER + " LONG,"
                + KEY_LOCKED_OUTPUT_HASH + " TEXT,"
                + KEY_LOCKED_OUTPUT_INDEX + " INTEGER,"
                + KEY_IS_VOTE_LOCKED + " INTEGER"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOTES);

        // Create tables again
        onCreate(db);
    }

    private ContentValues buildContentValues(Vote vote){
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_GENESIS_HASH,vote.getGenesisHashHex());
        contentValues.put(KEY_VOTE_TYPE,vote.getVote().toString());
        contentValues.put(KEY_VOTING_POWER,vote.getVotingPower());
        contentValues.put(KEY_LOCKED_OUTPUT_HASH,vote.getLockedOutputHex());
        contentValues.put(KEY_LOCKED_OUTPUT_INDEX,vote.getLockedOutputIndex());
        contentValues.put(KEY_IS_VOTE_LOCKED,vote.isOutputFrozen());
        return contentValues;
    }

    private String[] allColumns(){
        String[] colums = new String[7];
        colums[KEY_POS_ID] = KEY_ID;
        colums[KEY_POS_GENESIS_HASH] = KEY_GENESIS_HASH;
        colums[KEY_POS_VOTE_TYPE] = KEY_VOTE_TYPE;
        colums[KEY_POS_VOTING_POWER] = KEY_VOTING_POWER;
        colums[KEY_POS_LOCKED_OUTPUT_HASH] = KEY_LOCKED_OUTPUT_HASH;
        colums[KEY_POS_LOCKED_OUTPUT_INDEX] = KEY_LOCKED_OUTPUT_INDEX;
        colums[KEY_POS_IS_VOTE_LOCKED] = KEY_IS_VOTE_LOCKED;
        return colums;
    }

    private Vote buildVote(Cursor cursor) {
        long id = cursor.getLong(KEY_POS_ID);
        String genesisHash = cursor.getString(KEY_POS_GENESIS_HASH);
        Vote.VoteType voteType = Vote.VoteType.valueOf(cursor.getString(KEY_POS_VOTE_TYPE));
        long votingPower = cursor.getLong(KEY_POS_VOTING_POWER);
        String lockedOutputHash = cursor.getString(KEY_POS_LOCKED_OUTPUT_HASH);
        int lockedOutputIndex = cursor.getInt(KEY_POS_LOCKED_OUTPUT_INDEX);
        boolean isOutputFrozen = cursor.getInt(KEY_POS_IS_VOTE_LOCKED) > 0;

        return new Vote(id,genesisHash,voteType,votingPower,lockedOutputHash,lockedOutputIndex,isOutputFrozen);
    }


    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public long addVote(Vote vote) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(vote);

        // Inserting Row
        long id = db.insert(TABLE_VOTES, null, values);
        db.close(); // Closing database connection
        return id;
    }

    // Getting single vote
    Vote getVote(String genesisHashHex) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VOTES,allColumns(), KEY_GENESIS_HASH + "=?",
                new String[] { genesisHashHex }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Vote contact = null;
        if (cursor.getCount()>0)
            contact = buildVote(cursor);
        // return contact
        cursor.close();
        return contact;
    }

    public boolean exist(String genesisHashHex) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VOTES,allColumns(), KEY_GENESIS_HASH + "=?",
                new String[] { genesisHashHex }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        boolean exist = cursor.getCount()==1;
        cursor.close();
        // return contact
        return exist;
    }

    public boolean isLockedOutput(String parentVoteTransactionHash, long index) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VOTES,allColumns(), KEY_LOCKED_OUTPUT_HASH + " LIKE ? AND "+KEY_LOCKED_OUTPUT_INDEX+"=?",
                new String[] { parentVoteTransactionHash, String.valueOf(index)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        boolean exist = false;
        if (cursor.getCount()>0)
            exist = cursor.getInt(KEY_POS_IS_VOTE_LOCKED)>0;
        cursor.close();
        return exist;
    }


    // Getting All Contacts
    public List<Vote> getAllVotes() {
        List<Vote> contactList = new ArrayList<Vote>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_VOTES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Vote contact = buildVote(cursor);
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

//    // Updating single contact
//    public int updateContact(Vote contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = buildContentValues(contact);
//
//        // updating row
//        return db.update(TABLE_VOTES, values, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getID()) });
//    }

    // Deleting single contact
//    public void deleteContact(Contact contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getID()) });
//        db.close();
//    }

    public boolean delete(Vote vote) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(TABLE_VOTES, KEY_ID + " = ?",
                new String[] { String.valueOf(vote.getVoteId()) });
        db.close();
        return res == 1;
    }

    public boolean updateVote(String genesisHash,String genesisProposalHash, int lockedOutputIndex) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_LOCKED_OUTPUT_HASH,genesisProposalHash);
        contentValues.put(KEY_LOCKED_OUTPUT_INDEX,lockedOutputIndex);
        contentValues.put(KEY_IS_VOTE_LOCKED,true);
        // updating row
        int resp = db.update(TABLE_VOTES, contentValues, KEY_GENESIS_HASH + " = ?",
                new String[] { genesisHash });
        db.close();
        return resp==1;
    }

    public boolean updateVote(Vote vote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = buildContentValues(vote);
        // updating row
        int resp = db.update(TABLE_VOTES, contentValues, KEY_ID + " = ?",
                new String[] { String.valueOf(vote.getVoteId()) });
        db.close();
        return resp==1;
    }

    public boolean updateVote(String genesisProposalHash, boolean isOutputFrozen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_IS_VOTE_LOCKED,isOutputFrozen);
        // updating row
        int resp = db.update(TABLE_VOTES, contentValues, KEY_GENESIS_HASH + " LIKE ?",
                new String[] { genesisProposalHash });
        db.close();
        return resp==1;
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_VOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

    public void deleteDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOTES, null,null);
        db.close();
    }

}
