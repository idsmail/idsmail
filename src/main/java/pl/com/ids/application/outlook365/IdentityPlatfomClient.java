package pl.com.ids.application.outlook365;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

class TokenPair {

    private final String accessToken;
    private final String refreshToken;

    public TokenPair(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}

class InvalidTokenException extends IOException {
    InvalidTokenException(String message) {
        super(message);
    }
}

class RefreshTokenExpired extends IOException {
    RefreshTokenExpired(String message) {
        super(message);
    }

}

public class IdentityPlatfomClient {
    TokenPair processResponse(JsonParser parser) throws IOException {
        String accessToken = null;
        String refreshToken = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            if ("access_token".equals(name)) {
                parser.nextToken();
                accessToken = parser.getText();
            }
            if ("refresh_token".equals(name)) {
                parser.nextToken();
                refreshToken = parser.getText();
            }
        }
        return new TokenPair(accessToken, refreshToken);
    }

    private String decodeRedirectUri() throws UnsupportedEncodingException {
        return URLDecoder.decode("http://localhost:3000/auth/callback", StandardCharsets.UTF_8.toString());
    }

    public String getAuthorizationCode(String tenantId, String clientId) throws IOException {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/authorize?client_id=%s" +
                "&response_type=code" +
                "&redirect_uri=%s" +
                "&response_mode=query" +
                "&scope=%s" +
                "&state=12345", tenantId, clientId, decodeRedirectUri(), "offline_access%20user.read%20mail.read%20mail.send");
        System.out.println(endpoint);
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        return sb.toString();
    }

    private JsonParser executeCall(String endpoint, String postBody) throws IOException{
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postBody.getBytes());
        JsonFactory factory = new JsonFactory();
        try {
            conn.connect();
            return factory.createParser(conn.getInputStream());
        } catch (IOException ioe) {
            JsonParser parser = factory.createParser(conn.getErrorStream());

            LinkedList<String> errorCodes = new LinkedList<>();
            String errorDescription = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("error_description".equals(name)) {
                    errorDescription = parser.getText();
                }

                if ("error_codes".equals(name)) {
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        String errorCode = parser.getText();
                        errorCodes.add(errorCode);
                    }

                }
            }
            // https://learn.microsoft.com/en-us/azure/active-directory/develop/reference-aadsts-error-codes
            if (errorCodes.contains("50089") || errorCodes.contains("70008") || errorCodes.contains("70043")) {
                throw new RefreshTokenExpired(errorDescription);
            }

            if (errorCodes.contains("70000")) {
                throw new InvalidTokenException(errorDescription);
            }

            throw new IOException(errorDescription);
        }
    }

    public String getRefreshToken(String tenantId, String clientId, String clientSecret, String code) throws UnsupportedEncodingException {
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token ", tenantId);
        String postBody = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
                code, decodeRedirectUri(), clientId, clientSecret);
        System.out.printf("$body = @{\n\"grant_type\"=\"authorization_code\"\n\"code\"=\"%s\"\n\"redirect_uri\"=\"%s\"\n\"client_id\"=\"%s\"\n\"client_secret\"=\"%s\"\n}%n", code, decodeRedirectUri(), clientId, clientSecret);
        try {
            JsonParser parser = executeCall(endpoint, postBody);
            return processResponse(parser).getRefreshToken();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }


    public TokenPair refreshToken(String refreshToken, String clientId, String clientSecret) {
        String tenantId = "common";
        String endpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);
        try {
            String postBody = String.format("grant_type=refresh_token&refresh_token=%s&redirect_uri=%s&client_id=%s&client_secret=%s&scope=%s",
                    refreshToken, decodeRedirectUri(), clientId, clientSecret, "offline_access%20user.read%20mail.read%20mail.send");
            System.out.printf("$body = @{\n\"grant_type\"=\"refresh_token\"\n\"refresh_token\"=\"%s\"\n\"redirect_uri\"=\"%s\"\n\"client_id\"=\"%s\"\n\"client_secret\"=\"%s\"\n\"scope\"=\"%s\"}%n", refreshToken, decodeRedirectUri(), clientId, clientSecret, "offline_access%20user.read%20mail.read%20mail.send");
            JsonParser parser = executeCall(endpoint, postBody);
            return processResponse(parser);

        } catch (InvalidTokenException e) {
            System.err.println(e.getMessage());
            System.exit(16);
        } catch (RefreshTokenExpired e) {
            System.err.println(e.getMessage());
            System.exit(17);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(18);
        }
        return null;
    }

}
