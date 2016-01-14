package com.example.jose.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, new MovieDetailActivityFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static class MovieDetailActivityFragment extends Fragment {
        private String mTitleStr;
        private String mImageStr;
        private String mPlotStr;
        private String mRatingStr;
        private String mReleaseStr;
        private static final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);
            if (intent != null && intent.hasExtra(getString(R.string.title_key)) ){
                mTitleStr = intent.getStringExtra(getString(R.string.title_key));
                TextView titleTextView = (TextView)rootView.findViewById(R.id.detail_title);
                titleTextView.setText(mTitleStr);
                if (intent.hasExtra(getString(R.string.image_key))){
                    mImageStr = intent.getStringExtra(getString(R.string.image_key));
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_item_image);
                    Picasso.with(getContext())
                            .load(mImageStr)
                            .into(imageView);
                }
                if (intent.hasExtra(getString(R.string.plot_key))){
                    mPlotStr = intent.getStringExtra(getString(R.string.plot_key));
                    TextView plotTextView = (TextView)rootView.findViewById(R.id.plot);
                    plotTextView.setText(mPlotStr);
                }
                if (intent.hasExtra(getString(R.string.rating_key))){
                    mRatingStr = intent.getStringExtra(getString(R.string.rating_key));
                    TextView ratingTextView = (TextView)rootView.findViewById(R.id.userRating);
                    ratingTextView.setText(mRatingStr);

                }
                if (intent.hasExtra(getString(R.string.release_key))){
                    mReleaseStr = intent.getStringExtra(getString(R.string.release_key));
                    TextView releaseTextView = (TextView)rootView.findViewById(R.id.releaseDate);
                    releaseTextView.setText(mReleaseStr);
                }
            }


            return rootView;
        }
    }

}
