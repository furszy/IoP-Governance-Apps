package iop.org.iop_contributors_app.core.iop_sdk.governance;

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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import iop.org.iop_contributors_app.utils.exceptions.NotValidParametersException;

import static iop.org.iop_contributors_app.utils.Preconditions.checkEquals;

/**
 * Created by mati on 07/11/16.
 */

public class Proposal implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Proposal.class);

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
        ACTIVE,VOTING,ACCEPTED,REFUSED,FINISHED
    }

    private boolean isMine = true;
    private boolean isSent = false;
    private byte[] lockedOutputHash;
    private long lockedOutputIndex;
    private ProposalState state = ProposalState.ACTIVE;
    // IoPIP -> IoP improvement proposal
    private short version = 0x0100;
    private String title = "Propuesta a enviar numero 1011";
    private String subTitle = "subTitulo4";
    private String category = "categoria";
    private String body = "Esta es una propuesta para crear propuestas, por lo cual debo poner cosas locas acá. ";
    private int startBlock = 10;
    private int endBlock = 10;
    private long blockReward = 80000000;
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
        Proposal proposal = new Proposal();
        proposal.setTitle(getTagValue(formatedBody,TAG_TITLE));
        proposal.setSubTitle(getTagValue(formatedBody,TAG_SUBTITLE));
        proposal.setBody(getTagValue(formatedBody,TAG_BODY));
        proposal.setStartBlock(Integer.parseInt(getTagValue(formatedBody,TAG_START_BLOCK)));
        proposal.setEndBlock(Integer.parseInt(getTagValue(formatedBody,TAG_END_BLOCK)));
        proposal.setBlockReward(Long.parseLong(getTagValue(formatedBody,TAG_BLOCK_REWARD)));
        String address = getTagValue(formatedBody,TAG_BENEFICIARY_ADDRESS);
        long value = Long.parseLong(getTagValue(formatedBody,TAG_BENEFICIARY_VALUE));
        proposal.addBeneficiary(address,value);
        return proposal;
    }
    private static final Pattern TAG_REGEX = Pattern.compile("" +
            "<h1>(.+?)</h1>" +
            "|" +
            "<h2>(.+?)</h2>" +
            "|" +
            "<body>(.+?)</body>" +
            "|"+
            "<startBlock>(.+?)</startBlock>" +
            "|"+
            "<endBlock>(.+?)</endBlock>" +
            "|"+
            "<address>(.+?)</address>" +
            "|"+
            "<value>(.+?)</value>"
    );

    private static final Pattern TAG_TITLE = Pattern.compile("<h1>(.+?)</h1>");
    private static final Pattern TAG_SUBTITLE = Pattern.compile("<h2>(.+?)</h2>");
    private static final Pattern TAG_BODY = Pattern.compile("<contract_body>(.+?)</contract_body>");
    private static final Pattern TAG_START_BLOCK = Pattern.compile("<startBlock>(.+?)</startBlock>");
    private static final Pattern TAG_END_BLOCK = Pattern.compile("<endBlock>(.+?)</endBlock>");
    private static final Pattern TAG_BLOCK_REWARD = Pattern.compile("<blockReward>(.+?)</blockReward>");
    private static final Pattern TAG_BENEFICIARY_ADDRESS = Pattern.compile("<address>(.+?)</address>");
    private static final Pattern TAG_BENEFICIARY_VALUE = Pattern.compile("<value>(.+?)</value>");


    private static List<String> getTagValues(final String str, Pattern pattern) {
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }

    private static String getTagValue(final String str,Pattern pattern){
        List<String> list = getTagValues(str,pattern);
        if (!list.isEmpty()){
            return list.get(0);
        }else
            LOG.info("Tag not found for pattern: "+pattern.toString());
        return null;
    }

    public Proposal(){
        beneficiaries = new HashMap<>();
    }

    public Proposal(boolean isMine, String title, String subTitle, String category,String body, int startBlock, int endBlock, long blockReward, int forumId, Map<String, Long> beneficiaries,long extraFeeValue,boolean isSent,byte[] lockedOutputHash,long lockedOutputIndex,short version,byte[] ownerPk) {
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
        this.lockedOutputHash = lockedOutputHash;
        this.lockedOutputIndex = lockedOutputIndex;
        this.version = version;
        this.ownerPubKey = ownerPk;
    }

    public byte[] hash(){


        ByteString buffTitle = ByteString.copyFromUtf8(this.title);
        ByteString buffSubTitle = ByteString.copyFromUtf8(this.subTitle);
        ByteString buffCategory = ByteString.copyFromUtf8(this.category);
//        ByteString buffLink = ByteString.copyFromUtf8(forumLink);


        ByteBuffer byteBuffer = ByteBuffer.allocate(4048);
        byteBuffer.put(buffTitle.toByteArray());
        byteBuffer.put(buffSubTitle.toByteArray());
        byteBuffer.put(buffCategory.toByteArray());
        byteBuffer.putInt(startBlock);
        byteBuffer.putInt(endBlock);
        byteBuffer.putLong(blockReward);
//        byteBuffer.put(buffLink.toByteArray());

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
//        stringBuilder.append(appendFieldsWithName("forumLink",forumLink));
        stringBuilder.append(appendFieldsWithName("version",version));

        return stringBuilder.toString();
    }

    public String toForumBody(){

        StringBuilder stringBuilder = new StringBuilder()
                        .append("<h1>"+title+"</h1>")
                        .append("<h2>"+subTitle+"</h2>")
                        .append("<br/>")
                        .append("<contract_body><p><body>"+body+"</body></p></contract_body>")
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

    public short getVersion() {
        return version;
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
        this.startBlock = startBlock;
    }

    public void setEndBlock(int endBlock) {
        this.endBlock = endBlock;
    }

    public void setBlockReward(long blockReward) {
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

    @Override
    public String toString() {
        return "Proposal{" +
                "isMine=" + isMine +
                ", isSent=" + isSent +
                ", lockedOutputHash=" + Arrays.toString(lockedOutputHash) +
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
