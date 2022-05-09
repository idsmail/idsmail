package pl.com.ids.exchange;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.net.HttpURLConnection;
import java.net.URL;

public class AuthTokenAccess {

    public AuthTokenAccess() {
    }

    public static String getAccessToken(String tenantId, String clientId, String clientSecret, String scope) {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/token", tenantId);
        String postBody = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s&resource=%s&scope=%s",
                clientId, clientSecret, "00000003-0000-0000-c000-000000000000", scope);
        String accessToken = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();


            conn.setRequestMethod("POST");

            conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postBody.getBytes());
            conn.connect();
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(conn.getInputStream());
            //String accessToken = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("access_token".equals(name)) {
                    parser.nextToken();
                    accessToken = parser.getText();
                }
            }
        } catch (Exception e) {


        }
        return accessToken;
    }
}