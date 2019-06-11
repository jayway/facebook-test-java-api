package com.jayway.facebooktestjavaapi.testuser.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.facebooktestjavaapi.testuser.AccountSettingsChanger;
import com.jayway.facebooktestjavaapi.testuser.FacebookTestUserAccount;
import com.jayway.facebooktestjavaapi.testuser.FacebookTestUserStore;


/**
 * A FacebookTestUserAccount implementation that relies on HttpClientFacebookTestUserStore.
 */
public class HttpClientFacebookTestUserAccount implements FacebookTestUserAccount {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientFacebookTestUserAccount.class);

    private final HttpClientFacebookTestUserStore helper;
    private JSONObject jsonUser;

    public HttpClientFacebookTestUserAccount(HttpClientFacebookTestUserStore helper, JSONObject user) {
        this.helper = helper;
        this.jsonUser = user;
    }

    @Override
    public void delete() {
        String result = helper.delete("/%s", id());
        LOG.debug("Deleted account [{}]: [{}]", id(), result);
    }

    @Override
    public void copyToOtherApplication(String applicationId, String accessToken, boolean appInstalled, String permissions) {
        String result = helper.post("/%s/accounts/test-users",
                helper.buildList("installed", Boolean.toString(appInstalled), "permissions", permissions == null ? "email, offline_access" : permissions, "owner_access_token", helper.getAccessToken()),
                helper.buildList("access_token", accessToken), applicationId);
        LOG.debug("Copied account: " + result);
    }

    @Override
    public void copyToTestUserStore(FacebookTestUserStore testUserStore, boolean appInstalled, String permissions) {
        if (testUserStore instanceof HttpClientFacebookTestUserStore) {
            HttpClientFacebookTestUserStore knownStore = (HttpClientFacebookTestUserStore) testUserStore;
            copyToOtherApplication(knownStore.getApplicationId(), knownStore.getAccessToken(), appInstalled, permissions);
        } else {
            throw new IllegalArgumentException("The provided testUserStore is of unknown type");
        }
    }

    @Override
    public void makeFriends(FacebookTestUserAccount friend) {
        String requestResult = helper.post("/%s/friends/%s", null, helper.buildList("access_token", accessToken()), id(), friend.id());
        LOG.debug("Creating friend request: " + requestResult);
        String acceptResult = helper.post("/%s/friends/%s", null, helper.buildList("access_token", friend.accessToken()), friend.id(), id());
        LOG.debug("Accepting friend request: " + acceptResult);
    }

    @Override
    public AccountSettingsChanger changeAccountSettings()
    {
        return new DefaultAccountSettingsChanger();
    }

    @Override
    public String getFriends() {
        return get("/%s/friends", id());
    }

    @Override
    public String getProfileFeed() {
        return get("/%s/feed", id());
    }

    @Override
    public String getNewsFeed() {
        return get("/%s/home", id());
    }

    @Override
    public String getLikes() {
        return get("/%s/likes", id());
    }

    @Override
    public String getMovies() {
        return get("/%s/movies", id());

    }

    @Override
    public String getMusic() {
        return get("/%s/music", id());
    }

    @Override
    public String getBooks() {
        return get("/%s/books", id());
    }

    @Override
    public String getNotes() {
        return get("/%s/notes", id());
    }

    @Override
    public String getPhotoTags() {
        return get("/%s/photos", id());
    }

    @Override
    public String getPhotoAlbums() {
        return get("/%s/albums", id());
    }

    @Override
    public String getVideoTags() {
        return get("/%s/videos", id());
    }

    @Override
    public String getVideoUploads() {
        return get("/%s/videos/uploaded", id());
    }

    @Override
    public String getEvents() {
        return get("/%s/events", id());
    }

    @Override
    public String getGroups() {
        return get("/%s/groups", id());
    }

    @Override
    public String getCheckins() {
        return get("/%s/checkins", id());
    }

    @Override
    public String getUserDetails() {
        return get("%s", id());
    }

    @Override
    public String id() {
        return userDataAsString("id");
    }

    @Override
    public String accessToken() {
        return userDataAsString("access_token");
    }

    @Override
    public String loginUrl() {
        return userDataAsString("login_url");
    }

    @Override
    public String getPassword()
    {
        return userDataAsString("password");
    }
    
    public String getEmail()
    {
        return userDataAsString("email");
    }

    @Override
    public String json() {
        return jsonUser.toJSONString();
    }

    private String userDataAsString(String data) {
        if (jsonUser == null) {
            return null;
        }

        Object anObject = jsonUser.get(data);
        return anObject != null ? anObject.toString() : null;
    }

    private String get(String resource, Object... pathParams) {
        return helper.get(resource, helper.buildList("access_token", accessToken()), pathParams);
    }


    private class DefaultAccountSettingsChanger implements AccountSettingsChanger
    {
        private List<NameValuePair> settings = new LinkedList<NameValuePair>();

        @Override
        public AccountSettingsChanger newName(String name)
        {
            helper.appendToList(settings, "name", name);
            return this;
        }

        @Override
        public AccountSettingsChanger newPassword(String password)
        {
            helper.appendToList(settings,"password", password);
            return this;
        }

        @Override
        public void apply()
        {
            if (settings.size() > 0) {
                final String result = helper.post("/%s", settings, null, id());
                LOG.debug("Changed settings: " + result);
            }
        }
    }
}
