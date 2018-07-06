package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
/*
    For retweet, no need to launch ComposeActivity class, just send tweet with tweet including username in quotes"
    For reply, somehow add extra to intent? maybe boolean indicating reply = true
 */


public class TimelineActivity extends AppCompatActivity {
    // REQUEST_CODE can be any value we like, used to determine the result type later
    private final int REQUEST_CODE = 20;
    private SwipeRefreshLayout swipeContainer;
    TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        client = TwitterApp.getRestClient(this);


        // find the RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweet);

        // init the arraylist (data source)
        tweets = new ArrayList<>();
        // construct the adapter from this datasource
        tweetAdapter = new TweetAdapter(tweets);
        // RecyclerView setup (layout manager, use adapter)
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        // set the adapter
        rvTweets.setAdapter(tweetAdapter);

        populateTimeline();

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                // Remember to CLEAR OUT old items before appending in the new ones
                tweetAdapter.clear();

                // iterate through the JSON array
                // for each entry, deserialize the JSON object

                for (int i = 0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    // add that Tweet model to our data source
                    // notify the adapter that we've added an item
                    try {
                        Tweet tweet = Tweet.fromJson(response.getJSONObject(i));
                        tweets.add(tweet);
                        //tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // ...the data has come back, add new items to your adapter...
                tweetAdapter.addAll(tweets);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if intent was received successfully (if result ok matches result code
        if (resultCode == RESULT_OK) {
            // then unwrap the parcealized (vs serialized) tweet
            Tweet tweet = (Tweet) Parcels.unwrap(data.getParcelableExtra("tweet"));
            // add tweet to array and notify adapter of additiom
            tweets.add(0, tweet);
            tweetAdapter.notifyItemInserted(0);
            // scroll the list back to the top
            rvTweets.scrollToPosition(0);
        }
    }

    public void onComposeAction(MenuItem mi) {
        // first parameter is the context, second is the class of the activity to launch
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE); // brings up the second activity
    }
    public void onReplyAction(View view) {
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE); // brings up the second activity
    }
    public void onRetweetAction(View view) {
        Intent i = new Intent(this, ComposeActivity.class);
        startActivityForResult(i, REQUEST_CODE); // brings up the second activity
    }
    private void populateTimeline(){
        client.getHomeTimeline(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
  //              Log.d("TwitterClient", response.toString());
                // iterate through the JSON array
                // for each entry, deserialize the JSON object

                for (int i = 0; i < response.length(); i++){
                    // convert each object to a Tweet model
                    // add that Tweet model to our data source
                    // notify the adapter that we've added an item
                    try {
                        Tweet tweet = Tweet.fromJson(response.getJSONObject(i));
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }
}
