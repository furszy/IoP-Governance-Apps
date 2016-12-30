package iop.org.iop_contributors_app;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import iop.org.iop_contributors_app.utils.IoPRpcClient;

/**
 * Created by mati on 27/11/16.
 */


public class IoPRpcConnectionTest {


    @Test
    public void getRawTransationTest(){

//        try {
//            CloseableHttpClient httpProvider = ResourceUtils.getHttpProvider();
////            Properties nodeConfig = ResourceUtils.getNodeConfig();
//            Properties nodeConfig = new Properties();
//            nodeConfig.put("node.bitcoind.rpc.protocol","http");
//            nodeConfig.put("node.bitcoind.rpc.host","192.168.0.111");
//            nodeConfig.put("node.bitcoind.rpc.port",7685);
//            nodeConfig.put("node.bitcoind.rpc.user","IoPrpc");
//            nodeConfig.put("node.bitcoind.rpc.password","CT2dgLVV4SCLE1ctbg4iMu9H3n6rEqC2LLVes6qmwcvh");
//            nodeConfig.put("node.bitcoind.rpc.auth_scheme","Basic");
//
//            BtcdClient client = new VerboseBtcdClientImpl(httpProvider, nodeConfig);
//            String s = client.getRawTransaction("df4603161be7f8fde2afa7c2592a664998db8d1dce400c0f8eef8d6b490feffa");
//            System.out.println("##########################");
//            System.out.println(s);
//            System.out.println("##########################");
//        } catch (BitcoindException e) {
//            e.printStackTrace();
//        } catch (CommunicationException e) {
//            e.printStackTrace();
//        }

        IoPRpcClient client = new IoPRpcClient();
        try {
            JSONObject s = client.getInfo();
            System.out.println(s.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }





}
