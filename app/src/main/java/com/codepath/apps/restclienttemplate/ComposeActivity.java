package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {
    // text field containing tweet
    EditText et_simple;
    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        // resolve the text field from the layout
        et_simple = (EditText) findViewById(R.id.et_simple);

    }
    public void goBack(View view){
        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);
        finish();
    }
    public void composeTweet(View view){
        TwitterClient c = TwitterApp.getRestClient(this);
        c.sendTweet(et_simple.getText().toString(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    tweet = Tweet.fromJson(response);
                    // Prepare data intent
                    Intent data = new Intent();
                    // Pass tweet as 'extra' in intent back to previous activity
                    data.putExtra("tweet", Parcels.wrap(tweet)); // wrap tweet with parcels for speed over serializable
                    setResult(RESULT_OK, data); // send result code showing it was a success along with tweet
                    finish(); // close activity and return data (tweet) to intent
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
                // add message to log to make it easier to debug
                Log.d("SendTweet error", errorResponse.toString());
            }
        });

    }
}
