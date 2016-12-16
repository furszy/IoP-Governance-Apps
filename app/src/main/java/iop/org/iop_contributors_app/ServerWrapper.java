package iop.org.iop_contributors_app;

import com.google.gson.JsonObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumClientDiscourseImp;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.core.iop_sdk.forum.InvalidUserParametersException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscouseApiConstants;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.RequestParameter;
import iop.org.iop_contributors_app.core.iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.StringRequestParameter;
import iop.org.iop_contributors_app.core.iop_sdk.forum.wrapper.ResponseMessageConstants;

import static iop.org.iop_contributors_app.core.iop_sdk.forum.wrapper.ResponseMessageConstants.USER_ERROR_STR;
import static iop.org.iop_contributors_app.core.iop_sdk.utils.StreamsUtils.convertInputStreamToString;

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

        LOG.info("forum wrapper, connect to: "+url);

        String url = this.url+"/requestkey";

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

            JSONObject jsonObject = new JSONObject(result);


            if (httpResponse.getStatusLine().getStatusCode()==200){
                apiKey = jsonObject.getString(ResponseMessageConstants.API_KEY);
                return apiKey;
            }else {
                if (jsonObject.has(USER_ERROR_STR)){
                    throw new InvalidUserParametersException(jsonObject.getString(USER_ERROR_STR));
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
        } catch (JSONException e) {
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


    /**
     *
     *
     * @param blockHeight
     * @return list of proposal transactions hashes
     */
    public List<String> getVotingProposals(int blockHeight) throws Exception {
        String url = this.url+"/requestproposals";

        //url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";
        List<String> ret = new ArrayList<>();

        List<RequestParameter> requestParams = new ArrayList<>();


        try {


            int i = 0;
            for (RequestParameter requestParam: requestParams) {
                url += (i==0 && !url.contains("?"))?"?":"&";
                url += requestParam.format();
                i++;
            }

            LOG.info("getVotingProposals URL: "+url);


            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpGet httpGet = new HttpGet(url);
            //httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpGet.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpGet.setHeader("Content-type", "application/json");

            // make GET request to the given URL
            HttpResponse httpResponse = client.execute(httpGet);
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

//


            if (httpResponse.getStatusLine().getStatusCode()==200){
                JSONObject jsonObject = new JSONObject(result);
                JSONArray transactions = jsonObject.getJSONArray("transactions");
                for (i=0;i<transactions.length();i++){
                    ret.add(transactions.getString(i));
                }
            }else {
                throw new Exception("Something fail, server code: "+httpResponse.getStatusLine().getStatusCode());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
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
