package pl.com.ids.application.gcp;

import microsoft.exchange.webservices.data.core.ExchangeServiceBase;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OAuthClient {
    private boolean debug;

    public OAuthClient(boolean debug) {
        this.debug = debug;
    }

    public String refreshToken(String clientId, String clientSecret, String refreshToken, Proxy proxy) {
        CredentialsProvider defaultCredentialsProvider = new BasicCredentialsProvider();

        defaultCredentialsProvider.setCredentials(new AuthScope(proxy.getHostName(), proxy.getPort()), proxy.getCredentials());

        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(defaultCredentialsProvider).build();

        HttpPost httppost = new HttpPost("https://accounts.google.com/o/oauth2/token");


        RequestConfig defaultRequestConfig= RequestConfig.custom().build();
        final RequestConfig requestConfig;
        if (proxy.getHostName() != null) {
            requestConfig = RequestConfig.copy(defaultRequestConfig)
                    .setProxy(new HttpHost(proxy.getHostName(), proxy.getPort()))

                    .build();
        } else {
            requestConfig = defaultRequestConfig;
        }
        httppost.setConfig(requestConfig);

// Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(4);
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() > 200) {
                System.out.println(response);
            }
            return extractAccessToken(entity);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


    }

    private String extractAccessToken(HttpEntity entity) throws IOException {
        if (entity == null) {
            return "";
        }

        String json = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        if (debug) {
            System.out.println(json);
        }
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getString("access_token");
    }

}
