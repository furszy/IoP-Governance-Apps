package iop.org.iop_contributors_app;

import android.util.Log;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mati on 21/11/16.
 */

public class ForumConnectionTest {

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
