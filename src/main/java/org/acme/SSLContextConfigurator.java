package org.acme;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import io.quarkus.elasticsearch.restclient.lowlevel.ElasticsearchClientConfig;

@ElasticsearchClientConfig
public class SSLContextConfigurator implements HttpClientConfigCallback {

    @Override
    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
        try {
            Path caPath = Paths.get("C:\\Users\\abell\\avi-xe\\elastic-oracle-quarkus\\certs\\es01\\es01.crt");
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa;
            try(InputStream is = Files.newInputStream(caPath)) {
                trustedCa = factory.generateCertificate(is);
            }
            KeyStore truststore = KeyStore.getInstance("pkcs12");
            truststore.load(null, null);
            truststore.setCertificateEntry("ca", trustedCa);
            SSLContextBuilder sslBuilder = SSLContexts.custom()
                    .loadTrustMaterial(truststore, null);
            SSLContext sslContext = sslBuilder.build();
            httpClientBuilder.setSSLContext(sslContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return httpClientBuilder;
    }

}
