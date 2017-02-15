package iop.org.iop_contributors_app.profile_server.util;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import iop.org.iop_contributors_app.R;

/**
 * Created by mati on 08/11/16.
 */

public class SslContextFactory implements iop_sdk.profile_server.SslContextFactory{

    private static final String TAG = "SslContextFactory";

    private final Context context;

    public SslContextFactory(Context context) {
        this.context = context;
    }

    public SSLContext buildContext() throws Exception{
        try {
            InputStream inputStream = ((Context)context).getResources().openRawResource(R.raw.profile_server);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(inputStream);
            Log.d(TAG, "ca=" + caCert.getSubjectDN());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null); // You don't need the KeyStore instance to come from a file.
            ks.setCertificateEntry("caCert", caCert);
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        throw new Exception("See the exceptions above..");
    }


}
