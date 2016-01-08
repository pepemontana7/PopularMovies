package com.example.jose.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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

    public static class MovieDetailActivityFragment extends Fragment {
        private String mTempTitleStr;
        private String mTempImageStr;
        private static final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();

        public MovieDetailActivityFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT) ){
                mTempTitleStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView textView = (TextView)rootView.findViewById(R.id.detail_title);
                textView.setText(mTempTitleStr);
                if (intent.hasExtra("EXTRA_IMAGE_PATH")){
                    mTempImageStr = intent.getStringExtra("EXTRA_IMAGE_PATH");
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_item_image);
                    Picasso.with(getContext())
                            .load(mTempImageStr)
                            .into(imageView);
                }
            }


            return rootView;
        }
    }

}
