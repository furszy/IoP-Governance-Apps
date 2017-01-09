package org.iop.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iop_sdk.crypto.CryptoBytes;
import iop_sdk.governance.propose.Proposal;

;

public class ProposalsDatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "ProposalsDatabase";

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 14;

    // Database Name
    private static final String DATABASE_NAME = "walletManager";

    // Contacts table name
    private static final String TABLE_PROPOSALS = "proposals";

    // Contacts Table Columns names
    private static final String KEY_PROPOSAL_TITLE = "title";
    private static final String KEY_PROPOSAL_SUBTITLE = "subtitle";
    private static final String KEY_PROPOSAL_CATEGORY = "category";
    private static final String KEY_PROPOSAL_BODY = "body";
    private static final String KEY_PROPOSAL_START_BLOCK = "start_block";
    private static final String KEY_PROPOSAL_END_BLOCK = "end_block";
    private static final String KEY_PROPOSAL_BLOCK_REWARD = "block_reward";
    private static final String KEY_PROPOSAL_FORUM_ID = "forum_id";
    private static final String KEY_PROPOSAL_BENEFICIARIES = "beneficiaries";
    private static final String KEY_PROPOSAL_EXTRA_FEE_VALUE = "extra_fee_value";

    private static final String KEY_PROPOSAL_IS_MINE = "is_mine";
    private static final String KEY_PROPOSAL_IS_SENT = "is_sent";
    private static final String KEY_PROPOSAL_LOCKED_OUTPUT_HASH = "locked_output_hash";
    private static final String KEY_PROPOSAL_LOCKED_OUTPUT_POSITION = "locked_output_position";

    private static final String KEY_PROPOSAL_VERSION = "version";
    private static final String KEY_PROPOSAL_OWNER_PUBKEY = "owner_pubkey";
    private static final String KEY_PROPOSAL_STATE = "prop_state";
    private static final String KEY_PROPOSAL_BLOCKCHAIN_HASH = "blockchain_hash";

    private static final int KEY_PROPOSAL_POS_TITLE =                   0;
    private static final int KEY_PROPOSAL_POS_SUBTITLE =                1;
    private static final int KEY_PROPOSAL_POS_CATEGORY =                2;
    private static final int KEY_PROPOSAL_POS_BODY =                    3;
    private static final int KEY_PROPOSAL_POS_START_BLOCK =             4;
    private static final int KEY_PROPOSAL_POS_END_BLOCK =               5;
    private static final int KEY_PROPOSAL_POS_BLOCK_REWARD =            6;
    private static final int KEY_PROPOSAL_POS_FORUM_ID =                7;
    private static final int KEY_PROPOSAL_POS_BENEFICIARIES =           8;
    private static final int KEY_PROPOSAL_POS_EXTRA_FEE_VALUE =         9;

    private static final int KEY_PROPOSAL_POS_IS_MINE =                 10;
    private static final int KEY_PROPOSAL_POS_IS_SENT =                 11;
    private static final int KEY_PROPOSAL_POS_LOCKED_OUTPUT_HASH =      12;
    private static final int KEY_PROPOSAL_POS_LOCKED_OUTPUT_POSITION =  13;

    private static final int KEY_PROPOSAL_POS_VERSION =                 14;
    private static final int KEY_PROPOSAL_POS_OWNER_PUBKEY =            15;
    private static final int KEY_PROPOSAL_POS_PROPOSAL_STATE =          16;
    private static final int KEY_PROPOSAL_POS_BLOCKCHAIN_HASH =         17;



    public ProposalsDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PROPOSALS +
                "("
//                + KEY_PROPOSAL_DATABASE_ID + " LONG PRIMARY KEY,"
//                + KEY_PROPOSAL_IOP_ID + " LONG,"
                + KEY_PROPOSAL_TITLE + " TEXT PRIMARY KEY,"
                + KEY_PROPOSAL_SUBTITLE + " TEXT,"
                + KEY_PROPOSAL_CATEGORY + " TEXT,"
                + KEY_PROPOSAL_BODY + " TEXT,"
                + KEY_PROPOSAL_START_BLOCK + " INTEGER,"
                + KEY_PROPOSAL_END_BLOCK + " INTEGER,"
                + KEY_PROPOSAL_BLOCK_REWARD + " LONG,"
                + KEY_PROPOSAL_FORUM_ID + " INTEGER,"
                + KEY_PROPOSAL_BENEFICIARIES + " TEXT,"
                + KEY_PROPOSAL_EXTRA_FEE_VALUE + " LONG,"
                + KEY_PROPOSAL_IS_MINE + " BOOLEAN,"
                + KEY_PROPOSAL_IS_SENT + " BOOLEAN,"
                + KEY_PROPOSAL_LOCKED_OUTPUT_HASH + " TEXT,"
                + KEY_PROPOSAL_LOCKED_OUTPUT_POSITION + " LONG,"
                + KEY_PROPOSAL_VERSION + " SHORT,"
                + KEY_PROPOSAL_OWNER_PUBKEY + " BLOB,"
                + KEY_PROPOSAL_STATE + " TEXT,"
                + KEY_PROPOSAL_BLOCKCHAIN_HASH + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */

    // Upgrading database, todo: por ahora cada vez que hago un cambio en la db borra todo, más adelante tengo que sacar esto por un mejor update así no se pierde la data.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROPOSALS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public boolean addProposal(Proposal proposal) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(proposal);

        // Inserting Row
        long rowId = db.insert(TABLE_PROPOSALS, null, values);
        //db.close(); // Closing database connection

        return rowId>0;
    }

    // Getting single contact
    Proposal getProposal(String title) throws CantGetProposalException {
        Log.d(TAG,"getProposal");
        Proposal proposal = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PROPOSALS, new String[]{
                            KEY_PROPOSAL_TITLE,
                            KEY_PROPOSAL_SUBTITLE,
                            KEY_PROPOSAL_CATEGORY,
                            KEY_PROPOSAL_BODY,
                            KEY_PROPOSAL_START_BLOCK,
                            KEY_PROPOSAL_END_BLOCK,
                            KEY_PROPOSAL_BLOCK_REWARD,
                            KEY_PROPOSAL_FORUM_ID,
                            KEY_PROPOSAL_BENEFICIARIES,
                            KEY_PROPOSAL_EXTRA_FEE_VALUE,
                            KEY_PROPOSAL_IS_MINE,
                            KEY_PROPOSAL_IS_SENT,
                            KEY_PROPOSAL_LOCKED_OUTPUT_HASH,
                            KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,
                            KEY_PROPOSAL_VERSION,
                            KEY_PROPOSAL_OWNER_PUBKEY,
                            KEY_PROPOSAL_STATE,
                            KEY_PROPOSAL_BLOCKCHAIN_HASH}, KEY_PROPOSAL_TITLE + "=?",
                    new String[]{title}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            proposal = buildProposal(cursor);

            Log.d(TAG, "proposal: " + proposal.toString());

            cursor.close();
            db.close();
        } catch (IOException e) {
            throw new CantGetProposalException("CantUpdateProposal",e);
        }
        // return contact
        return proposal;
    }

    // Getting single contact
    Proposal getProposal(int forumId) throws CantGetProposalException {
        Log.d(TAG,"getProposal");
        Proposal proposal = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PROPOSALS, new String[]{
                            KEY_PROPOSAL_TITLE,
                            KEY_PROPOSAL_SUBTITLE,
                            KEY_PROPOSAL_CATEGORY,
                            KEY_PROPOSAL_BODY,
                            KEY_PROPOSAL_START_BLOCK,
                            KEY_PROPOSAL_END_BLOCK,
                            KEY_PROPOSAL_BLOCK_REWARD,
                            KEY_PROPOSAL_FORUM_ID,
                            KEY_PROPOSAL_BENEFICIARIES,
                            KEY_PROPOSAL_EXTRA_FEE_VALUE,
                            KEY_PROPOSAL_IS_MINE,
                            KEY_PROPOSAL_IS_SENT,
                            KEY_PROPOSAL_LOCKED_OUTPUT_HASH,
                            KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,
                            KEY_PROPOSAL_VERSION,
                            KEY_PROPOSAL_OWNER_PUBKEY,
                            KEY_PROPOSAL_STATE,
                            KEY_PROPOSAL_BLOCKCHAIN_HASH}, KEY_PROPOSAL_FORUM_ID + "=?",
                    new String[]{String.valueOf(forumId)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            proposal = buildProposal(cursor);

            Log.d(TAG, "proposal: " + proposal.toString());

            cursor.close();
            db.close();
        } catch (IOException e) {
            throw new CantGetProposalException("CantUpdateProposal",e);
        }
        // return contact
        return proposal;
    }


    // Getting All Contacts
    public List<Proposal> getAllProposals() {
        List<Proposal> contactList = new ArrayList<Proposal>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PROPOSALS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Proposal proposal = null;
                try {
                    proposal = buildProposal(cursor);

                    // Adding contact to list
                    contactList.add(proposal);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // return contact list
        return contactList;
    }

    public List<Proposal> getAllProposals(List<String> transactionHashes) {
        Log.d(TAG,"getProposal");
        List<Proposal> proposals = new ArrayList<>();
        try {

            StringBuilder selection = new StringBuilder();
            String[] where = new String[transactionHashes.size()];

            for (int i = 0; i < transactionHashes.size(); i++) {
                String hash = transactionHashes.get(i);

                selection.append(KEY_PROPOSAL_BLOCKCHAIN_HASH+"=?");
                where[i] = hash;

                if (i!=transactionHashes.size()-1){
                    selection.append(" OR ");
                }
            }

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_PROPOSALS, new String[]{
                            KEY_PROPOSAL_TITLE,
                            KEY_PROPOSAL_SUBTITLE,
                            KEY_PROPOSAL_CATEGORY,
                            KEY_PROPOSAL_BODY,
                            KEY_PROPOSAL_START_BLOCK,
                            KEY_PROPOSAL_END_BLOCK,
                            KEY_PROPOSAL_BLOCK_REWARD,
                            KEY_PROPOSAL_FORUM_ID,
                            KEY_PROPOSAL_BENEFICIARIES,
                            KEY_PROPOSAL_EXTRA_FEE_VALUE,
                            KEY_PROPOSAL_IS_MINE,
                            KEY_PROPOSAL_IS_SENT,
                            KEY_PROPOSAL_LOCKED_OUTPUT_HASH,
                            KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,
                            KEY_PROPOSAL_VERSION,
                            KEY_PROPOSAL_OWNER_PUBKEY,
                            KEY_PROPOSAL_STATE,
                            KEY_PROPOSAL_BLOCKCHAIN_HASH}, selection.toString(), where, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    Proposal proposal = null;
                    try {
                        proposal = buildProposal(cursor);

                        // Adding contact to list
                        proposals.add(proposal);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }

            Log.d(TAG,"Proposal list return with: "+proposals.size()+" results");


            cursor.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return proposals;
    }

    // Updating single contact
    public int updateProposal(Proposal proposal) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(proposal);
//        // updating row
        try {
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_TITLE + " = ?",
                    new String[]{proposal.getTitle()});
        }finally {
            db.close();
        }
    }

    public int markSentProposal(int forumId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROPOSAL_IS_SENT,true);
//        // updating row
        try {
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_FORUM_ID + " = ?",
                    new String[]{String.valueOf(forumId)});
        }finally {
            db.close();
        }
    }

    public int markSentProposalAndChangeState(int forumId, Proposal.ProposalState proposalState) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROPOSAL_IS_SENT,true);
        values.put(KEY_PROPOSAL_STATE,proposalState.ordinal());
//        // updating row
        try {
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_FORUM_ID + " = ?",
                    new String[]{String.valueOf(forumId)});
        }finally {
            db.close();
        }
    }

    // Deleting single contact
    public void deleteContact(Proposal contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROPOSALS, KEY_PROPOSAL_TITLE + " = ?",
                new String[] { contact.getTitle() });
        db.close();
    }

    public void deleteDb() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROPOSALS, null,null);
        db.close();
    }


    // Getting contacts Count
    public int getProposalsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PROPOSALS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        return count;
    }


    public long getSentProposalsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PROPOSALS + " WHERE "+KEY_PROPOSAL_IS_SENT+"=?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(1)});
        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d(TAG,"Sent proposals count: "+count);
        // return count
        return count;
    }

    public int lockOutput(int forumId,String hashHex, int index) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROPOSAL_LOCKED_OUTPUT_HASH,hashHex);
        values.put(KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,index);

        try {
//        // updating row
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_FORUM_ID + " = ?",
                    new String[]{String.valueOf(forumId)});
        }finally {
            db.close();
        }
    }



    public boolean exist(String title) {
        return checkExistense(new Object[]{title},KEY_PROPOSAL_TITLE);
    }

    public boolean isOutputLocked(String hashHex, long position) {
        return checkExistense(new Object[]{hashHex,position},KEY_PROPOSAL_LOCKED_OUTPUT_HASH, KEY_PROPOSAL_LOCKED_OUTPUT_POSITION);
    }

    private boolean checkExistense(Object[] dataToCompare,String... keyToCompare){
        SQLiteDatabase db = this.getReadableDatabase();

        // todo: quizás esto no haga falta
        String[] fields = new String[keyToCompare.length];
        for (int i = 0; i < keyToCompare.length; i++) {
            fields[i] = keyToCompare[i];
        }


        StringBuilder stringBuilder = new StringBuilder();
        String[] valuesToCompare = new String[dataToCompare.length];

        for (int i = 0; i < dataToCompare.length; i++) {

            // build the sentence
            stringBuilder.append(keyToCompare[i]).append("=?");
            if (i!=dataToCompare.length-1){
                stringBuilder.append(" AND ");
            }

            //build the values
            valuesToCompare[i]=String.valueOf(dataToCompare[i]);
        }

        Cursor cursor = db.query(TABLE_PROPOSALS, fields, stringBuilder.toString(), valuesToCompare, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        cursor.close();
        //db.close();
        return cursor.getCount()>0;
    }

    public boolean isProposalMine(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isMine = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT is_mine FROM proposals WHERE title='"+forumTitleToDbTitle(title)+"'", null);
            //cursor = db.query(TABLE_PROPOSALS, new String[]{KEY_PROPOSAL_IS_MINE}, KEY_PROPOSAL_TITLE + "=?", new String[]{dbTitle}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0)
                    isMine = ((cursor.getInt(0) == 0) ? false : true);
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return isMine;
    }

    public boolean isProposalMine(int forumId) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean isMine = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT is_mine FROM proposals WHERE "+KEY_PROPOSAL_FORUM_ID+"="+forumId, null);
            //cursor = db.query(TABLE_PROPOSALS, new String[]{KEY_PROPOSAL_IS_MINE}, KEY_PROPOSAL_TITLE + "=?", new String[]{dbTitle}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                if (cursor.getCount() > 0)
                    isMine = ((cursor.getInt(0) == 0) ? false : true);
            }
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return isMine;
    }

    private String forumTitleToDbTitle(String title){
        return title.replace("-"," ");
    }
    /**
     *
     * @param cursor
     * @return
     * @throws IOException
     */

    private Proposal buildProposal(Cursor cursor) throws IOException {
        Log.d(TAG,"buildProposal");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.readValue(
                cursor.getString(KEY_PROPOSAL_POS_BENEFICIARIES),
                new TypeReference<Map<String, Long>>() {
                });

        boolean isMine = ((cursor.getInt(KEY_PROPOSAL_POS_IS_MINE)==0)?false:true);
        String title =  cursor.getString(KEY_PROPOSAL_POS_TITLE);
        String subTitle = cursor.getString(KEY_PROPOSAL_POS_SUBTITLE);
        String category = cursor.getString(KEY_PROPOSAL_POS_CATEGORY);
        String body = cursor.getString(KEY_PROPOSAL_POS_BODY);
        int startBlock = cursor.getInt(KEY_PROPOSAL_POS_START_BLOCK);
        int endBlock = cursor.getInt(KEY_PROPOSAL_POS_END_BLOCK);
        long blockReward = cursor.getLong(KEY_PROPOSAL_POS_BLOCK_REWARD);
        int forumId = cursor.getInt(KEY_PROPOSAL_POS_FORUM_ID);
        long extraFee = cursor.getLong(KEY_PROPOSAL_POS_EXTRA_FEE_VALUE);
        boolean isSent = cursor.getInt(KEY_PROPOSAL_POS_IS_SENT)==0 ? false:true;
        String lockedOutputHashHex = cursor.getString(KEY_PROPOSAL_POS_LOCKED_OUTPUT_HASH);
        long lockedOutputPos = cursor.getLong(KEY_PROPOSAL_POS_LOCKED_OUTPUT_POSITION);
        short version = cursor.getShort(KEY_PROPOSAL_POS_VERSION);
        byte[] ownerPk = cursor.getBlob(KEY_PROPOSAL_POS_OWNER_PUBKEY);
        Proposal.ProposalState proposalState = Proposal.ProposalState.valueOf(cursor.getString(KEY_PROPOSAL_POS_PROPOSAL_STATE));
        byte[] blockchainHash = null;
        String blockchainHashHex = cursor.getString(KEY_PROPOSAL_POS_BLOCKCHAIN_HASH);
        if (blockchainHashHex!=null) blockchainHash = CryptoBytes.fromHexToBytes(blockchainHashHex);


        Proposal proposal = new Proposal(
                isMine,
                title,
                subTitle,
                category,
                body,
                startBlock,
                endBlock,
                blockReward,
                forumId,
                map,
                extraFee,
                isSent,
                lockedOutputHashHex,
                lockedOutputPos,
                version,
                ownerPk,
                proposalState
        );
        proposal.setBlockchainHash(blockchainHash);
        return proposal;
    }

    private ContentValues buildContentValues(Proposal proposal) throws JsonProcessingException {
        ContentValues values = new ContentValues();
//        values.put(KEY_PROPOSAL_DATABASE_ID, proposal.getId());
//        values.put(KEY_PROPOSAL_IOP_ID, proposal.getIoPIP());
        values.put(KEY_PROPOSAL_TITLE, proposal.getTitle());
        values.put(KEY_PROPOSAL_SUBTITLE, proposal.getSubTitle());
        values.put(KEY_PROPOSAL_CATEGORY, proposal.getCategory());
        values.put(KEY_PROPOSAL_BODY, proposal.getBody());
        values.put(KEY_PROPOSAL_START_BLOCK,proposal.getStartBlock());
        values.put(KEY_PROPOSAL_END_BLOCK,proposal.getEndBlock());
        values.put(KEY_PROPOSAL_BLOCK_REWARD,proposal.getBlockReward());
        values.put(KEY_PROPOSAL_FORUM_ID,proposal.getForumId());
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String beneficiaries = mapper.writeValueAsString(proposal.getBeneficiaries());
        values.put(KEY_PROPOSAL_BENEFICIARIES,beneficiaries);
        values.put(KEY_PROPOSAL_EXTRA_FEE_VALUE,proposal.getExtraFeeValue());
        values.put(KEY_PROPOSAL_IS_MINE,proposal.isMine());
        values.put(KEY_PROPOSAL_IS_SENT,proposal.isSent());
        if (proposal.getGenesisTxHash()!=null)values.put(KEY_PROPOSAL_LOCKED_OUTPUT_HASH,proposal.getGenesisTxHash());
        if (proposal.getLockedOutputIndex()!=-1) values.put(KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,proposal.getLockedOutputIndex());
        values.put(KEY_PROPOSAL_VERSION,proposal.getVersion());
        values.put(KEY_PROPOSAL_OWNER_PUBKEY,proposal.getOwnerPubKey());
        values.put(KEY_PROPOSAL_STATE,proposal.getState().toString());
        if (proposal.getBlockchainHash()!=null) values.put(KEY_PROPOSAL_BLOCKCHAIN_HASH,CryptoBytes.toHexString(proposal.getBlockchainHash()));
        return values;
    }

    private ContentValues buildContentValues(Object[] values,String[] keys){
        ContentValues contentValues = new ContentValues();
        try {
            Field map = contentValues.getClass().getField("mValues");
            map.setAccessible(true);
            HashMap<String, Object> valuesMap = new HashMap<>();
            valuesMap = (HashMap<String, Object>) map.get(values);
            for (int i = 0; i < values.length; i++) {
                valuesMap.put(keys[i],values[i]);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return contentValues;
    }



}