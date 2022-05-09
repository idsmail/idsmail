package pl.com.ids.exchange;
import microsoft.exchange.webservices.data.core.request.HttpWebRequest;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;

import java.util.Map;
public final class BearerTokenCredentials extends ExchangeCredentials {

    private static final String BEARER_TOKEN_FORMAT_REGEX = "^[-._~+/A-Za-z0-9]+=*$";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_AUTH_PREAMBLE = "Bearer ";
    private String token;

    public String getToken() {
        return token;
    }

    public BearerTokenCredentials(String bearerToken) {
        if (bearerToken == null) {
            throw new IllegalArgumentException("Bearer token can not be null");
        }
        this.validateToken(bearerToken);
        this.token = bearerToken;
    }

    protected void validateToken(String bearerToken) throws IllegalArgumentException {
        if (!bearerToken.matches(BEARER_TOKEN_FORMAT_REGEX)) {
            throw new IllegalArgumentException("Bearer token format is invalid.");
        }
    }

    @Override
    public void prepareWebRequest(HttpWebRequest request) {
        Map<String, String> headersMap = request.getHeaders();
        String bearerValue = BEARER_AUTH_PREAMBLE + token;
        headersMap.put(AUTHORIZATION, bearerValue);
        //headersMap.put("X-AnchorMailbox","esj_office365_imap@genesyslab.onmicrosoft.com");
        request.setHeaders(headersMap);
    }
}