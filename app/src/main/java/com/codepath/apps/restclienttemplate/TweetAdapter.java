package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder>{
    private List<Tweet> mTweets;
    Context context;

    // pass in the Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets){
        mTweets = tweets;
    }

    // for each row, inflate the layout and cache references into ViewHolder

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);

        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // bind the values based on the position of the element

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data according to position
        Tweet tweet = mTweets.get(position);
        // populate the views according to this data
        holder.tvUserName.setText(tweet.user.name);
        holder.tvBody.setText(tweet.body);
        holder.createdAt.setText(getRelativeTimeAgo(tweet.createdAt));
        holder.tvretweet.setText(Integer.toString(tweet.retweetcount));
        // Round the corners of the profile images
        final RoundedCornersTransformation roundedCornersTransformation
                = new RoundedCornersTransformation(100, 15);
        final RequestOptions requestOptions = RequestOptions.bitmapTransform(roundedCornersTransformation);
        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .apply(requestOptions)
                .into(holder.ivProfileImage);
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // relative date needs to be shortened to '7h' or '8m' or '9s'
        String shortened = relativeDate.substring(0,relativeDate.indexOf(" ") + 2);
        int i = shortened.indexOf(" ");
        shortened = shortened.substring(0,i) + shortened.substring(i+1);
        return shortened;
    }
    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        mTweets.addAll(list);
        notifyDataSetChanged();
    }


    // create ViewHolder class

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView createdAt;
        public Button btnReply;
        public ImageView ivRetweet;
        public TextView tvretweet;


        public ViewHolder(View itemView){
            super(itemView);

            // perform findViewById lookups

            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvretweet = (TextView)  itemView.findViewById(R.id.tvretweet);
            createdAt = (TextView) itemView.findViewById(R.id.tvDate);
            btnReply = (Button) itemView.findViewById(R.id.btnReply);
            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // gets item position
                    int position = getAdapterPosition();
                    // make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION) {
                        // get the movie at the position, this won't work if the class is static
                        Tweet t = mTweets.get(position);
                        // create intent for the new activity
                        Intent intent = new Intent(context, ComposeActivity.class);
                        intent.putExtra("replying_to", "@" + t.user.screenName);
                        intent.putExtra("uid", t.uid);
                        // serialize the tweet using parceler, use its short name as a key
                        //intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(t));
                        // show the activity
                        ((Activity) context).startActivityForResult(intent, 404);
                    }
                }
            });
            ivRetweet = (ImageView) itemView.findViewById(R.id.iv_retweet);
            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // gets item position
                    int position = getAdapterPosition();
                    // make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION) {
                        // get the movie at the position, this won't work if the class is static
                        Tweet t = mTweets.get(position);
                        TwitterClient c = TwitterApp.getRestClient(context);
                        c.retweet(t.uid, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);
                                try {
                                    Tweet tweet = Tweet.fromJson(response);
                                    // Prepare data intent
                                    Intent data = new Intent();
                                    // Pass tweet as 'extra' in intent back to previous activity
                                    data.putExtra("tweet", Parcels.wrap(tweet)); // wrap tweet with parcels for speed over serializable
                                    ((Activity) context).startActivityForResult(data, 404);
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
            });
        }
    }


}
