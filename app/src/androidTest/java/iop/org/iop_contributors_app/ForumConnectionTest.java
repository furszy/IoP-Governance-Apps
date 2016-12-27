package iop.org.iop_contributors_app;

import android.util.Log;

import com.google.gson.JsonObject;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscourseApiClient;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.DiscouseApiConstants;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseListener;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseMeta;
import iop_sdk.forum.discourge.com.wareninja.opensource.discourse.utils.ResponseModel;

/**
 * Created by mati on 21/11/16.
 */

public class ForumConnectionTest {


    @Test
    public void connectDiscourseTest(){


        final DiscourseApiClient mDiscourseApiClient = new DiscourseApiClient(
                DiscouseApiConstants.FORUM_URL,//args[0] // api_url  : e.g. http://your_domain.com
                DiscouseApiConstants.API_KEY, //, args[1] // api_key : you get from discourse admin
                DiscouseApiConstants.API_USER_BEHALF_OF //, args[2] // api_username : you make calls on behalf of
        );

        Map<String, String> parameters = null;

        // ## username for testing each function ##
        String test_username = "juan";

        ResponseModel responseModel;
        // --- createUser ---
		/*
		// createUser parameters MUST already contain
		'name': name,
	    'email': email,
	    'username': username,
	    'password': password,
	    */
        parameters = new HashMap<String, String>();
        parameters.put("name", test_username);
        parameters.put("email", "juanpedro@gmail.com");
        parameters.put("username", test_username);
        parameters.put("password", test_username+"_pwd");
        responseModel = mDiscourseApiClient.createUser(parameters);
        System.out.println("createUser responseModel -> " + responseModel.toString());

        JsonObject userObject = null;

//        // --- getUser & activate+approve ---
//        parameters = new HashMap<String, String>();
//        parameters.put("username", test_username);
//        responseModel = mDiscourseApiClient.createUser(parameters);
//        if (responseModel.meta.code>201 || responseModel.data==null) {// error!
//            System.out.println(test_username+" NOT exists!!! responseModel -> " + responseModel.toString());
//        }else {
//
//            System.out.println(responseModel.toString());
//
//        }

    }

    @Test
    public void updatePostTest(){
//        ForumClientDiscourseImp clientDiscourseImp = new ForumClientDiscourseImp(null);
//        try {
//            clientDiscourseImp.setForunLink("http://localhost:7070");
//            boolean resp = clientDiscourseImp.updatePost("new title putted by mati",32,"new category","Este es el nuevo body del foro, así que tengo que hacer algo y bla bla bla");
//
//
//        } catch (CantUpdateProposalException e) {
//            e.printStackTrace();
//        }

        try {
            int id = 29;
            String urlStr = "http://fermat.community/posts/32.json"+"?api_username=system&api_key="+DiscouseApiConstants.API_KEY;
            URL url = new URL(urlStr);
            HttpURLConnection myHttpURLConnection = (HttpURLConnection) url.openConnection();
            myHttpURLConnection.setRequestMethod("PUT");
            myHttpURLConnection.setRequestProperty("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            myHttpURLConnection.setUseCaches(true);
            myHttpURLConnection.setDoInput(true);
            myHttpURLConnection.setDoOutput(true);
            myHttpURLConnection.connect();

            String put = "post[\""+"hola que tal desde java, soy una bestia muajaja, es lo que hay"+"\"]&id=32&api_key="+DiscouseApiConstants.API_KEY+"&api_username=system";

            OutputStream os = myHttpURLConnection.getOutputStream();

            Charset charset = Charset.forName("UTF-8");
            ByteBuffer byteBuffer = charset.encode(put);

            os.write(byteBuffer.array());
            //os.close();

            byte[] array = new byte[8127];
            InputStream in = myHttpURLConnection.getInputStream();
            in.read();
            int bytesReaded = in.read(array);

            ByteBuffer byteBuffer1 = ByteBuffer.allocate(bytesReaded);
            byteBuffer1.put(array,0,bytesReaded);
            CharBuffer charBuffer = charset.decode(byteBuffer1);
            String resp = charBuffer.toString();

            System.out.println("respuesta: "+resp);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putUpdate(){

        String urlStr = "http://fermat.community/posts/32.json"+"?api_username=Matias&api_key="+DiscouseApiConstants.API_KEY;

        HttpPut httpPut = new HttpPut(urlStr);
        httpPut.setHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

        String put = "post[raw=\"hola que tal desde java, soy una bestia muajaja, es lo que hay, i'm a fucking genius!\"]&id=32&api_key="+DiscouseApiConstants.API_KEY+"&api_username=Matias";

//        String put = "{" +
//                "  \"posts\": [" +
//                "    {" +
//                "      \"raw\": \"holas soy yo desde adentro de java y soy re capo muajajaja\"" +
//                "    }" +
//                "  ]," +
//                "  \"id\": 32," +
//            //    "  \"topic_id\": 29," +
//                "  \"api_key\": \"839d04a947a866afac826eceb760800d41cd0a5e904047175f26ea5f26b8a0f5\"," +
//                "  \"api_username\": \"Matias\"" +
//                "}";

        try {
            //StringEntity(String,ContentType) is not supported by HttpClient Android 4.3.5
            StringEntity requestEntity = new StringEntity(put,"UTF-8");
            httpPut.setEntity(requestEntity);
//            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Content-type", "application/x-www-form-urlencoded");
        } catch (Exception e){
            e.printStackTrace();
        }

        String responseStr;

        SSLContext sslContext = SSLContexts.createSystemDefault();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);


        try {
            HttpClient httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(sslsf)
                    .build();
            HttpResponse httpResponse = httpClient.execute(httpPut);
            Integer httpResponseCode = null;

            httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity responseEntity = httpResponse.getEntity();
            if (httpResponseCode >= 200 && httpResponseCode < 300) {
                responseStr = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
            } else {
                //throw new ClientProtocolException("Unexpected response status: " + status);
                responseStr = httpResponseCode + "|" + "ERROR"
                        + " | " + (responseEntity != null ? EntityUtils.toString(responseEntity) : "")
                ;
//				responseStr = convertInputStreamToString(httpResponse.getEntity().getContent());
            }

            System.out.println(responseStr);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Test
    public void forumLoginTest(){

        final DiscourseApiClient mDiscourseApiClient = new DiscourseApiClient(
                DiscouseApiConstants.FORUM_URL,//args[0] // api_url  : e.g. http://your_domain.com
                DiscouseApiConstants.API_KEY, //, args[1] // api_key : you get from discourse admin
                DiscouseApiConstants.API_USER_BEHALF_OF //, args[2] // api_username : you make calls on behalf of
        );
        Map<String, String> parameters = new HashMap<>();
        String test_username = "matias.furszyfer@fermat.org";
        parameters.put("username", test_username);
        parameters.put("password", "forum1234567890");
        ResponseModel responseModel = mDiscourseApiClient.loginUser(parameters);
        System.out.println("loginUser responseModel -> " + responseModel.toString());


    }

    @Test
    public void forumPostTopic(){
        final DiscourseApiClient mDiscourseApiClient = new DiscourseApiClient(
                DiscouseApiConstants.FORUM_URL,//args[0] // api_url  : e.g. http://your_domain.com
                DiscouseApiConstants.API_KEY, //, args[1] // api_key : you get from discourse admin
                DiscouseApiConstants.API_USER_BEHALF_OF //, args[2] // api_username : you make calls on behalf of
        );
        Map<String, String> parameters = new HashMap<>();
        String test_username = "Matias";
        parameters.put("username", test_username);
        //parameters.put("username", new_username);
        parameters.put("category", "tweets");
        parameters.put("title", "PRIMER POSTEO!");
        parameters.put("raw", "raw_test/n estoy es un body con un tag diferente <Money>23123</Money>");
        mDiscourseApiClient.createTopic(parameters, new ResponseListener(){

            @Override
            public void onBegin(String info) {
                System.out.println("info: "+info);
            }
            @Override
            public void onComplete_wModel(ResponseModel responseModel) {
                // successful result
                System.out.println("SUCCESS! -> " + responseModel.toString());
            }

            @Override
            public void onError_wMeta(ResponseMeta responseMeta) {
                // error
                System.out.println("ERROR! -> " + responseMeta.toString());
            }
        });
    }

    @Test
    public void forumCreateTopic(){

        final DiscourseApiClient mDiscourseApiClient = new DiscourseApiClient(
                DiscouseApiConstants.FORUM_URL,//args[0] // api_url  : e.g. http://your_domain.com
                DiscouseApiConstants.API_KEY, //, args[1] // api_key : you get from discourse admin
                DiscouseApiConstants.API_USER_BEHALF_OF //, args[2] // api_username : you make calls on behalf of
        );
        Map<String, String> parameters = new HashMap<>();
        String test_username = "matias.furszyfer@fermat.org";
        parameters.put("username", test_username);
        parameters.put("password", "forum1234567890");
        ResponseModel responseModel = mDiscourseApiClient.loginUser(parameters);
        System.out.println("loginUser responseModel -> " + responseModel.toString());


    }

    @Test
    public void forumRegisterUserTest2(){

        try {

            String url = "http://fermat.community/users";

            url = url + "?api_key=" + DiscouseApiConstants.API_KEY + "&api_username=system";

            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost(url);
            //httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpPost.addHeader("Accept", "text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
            httpPost.setHeader("Content-type", "application/json");

            String test_username = "furszy2";
            Map<String, String> parameters = null;
            parameters = new HashMap<String, String>();
            parameters = new HashMap<String, String>();
            parameters.put("name", test_username);
            parameters.put("email", "mati_fur@hotmail.com");
            parameters.put("username", test_username);
            parameters.put("password", test_username + "_pwd");
            parameters.put("active", "true");

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(getJsonFromParams(parameters),"UTF-8");
            //sets the post request as the resulting string
            httpPost.setEntity(se);


            // make GET request to the given URL
            HttpResponse httpResponse = client.execute(httpPost);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            String result = null;
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);

            Assert.assertNotNull(result);

            System.out.println("###########################");
            System.out.println(result);
            System.out.println("###########################");
            JSONObject jsonObject = new JSONObject(result);



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

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

    @Test
    public void forumRegisterUserTest(){

        try {

            String url = "http://fermat.community/api/token";

            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/vnd.api+json");

            String data =
                            "{" +
                                "\"identification\": \"mati\"," +
                                "\"password\": \"1234567890\"" +
                            "}";


            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(data);
            //sets the post request as the resulting string
            httpPost.setEntity(se);


            // make GET request to the given URL
            HttpResponse httpResponse = client.execute(httpPost);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            String result = null;
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);

            Assert.assertNotNull(result);


            JSONObject jsonObject = new JSONObject(result);

            post(jsonObject.getString("token"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void post(String token){
        try {

            String url = "http://fermat.community/api/discussions";

            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/vnd.api+json");
            httpPost.setHeader("Authorization","Token "+token);
//
            String data =
                    "{\"data\": " +

                            "{\"attributes\": " +
                                "{" +
                                    "\"title\": \"Voting System Development\"," +
                                    "\"content\": \"Develop the entire voting system\"," +
                                    "\"tags\": \"community-foundation\"" +
                                "}" +
                            "}" +

                    "}";


            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(data);
            //sets the post request as the resulting string
            httpPost.setEntity(se);


            // make GET request to the given URL
            HttpResponse httpResponse = client.execute(httpPost);
            InputStream inputStream = null;
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            String result = null;
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);

            Assert.assertNotNull(result);




        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Test
    public void forumRegisterNewUserTest(){

        try {

            String url = "http://fermat.community/api/users";

            HttpClient client = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/vnd.api+json");



            String data =
                    "{\"data\": " +

                                "{\"attributes\": " +
                                    "{" +
                                        "\"username\": \"mati\"," +
                                        "\"password\": \"1234567890\"," +
                                        "\"email\": \"matiasfurszyfer@gmail.com\"" +
                                    "}" +
                                "}" +

                    "}";


            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(data);
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

            Assert.assertNotNull(result);


        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
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


    private static JSONObject getJsonObjectFromMap(Map params) throws JSONException {

        //all the passed parameters from the post request
        //iterator used to loop through all the parameters
        //passed in the post request
        Iterator iter = params.entrySet().iterator();

        //Stores JSON
        JSONObject holder = new JSONObject();

        //using the earlier example your first entry would get email
        //and the inner while would get the value which would be 'foo@bar.com'
        //{ fan: { email : 'foo@bar.com' } }

        //While there is another entry
        while (iter.hasNext())
        {
            //gets an entry in the params
            Map.Entry pairs = (Map.Entry)iter.next();

            //creates a key for Map
            String key = (String)pairs.getKey();

            //Create a new map
            Map m = (Map)pairs.getValue();

            //object for storing Json
            JSONObject data = new JSONObject();

            //gets the value
            Iterator iter2 = m.entrySet().iterator();
            while (iter2.hasNext())
            {
                Map.Entry pairs2 = (Map.Entry)iter2.next();
                data.put((String)pairs2.getKey(), (String)pairs2.getValue());
            }

            //puts email and 'foo@bar.com'  together in map
            holder.put(key, data);
        }
        return holder;
    }


}
