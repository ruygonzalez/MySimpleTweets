package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Tweet {

    // list out the attributes
    public String body;
    public long uid; // database ID for the tweet
    public User user;
    public String createdAt;
    public int retweetcount;
    public int favoritescount;

    // deserialize the JSON
    public static Tweet fromJson(JSONObject jsonObject) throws JSONException{
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweetcount = jsonObject.getInt("retweet_count");
        tweet.favoritescount = jsonObject.getInt("favorite_count");
        //tweet.imageUrl = jsonObject.getJSONObject("entities").getJSONArray("media").getJSONObject(0).getString("media_url");
        return tweet;
    }


}
