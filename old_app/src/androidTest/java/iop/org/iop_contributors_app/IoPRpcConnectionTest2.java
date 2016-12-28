package iop.org.iop_contributors_app;


import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by mati on 27/11/16.
 */
public class IoPRpcConnectionTest2 {

    @Test
    public void getContractTransactionsTest(){

//        IoPRpcClient client = new IoPRpcClient();
//        try {
//            JSONObject s = client.getInfo();
//            System.out.println(s.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            issueRequest("getblocks",null);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Issue the Bitcoin RPC request and return the parsed JSON response
     *
     * @param       requestType             Request type
     * @param       requestParams           Request parameters in JSON format or null if no parameters
     * @return                              Parsed JSON response
     * @throws      IOException             Unable to issue Bitcoin RPC request
     */
    private static void issueRequest(String requestType, String requestParams)
            throws IOException {
        long id = UUID.randomUUID().getLeastSignificantBits();//requestId.incrementAndGet();
//        Response response = null;
        try {
            URL url = new URL(String.format("http://%s:%d/",
                    "192.168.0.111",6954));
            String request;
            if (requestParams != null) {
                request = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"%s\", \"params\": %s, \"id\": %d}",
                        requestType, requestParams, id);
            } else {
                request = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"%s\", \"id\": %d}",
                        requestType, id);
            }
//            log.debug(String.format("Issue HTTP request to %s:%d: %s",
//                    Main.serverConnection.getHost(), Main.serverConnection.getPort(), request));
            byte[] requestBytes = request.getBytes("UTF-8");
            //
            // Issue the request
            //
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json-rpc");
            conn.setRequestProperty("Cache-Control", "no-cache, no-store");
            conn.setRequestProperty("Content-Length", String.format("%d", requestBytes.length));
            conn.setRequestProperty("Accept", "application/json-rpc");
            String userpass = "IoPrpc" + ":" + "CT2dgLVV4SCLE1ctbg4iMu9H3n6rEqC2LLVes6qmwcvh";
            String encodedAuthentication = Base64.encodeToString(userpass.getBytes("UTF-8"), Base64.NO_PADDING & Base64.DEFAULT); //Base64.encodeBase64String(userpass.getBytes("UTF-8"));
            //String encodedAuthentication = Base64.getEncoder().withoutPadding().encodeToString(userpass.getBytes("UTF-8"));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuthentication);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.connect();
            try (FilterOutputStream out = new FilterOutputStream(conn.getOutputStream())) {
                out.write(requestBytes);
                out.flush();
                int code = conn.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK) {
                    String errorText = String.format("Response code %d for %s request\n  %s",
                            code, requestType, conn.getResponseMessage());
                    throw new IOException(errorText);
                }
            }
            //
            // Parse the response
            //
            try (InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8")) {
                BufferedReader streamReader = new BufferedReader(in);
                StringBuilder responseStrBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());

                System.out.println(jsonObject);

                //Response errorResponse = response.getObject("error");
//                if (errorResponse != null) {
//                    String errorText = String.format("Error %d returned for %s request\n  %s",
//                            errorResponse.getInt("code"), requestType, errorResponse.getString("message"));
//                    throw new IOException(errorText);
//                }
                streamReader.close();
                in.close();
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            if (log.isDebugEnabled()) {
//                log.debug(String.format("Request complete\n%s", Utils.formatJSON(response)));
//            }
        } catch (MalformedURLException exc) {
            throw new IOException("Malformed Bitcoin RPC URL", exc);
        } catch (IOException exc) {
            String errorText = String.format("I/O error on %s request: %s", requestType, exc.getMessage());
            throw new IOException(errorText);
        }
        return ;
    }

}
