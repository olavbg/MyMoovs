package olavbg.com.mymoovs_beta;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tools.PreferenceHandler;

public class UserFunctions {

    private static String loginURL = "http://www.olavbg.com/android.php";
    private static String registerURL = "http://www.olavbg.com/android.php";
    private static String login_tag = "login";
    private static String register_tag = "register";
    private JSONParser jsonParser;
    public static boolean add_details = true;
    private Context context;
    public static ArrayList<String> offlineChanges = new ArrayList<String>();
    PreferenceHandler preferenceHandler;

    public ArrayList<Object> objects = new ArrayList<Object>();

    // constructor
    public UserFunctions(Context context) {
        jsonParser = new JSONParser();
        this.context = context;
    }

    /**
     * function make Login Request
     *
     * @param username
     * @param password
     */
    public JSONObject loginUser(String username, String password) {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", login_tag));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("hash", "1"));
            objects.add(params);
            JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
            // return json
            //Log.e("JSON", json.toString());
            return json;
        }
        return null;
    }

    public JSONArray getMovies(Integer userID) {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "getMovies"));
            params.add(new BasicNameValuePair("userID", userID.toString()));
            JSONArray json = jsonParser.getJSONArrayFromUrl(loginURL, params);
            //Log.d("Response",json.toString());
            return json;
        }
        return null;
    }

    public JSONArray getMovieFormats() {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "getMovieFormats"));
            JSONArray json = jsonParser.getJSONArrayFromUrl(loginURL, params);
//        Log.d("Response", json.toString());
            return json;
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public JSONObject add_movie(Movie movie) throws JSONException {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", "add_movie"));
        params.add(new BasicNameValuePair("title", movie.getTitle()));
        params.add(new BasicNameValuePair("format", movie.getFormat()));
        params.add(new BasicNameValuePair("brukerID", String.valueOf(MainActivity.jsonUser.getInt("brukerID"))));
        if (add_details) {
            params.add(new BasicNameValuePair("addDetails", String.valueOf(add_details)));
        }
        if (!isConnected()) {
            offlineChanges.add(getJsonStringFromBNVP(params));
            System.out.println(offlineChanges);
        } else {
            return jsonParser.getJSONFromUrl(loginURL, params);
        }
        return null;
    }

    private String getJsonStringFromBNVP(List<NameValuePair> params) {
        StringBuilder jsonString = new StringBuilder("{");
        for (NameValuePair param : params) {
            jsonString.append("'").append(param.getName()).append("':'").append(param.getValue()).append("',");
        }
        return jsonString.toString().substring(0, jsonString.length() - 1) + "}";
    }

    public void delete_movie(Movie movie) {
        delete_movie(movie.getMovieID());
    }

    public void delete_movie(int movieID) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (isConnected()) {
            params.add(new BasicNameValuePair("tag", "delete_movie"));
            params.add(new BasicNameValuePair("movieID", String.valueOf(movieID)));
            jsonParser.getJSONFromUrl(loginURL, params);
        } else {
            try {
                params.add(new BasicNameValuePair("tag", "delete_movie_withoutID"));
                params.add(new BasicNameValuePair("userID", String.valueOf(MainActivity.jsonUser.getInt("brukerID"))));
                Movie movie = MainActivity.findMovie(movieID);
                params.add(new BasicNameValuePair("title", movie.getTitle()));
                params.add(new BasicNameValuePair("format", movie.getFormat()));
                offlineChanges.add(getJsonStringFromBNVP(params));
                System.out.println(offlineChanges);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * function make Register Request
     *
     * @param name
     * @param email
     * @param password
     */
    public JSONObject registerUser(String name, String email, String password) {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", register_tag));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            // getting JSON Object
            JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
            // return json
            return json;
        }
        return null;
    }

    public void borrowed_movie(Movie newMovie) {
        // Building Parameters
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("navn", newMovie.getNavn()));
            params.add(new BasicNameValuePair("userID", String.valueOf(MainActivity.jsonUser.getInt("brukerID"))));
            if (isConnected()){
                params.add(new BasicNameValuePair("tag", "borrowedMovie"));
                params.add(new BasicNameValuePair("movieID", String.valueOf(newMovie.getMovieID())));
                jsonParser.getJSONFromUrl(loginURL, params);
            }else{
                params.add(new BasicNameValuePair("tag", "borrowedMovie_withoutID"));
                params.add(new BasicNameValuePair("title", newMovie.getTitle()));
                params.add(new BasicNameValuePair("format", newMovie.getFormat()));
                offlineChanges.add(getJsonStringFromBNVP(params));
                System.out.println(offlineChanges);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void lend_movie(Movie newMovie) {
        // Building Parameters
        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("navn", newMovie.getNavn()));
            params.add(new BasicNameValuePair("userID", String.valueOf(MainActivity.jsonUser.getInt("brukerID"))));
            if (isConnected()){
                params.add(new BasicNameValuePair("tag", "lentMovie"));
                params.add(new BasicNameValuePair("movieID", String.valueOf(newMovie.getMovieID())));
                jsonParser.getJSONFromUrl(loginURL, params);
            }else{
                params.add(new BasicNameValuePair("tag", "lentMovie_withoutID"));
                params.add(new BasicNameValuePair("title", newMovie.getTitle()));
                params.add(new BasicNameValuePair("format", newMovie.getFormat()));
                offlineChanges.add(getJsonStringFromBNVP(params));
                System.out.println(offlineChanges);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void returned_movie(Movie movie) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("ut", String.valueOf(movie.getUt())));
        if (isConnected()){
            params.add(new BasicNameValuePair("tag", "returnedMovie"));
            params.add(new BasicNameValuePair("movieID", String.valueOf(movie.getMovieID())));
            jsonParser.getJSONFromUrl(loginURL,params);
        }else{
            params.add(new BasicNameValuePair("tag", "returnedMovie_withoutID"));
            params.add(new BasicNameValuePair("title",movie.getTitle()));
            params.add(new BasicNameValuePair("format",movie.getFormat()));
            offlineChanges.add(getJsonStringFromBNVP(params));
            System.out.println(offlineChanges);
        }
    }

    public JSONObject registerUserGoogle(String googleID, String username, String email) {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "signInGPlus"));
            params.add(new BasicNameValuePair("googleID", googleID));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("email", email));

            // getting JSON Object
            JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
            // return json
            return json;
        }
        return null;
    }

    public JSONObject search_ean(String nr) {
        if (isConnected()) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "search_ean"));
            params.add(new BasicNameValuePair("eanNr", nr));
            // getting JSON Object
            JSONObject json = jsonParser.getJSONFromUrl("http://www.testing.olavbg.com/android_search_ean.php", params);
            // return json
            return json;
        }
        return null;
    }

    public boolean userHasMovie(String title, String format) throws JSONException {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", "userHasMovie"));
        params.add(new BasicNameValuePair("userID", String.valueOf(MainActivity.jsonUser.getInt("brukerID"))));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("format", format));
        if (!isConnected()) {
            return MainActivity.findMovie(title, format) != null;
        } else {
            JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
            if (json.getString("hasMovie").equals("1")) {
                return true;
            }
        }
        //Log.d("Response",json.toString());
        return false;
    }

    public JSONObject updateMovie(int movieID, String newTitle, String newFormat) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (isConnected()){
            params.add(new BasicNameValuePair("tag", "updateMovie"));
            params.add(new BasicNameValuePair("movieID", String.valueOf(movieID)));
            params.add(new BasicNameValuePair("title", newTitle));
            params.add(new BasicNameValuePair("format", newFormat));
            return jsonParser.getJSONFromUrl(loginURL, params);
        }else{
            final Movie movie = MainActivity.findMovie(movieID);
            params.add(new BasicNameValuePair("tag","updateMovie_withoutID"));
            params.add(new BasicNameValuePair("oldTitle",movie.getTitle()));
            params.add(new BasicNameValuePair("oldFormat",movie.getFormat()));
            params.add(new BasicNameValuePair("newTitle", newTitle));
            params.add(new BasicNameValuePair("newFormat", newFormat));
            offlineChanges.add(getJsonStringFromBNVP(params));
            System.out.println(offlineChanges);
        }
        //Log.d("Response",json.toString());
        return null;
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}