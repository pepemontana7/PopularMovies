package com.example.jose.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
                Toast.makeText(rootView.getContext(), mGridAdapter.getItem(position).getTitle(), Toast.LENGTH_SHORT).show();
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
            mGridData.add(item);
            resultStrs[i] = title + "@!@" + release  + "@!@" + rating + "@!@" + imagePath + "@!@" + plot;
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Movie entry: " + s);
        }
        return resultStrs;

    }
    @Override
    public void onStart(){
        super.onStart();
        updateMovie();
    }
    private void updateMovie(){
        FetchMovieTask movieTask = new FetchMovieTask() ;
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String postalCode = sharedPref.getString(getString(R.string.pref_location_key),
        //        getString(R.string.pref_location_default));
        movieTask.execute(getString(R.string.sort_by_popularity));
    }
    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params ) {
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

            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String APPID_PARAM = "api_key";

                Uri builtUrl = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortBy)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_THE_MOVIE_DB_API_KEY)
                        .build();
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


                return movieStrings;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "exception creating array of stirngs from Json response", e);
            }

            return new String[0];

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
