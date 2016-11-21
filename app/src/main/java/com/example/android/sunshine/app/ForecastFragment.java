package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ForecastFragment extends Fragment {
    private String mock = "{\"city\":{\"id\":5375480,\"name\":\"Mountain View\",\"coord\":{\"lon\":-122.083847,\"lat\":37.386051},\"country\":\"US\",\"population\":0},\"cod\":\"200\",\"message\":0.3371,\"cnt\":7,\"list\":[{\"dt\":1479668400,\"temp\":{\"day\":16.13,\"min\":8.78,\"max\":16.13,\"night\":8.78,\"eve\":14.45,\"morn\":16.13},\"pressure\":989.65,\"humidity\":97,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":1.22,\"deg\":147,\"clouds\":56,\"rain\":3.44},{\"dt\":1479754800,\"temp\":{\"day\":11.57,\"min\":4.27,\"max\":13.32,\"night\":6.5,\"eve\":12.04,\"morn\":4.27},\"pressure\":993.63,\"humidity\":99,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.47,\"deg\":306,\"clouds\":24,\"rain\":0.23},{\"dt\":1479841200,\"temp\":{\"day\":9.63,\"min\":4.02,\"max\":12.66,\"night\":5.44,\"eve\":11.99,\"morn\":5.28},\"pressure\":997.03,\"humidity\":98,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":1.46,\"deg\":121,\"clouds\":48},{\"dt\":1479927600,\"temp\":{\"day\":12.31,\"min\":7.84,\"max\":12.7,\"night\":7.84,\"eve\":12.7,\"morn\":10.83},\"pressure\":1016.73,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":2.96,\"deg\":319,\"clouds\":64,\"rain\":11.6},{\"dt\":1480014000,\"temp\":{\"day\":11.19,\"min\":5.78,\"max\":12.83,\"night\":8.39,\"eve\":12.83,\"morn\":5.78},\"pressure\":1021.82,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"clear sky\",\"icon\":\"01d\"}],\"speed\":1.51,\"deg\":133,\"clouds\":26},{\"dt\":1480100400,\"temp\":{\"day\":12.06,\"min\":8.75,\"max\":13.42,\"night\":9.82,\"eve\":13.42,\"morn\":8.75},\"pressure\":1017.94,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":3.6,\"deg\":174,\"clouds\":73,\"rain\":8.08},{\"dt\":1480186800,\"temp\":{\"day\":12.14,\"min\":9.97,\"max\":13.6,\"night\":13.34,\"eve\":13.6,\"morn\":9.97},\"pressure\":1016.9,\"humidity\":0,\"weather\":[{\"id\":501,\"main\":\"Rain\",\"description\":\"moderate rain\",\"icon\":\"10d\"}],\"speed\":2.96,\"deg\":182,\"clouds\":78,\"rain\":10.66}]}";
    private ArrayAdapter<String> adapter;
    private ListView listView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] list = new String[]{"Today - Sunny", "Tomorrow - Rainy", "Weds - Cloudy"};

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("94043");
        }
        return true;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private String getReadableDateString(long time) {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {
            return String.valueOf(high) + "/" + String.valueOf(low);
        }

        @Override
        protected String[] doInBackground(String... zip) {
            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (zip.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, zip[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
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
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v("TEST", "Forecast: " + s);
            }
            return resultStrs;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            adapter = new ArrayAdapter<>(
                    getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview,
                    Arrays.asList(strings));
            listView.setAdapter(adapter);
        }
    }


}
