package iop.org.iop_contributors_app.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by mati on 27/11/16.
 */



public class IoPRpcClient {

    private static final String COMMAND_GET_BALANCE = "getbalance";
    private static final String COMMAND_GET_INFO = "getinfo";
    private static final String COMMAND_GET_NEW_ADDRESS = "getnewaddress";

    private JSONObject invokeRPC(String id, String method, List<String> params) {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("method", method);
            if (null != params) {
                JSONArray array = new JSONArray();
                array.put(params);
                json.put("params", params);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject responseJsonObj = null;
        try {
            httpclient.getCredentialsProvider().setCredentials(new AuthScope("http://192.168.0.111", 6954),
                    new UsernamePasswordCredentials("IoPrpc", "CT2dgLVV4SCLE1ctbg4iMu9H3n6rEqC2LLVes6qmwcvh"));
            StringEntity myEntity = new StringEntity(json.toString());
            System.out.println(json.toString());
            HttpPost httppost = new HttpPost("http://192.168.0.111:6954");
            httppost.setEntity(myEntity);

            System.out.println("executing request" + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost, new ResponseHandler<HttpResponse>() {
                @Override
                public HttpResponse handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    System.out.println("##############33 acaaaaaaaaaaaaaaaaaaa");
                    return httpResponse;
                }
            });
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                // System.out.println(EntityUtils.toString(entity));
            }
            responseJsonObj = new JSONObject(EntityUtils.toString(entity));

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
        return responseJsonObj;
    }

    public Double getBalance(String account) throws JSONException {
        String[] params = { account };
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), COMMAND_GET_BALANCE, Arrays.asList(params));
        return (Double)json.get("result");
    }

    public String getNewAddress(String account) throws JSONException {
        String[] params = { account };
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), COMMAND_GET_NEW_ADDRESS, Arrays.asList(params));
        return (String)json.get("result");
    }

    public JSONObject getInfo() throws JSONException {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), COMMAND_GET_INFO, null);
        return (JSONObject)json.get("result");
    }
}