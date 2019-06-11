package com.jayway.facebooktestjavaapi.testuser;

/**
 * API for a Facebook test user, encapsulating the ID, Access token and the Login URL for the user.
 * <p/>
 * This API also provides access to JSON string representation of connections to user objects
 * described in the <a href="http://developers.facebook.com/docs/api">Facebook Graph API</a>
 * documentation, which is essentially resources accessible using the
 * <code>https://graph.facebook.com/ID/CONNECTION_TYPE</code> syntax.
 */
public interface FacebookTestUserAccount
{

    /**
     * Deletes this test user in Facebook.
     */
    void delete();

    /**
     * Copies this test user to another application
     *
     * @param otherApplicationId          The application ID of the other application
     * @param otherApplicationAccessToken The access token for the other application
     * @param appInstalled                True if the account should directly accept the application requesting the specified permissions,
     *                                    false to use the {@link com.jayway.facebooktestjavaapi.testuser.FacebookTestUserAccount#loginUrl()}}.
     * @param permissions                 Comma-separated list of permissions for the Facebook application. See
     *                                    <a href="http://developers.facebook.com/docs/authentication/permissions">Permissions</a>
     *                                    documentation.
     */
    void copyToOtherApplication(String otherApplicationId, String otherApplicationAccessToken, boolean appInstalled, String permissions);

    /**
     * Copies this test user to another FacebookTestUserStore.
     *
     * @param testUserStore The user store to copy this user to.
     * @param appInstalled  True if the account should directly accept the application requesting the specified permissions,
     *                      false to use the {@link com.jayway.facebooktestjavaapi.testuser.FacebookTestUserAccount#loginUrl()}}.
     * @param permissions   Comma-separated list of permissions for the Facebook application. See
     *                      <a href="http://developers.facebook.com/docs/authentication/permissions">Permissions</a>
     *                      documentation.
     */
    void copyToTestUserStore(FacebookTestUserStore testUserStore, boolean appInstalled, String permissions);

    /**
     * Creates a friend relationship between this test user and the test user in the parameter.
     *
     * @param friend A Facebook test user account.
     */
    void makeFriends(FacebookTestUserAccount friend);

    /**
     * Gives access to change settings for the test user.
     * @return An {@code AccountSettingsChanger} responsible for updating the account settings.
     */
    AccountSettingsChanger changeAccountSettings();

    /**
     * @return A JSON string array of /me/friends
     */
    String getFriends();

    /**
     * @return The JSON string array of /ID/feed
     */
    String getProfileFeed();

    /**
     * @return The JSON string array of /ID/home
     */
    String getNewsFeed();

    /**
     * @return The JSON string array of /ID/likes
     */
    String getLikes();

    /**
     * @return The JSON string array of /ID/movies
     */
    String getMovies();

    /**
     * @return The JSON string array of /ID/music
     */
    String getMusic();

    /**
     * @return The JSON string array of /ID/books
     */
    String getBooks();

    /**
     * @return The JSON string array of /ID/notes
     */
    String getNotes();

    /**
     * @return The JSON string array of /ID/photos
     */
    String getPhotoTags();

    /**
     * @return The JSON string array of /ID/albums
     */
    String getPhotoAlbums();

    /**
     * @return The JSON string array of /ID/videos
     */
    String getVideoTags();

    /**
     * @return The JSON string array of /ID/videos/uploaded
     */
    String getVideoUploads();

    /**
     * @return The JSON string array of /ID/events
     */
    String getEvents();

    /**
     * @return The JSON string array of /ID/groups
     */
    String getGroups();

    /**
     * @return The JSON string array of /ID/checkins
     */
    String getCheckins();

    /**
     * @return A JSON string containing user details for this test user (/me)
     */
    String getUserDetails();

    /**
     * @return The Facebook ID for this test user
     */
    String id();

    /**
     * @return The access token for this test user.
     */
    String accessToken();

    /**
     * The login URL gives access to the Facebook page for this test user.
     * <p/>
     * The <a href="http://developers.facebook.com/docs/test_users">Facebook documentation</a>
     * states that the URL expires after first use or after 10 minutes, whichever comes first.
     *
     * @return The login URL for this test user.
     */
    String loginUrl();

    /**
     * @return The password for the test user.
     */
    String getPassword();
    
    /**
     * @return The email for the test user.
     */
    String getEmail();

    /**
     * @return The JSON string representation of the id, access token and login URL.
     */
    String json();
}
