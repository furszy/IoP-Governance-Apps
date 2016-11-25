package iop.org.iop_contributors_app.profile_server.protocol;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.concurrent.atomic.AtomicInteger;

import iop.org.iop_contributors_app.Signer;

/**
 * Created by mati on 20/09/16.
 */
public class MessageFactory {

    private static AtomicInteger messageIdGenerator = new AtomicInteger(0);

    /**
     * Build ping request message
     *
     * @param payload
     * @param version
     * @return
     */
    public static IopHomeNodeProto3.Message buildPingRequestMessage(byte[] payload,byte[] version){
        IopHomeNodeProto3.PingRequest.Builder pingRequest = IopHomeNodeProto3.PingRequest.newBuilder().setPayload(ByteString.copyFrom(payload));
        IopHomeNodeProto3.SingleRequest.Builder singleRequestBuilder = IopHomeNodeProto3.SingleRequest.newBuilder().setPing(pingRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequestBuilder);

    }

    /**
     * Build ping response message
     *
     * @param pingResponse
     * @param version
     * @return
     */
    public static IopHomeNodeProto3.Message buildPingResponseMessage(IopHomeNodeProto3.PingResponse pingResponse,byte[] version){
        IopHomeNodeProto3.SingleResponse.Builder singleResponseBuilder = IopHomeNodeProto3.SingleResponse.newBuilder().setPing(pingResponse).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleResponseBuilder);
    }

    /**
     *  Build server list roles request message
     *
     * @param version
     * @return
     */

    public static IopHomeNodeProto3.Message buildServerListRolesRequestMessage(byte[] version){
        IopHomeNodeProto3.ListRolesRequest.Builder listRolesRequest = IopHomeNodeProto3.ListRolesRequest.newBuilder();
        IopHomeNodeProto3.SingleRequest.Builder singleRequest = IopHomeNodeProto3.SingleRequest.newBuilder().setListRoles(listRolesRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequest);
    }

    /**
     *  Build server list roles response message
     *
     * @param listRolesRequest
     * @param version
     * @return
     */
    public static IopHomeNodeProto3.Message buildServerListRolesResponseMessage(IopHomeNodeProto3.ListRolesResponse listRolesRequest,byte[] version){
        IopHomeNodeProto3.SingleResponse.Builder singleRequest = IopHomeNodeProto3.SingleResponse.newBuilder().setListRoles(listRolesRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequest);
    }

    public static IopHomeNodeProto3.Message buildHomeNodeRequestRequest(byte[] identityPk,String identityType,long contractStartTime,byte[] planId){
        IopHomeNodeProto3.HomeNodePlanContract.Builder contract = IopHomeNodeProto3.HomeNodePlanContract
                .newBuilder()
                .setIdentityPublicKey(ByteString.copyFrom(identityPk))
                .setIdentityType(identityType)
                .setStartTime(contractStartTime);
        if (planId!=null) contract.setPlanId(ByteString.copyFrom(planId));
        return buildHomeNodeRequestRequest(contract);
    }

    /**
     *  Build serverRole
     *
     * @param roleType
     * @param port
     * @param isSecure
     * @param isTcp
     * @return
     */
    public static IopHomeNodeProto3.ServerRole buildServerRole(IopHomeNodeProto3.ServerRoleType roleType, int port, boolean isSecure, boolean isTcp){
        return IopHomeNodeProto3.ServerRole.newBuilder().setIsTcp(isTcp).setIsTls(isSecure).setPort(port).setRole(roleType).build();
    }

    public static IopHomeNodeProto3.HomeNodePlan buildHomeNodePlan(String planId,String identityType,int billingPeriodseconds,long fee){
        return IopHomeNodeProto3.HomeNodePlan.newBuilder().setIdentityType(identityType).setBillingPeriodSeconds(billingPeriodseconds).setPlanId(ByteString.copyFromUtf8(planId)).setFee(fee).build();
    }

    public static IopHomeNodeProto3.HomeNodePlanContract buildHomeNodePlanContract(String identityPk, long startTime, String planId){
        return IopHomeNodeProto3.HomeNodePlanContract.newBuilder().setIdentityPublicKey(ByteString.copyFromUtf8(identityPk)).setStartTime(startTime).setPlanId(ByteString.copyFromUtf8(planId)).build();
    }

    public static IopHomeNodeProto3.Message buildHomeNodeRequestRequest(IopHomeNodeProto3.HomeNodePlanContract.Builder contract){
        IopHomeNodeProto3.HomeNodeRequestRequest homeNodeRequestRequest = IopHomeNodeProto3.HomeNodeRequestRequest.newBuilder().setContract(contract).build();
        IopHomeNodeProto3.ConversationRequest.Builder conversaBuilder = IopHomeNodeProto3.ConversationRequest.newBuilder().setHomeNodeRequest(homeNodeRequestRequest);
        return buildMessage(conversaBuilder);
    }

    public static IopHomeNodeProto3.Message buildHomeNodeResponseRequest(IopHomeNodeProto3.HomeNodePlanContract contract,String signature){
        IopHomeNodeProto3.HomeNodeRequestResponse homeNodeRequestRequest = IopHomeNodeProto3.HomeNodeRequestResponse.newBuilder().setContract(contract).build();
        IopHomeNodeProto3.ConversationResponse.Builder conversaBuilder = IopHomeNodeProto3.ConversationResponse.newBuilder().setHomeNodeRequest(homeNodeRequestRequest);
        conversaBuilder.setSignature(ByteString.copyFromUtf8(signature));
        return buildMessage(conversaBuilder);
    }

    public static IopHomeNodeProto3.Message buildCheckInResponse(String signature){
        IopHomeNodeProto3.CheckInResponse checkInResponse = IopHomeNodeProto3.CheckInResponse.newBuilder().build();
        IopHomeNodeProto3.ConversationResponse.Builder conversaBuilder = IopHomeNodeProto3.ConversationResponse.newBuilder().setCheckIn(checkInResponse);
        conversaBuilder.setSignature(ByteString.copyFromUtf8(signature));
        return buildMessage(conversaBuilder);
    }



    public static IopHomeNodeProto3.Message buildStartConversationRequest(byte[] clientPk,byte[] challenge,byte[] version){
        IopHomeNodeProto3.StartConversationRequest.Builder builder = IopHomeNodeProto3.StartConversationRequest
                .newBuilder()
                    .setClientChallenge(ByteString.copyFrom(challenge))
                    .setPublicKey(ByteString.copyFrom(clientPk))
                    .addSupportedVersions(ByteString.copyFrom(version));
        return buildMessage(builder);
    }


    public static IopHomeNodeProto3.Message buildCheckInRequest(byte[] nodeChallenge, Signer signer){
        IopHomeNodeProto3.CheckInRequest.Builder builder = IopHomeNodeProto3.CheckInRequest
                .newBuilder()
                    .setChallenge(ByteString.copyFrom(nodeChallenge));
        IopHomeNodeProto3.CheckInRequest checkInRequest = builder.build();
        byte[] signature = signer.sign(checkInRequest.toByteArray());
        return buildMessage(checkInRequest,signature);
    }

    public static IopHomeNodeProto3.Message buildUpdateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) {
        IopHomeNodeProto3.UpdateProfileRequest.Builder updateProfileRequest = IopHomeNodeProto3.UpdateProfileRequest.newBuilder();

        if (version!=null && version.length>0){
            updateProfileRequest.setSetVersion(true);
            updateProfileRequest.setVersion(ByteString.copyFrom(version));
        }else
            updateProfileRequest.setSetVersion(false);

        if (name!=null && !name.equals("")){
            updateProfileRequest.setSetName(true);
            updateProfileRequest.setName(name);
        } else
            updateProfileRequest.setSetName(false);

        if (img!=null && img.length>0){
            updateProfileRequest.setImage(ByteString.copyFrom(img));
            updateProfileRequest.setSetImage(true);
        }else
            updateProfileRequest.setSetImage(false);


        /**
         *
         you take DD format (e.g. 70.456789123 -120.58489489)
         10:52
         and you multiply it by 1,000,000 (edited)
         10:52
         70.456789123 -120.58489489 -> 70456789.123 -120584894.89
         10:53
         then you cut off everything on the right of decimal point
         10:53
         70.456789123 -120.58489489 -> 70456789.123 -120584894.89 -> lat=70456789, lon=-120584894
         10:55
         therefore
         10:55
         //                  For latitudes, valid values are in range [-90,000,000;90,000,000], for longitude the range is
         //                  [-179,999,999;180,000,000]. A special constant NO_LOCATION = (int)0xFFFFFFFF is reserved for no location.

         si la locación es negativa la tengo que poner positiva.
         *
         */

        // todo: ver esto..
//        if (latitude>0 && longitude>0){
            updateProfileRequest.setSetLocation(true);
            updateProfileRequest.setLatitude(latitude);
            updateProfileRequest.setLongitude(longitude);
//        }else {
//            updateProfileRequest.setSetLocation(false);
//            updateProfileRequest.setLongitude(0xFFFFFFFF);
//            updateProfileRequest.setLatitude(0xFFFFFFFF);
//        }

        if (extraData!=null && !extraData.equals("")){
            updateProfileRequest.setExtraData(extraData);
            updateProfileRequest.setSetExtraData(true);
        }else
            updateProfileRequest.setSetExtraData(false);


        IopHomeNodeProto3.UpdateProfileRequest request = updateProfileRequest.build();
        byte[] signature = signer.sign(request.toByteArray());
        return buildMessage(request,signature);
    }



    /**
     * Error messages
     */


    public static IopHomeNodeProto3.Message buildInvalidMessageHeaderResponse(){
        return buildMessage(IopHomeNodeProto3.Status.ERROR_PROTOCOL_VIOLATION);
    }

    public static IopHomeNodeProto3.Message buildVersionNotSupportResponse() {
        // todo: Creo que se devuelve el unnsopported cuando la versión no es valida, deberia chequear esto..
        return buildMessage(IopHomeNodeProto3.Status.ERROR_UNSUPPORTED);
    }


    /**
     *  Private builders
     */

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.Status responseStatus){
        return buildMessage(IopHomeNodeProto3.Response.newBuilder().setStatus(responseStatus));
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.SingleRequest.Builder singleRequest){
        IopHomeNodeProto3.Request.Builder requestBuilder = IopHomeNodeProto3.Request.newBuilder().setSingleRequest(singleRequest);
        return buildMessage(requestBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.ConversationRequest.Builder conversationRequest){
        IopHomeNodeProto3.Request.Builder requestBuilder = IopHomeNodeProto3.Request.newBuilder().setConversationRequest(conversationRequest);
        return buildMessage(requestBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.Request.Builder request){
        IopHomeNodeProto3.Message.Builder messageBuilder = IopHomeNodeProto3.Message.newBuilder().setRequest(request);
        return initMessage(messageBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.SingleResponse.Builder singleResponse){
        IopHomeNodeProto3.Response.Builder responseBuilder = IopHomeNodeProto3.Response.newBuilder().setSingleResponse(singleResponse);
        return buildMessage(responseBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.Response.Builder response){
        IopHomeNodeProto3.Message.Builder messageBuilder = IopHomeNodeProto3.Message.newBuilder().setResponse(response);
        return initMessage(messageBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.ConversationResponse.Builder conversationResponse) {
        IopHomeNodeProto3.Response.Builder requestBuilder = IopHomeNodeProto3.Response.newBuilder().setConversationResponse(conversationResponse);
        return buildMessage(requestBuilder);
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.CheckInRequest checkInRequestBuilder,byte[] signature) {
        return buildMessage(IopHomeNodeProto3.ConversationRequest.newBuilder().setSignature(ByteString.copyFrom(signature)).setCheckIn(checkInRequestBuilder));
    }
    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.UpdateProfileRequest updateProfileRequest,byte[] signature) {
        return buildMessage(IopHomeNodeProto3.ConversationRequest.newBuilder().setSignature(ByteString.copyFrom(signature)).setUpdateProfile(updateProfileRequest));
    }

    private static IopHomeNodeProto3.Message buildMessage(IopHomeNodeProto3.StartConversationRequest.Builder startConversationBuilder) {
        return buildMessage(IopHomeNodeProto3.ConversationRequest.newBuilder().setStart(startConversationBuilder));
    }

    private static IopHomeNodeProto3.Message initMessage(IopHomeNodeProto3.Message.Builder messageBuilder) {
        messageBuilder.setId(getMessageId());
        return messageBuilder.build();
    }

    private static int getMessageId(){
        return messageIdGenerator.incrementAndGet();
    }


}
