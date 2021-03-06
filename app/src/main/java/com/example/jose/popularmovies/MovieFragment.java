package com.example.jose.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {
    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    private GridView mGridView;
    private ProgressBar mProgressBar;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;

    //private ArrayList<GridItem> mGridData;
    public MovieFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView)  rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(rootView.getContext(), mGridAdapter.getItem(position).getTitle(), Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                detailIntent.putExtra(getString(R.string.title_key), mGridAdapter.getItem(position).getTitle());
                detailIntent.putExtra(getString(R.string.plot_key), mGridAdapter.getItem(position).getPlot());
                detailIntent.putExtra(getString(R.string.image_key), mGridAdapter.getItem(position).getImage());
                detailIntent.putExtra(getString(R.string.release_key), mGridAdapter.getItem(position).getReleaseDate());
                detailIntent.putExtra(getString(R.string.rating_key), mGridAdapter.getItem(position).getUserRating());
                startActivity(detailIntent);
            }
        });
        return rootView;
}
    private String[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {
        GridItem item;
        // These are the names of the JSON objects that need to be extracted.
        final String TMD_RESULTS = "results";
        final int PAGE_COUNT = 20;
        final String TMD_PAGE = "page";
        final String TMD_TITLE = "original_title";
        final String TMD_PLOT = "overview";
        final String TMD_USER_RATING = "vote_average";
        final String TMD_RELEASE_DATE = "release_date";
        final String TMD_IMAGE = "poster_path";
        final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(TMD_RESULTS);


        String[] resultStrs = new String[movieArray.length()];
        for(int i = 0; i < movieArray.length(); i++) {

            String title;
            String plot;
            String release;
            String imagePath;
            String rating;

            JSONObject movieObject = movieArray.getJSONObject(i);

            title = movieObject.getString(TMD_TITLE);
            plot = movieObject.getString(TMD_PLOT);
            release = movieObject.getString(TMD_RELEASE_DATE);
            rating =  movieObject.getString(TMD_USER_RATING);
            imagePath = IMAGE_BASE_URL + movieObject.getString(TMD_IMAGE);
            item = new GridItem();
            item.setImage(imagePath);
            item.setTitle(title);
            item.setPlot(plot);
            item.setReleaseDate(release);
            item.setUserRating(rating);
            mGridData.add(item);
            resultStrs[i] = title + "@!@" + release  + "@!@" + rating + "@!@" + imagePath + "@!@" + plot;
            //Log.v(LOG_TAG, "strings count: " + resultStrs.length);
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Movie entry: " + s);
        }
        return resultStrs;

    }
    @Override
    public void onStart(){
        Log.v(LOG_TAG, "On START MOV FRAG: "  );
        super.onStart();
        updateMovie();
    }

    @Override
    public void onResume()

    {  // After a pause OR at startup
        super.onResume();
        Log.v(LOG_TAG, "On RESUME MOV FRAG: ");
        //Refresh your stuff here
        mGridData.clear();
         
    }

    private void updateMovie(){
        FetchMovieTask movieTask = new FetchMovieTask() ;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = sharedPref.getString(getString(R.string.pref_sortby_key),
                getString(R.string.pref_sortby_default));
        Log.v(LOG_TAG, "sort by: " + sortBy);
        movieTask.execute(sortBy);
    }
    public class FetchMovieTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params ) {
            if (params.length == 0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            String format = "json";
            //String sortBy = String.valueOf(R.string.sort_by_popularity);
            String sortBy =  params[0];
            Integer result = 0;
            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String APPID_PARAM = "api_key";
                final String VOTE_COUNT_PARAM = "vote_count.gte";
                final String MIN_VOTES = "100";

                Uri builtUrl = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortBy)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_THE_MOVIE_DB_API_KEY).build();
                Log.v(LOG_TAG,"true sort: " + String.valueOf(sortBy.equals(getString(R.string.sort_by_rated))));
                if(sortBy.equals(getString(R.string.sort_by_rated))){
                    builtUrl = Uri.parse(builtUrl.toString()).buildUpon().appendQueryParameter(VOTE_COUNT_PARAM, MIN_VOTES).build();
                }

                URL url = new URL(builtUrl.toString());
                Log.v(LOG_TAG,"URL Builder url: " + url);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return   null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return   null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                String[] movieStrings =  getMovieDataFromJson(movieJsonStr);
                Log.v(LOG_TAG, "movie string: " + movieStrings[0]);
                String firstMovie = movieStrings[0];
                for (String retval: firstMovie.split("@!@")){
                    Log.v(LOG_TAG, "split string: " + retval);
                }
                result = 1;

                //return movieStrings;
            } catch (JSONException e) {
                result = 0;
                Log.e(LOG_TAG, "exception creating array of stirngs from Json response", e);
            }

            return result;

        }


        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
            mProgressBar.setVisibility(View.GONE);
        }

    }
}
