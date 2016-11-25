package iop.org.iop_contributors_app.core;

import com.google.protobuf.ByteString;

import org.bitcoinj.core.Sha256Hash;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mati on 07/11/16.
 */

public class Proposal implements Serializable {


    // internal app id
    private long id = 1;
    private boolean isMine = false;
    private boolean isSent = false;
    private byte[] lockedOutputHash;
    private long lockedOutputIndex;
    // IoPIP -> IoP improvement proposal
    private long IoPIP = 1;
    private short version = 0x0100;
    private String title = "Propuesta 1";
    private String subTitle = "subTitulo1";
    private String category = "categoria";
    private String body = "body";
    private int startBlock = 10;
    private int endBlock = 10;
    private long blockReward = 80000000;
    private String forumLink = "link.com";
    // address -> value
    private Map<String,Long> beneficiaries;
    /** Contributor owner */
    private byte[] ownerPubKey;
    /** will be used to put the proposal upper or lower in the voters list */
    private long extraFeeValue = 1000;



    public Proposal(){
        beneficiaries = new HashMap<>();
    }

    public Proposal(long id,boolean isMine,long IoPIP, String title, String subTitle, String category,String body, int startBlock, int endBlock, long blockReward, String forumLink, Map<String, Long> beneficiaries,long extraFeeValue,boolean isSent,byte[] lockedOutputHash,long lockedOutputIndex,short version,byte[] ownerPk) {
        this.id = id;
        this.isMine = isMine;
        this.IoPIP = IoPIP;
        this.title = title;
        this.subTitle = subTitle;
        this.category = category;
        this.body = body;
        this.startBlock = startBlock;
        this.endBlock = endBlock;
        this.blockReward = blockReward;
        this.forumLink = forumLink;
        this.beneficiaries = beneficiaries;
        this.extraFeeValue = extraFeeValue;
        this.isSent = isSent;
        this.lockedOutputHash = lockedOutputHash;
        this.lockedOutputIndex = lockedOutputIndex;
        this.version = version;
        this.ownerPubKey = ownerPk;
    }

    public byte[] hash(){


        ByteString buffTitle = ByteString.copyFromUtf8(this.title);
        ByteString buffSubTitle = ByteString.copyFromUtf8(this.subTitle);
        ByteString buffCategory = ByteString.copyFromUtf8(this.category);
        ByteString buffLink = ByteString.copyFromUtf8(forumLink);


        ByteBuffer byteBuffer = ByteBuffer.allocate(4048);
        byteBuffer.putLong(IoPIP);
        byteBuffer.put(buffTitle.toByteArray());
        byteBuffer.put(buffSubTitle.toByteArray());
        byteBuffer.put(buffCategory.toByteArray());
        byteBuffer.putInt(startBlock);
        byteBuffer.putInt(endBlock);
        byteBuffer.putLong(blockReward);
        byteBuffer.put(buffLink.toByteArray());

        int position = byteBuffer.position();
        byte[] buffToHash = new byte[position];
        byteBuffer.get(buffToHash,0,position);

        return Sha256Hash.hash(buffToHash);
    }

    public boolean checkHash(byte[] hash) {
        return Arrays.equals(hash(), hash);
    }

    public String buildExtraData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appendFieldsWithName("title",title));
        stringBuilder.append(appendFieldsWithName("subtitle",subTitle));
        stringBuilder.append(appendFieldsWithName("category",category));
        stringBuilder.append(appendFieldsWithName("body",body));
        stringBuilder.append(appendFieldsWithName("startBlock",startBlock));
        stringBuilder.append(appendFieldsWithName("endBlockHeight",endBlock));
        stringBuilder.append(appendFieldsWithName("blockReward",blockReward));
        stringBuilder.append(appendFieldsWithName("forumLink",forumLink));
        stringBuilder.append(appendFieldsWithName("version",version));

        return stringBuilder.toString();
    }


    private String appendFieldsWithName(String key,Object value){
        return key+"="+value;
    }

    public void addBeneficiary(String address,long value){
        beneficiaries.put(address,value);
    }

    public long getId() {
        return id;
    }

    public long getIoPIP() {
        return IoPIP;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getCategory() {
        return category;
    }

    public int getStartBlock() {
        return startBlock;
    }

    public int getEndBlock() {
        return endBlock;
    }

    public long getBlockReward() {
        return blockReward;
    }

    public String getForumLink() {
        return forumLink;
    }

    public Map<String, Long> getBeneficiaries() {
        return beneficiaries;
    }

    public long getExtraFeeValue() {
        return extraFeeValue;
    }

    public String getBody() {
        return body;
    }

    public short getVersion() {
        return version;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isSent() {
        return isSent;
    }

    public byte[] getLockedOutputHash() {
        return lockedOutputHash;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public void setLockedOutputHash(byte[] lockedOutputHash) {
        this.lockedOutputHash = lockedOutputHash;
    }

    public void setIoPIP(long ioPIP) {
        IoPIP = ioPIP;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setStartBlock(int startBlock) {
        this.startBlock = startBlock;
    }

    public void setEndBlock(int endBlock) {
        this.endBlock = endBlock;
    }

    public void setBlockReward(long blockReward) {
        this.blockReward = blockReward;
    }

    public void setForumLink(String forumLink) {
        this.forumLink = forumLink;
    }

    public void setBeneficiaries(Map<String, Long> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public void setExtraFeeValue(long extraFeeValue) {
        this.extraFeeValue = extraFeeValue;
    }

    public long getLockedOutputIndex() {
        return lockedOutputIndex;
    }

    public void setLockedOutputIndex(int lockedOutputIndex) {
        this.lockedOutputIndex = lockedOutputIndex;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public void setOwnerPubKey(byte[] ownerPubKey) {
        this.ownerPubKey = ownerPubKey;
    }

    public byte[] getOwnerPubKey() {
        return ownerPubKey;
    }
}
