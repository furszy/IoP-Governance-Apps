package iop_sdk.governance.propose;

import com.google.protobuf.ByteString;

import org.bitcoinj.core.Sha256Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iop_sdk.crypto.CryptoBytes;
import iop_sdk.global.exceptions.NotValidParametersException;
import iop_sdk.governance.ProposalForum;

import static iop_sdk.global.utils.Preconditions.checkEquals;
import static iop_sdk.governance.ProposalForum.TAG_BENEFICIARY_ADDRESS;
import static iop_sdk.governance.ProposalForum.TAG_BENEFICIARY_VALUE;
import static iop_sdk.governance.ProposalForum.TAG_BLOCK_REWARD;
import static iop_sdk.governance.ProposalForum.TAG_BODY;
import static iop_sdk.governance.ProposalForum.TAG_END_BLOCK;
import static iop_sdk.governance.ProposalForum.TAG_START_BLOCK;
import static iop_sdk.governance.ProposalForum.TAG_SUBTITLE;
import static iop_sdk.governance.ProposalForum.TAG_TITLE;


/**
 * Created by mati on 07/11/16.
 * //todo: ponerle el class en el tag para determinar que campo es.
 */

public class Proposal implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Proposal.class);

    public static final int BLOCK_REWARD_MAX_VALUE = 10000000;
    public static final double START_BLOCK_MAX_VALUE = Math.pow((double) 2, (double) 24);


    /**
     * Proposal states
     * SUBMITTED:  Transaction confirmed on blockchain. No votes yet.
     * APPROVED: YES > NO. Current height  > (BlockStart + 1000 blocks).
     * NOT_APPROVED: NO > YES. Current height  > (BlockStart + 1000 blocks)
     * QUEUED_FOR_EXECUTION: YES > NO. Current height  < (BlockStart + 1000 blocks).
     * DEQUEUED: NO > YES. Current height  < (BlockStart + 1000 blocks).
     * IN_EXECUTION: YES > NO. Current height  > BlockStart
     * EXECUTION_CANCELLED: NO > YES. Current height  > BlockStart
     * EXECUTED: YES > NO. Current height  > BlockEnd
     */
    public enum ProposalState{
        DRAFT,          // Proposal in a edit state
        FORUM,          // Proposal created and posted in the forum
        VOTING,         // Proposal in blockchain being voted
        CANCELED_BY_OWNER,  // Proposal canceled by the owner, moving the locked funds to another address.
        APPOVED,        // Proposal YES > NO && current height > (blockStart + 1000 blocks)
        NOT_APPROVED,   // Proposal NO > YES && current height > (blockStart + 1000 blocks)
        // todo: faltan estados..
        EXECUTED        // Proposal YES > NO && Current height > blockEnd
    }

    private boolean isMine;
    private boolean isSent;
    private String lockedOutputHashHex;
    private long lockedOutputIndex;
    private ProposalState state = ProposalState.DRAFT;
    private byte[] blockchainHash;
    // IoPIP -> IoP improvement proposal
    private short version = 0x0100;
    private String title;
    private String subTitle;
    private String category;
    private String body;
    private int startBlock;
    private int endBlock;
    private long blockReward;
    /** topic id */
    private int forumId;
    /** post id */
    private int forumPostId;
    // address -> value
    private Map<String,Long> beneficiaries;
    /** Contributor owner */
    private byte[] ownerPubKey;
    /** will be used to put the proposal upper or lower in the voters list */
    private long extraFeeValue = 1000;

    public static Proposal buildRandomProposal(){
        Proposal proposal = new Proposal();
        proposal.setMine(true);
        proposal.setSent(false);
        proposal.setState(ProposalState.DRAFT);
        proposal.setTitle("Propuesta a enviar numero 1011");
        proposal.setSubTitle("subTitulo4");
        proposal.setCategory("categoria");
        proposal.setBody("Esta es una propuesta para crear propuestas, por lo cual debo poner cosas locas acá. ");
        proposal.setStartBlock(10);
        proposal.setEndBlock(10);
        proposal.setBlockReward(8000000);
        return proposal;
    }

    /**
     * StringBuilder stringBuilder = new StringBuilder()
     .append("<h1>"+title+"</h1>")
     .append("<h2>"+subTitle+"</h2>")
     .append("<br/>")
     .append("<p><body>"+body+"</body></p>")
     .append("Start block: <startBlock>"+startBlock+"</startBlock>")
     .append("   ")
     .append("EndBlock: <endBlock>"+endBlock+"</endBlock>")
     .append("<br/>")
     .append("Block reward <blockReward>"+blockReward+"</blockReward>")
     .append("<br/>");

     int pos = 1;
     for (Map.Entry<String, Long> beneficiary : beneficiaries.entrySet()) {
     stringBuilder.append("Beneficiary"+pos+": Address: <address>"+beneficiary.getKey()+"</address>   value: <value>"+beneficiary.getValue()+"</value> IoPs");
     }

     return stringBuilder.toString();
     * @param formatedBody
     */
    public static Proposal buildFromBody(String formatedBody) {
        LOG.info("buildFromBody: "+formatedBody);
        Proposal proposal = new Proposal();
        proposal.setTitle(getTagValue(formatedBody, PATTERN_TAG_TITLE,0));
        proposal.setSubTitle(getTagValue(formatedBody, PATTERN_TAG_SUBTITLE,0));
        proposal.setBody(getTagValue(formatedBody,PATTERN_TAG_BODY,0));
        proposal.setStartBlock(Integer.parseInt(getTagValue(formatedBody,PATTERN_TAG_START_BLOCK,1)));
        proposal.setEndBlock(Integer.parseInt(getTagValue(formatedBody,PATTERN_TAG_END_BLOCK,2)));
        proposal.setBlockReward(Long.parseLong(getTagValue(formatedBody,PATTERN_TAG_BLOCK_REWARD,3)));
        String address = getTagValue(formatedBody,PATTERN_TAG_BENEFICIARY_ADDRESS,4);
        long value = Long.parseLong(getTagValue(formatedBody,PATTERN_TAG_BENEFICIARY_VALUE,5));
        proposal.addBeneficiary(address,value);
        return proposal;
    }

    private static final Pattern PATTERN_TAG_TITLE = Pattern.compile(replaceTag(TAG_TITLE,"(.+?)"));//("<h1>(.+?)</h1>");
    private static final Pattern PATTERN_TAG_SUBTITLE = Pattern.compile(replaceTag(TAG_SUBTITLE,"(.+?)"));//("<h2>(.+?)</h2>");
    private static final Pattern PATTERN_TAG_BODY = Pattern.compile(replaceTag(TAG_BODY,"(.+?)"));//("<contract_body>(.+?)</contract_body>");
    private static final Pattern PATTERN_TAG_START_BLOCK = Pattern.compile(replaceTag(TAG_START_BLOCK,"(.+?)"));//("<startBlock>(.+?)</startBlock>");
    private static final Pattern PATTERN_TAG_END_BLOCK = Pattern.compile(replaceTag(TAG_END_BLOCK,"(.+?)"));//("<endBlock>(.+?)</endBlock>");
    private static final Pattern PATTERN_TAG_BLOCK_REWARD = Pattern.compile(replaceTag(TAG_BLOCK_REWARD,"(.+?)"));//("<blockReward>(.+?)</blockReward>");
    private static final Pattern PATTERN_TAG_BENEFICIARY_ADDRESS = Pattern.compile(replaceTag(TAG_BENEFICIARY_ADDRESS,"(.+?)"));//("<address>(.+?)</address>");
    private static final Pattern PATTERN_TAG_BENEFICIARY_VALUE = Pattern.compile(replaceTag(TAG_BENEFICIARY_VALUE,"(.+?)"));//("<value>(.+?)</value>");

    private static String replaceTag(String tag,String replace){
        return tag.replace("?",replace);
    }

    private static List<String> getTagValues(final String str, Pattern pattern) {
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

    private static String getTagValue(final String str,Pattern pattern, int index){
        List<String> list = getTagValues(str,pattern);
        if (!list.isEmpty()){
            return list.get(index);
        }else
            LOG.info("Tag not found for pattern: "+pattern.toString());
        return null;
    }

    public Proposal(){
        beneficiaries = new HashMap<>();
    }

    public Proposal(boolean isMine, String title, String subTitle, String category,String body, int startBlock, int endBlock, long blockReward, int forumId, Map<String, Long> beneficiaries,long extraFeeValue,boolean isSent,String lockedOutputHashhex,long lockedOutputIndex,short version,byte[] ownerPk,ProposalState proposalState) {
        this.isMine = isMine;
        this.title = title;
        this.subTitle = subTitle;
        this.category = category;
        this.body = body;
        this.startBlock = startBlock;
        this.endBlock = endBlock;
        this.blockReward = blockReward;
        this.forumId = forumId;
        this.beneficiaries = beneficiaries;
        this.extraFeeValue = extraFeeValue;
        this.isSent = isSent;
        this.lockedOutputHashHex = lockedOutputHashhex;
        this.lockedOutputIndex = lockedOutputIndex;
        this.version = version;
        this.ownerPubKey = ownerPk;
        this.state = proposalState;
    }

    public byte[] hash(){


        ByteString buffTitle = ByteString.copyFromUtf8(this.title);
        ByteString buffSubTitle = ByteString.copyFromUtf8(this.subTitle);
//        ByteString buffCategory = ByteString.copyFromUtf8(this.category);
//        ByteString buffLink = ByteString.copyFromUtf8(forumLink);


        ByteBuffer byteBuffer = ByteBuffer.allocate(4048);
        byteBuffer.put(buffTitle.toByteArray());
        byteBuffer.put(buffSubTitle.toByteArray());
//        byteBuffer.put(buffCategory.toByteArray());
        byteBuffer.putInt(startBlock);
        byteBuffer.putInt(endBlock);
        byteBuffer.putLong(blockReward);
//        byteBuffer.put(buffLink.toByteArray());

        int position = byteBuffer.position();
        byte[] buffToHash = new byte[position];
        byteBuffer.get(buffToHash,0,position);

        return Sha256Hash.hash(buffToHash);
    }

    /**
     * Check hash, esto se va a hacer cuando setee los datos del foro el la propuesta decodificada de la blockchain
     * @return
     */
    public boolean checkHash() {
        if (blockchainHash!=null){
            return Arrays.equals(blockchainHash,hash());
        }
        return false;
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
//        stringBuilder.append(appendFieldsWithName("forumLink",forumLink));
        stringBuilder.append(appendFieldsWithName("version",version));

        return stringBuilder.toString();
    }

    public String toForumBody(){

        StringBuilder stringBuilder = new StringBuilder()
                        .append(replaceTag(TAG_TITLE,title))//"<h1>"+title+"</h1>")
                        .append(replaceTag(TAG_SUBTITLE,subTitle))//"<h2>"+subTitle+"</h2>")
                        .append("<br/>")
                        .append("<p><body>"+replaceTag(TAG_BODY,body)+"</body></p>")
                        .append("Start block: "+replaceTag(TAG_START_BLOCK, String.valueOf(startBlock)))
                        .append("   ")
                        .append("EndBlock: "+replaceTag(TAG_END_BLOCK, String.valueOf(endBlock)))
                        .append("<br/>")
                        .append("Block reward "+replaceTag(TAG_BLOCK_REWARD, String.valueOf(blockReward)))
                        .append("<br/>");

        int pos = 1;
        for (Map.Entry<String, Long> beneficiary : beneficiaries.entrySet()) {
            stringBuilder.append("Beneficiary"+pos+": Address: <address>"+replaceTag(TAG_BENEFICIARY_ADDRESS,beneficiary.getKey())+"</address>   value: "+replaceTag(TAG_BENEFICIARY_VALUE, String.valueOf(beneficiary.getValue()))+" IoPs");
        }

        return stringBuilder.toString();

    }


    private String appendFieldsWithName(String key,Object value){
        return key+"="+value;
    }

    public void addBeneficiary(String address,long value){
        beneficiaries.put(address,value);
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

    public Map<String, Long> getBeneficiaries() {
        return beneficiaries;
    }

    public long getExtraFeeValue() {
        return extraFeeValue;
    }

    public String getBody() {
        return body;
    }


    public byte[] getBlockchainHash() {
        return blockchainHash;
    }
    public short getVersion() {
        return version;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isSent() {
        return isSent;
    }

    public String getLockedOutputHashHex() {
        return lockedOutputHashHex;
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

    public void setLockedOutputHashHex(String lockedOutputHashHex) {
        this.lockedOutputHashHex = lockedOutputHashHex;
    }

    public int getForumId() {
        return forumId;
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
        if (startBlock>START_BLOCK_MAX_VALUE) throw new IllegalArgumentException("Start block must be lower than "+START_BLOCK_MAX_VALUE);
        this.startBlock = startBlock;
    }

    public void setEndBlock(int endBlock) {
        this.endBlock = endBlock;
    }

    public void setBlockReward(long blockReward) {
        if (blockReward>BLOCK_REWARD_MAX_VALUE)throw new IllegalArgumentException("Block reward must be lower than "+BLOCK_REWARD_MAX_VALUE);
        this.blockReward = blockReward;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
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

    public ProposalState getState() {
        return state;
    }

    public void setState(ProposalState state) {
        this.state = state;
    }

    public int getForumPostId() {
        return forumPostId;
    }

    public void setForumPostId(int forumPostId) {
        this.forumPostId = forumPostId;
    }

    public void setBlockchainHash(byte[] blockchainHash) {
        this.blockchainHash = blockchainHash;
    }

    public boolean equals(Proposal o2) throws NotValidParametersException {
        checkEquals(getTitle(),o2.getTitle(),"tittle is changed");
        checkEquals(getSubTitle(),o2.getSubTitle(),"Subtitle is changed");
        checkEquals(getBody(),o2.getBody(),"Body is changed");
        checkEquals(getStartBlock(),o2.getStartBlock(),"StartBlock is changed");
        checkEquals(getEndBlock(),o2.getEndBlock(),"EndBlock is changed");
        checkEquals(getBlockReward(),o2.getBlockReward(),"BlockReward is changed");
        for (Map.Entry<String, Long> beneficiary : getBeneficiaries().entrySet()) {
            if (!o2.getBeneficiaries().containsKey(beneficiary.getKey())) throw new NotValidParametersException("Beneficiary address is changed");
            if (!o2.getBeneficiaries().containsValue(beneficiary.getValue())) throw new NotValidParametersException("Beneficiary value is changed");
        }
        return true;
    }

    public String toStringBlockchain(){
        return  "Proposal{" +
                ", startBlock=" + startBlock +
                ", endBlock=" + endBlock +
                ", blockReward=" + blockReward +
                ", forumId=" + forumId +
                ", blockchainHash="+ CryptoBytes.toHexString(blockchainHash)+
                '}';
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "isMine=" + isMine +
                ", isSent=" + isSent +
                ", lockedOutputHash=" + lockedOutputHashHex +
                ", lockedOutputIndex=" + lockedOutputIndex +
                ", state=" + state +
                ", version=" + version +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", category='" + category + '\'' +
                ", body='" + body + '\'' +
                ", startBlock=" + startBlock +
                ", endBlock=" + endBlock +
                ", blockReward=" + blockReward +
                ", forumId=" + forumId +
                ", beneficiaries=" + beneficiaries +
                ", ownerPubKey=" + Arrays.toString(ownerPubKey) +
                ", extraFeeValue=" + extraFeeValue +
                '}';
    }
}