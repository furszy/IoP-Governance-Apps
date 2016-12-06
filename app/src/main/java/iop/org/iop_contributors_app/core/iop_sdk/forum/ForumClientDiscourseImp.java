package iop.org.iop_contributors_app.core.iop_sdk.forum;

import android.content.Intent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscourseApiClient;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscouseApiConstants;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseListener;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseMeta;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseModel;
import iop.org.iop_contributors_app.core.iop_sdk.forum.wrapper.ResponseMessageConstants;
import iop.org.iop_contributors_app.core.iop_sdk.governance.Proposal;
import iop.org.iop_contributors_app.utils.exceptions.NotValidParametersException;
import iop.org.iop_contributors_app.wallet.db.CantUpdateProposalException;

import static iop.org.iop_contributors_app.core.iop_sdk.forum.wrapper.ResponseMessageConstants.REGISTER_ERROR_STR;
import static iop.org.iop_contributors_app.core.iop_sdk.forum.wrapper.ResponseMessageConstants.USER_ERROR_STR;
import static iop.org.iop_contributors_app.utils.Preconditions.checkEquals;

/**
 * Created by mati on 28/11/16.
 */

public class ForumClientDiscourseImp implements ForumClient {

    private static final Logger LOG = LoggerFactory.getLogger(ForumClientDiscourseImp.class);

    private final ForumConfigurations conf;

    private DiscourseApiClient client;

    private ForumProfile forumProfile;

    private String forunLink = DiscouseApiConstants.FORUM_URL;

    private String apiKey;

    private boolean isActive;

    public ForumClientDiscourseImp(ForumConfigurations forumConfigurations) {
        this.conf = forumConfigurations;
        apiKey = conf.getApiKey();
        forumProfile = forumConfigurations.getForumUser();
        init();
    }

    private void init(){
        client = new DiscourseApiClient(
                forunLink,//args[0] // api_url  : e.g. http://your_domain.com
                apiKey, //, args[1] // api_key : you get from discourse admin
                (forumProfile!=null)?forumProfile.getUsername():null//, args[2] // api_username : you make calls on behalf of
        );
    }


    @Override
    public ForumProfile getForumProfile() {
        return forumProfile;
    }

    @Override
    public boolean isRegistered() {
        return conf.isRegistered();
    }

    /**
     *  Check if the user exist
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public boolean connect(String username, String password) throws InvalidUserParametersException {
        if (isActive) throw new IllegalStateException("Forum is already connected");
        //init();
        LOG.debug("connect");
        if (apiKey == null) {
            try {
                // request api key to Mati server, if the user exist the api key will be created.

                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("username", username);
                parameters.put("password", password);

                String url = "http://"+DiscouseApiConstants.FORUM_WRAPPER_URL+":7070/fermat/requestkey";

                //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";

                try {
                    HttpClient client = new DefaultHttpClient(new BasicHttpParams());
                    HttpPost httpPost = new HttpPost(url);
                    //httpPost.setHeader("Content-type", "application/vnd.api+json");
                    httpPost.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
                    httpPost.setHeader("Content-type", "application/json");

                    //passes the results to a string builder/entity
                    StringEntity se = new StringEntity(getJsonFromParams(parameters), "UTF-8");
                    //sets the post request as the resulting stringc
                    httpPost.setEntity(se);


                    // make GET request to the given URL
                    HttpResponse httpResponse = client.execute(httpPost);
                    InputStream inputStream = null;
                    // receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();
                    String result = null;
                    // convert inputstream to string
                    if (inputStream != null)
                        result = convertInputStreamToString(inputStream);


                    LOG.info("###########################");
                    LOG.info(result);
                    LOG.info("###########################");

                    JSONObject jsonObject = new JSONObject(result);



                    if (httpResponse.getStatusLine().getStatusCode()==200){
                        apiKey = jsonObject.getString(ResponseMessageConstants.API_KEY);
                        conf.setApiKey(apiKey);
                        this.client.setApiKey(apiKey);
                        forumProfile = new ForumProfile(username,password,null);
                        saveForumData(true,username,password,null);
                        return true;
                    }else {
                        if (jsonObject.has(USER_ERROR_STR)){
                            throw new InvalidUserParametersException(jsonObject.getString(USER_ERROR_STR));
                        }
                        return false;
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * todo: falta probar el tema del topic id y el forum id..
     *
     * @param title
     * @param category
     * @param raw
     * @return
     * @throws CantCreateTopicException
     */
    @Override
    public int createTopic(String title, String category, String raw) throws CantCreateTopicException {
        LOG.info("CreateTopic");
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("username", forumProfile.getUsername());
            //parameters.put("username", new_username);
            parameters.put("category", category);
            parameters.put("title", title);
            parameters.put("raw", raw);
            ResponseModel responseModel = client.createTopic(parameters);
            if (responseModel.meta.code > 201) {
                LOG.error("topic fail. data: " + responseModel.data);
                int index = responseModel.data.toString().lastIndexOf("|");
                String errorDetail = responseModel.data.toString().substring(index+1);
                if (errorDetail.contains("<!DOCTYPE html>")){
                    throw new CantCreateTopicException("Cant create topic, something bad happen with the forum");
                }else
                    throw new CantCreateTopicException(errorDetail);
            } else {
                LOG.info("topic created.");
                LOG.info("### data: "+responseModel.data.toString());
                JSONObject jsonObject = new JSONObject(responseModel.data.toString());
                // post id
                int postId = Integer.valueOf(jsonObject.get("id").toString());
                // forum id
                int topic_id = Integer.valueOf(jsonObject.get("topic_id").toString());
                LOG.info("## Forum id obtained: "+topic_id);
                return topic_id;
            }
        } catch (CantCreateTopicException e){
          throw e;
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    /**
     *
     * curl -X PUT -d id=32 -d post[raw="This is mey weafafearqwrwas]  afafafwaea" http://fermat.community/posts/32.json?api_key=70f4d61d58a2ebcf024e3514e2e64c5e106985ec9d9fd99b9b19bcc8742648e6&api_username=jhon123

     *
     * @param title
     * @param postId
     * @param category
     * @param raw
     * @return
     * @throws CantUpdateProposalException
     */
    @Override
    public boolean updatePost(String title, int postId, String category, String raw) throws CantUpdateProposalException {
        LOG.info("updatePost");
        Map<String, String> parameters = new HashMap<>();
//        parameters.put("username", forumProfile.getUsername());
        parameters.put("id",String.valueOf(postId));
        parameters.put("category", category);
        parameters.put("title", title);
        parameters.put("raw", raw);
        ResponseModel responseModel = client.updatePost(parameters);
        if (responseModel.meta.code > 201) {
            LOG.error("topic fail. data: " + responseModel.data);
            int index = responseModel.data.toString().lastIndexOf("|");
            String errorDetail = responseModel.data.toString().substring(index+1);
            throw new CantUpdateProposalException(errorDetail);
        } else {
            LOG.info("topic updated.");
            LOG.info("### data: "+responseModel.data.toString());
            LOG.info("## Forum id obtained: "+postId);
            return true;
        }
    }

    /**
     * Get topic
     * ../t/:id.json
     *
     * @param forumId
     * @return
     */
    @Override
    public Proposal getProposal(int forumId) {
        LOG.info("getForumProposal");
        Proposal proposal = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id", String.valueOf(forumId));
        ResponseModel responseModel = client.getTopic(parameters);

        try {
            JSONObject jsonObject = new JSONObject(responseModel.data.toString());
            jsonObject = jsonObject.getJSONObject("post_stream");
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
//            JSONArray jsonArray = new JSONArray(jsonObject.get("posts"));

            // post
            jsonObject = (JSONObject) jsonArray.get(0);
            // body
            String formatedBody = jsonObject.getString("cooked");
            proposal = Proposal.buildFromBody(formatedBody);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return proposal;
    }

    @Override
    public boolean getAndCheckValid(Proposal proposal) {
        return true;
//        Proposal forumProposal = getProposal(proposal.getForumId());
//        boolean result = false;
//        try {
//            result = proposal.equals(forumProposal);
//        } catch (NotValidParametersException e) {
//            result = false;
//        }
//        return result;
    }


    public ForumProfile getUser(String username){
        LOG.debug("getUser");
        ForumProfile forumProfile = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters = new HashMap<String, String>();
        parameters.put("username", username);
        ResponseModel responseModel = client.getUser(parameters);
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse( ""+responseModel.data );
        JsonArray jsonArray = null;
        JsonObject userObject = null;
        if (jsonElement!=null) {
            userObject = jsonElement.isJsonObject()?jsonElement.getAsJsonObject():null;
            jsonArray = jsonElement.isJsonArray()?jsonElement.getAsJsonArray():null;
        }

        if (userObject!=null && userObject.has("user")) {
            userObject = userObject.getAsJsonObject("user");
        }else
            return null;

        forumProfile = new ForumProfile(
                userObject.get("id").getAsLong(),
                userObject.get("name").getAsString(),
                userObject.get("username").getAsString()
        );
        return forumProfile;

    }

    /**
     *
     *
     * @param forumProfile
     * @return
     */
    private String requestApiKey(ForumProfile forumProfile){
        // -> generate api_key
        String apiKey = null;
        Map<String, String> parameters = new HashMap<String, String>();
        parameters = new HashMap<String, String>();
        parameters.put("userid", ""+forumProfile.getForumId());
        parameters.put("username", forumProfile.getUsername());
        ResponseModel responseModel = client.generateApiKey(parameters);

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject1 = null;
        jsonObject1 = jsonParser.parse( ""+responseModel.data ).getAsJsonObject();

        if (jsonObject1!=null && jsonObject1.has("api_key")) {
            jsonObject1 = jsonObject1.getAsJsonObject("api_key");

            if (jsonObject1.has("key")) {
                apiKey = jsonObject1.get("key").getAsString();
            }
        }
        return apiKey;
    }

    @Override
    public boolean registerUser(String username, String password, String email) throws InvalidUserParametersException {
        //if (forumProfile!=null) throw new IllegalStateException("Forum profile already exist");
        LOG.debug("registerUser");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("name", username);
        parameters.put("email", email);
        parameters.put("username", username);
        parameters.put("password", password);

        String url = "http://"+DiscouseApiConstants.FORUM_WRAPPER_URL+":7070/fermat/register";

        //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";

        try {
            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost(url);
            //httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpPost.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpPost.setHeader("Content-type", "application/json");

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(getJsonFromParams(parameters), "UTF-8");
            //sets the post request as the resulting string
            httpPost.setEntity(se);


            // make GET request to the given URL
            HttpResponse httpResponse = client.execute(httpPost);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            String result = null;
            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);

            System.out.println("###########################");
            System.out.println(result);
            System.out.println("###########################");

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(result);

            if (httpResponse.getStatusLine().getStatusCode()==200){
                saveForumData(false,username,password,email);
                forumProfile = new ForumProfile(username,password,email);
                return true;
            }else {
                if (jsonObject.has(REGISTER_ERROR_STR)){
                    throw new InvalidUserParametersException(jsonObject.get(REGISTER_ERROR_STR).toString());
                }
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.getMessage();
        }


        //JSONObject jsonObject = new JSONObject(result);

        //parameters.put("active","true"); // por ahora lo dejo activo hasta que vea como hacer la auth..
//        ResponseModel responseModel = client.createUser(parameters);
//        LOG.info("createUser responseModel -> " + responseModel.toString());
//        if (checkIfResponseFail(responseModel)){
//            throwException(responseModel);
//        }

        return false;
    }


    private boolean checkIfResponseFail(ResponseModel responseModel){
        return responseModel.meta.code>201 || responseModel.data==null;
    }

    private void throwException(ResponseModel responseModel){
        LOG.error("throwException");
        switch (responseModel.meta.code){
            default:

                break;
        }

    }

    private void saveForumData(boolean isRegistered,String username,String password,String email){
        conf.setIsRegistered(isRegistered);
        conf.setForumUser(username,password,email);
    }


    public String getJsonFromParams(Map<String,String> requestParams) {

        JsonObject jsonObject = new JsonObject();

        for (Map.Entry<String, String> stringStringEntry : requestParams.entrySet()) {
            if ( !stringStringEntry.getKey().equalsIgnoreCase("api_key") && !stringStringEntry.getKey().equalsIgnoreCase("api_username")) {
                jsonObject.addProperty(
                        stringStringEntry.getKey()
                        , ""+stringStringEntry.getValue()//, requestParam.getValueStr()
                );
            }
        }
        return jsonObject.toString();
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    public void setForunLink(String forunLink) {
        this.forunLink = forunLink;
    }
}
