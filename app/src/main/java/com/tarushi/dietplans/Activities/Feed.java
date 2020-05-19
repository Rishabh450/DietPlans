package com.tarushi.dietplans.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tarushi.dietplans.Adapters.FeedAdapter;
import com.tarushi.dietplans.Adapters.StatusAdapter;
import com.tarushi.dietplans.Modals.Comments;
import com.tarushi.dietplans.Modals.FeedPost;
import com.tarushi.dietplans.Modals.Likes;
import com.tarushi.dietplans.Modals.MyCallback;
import com.tarushi.dietplans.Modals.Seen;
import com.tarushi.dietplans.Modals.Status;
import com.tarushi.dietplans.R;

import java.util.ArrayList;

public class Feed extends AppCompatActivity {
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private NavigationView mNavigationDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mPullUp;
    private AlertDialog.Builder builder;
    private TranslateAnimation mAnimation;
    private ProgressBar recyclerview_progress;
    RecyclerView status;
    private SwipeRefreshLayout refreshLayout;

    //Poster shit
    private static final int BANNER_DELAY_TIME = 5 * 1000;
    private static final int BANNER_TRANSITION_DELAY = 1200;
    private Runnable runnable;
    private Handler handler;
    private boolean firstScroll = true;
    TextView nopost;

    private RecyclerView mRecyclerView;
    private FeedAdapter mFeedAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private static final int ACCESSIBILITY_ENABLED = 1;

    private DatabaseReference dref;

    private ArrayList<FeedPost> listposts;
    private ArrayList<Status> statusArrayList = new ArrayList<>();
    StatusAdapter statusAdapter;

    private String currentuid;
    ImageView statusmypic;

    private FrameLayout homeContainer;
    private ViewPager viewPager;
    ValueEventListener valueEventListener;
    String providerr, currentUser;
    ValueEventListener listener;
    int flag = 1;

    private NestedScrollView scrollView;
    private boolean mScrollDown = false;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);


        mRecyclerView = findViewById(R.id.feed_recycler_view);
        status = findViewById(R.id.statusRecycler);

        nopost=findViewById(R.id.nopost);

        refreshLayout=findViewById(R.id.swipe_refresh);



        listposts = new ArrayList<>();
/*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("PostNo", Integer.toString(0));*/
        /*editor.apply();*/
        fetchFeedsDataFromFirebase();
        getData();
        refresh();


    }
    void refresh() {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                fetchFeedsDataFromFirebase();
//                if (mFeedAdapter != null)
//                    mFeedAdapter.notifyDataSetChanged();
                if (mFeedAdapter != null)
                    mFeedAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
            }
        });
    }
    public void getData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Status");
        reference.keepSynced(false);
        reference.addValueEventListener(listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                statusArrayList.clear();
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final boolean[] flagseen = {false};
                    final String statusid = ds.getKey();

                    //String attr=child.getKey();
                    final String userid = ds.child("userid").getValue(String.class);


                            if(true) {
                                String text = ds.child("text").getValue(String.class);
                                String url = ds.child("url").getValue(String.class);
                                Log.d("userstatusid", userid + " " + statusid);

                                ArrayList<Seen> seen = new ArrayList<>();
                                for (DataSnapshot dsseen : ds.child("seen").getChildren()) {
                                    //Likes like=dslike.getValue(Likes.class);
                                    String temp = dsseen.getKey();
                                    Seen sea = new Seen(temp, dsseen.child("time").getValue(String.class));
                                    Log.d("userstatusi", sea.getUserid() + " " + sea.getTime());
                                    seen.add(sea);
                                    if (temp.equals(currentUser)) {
                                        flagseen[0] = true;
                                    }

                                }
                                Status status = new Status(statusid, userid, flagseen[0], seen, text, url);
                                statusArrayList.add(status);
                                setupStatus();
                                statusAdapter.notifyDataSetChanged();
                            }




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fetchFeedsDataFromFirebase() {

        dref = FirebaseDatabase.getInstance().getReference().child("Feeds");

        dref.keepSynced(true);
        dref.addListenerForSingleValueEvent(valueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // recyclerview_progress.setVisibility(View.VISIBLE);
                listposts.clear();
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    //FeedPost post = ds.getValue(FeedPost.class);
                    final String post_id_temp = ds.getKey();
                    String is = "";
                    if (true) {
                        final String sender_id = ds.child("senderID").getValue().toString();

                                if (true) {
                                    boolean flag = false;

                                    ArrayList<Likes> mlikes = new ArrayList<>();
                                    for (DataSnapshot dslike : ds.child("likes").getChildren()) {

                                        String temp = dslike.getValue().toString();
                                        Likes like = new Likes(temp);
                                        mlikes.add(like);
                                        if (temp.equals(currentUser)) {
                                            flag = true;
                                        }
                                    }

                                    ArrayList<Comments> mcomments = new ArrayList<>();

                                    String content = ds.child("content").getValue().toString();
                                    String event_name = ds.child("event").getValue().toString();
                                    String subevent_name = ds.child("subEvent").getValue().toString();
                                    String image_url = ds.child("imageURL").getValue().toString();
                                    String sender_url = ds.child("senderURL").getValue(String.class);

                                    FeedPost post = new FeedPost(flag, post_id_temp, content, event_name, image_url, subevent_name, mlikes, mcomments, sender_url, sender_id);

                                    Log.e("vila", post.getImageURL());
                                    listposts.add(post);
                                    if (flag == true)
                                        setUpfirstRecyclerView();
                                    else
                                        setUpRecyclerView();
                                    Log.e("VIVZ", "onDataChange: listposts count = " + listposts.size());
                                    if(listposts.size()==0) {
                                        nopost.setVisibility(View.VISIBLE);
                                        // Log.e("VIVZ", "onDataChange: listposts count =visible " + listposts.size());

                                    }
                                    else {
                                        nopost.setVisibility(View.GONE);
                                        // Log.e("VIVZ", "onDataChange: listposts count =gone " + listposts.size());

                                    }
                                }


                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private void setupStatus() {
        status.setHasFixedSize(true);

        status.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        statusAdapter = new StatusAdapter(this, statusArrayList, currentUser);
        status.scrollToPosition(statusAdapter.getItemCount() - 1);

        status.setAdapter(statusAdapter);

    }

    private void setUpfirstRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);


        mFeedAdapter = new FeedAdapter(this, getSupportFragmentManager(), listposts, currentUser);
        mRecyclerView.setAdapter(mFeedAdapter);
        //recyclerview_progress.setVisibility(View.GONE);

/*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
*/

      /*  String numpost = preferences.getString("PostNo", "");
        mRecyclerView.scrollToPosition(mFeedAdapter.getItemCount() - 1);*/
        flag = 0;
    }

    /*CallBackk callback;

    void onEvent(String userr) {
        callback.onComplete(userr);
    }*/
    private void setUpRecyclerView() {

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);


        mFeedAdapter = new FeedAdapter(this, getSupportFragmentManager(), listposts, currentUser);
        mRecyclerView.setAdapter(mFeedAdapter);
        //recyclerview_progress.setVisibility(View.GONE);

       /* SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String numpost = preferences.getString("PostNo", "");*/
        /*mRecyclerView.scrollToPosition(Integer.parseInt(numpost));*/
    }

}


