package iop.org.iop_contributors_app.wallet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import iop.org.iop_contributors_app.core.Proposal;

public class ProposalsDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "walletManager";

    // Contacts table name
    private static final String TABLE_PROPOSALS = "proposals";

    // Contacts Table Columns names
    private static final String KEY_PROPOSAL_DATABASE_ID = "id";
    // IoPIP -> IoP improvement proposal
    private static final String KEY_PROPOSAL_IOP_ID = "IoPIP_id";
    private static final String KEY_PROPOSAL_TITLE = "title";
    private static final String KEY_PROPOSAL_SUBTITLE = "subtitle";
    private static final String KEY_PROPOSAL_CATEGORY = "category";
    private static final String KEY_PROPOSAL_BODY = "body";
    private static final String KEY_PROPOSAL_START_BLOCK = "start_block";
    private static final String KEY_PROPOSAL_END_BLOCK = "end_block";
    private static final String KEY_PROPOSAL_BLOCK_REWARD = "block_reward";
    private static final String KEY_PROPOSAL_FORUM_LINK = "link";
    private static final String KEY_PROPOSAL_BENEFICIARIES = "beneficiaries";
    private static final String KEY_PROPOSAL_EXTRA_FEE_VALUE = "extra_fee_value";

    private static final String KEY_PROPOSAL_IS_MINE = "is_mine";
    private static final String KEY_PROPOSAL_IS_SENT = "is_sent";
    private static final String KEY_PROPOSAL_LOCKED_OUTPUT_HASH = "locked_output_hash";
    private static final String KEY_PROPOSAL_LOCKED_OUTPUT_POSITION = "locked_output_position";

    private static final String KEY_PROPOSAL_VERSION = "version";
    private static final String KEY_PROPOSAL_OWNER_PUBKEY = "owner_pubkey";


    public ProposalsDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PROPOSALS +
                "("
                + KEY_PROPOSAL_DATABASE_ID + " LONG PRIMARY KEY,"
                + KEY_PROPOSAL_IOP_ID + " LONG,"
                + KEY_PROPOSAL_TITLE + " TEXT,"
                + KEY_PROPOSAL_SUBTITLE + " TEXT,"
                + KEY_PROPOSAL_CATEGORY + " TEXT,"
                + KEY_PROPOSAL_BODY + " TEXT,"
                + KEY_PROPOSAL_START_BLOCK + " INTEGER,"
                + KEY_PROPOSAL_END_BLOCK + " INTEGER,"
                + KEY_PROPOSAL_BLOCK_REWARD + " LONG,"
                + KEY_PROPOSAL_FORUM_LINK + " TEXT,"
                + KEY_PROPOSAL_BENEFICIARIES + " TEXT,"
                + KEY_PROPOSAL_EXTRA_FEE_VALUE + " LONG,"
                + KEY_PROPOSAL_IS_MINE + " BOOLEAN,"
                + KEY_PROPOSAL_IS_SENT + " BOOLEAN,"
                + KEY_PROPOSAL_LOCKED_OUTPUT_HASH + " BLOB,"
                + KEY_PROPOSAL_LOCKED_OUTPUT_POSITION + " LONG,"
                + KEY_PROPOSAL_VERSION + " SHORT,"
                + KEY_PROPOSAL_OWNER_PUBKEY + " BLOB"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

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
        db.close(); // Closing database connection

        return rowId>0;
    }

    // Getting single contact
    Proposal getProposal(int IoPId) throws IOException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PROPOSALS, new String[] {
                KEY_PROPOSAL_DATABASE_ID,
                KEY_PROPOSAL_IOP_ID,
                KEY_PROPOSAL_TITLE,
                KEY_PROPOSAL_SUBTITLE,
                KEY_PROPOSAL_CATEGORY,
                KEY_PROPOSAL_BODY,
                KEY_PROPOSAL_START_BLOCK,
                KEY_PROPOSAL_END_BLOCK,
                KEY_PROPOSAL_BLOCK_REWARD,
                KEY_PROPOSAL_FORUM_LINK,
                KEY_PROPOSAL_BENEFICIARIES,
                KEY_PROPOSAL_EXTRA_FEE_VALUE,
                KEY_PROPOSAL_IS_MINE,
                KEY_PROPOSAL_IS_SENT,
                KEY_PROPOSAL_LOCKED_OUTPUT_HASH,
                KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,
                KEY_PROPOSAL_VERSION,
                KEY_PROPOSAL_OWNER_PUBKEY}, KEY_PROPOSAL_IOP_ID + "=?",
                new String[] { String.valueOf(IoPId) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Proposal proposal = buildProposal(cursor);

        cursor.close();
        db.close();

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

    // Updating single contact
    public int updateProposal(Proposal proposal) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = buildContentValues(proposal);
//        // updating row
        try {
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_DATABASE_ID + " = ?",
                    new String[]{String.valueOf(proposal.getId())});
        }finally {
            db.close();
        }
    }

    // Deleting single contact
    public void deleteContact(Proposal contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROPOSALS, KEY_PROPOSAL_DATABASE_ID + " = ?",
                new String[] { String.valueOf(contact.getId()) });
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

    public int lockOutput(long ContractId,byte[] hash, int index) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROPOSAL_LOCKED_OUTPUT_HASH,hash);
        values.put(KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,index);

        try {
//        // updating row
            return db.update(TABLE_PROPOSALS, values, KEY_PROPOSAL_DATABASE_ID + " = ?",
                    new String[]{String.valueOf(ContractId)});
        }finally {
            db.close();
        }
    }



    public boolean exist(long ioPIP) {
        return checkExistense(new Object[]{ioPIP},KEY_PROPOSAL_IOP_ID);
    }

    public boolean isOutputLocked(byte[] hash, long position) {
        return checkExistense(new Object[]{hash,position},KEY_PROPOSAL_LOCKED_OUTPUT_HASH, KEY_PROPOSAL_LOCKED_OUTPUT_POSITION);
    }

    private boolean checkExistense(Object[] dataToCompare,String... keyToCompare){
        SQLiteDatabase db = this.getReadableDatabase();

        // todo: quizás esto no haga falta
        String[] fields = new String[keyToCompare.length+1];
        fields[0] = KEY_PROPOSAL_DATABASE_ID;
        for (int i = 1; i < keyToCompare.length; i++) {
            fields[i] = keyToCompare[i-1];
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
        db.close();
        return cursor.getCount()>0;
    }



    private Proposal buildProposal(Cursor cursor) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.readValue(
                cursor.getString(10),
                new TypeReference<Map<String, Long>>() {
                });
        return new Proposal(
                Long.parseLong(cursor.getString(0)),
                ((cursor.getInt(12)==0)?false:true),
                Long.parseLong(cursor.getString(1)),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getLong(8),
                cursor.getString(9),
                map,
                cursor.getLong(11),
                cursor.getInt(13)==0 ? false:true,
                cursor.getBlob(14),
                cursor.getLong(15),
                cursor.getShort(16),
                cursor.getBlob(17)
        );
    }

    private ContentValues buildContentValues(Proposal proposal) throws JsonProcessingException {
        ContentValues values = new ContentValues();
        values.put(KEY_PROPOSAL_DATABASE_ID, proposal.getId());
        values.put(KEY_PROPOSAL_IOP_ID, proposal.getIoPIP());
        values.put(KEY_PROPOSAL_TITLE, proposal.getTitle());
        values.put(KEY_PROPOSAL_SUBTITLE, proposal.getSubTitle());
        values.put(KEY_PROPOSAL_CATEGORY, proposal.getCategory());
        values.put(KEY_PROPOSAL_BODY, proposal.getBody());
        values.put(KEY_PROPOSAL_START_BLOCK,proposal.getStartBlock());
        values.put(KEY_PROPOSAL_END_BLOCK,proposal.getEndBlock());
        values.put(KEY_PROPOSAL_BLOCK_REWARD,proposal.getBlockReward());
        values.put(KEY_PROPOSAL_FORUM_LINK,proposal.getForumLink());
        ObjectMapper mapper = new ObjectMapper();
        //Object to JSON in String
        String beneficiaries = mapper.writeValueAsString(proposal.getBeneficiaries());
        values.put(KEY_PROPOSAL_BENEFICIARIES,beneficiaries);
        values.put(KEY_PROPOSAL_EXTRA_FEE_VALUE,proposal.getExtraFeeValue());
        values.put(KEY_PROPOSAL_IS_MINE,proposal.isMine());
        values.put(KEY_PROPOSAL_IS_SENT,proposal.isSent());
        if (proposal.getLockedOutputHash()!=null)values.put(KEY_PROPOSAL_LOCKED_OUTPUT_HASH,proposal.getLockedOutputHash());
        if (proposal.getLockedOutputIndex()!=-1) values.put(KEY_PROPOSAL_LOCKED_OUTPUT_POSITION,proposal.getLockedOutputIndex());
        values.put(KEY_PROPOSAL_VERSION,proposal.getVersion());
        values.put(KEY_PROPOSAL_OWNER_PUBKEY,proposal.getOwnerPubKey());
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