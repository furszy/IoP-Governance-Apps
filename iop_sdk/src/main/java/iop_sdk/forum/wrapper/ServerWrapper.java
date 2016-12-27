package iop_sdk.forum.wrapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import iop_sdk.forum.InvalidUserParametersException;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.RequestParameter;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.StringRequestParameter;
import iop_sdk.global.exceptions.ConnectionRefusedException;

import static iop_sdk.forum.wrapper.ResponseMessageConstants.BEST_CHAIN_HEIGHT_HASH;
import static iop_sdk.forum.wrapper.ResponseMessageConstants.USER_ERROR_STR;
import static iop_sdk.utils.StreamsUtils.convertInputStreamToString;

/**
 * Created by mati on 12/12/16.
 */

public class ServerWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(ServerWrapper.class);

    private String wrapperUrl;

    private String url;

    public ServerWrapper(String forumWrapper) {
        this.wrapperUrl = forumWrapper;
        changeUrl();
    }

    private void changeUrl(){
        url = wrapperUrl+":7070/fermat";
        if (!url.contains("http://")){
            url = "http://"+url;
        }
    }

    /**
     *
     * @param parameters
     * @return api_key
     */
    public String connect(Map<String, String> parameters) throws InvalidUserParametersException, ConnectionRefusedException {

        String url = this.url+"/requestkey";

        LOG.info("forum wrapper, connect to: "+url);

        //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";

        String apiKey = null;

        try {

            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
            HttpClient client = new DefaultHttpClient(basicHttpParams);
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

//            JSONObject jsonObject = new JSONObject(result);

            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(result);


            if (httpResponse.getStatusLine().getStatusCode()==200){
                apiKey = jsonObject.get(ResponseMessageConstants.API_KEY).getAsString();
                return apiKey;
            }else {
                if (jsonObject.has(USER_ERROR_STR)){
                    throw new InvalidUserParametersException(jsonObject.get(USER_ERROR_STR).getAsString());
                }
                return null;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (HttpHostConnectException e) {
            e.printStackTrace();
            throw new ConnectionRefusedException("server is not available", e);
        }catch (SocketTimeoutException e){
            e.printStackTrace();
            throw new ConnectionRefusedException("server is not available", e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean requestCoins(String address){

        String url = this.url+"/requestcoins";

        //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";

        List<RequestParameter> requestParams = new ArrayList<>();
        requestParams.add(new StringRequestParameter("address",address));


        try {


            int i = 0;
            for (RequestParameter requestParam: requestParams) {
                url += (i==0 && !url.contains("?"))?"?":"&";
                url += requestParam.format();
                i++;
            }

            LOG.info("URL: "+url);


            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpPost = new HttpGet(url);
            //httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpPost.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpPost.setHeader("Content-type", "application/json");

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

//            JSONObject jsonObject = new JSONObject(result);


            if (httpResponse.getStatusLine().getStatusCode()==200){
                return true;
            }else {
                return false;
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public class RequestProposalsResponse{

        int bestChainHeight;
        String bestChainHash;
        List<String> txHashes;

        public RequestProposalsResponse() {
        }

        public List<String> getTxHashes() {
            return txHashes;
        }

        public int getBestChainHeight() {
            return bestChainHeight;
        }

        public String getBestChainHash() {
            return bestChainHash;
        }
    }


    /**
     *
     *
     * @param blockHeight
     * @return list of proposal transactions hashes
     */
    public RequestProposalsResponse getVotingProposals(int blockHeight) throws CantGetProposalsFromServer {
        String url = this.url+"/requestproposals";

        //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";


        RequestProposalsResponse requestProposalsResponse = new RequestProposalsResponse();

        List<RequestParameter> requestParams = new ArrayList<>();
        HttpResponse httpResponse = null;
        String result = null;
        try {


            int i = 0;
            for (RequestParameter requestParam: requestParams) {
                url += (i==0 && !url.contains("?"))?"?":"&";
                url += requestParam.format();
                i++;
            }

            LOG.info("getVotingProposals URL: "+url);

            BasicHttpParams basicHttpParams = new BasicHttpParams();
            HttpConnectionParams.setSoTimeout(basicHttpParams, (int) TimeUnit.MINUTES.toMillis(1));
            HttpClient client = new DefaultHttpClient(basicHttpParams);
            HttpGet httpGet = new HttpGet(url);
            //httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpGet.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpGet.setHeader("Content-type", "application/json");

            // make GET request to the given URL
            httpResponse = client.execute(httpGet);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);


            LOG.info("###########################");
            LOG.info(result);
            LOG.info("###########################");

//


            if (httpResponse.getStatusLine().getStatusCode()==200){
                List<String> ret = new ArrayList<>();

                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = jsonParser.parse(result).getAsJsonObject();
                JsonElement jsonElement = jsonObject.get("transactions");
                if (jsonElement!=null) {
                    JsonArray transactions = jsonElement.getAsJsonArray();
//                JSONObject jsonObject = new JSONObject(result);
//                JSONArray transactions = jsonObject.getJSONArray("transactions");
                    for (i = 0; i < transactions.size(); i++) {
                        ret.add(transactions.get(i).getAsString());
                    }
                }
                requestProposalsResponse.bestChainHeight = ((JsonObject)jsonObject.get("Data")).get("currentheight").getAsInt();
                requestProposalsResponse.txHashes = ret;
                requestProposalsResponse.bestChainHash = jsonObject.get(BEST_CHAIN_HEIGHT_HASH).getAsString();
            }else {
                throw new CantGetProposalsFromServer("Something fail, server code: "+((httpResponse!=null)?httpResponse.getStatusLine().getStatusCode():"null"));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (HttpHostConnectException e){
            throw new CantGetProposalsFromServer("url: "+url,e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IllegalStateException e){
            throw new CantGetProposalsFromServer("Something fail, server return: "+result+", status code: "+httpResponse.getStatusLine().getStatusCode());
        }
        return requestProposalsResponse;
    }


    public HttpResponse registerUser(Map<String, String> parameters) throws IOException {

        String url = this.url+"/register";

        LOG.info("registerUser URL: "+url);

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

        return httpResponse;
    }

    public String getTopic(Map<String, String> parameters) {
        String url = this.url+"/getTopic";
        LOG.info("getTopic URL: "+url);
        return post(url,parameters);
    }

    private String post(String url, Map<String,String> parameters){
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

            return result;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public void setWrapperUrl(String wrapperUrl) {
        this.wrapperUrl = wrapperUrl;
        changeUrl();
    }
}
