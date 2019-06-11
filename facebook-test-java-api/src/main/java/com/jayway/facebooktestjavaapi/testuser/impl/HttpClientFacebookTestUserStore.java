package com.jayway.facebooktestjavaapi.testuser.impl;

import com.jayway.facebooktestjavaapi.testuser.FacebookTestUserAccount;
import com.jayway.facebooktestjavaapi.testuser.FacebookTestUserStore;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of FacebookTestUserStore that relies on apache HttpClient for communication with Facebook.
 */
public class HttpClientFacebookTestUserStore implements FacebookTestUserStore {

    private static final Logger log = LoggerFactory.getLogger(HttpClientFacebookTestUserStore.class);

    private static final String FACEBOOK_HOST = "graph.facebook.com";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ENCODING = "UTF-8";

    private String appAccessToken;
    private JSONParser jsonParser;
    private final String applicationId;
    private final String applicationSecret;
    private final HttpClient client;

    /**
     * Creates a new instance. For more information, see {@link FacebookTestUserStore}.
     *
     * @param applicationId     The Facebook application ID on which test users should be registered.
     * @param applicationSecret The application secret.
     * @param httpClient        A configured HttpClient which will be used when communicating with Facebook.
     */
    public HttpClientFacebookTestUserStore(String applicationId, String applicationSecret, HttpClient httpClient) {
        this.applicationId = applicationId;
        this.applicationSecret = applicationSecret;
        this.jsonParser = new JSONParser();
        this.client = httpClient;
    }

    /**
     * Creates a new instance. For more information, see {@link FacebookTestUserStore}.
     *
     * @param applicationId     The Facebook application ID on which test users should be registered.
     * @param applicationSecret The application secret.
     */
    public HttpClientFacebookTestUserStore(String applicationId, String applicationSecret) {
        this(applicationId, applicationSecret, new DefaultHttpClient());
    }

    private void init() {
        if (this.appAccessToken == null) {
            this.appAccessToken = getAccessToken(applicationId, applicationSecret);
        }
    }

    private String getAccessToken(String applicationId, String applicationSecret) {
        String result = get("/oauth/access_token", buildList("grant_type", "client_credentials", "client_id", applicationId, "client_secret", applicationSecret));
        try {
            return ((JSONObject) new JSONParser().parse(result)).get("access_token").toString();
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not get access token for provided authentication");
        }
    }

    public FacebookTestUserAccount createTestUser(boolean appInstalled, String permissions) {
        init();

        String jsonResponse = post("/%s/accounts/test-users", buildList("installed", Boolean.toString(appInstalled), "permissions", getPermissionsOrDefault(permissions)), null, applicationId);

        log.debug(jsonResponse);

        JSONObject user = parseJsonObject(jsonResponse);

        FacebookTestUserAccount facebookAccount = buildFacebookAccount(user);

        log.debug(String.format("* Created account on Facebook with id [%s], access_token [%s], login_url: [%s] ", facebookAccount.id(), facebookAccount.accessToken(), facebookAccount.loginUrl()));

        return facebookAccount;

    }

    private String getPermissionsOrDefault(String permissions) {
        if (permissions == null) {
            return "email,offline_access";
        }
        return permissions;
    }

    public List<FacebookTestUserAccount> getAllTestUsers() {
        init();

        String jsonResponse = get("/%s/accounts/test-users", applicationId);

        JSONObject accounts = parseJsonObject(jsonResponse);

        LinkedList<FacebookTestUserAccount> result = new LinkedList<FacebookTestUserAccount>();
        JSONArray jsonArray = (JSONArray) accounts.get("data");
        for (Object element : jsonArray) {
            JSONObject jsonUser = (JSONObject) element;
            result.add(buildFacebookAccount(jsonUser));
        }

        log.debug("* Found [{}] accounts on Facebook ", result.size());

        return result;
    }

    public void deleteAllTestUsers() {
        List<FacebookTestUserAccount> accounts = getAllTestUsers();
        for (FacebookTestUserAccount account : accounts) {
            account.delete();
        }
    }

    String getApplicationId() {
        return applicationId;
    }

    String getAccessToken() {
        return appAccessToken;
    }

    public boolean isInitialized() {
        return appAccessToken != null;
    }


    protected String get(String resource) throws URISyntaxException {
        return get(resource, new ArrayList<NameValuePair>());
    }

    protected String get(String resource, Object... pathParams) {
        return get(resource, null, pathParams);
    }

    protected String get(String resource, List<NameValuePair> providedQueryParams, Object... pathParams) {
        List<NameValuePair> queryParams = ensureList(providedQueryParams);

        if (appAccessToken != null && !containsName(queryParams, ACCESS_TOKEN)) {
            queryParams.add(new BasicNameValuePair(ACCESS_TOKEN, appAccessToken));
        }

        try {
            return getRequestResponse(buildGetResource(resource, queryParams, pathParams));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

    protected String post(String resource, List<NameValuePair> providedQueryParams, List<NameValuePair> providedFormParams, Object... pathParams) {
        List<NameValuePair> queryParams = ensureList(providedQueryParams);
        List<NameValuePair> formParams = ensureList(providedFormParams);

        if (appAccessToken != null && !containsName(formParams, ACCESS_TOKEN)) {
            formParams.add(new BasicNameValuePair(ACCESS_TOKEN, appAccessToken));
        }

        try {
            return getRequestResponse(buildPostResource(resource, queryParams, formParams, pathParams));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected String delete(String resource, Object... pathParams) {
        return delete(resource, null, pathParams);
    }

    protected String delete(String resource, List<NameValuePair> providedQueryParams, Object... pathParams) {
        List<NameValuePair> queryParams = ensureList(providedQueryParams);

        if (appAccessToken != null && !containsName(queryParams, ACCESS_TOKEN)) {
            queryParams.add(new BasicNameValuePair(ACCESS_TOKEN, appAccessToken));
        }

        try {
            return getRequestResponse(buildDeleteResource(resource, queryParams, pathParams));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private List<NameValuePair> ensureList(List<NameValuePair> providedQueryParams) {
        if (providedQueryParams == null) {
            return new ArrayList<NameValuePair>();
        }

        return providedQueryParams;
    }

    private boolean containsName(List<NameValuePair> queryParams, String name) {
        for (NameValuePair queryParam : queryParams) {
            if (queryParam.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String getRequestResponse(HttpRequestBase webResource) {
        try {
            HttpResponse response = client.execute(webResource);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        } catch (Exception e) {
            logError(e);
            throw new RuntimeException(e);
        }
    }

    private void logError(Exception e) {
        log.error("Error from Facebook: " + e.getMessage());
    }

    private FacebookTestUserAccount buildFacebookAccount(JSONObject user) {
        return new HttpClientFacebookTestUserAccount(this, user);
    }

    private JSONObject parseJsonObject(String json) {
        try {
            return (JSONObject) jsonParser.parse(json);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Could not parse JSON: " + json);
        }

    }

    private HttpGet buildGetResource(String resource, List<NameValuePair> queryParams, Object... pathParams) throws URISyntaxException {
        URI uri = getUri(resource, queryParams, pathParams);
        return new HttpGet(uri);
    }

    private HttpPost buildPostResource(String resource, List<NameValuePair> queryParams, List<NameValuePair> formParams, Object... pathParams) throws UnsupportedEncodingException, URISyntaxException {
        URI uri = getUri(resource, queryParams, pathParams);
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, ENCODING);
        HttpPost post = new HttpPost(uri);
        post.setEntity(entity);
        return post;
    }

    private HttpDelete buildDeleteResource(String resource, List<NameValuePair> queryParams, Object... pathParams) throws URISyntaxException {
        URI uri = getUri(resource, queryParams, pathParams);
        HttpDelete delete = new HttpDelete(uri);
        delete.setHeader("Content-length", "0");
        return delete;
    }

    private URI getUri(String resource, List<NameValuePair> queryParams, Object[] pathParams) throws URISyntaxException {
        return URIUtils.createURI("https", FACEBOOK_HOST, -1, String.format(resource, pathParams), URLEncodedUtils.format(queryParams, ENCODING), null);
    }

    protected List<NameValuePair> buildList(String... queryParams) {
        List<NameValuePair> result = new LinkedList<NameValuePair>();

        putInList(result, queryParams);

        return result;
    }

    protected void appendToList(List<NameValuePair> list, String... queryParams) {
        putInList(list, queryParams);
    }

    private void putInList(List<NameValuePair> result, String[] queryParams) {
        if (queryParams.length % 2 == 1) {
            throw new IllegalArgumentException("There must be an even number of query parameters (key, value)");
        }

        for (int i = 0; i < queryParams.length; ) {
            result.add(new BasicNameValuePair(queryParams[i], queryParams[i + 1]));
            i += 2;
        }
    }
}
