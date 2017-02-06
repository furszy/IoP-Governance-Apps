package iop_sdk.profile_server.protocol;

import com.google.protobuf.ByteString;

import java.util.concurrent.atomic.AtomicInteger;

import iop_sdk.profile_server.Signer;


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
    public static IopProfileServer.Message buildPingRequestMessage(byte[] payload,byte[] version){
        IopProfileServer.PingRequest.Builder pingRequest = IopProfileServer.PingRequest.newBuilder().setPayload(ByteString.copyFrom(payload));
        IopProfileServer.SingleRequest.Builder singleRequestBuilder = IopProfileServer.SingleRequest.newBuilder().setPing(pingRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequestBuilder);

    }

    /**
     * Build ping response message
     *
     * @param pingResponse
     * @param version
     * @return
     */
    public static IopProfileServer.Message buildPingResponseMessage(IopProfileServer.PingResponse pingResponse,byte[] version){
        IopProfileServer.SingleResponse.Builder singleResponseBuilder = IopProfileServer.SingleResponse.newBuilder().setPing(pingResponse).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleResponseBuilder);
    }

    /**
     *  Build server list roles request message
     *
     * @param version
     * @return
     */

    public static IopProfileServer.Message buildServerListRolesRequestMessage(byte[] version){
        IopProfileServer.ListRolesRequest.Builder listRolesRequest = IopProfileServer.ListRolesRequest.newBuilder();
        IopProfileServer.SingleRequest.Builder singleRequest = IopProfileServer.SingleRequest.newBuilder().setListRoles(listRolesRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequest);
    }

    /**
     *  Build server list roles response message
     *
     * @param listRolesRequest
     * @param version
     * @return
     */
    public static IopProfileServer.Message buildServerListRolesResponseMessage(IopProfileServer.ListRolesResponse listRolesRequest,byte[] version){
        IopProfileServer.SingleResponse.Builder singleRequest = IopProfileServer.SingleResponse.newBuilder().setListRoles(listRolesRequest).setVersion(ByteString.copyFrom(version));
        return buildMessage(singleRequest);
    }

    public static IopProfileServer.Message buildHomeNodeRequestRequest(byte[] identityPk,String identityType,long contractStartTime,byte[] planId){
        IopProfileServer.HostingPlanContract.Builder contract = IopProfileServer.HostingPlanContract
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
    public static IopProfileServer.ServerRole buildServerRole(IopProfileServer.ServerRoleType roleType, int port, boolean isSecure, boolean isTcp){
        return IopProfileServer.ServerRole.newBuilder().setIsTcp(isTcp).setIsTls(isSecure).setPort(port).setRole(roleType).build();
    }

    public static IopProfileServer.HostingPlan buildHomeNodePlan(String planId,String identityType,int billingPeriodseconds,long fee){
        return IopProfileServer.HostingPlan.newBuilder().setIdentityType(identityType).setBillingPeriodSeconds(billingPeriodseconds).setPlanId(ByteString.copyFromUtf8(planId)).setFee(fee).build();
    }

    public static IopProfileServer.HostingPlanContract buildHomeNodePlanContract(String identityPk, long startTime, String planId){
        return IopProfileServer.HostingPlanContract.newBuilder().setIdentityPublicKey(ByteString.copyFromUtf8(identityPk)).setStartTime(startTime).setPlanId(ByteString.copyFromUtf8(planId)).build();
    }

    public static IopProfileServer.Message buildHomeNodeRequestRequest(IopProfileServer.HostingPlanContract.Builder contract){
        IopProfileServer.RegisterHostingRequest homeNodeRequestRequest = IopProfileServer.RegisterHostingRequest.newBuilder().setContract(contract).build();
        IopProfileServer.ConversationRequest.Builder conversaBuilder = IopProfileServer.ConversationRequest.newBuilder().setRegisterHosting(homeNodeRequestRequest);
        return buildMessage(conversaBuilder);
    }

    public static IopProfileServer.Message buildHomeNodeResponseRequest(IopProfileServer.HostingPlanContract contract,String signature){
        IopProfileServer.RegisterHostingResponse homeNodeRequestRequest = IopProfileServer.RegisterHostingResponse.newBuilder().setContract(contract).build();
        IopProfileServer.ConversationResponse.Builder conversaBuilder = IopProfileServer.ConversationResponse.newBuilder().setRegisterHosting(homeNodeRequestRequest);
        conversaBuilder.setSignature(ByteString.copyFromUtf8(signature));
        return buildMessage(conversaBuilder);
    }

    public static IopProfileServer.Message buildCheckInResponse(String signature){
        IopProfileServer.CheckInResponse checkInResponse = IopProfileServer.CheckInResponse.newBuilder().build();
        IopProfileServer.ConversationResponse.Builder conversaBuilder = IopProfileServer.ConversationResponse.newBuilder().setCheckIn(checkInResponse);
        conversaBuilder.setSignature(ByteString.copyFromUtf8(signature));
        return buildMessage(conversaBuilder);
    }



    public static IopProfileServer.Message buildStartConversationRequest(byte[] clientPk,byte[] challenge,byte[] version){
        IopProfileServer.StartConversationRequest.Builder builder = IopProfileServer.StartConversationRequest
                .newBuilder()
                    .setClientChallenge(ByteString.copyFrom(challenge))
                    .setPublicKey(ByteString.copyFrom(clientPk))
                    .addSupportedVersions(ByteString.copyFrom(version));
        return buildMessage(builder);
    }


    public static IopProfileServer.Message buildCheckInRequest(byte[] nodeChallenge, Signer signer){
        IopProfileServer.CheckInRequest.Builder builder = IopProfileServer.CheckInRequest
                .newBuilder()
                    .setChallenge(ByteString.copyFrom(nodeChallenge));
        IopProfileServer.CheckInRequest checkInRequest = builder.build();
        byte[] signature = signer.sign(checkInRequest.toByteArray());
        return buildMessage(checkInRequest,signature);
    }

    public static IopProfileServer.Message buildUpdateProfileRequest(Signer signer, byte[] version, String name, byte[] img, int latitude, int longitude, String extraData) {
        IopProfileServer.UpdateProfileRequest.Builder updateProfileRequest = IopProfileServer.UpdateProfileRequest.newBuilder();

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


        IopProfileServer.UpdateProfileRequest request = updateProfileRequest.build();
        byte[] signature = signer.sign(request.toByteArray());
        return buildMessage(request,signature);
    }



    /**
     * Error messages
     */


    public static IopProfileServer.Message buildInvalidMessageHeaderResponse(){
        return buildMessage(IopProfileServer.Status.ERROR_PROTOCOL_VIOLATION);
    }

    public static IopProfileServer.Message buildVersionNotSupportResponse() {
        // todo: Creo que se devuelve el unnsopported cuando la versión no es valida, deberia chequear esto..
        return buildMessage(IopProfileServer.Status.ERROR_UNSUPPORTED);
    }


    /**
     *  Private builders
     */

    private static IopProfileServer.Message buildMessage(IopProfileServer.Status responseStatus){
        return buildMessage(IopProfileServer.Response.newBuilder().setStatus(responseStatus));
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.SingleRequest.Builder singleRequest){
        IopProfileServer.Request.Builder requestBuilder = IopProfileServer.Request.newBuilder().setSingleRequest(singleRequest);
        return buildMessage(requestBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.ConversationRequest.Builder conversationRequest){
        IopProfileServer.Request.Builder requestBuilder = IopProfileServer.Request.newBuilder().setConversationRequest(conversationRequest);
        return buildMessage(requestBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.Request.Builder request){
        IopProfileServer.Message.Builder messageBuilder = IopProfileServer.Message.newBuilder().setRequest(request);
        return initMessage(messageBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.SingleResponse.Builder singleResponse){
        IopProfileServer.Response.Builder responseBuilder = IopProfileServer.Response.newBuilder().setSingleResponse(singleResponse);
        return buildMessage(responseBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.Response.Builder response){
        IopProfileServer.Message.Builder messageBuilder = IopProfileServer.Message.newBuilder().setResponse(response);
        return initMessage(messageBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.ConversationResponse.Builder conversationResponse) {
        IopProfileServer.Response.Builder requestBuilder = IopProfileServer.Response.newBuilder().setConversationResponse(conversationResponse);
        return buildMessage(requestBuilder);
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.CheckInRequest checkInRequestBuilder,byte[] signature) {
        return buildMessage(IopProfileServer.ConversationRequest.newBuilder().setSignature(ByteString.copyFrom(signature)).setCheckIn(checkInRequestBuilder));
    }
    private static IopProfileServer.Message buildMessage(IopProfileServer.UpdateProfileRequest updateProfileRequest,byte[] signature) {
        return buildMessage(IopProfileServer.ConversationRequest.newBuilder().setSignature(ByteString.copyFrom(signature)).setUpdateProfile(updateProfileRequest));
    }

    private static IopProfileServer.Message buildMessage(IopProfileServer.StartConversationRequest.Builder startConversationBuilder) {
        return buildMessage(IopProfileServer.ConversationRequest.newBuilder().setStart(startConversationBuilder));
    }

    private static IopProfileServer.Message initMessage(IopProfileServer.Message.Builder messageBuilder) {
        messageBuilder.setId(getMessageId());
        return messageBuilder.build();
    }

    private static int getMessageId(){
        return messageIdGenerator.incrementAndGet();
    }


}
