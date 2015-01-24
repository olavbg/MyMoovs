package olavbg.com.mymoovs;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import google.zxing.client.mymoovs.IntentIntegrator;
import google.zxing.client.mymoovs.IntentResult;
import tools.PreferenceHandler;

import static tools.Functions.getTodaysDate;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String MyPREFERENCES = "MyPrefs";
    public static ListView lstMovies;
    public static ArrayAdapter<Movie> adapter;
    public static ArrayAdapter<Movie> borrowedAdapter;
    public static ArrayAdapter<Movie> lentAdapter;
    public static ArrayAdapter<Movie> valgtAdapter = adapter;
    public static TextView lblRegisteredMovies;
    public static Context context;
    public static ArrayList<Movie> movies = new ArrayList<Movie>();
    public static ArrayList<Movie> borrowedMovies = new ArrayList<Movie>();
    public static ArrayList<Movie> lentMovies = new ArrayList<Movie>();
    public static ArrayList<Movie> valgtListe = movies;
    public static JSONObject jsonUser;
    public static Boolean refresh = false;
    private static JSONArray json;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Intent login_screen;
    Intent settings_screen;
    JSONObject search_res = null;
    private EditText txtTitle;
    private Spinner cbxFormats;
    public static UserFunctions userFunction;
    ProgressDialog dialog;
    public static Movie selected_movie = null;
    public final Gson gson = new Gson();
    public PreferenceHandler preferenceHandler;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public MainActivity() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (refresh) {
            refreshLists(valgtListe);
            refresh = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get objects from XML
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        cbxFormats = (Spinner) findViewById(R.id.cbxFormat);
        lstMovies = (ListView) findViewById(R.id.lstMovies);
        lblRegisteredMovies = (TextView) findViewById(R.id.lblRegisteredMovies);
        login_screen = new Intent(MainActivity.this, LoginActivity.class);
        settings_screen = new Intent(MainActivity.this, SettingsActivity.class);
        dialog = new ProgressDialog(getApplicationContext());

        context = this;
        preferenceHandler = new PreferenceHandler(context);
        startLoading("", getString(R.string.fetching_movies));

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        movies.clear();
        adapter = new ArrayAdapter<Movie>(this,
                android.R.layout.simple_list_item_1, movies);
        lentAdapter = new ArrayAdapter<Movie>(this,
                android.R.layout.simple_list_item_1, lentMovies);
        borrowedAdapter = new ArrayAdapter<Movie>(this,
                android.R.layout.simple_list_item_1, borrowedMovies);

        // Assign adapter to ListView
        lstMovies.setAdapter(adapter);

        registerForContextMenu(lstMovies);

        //Binding enter button to add_new_movie
        txtTitle.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    add_new_movie();
                    return true;
                }
                return false;
            }
        });

        lstMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_movie = (Movie) adapterView.getItemAtPosition(i);
                Intent movie_details = new Intent(getApplicationContext(), MovieDetails.class);
                movie_details.putExtra("movie_id", selected_movie.getMovieID());
                startActivity(movie_details);
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        userFunction = new UserFunctions(context);

        if (sharedpreferences.contains("userdata")) {
            try {
                jsonUser = new JSONObject(sharedpreferences.getString("userdata", ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            startActivity(login_screen);
        }

        update_movies();
        addFormatsToSpinner();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void addFormatsToSpinner() {
        if (!isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                JSONArray movieFormats = userFunction.getMovieFormats();
                final ArrayList<String> movieFormatsList = new ArrayList<String>();
                if (movieFormats != null) {
                    for (int i = 0; i < movieFormats.length(); i++) {
                        try {
                            JSONObject movieFormat = movieFormats.getJSONObject(i);
                            movieFormatsList.add(movieFormat.getString("beskrivelse"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!movieFormatsList.isEmpty()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                                    android.R.layout.simple_spinner_item, movieFormatsList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cbxFormats.setAdapter(dataAdapter);
                            cbxFormats.invalidate();
                        }
                    });
                }
            }
        }).start();
    }

    public void update_movies() {
        //Retrieving movies from server
        if (isConnected()) {
            startLoading("", getString(R.string.fetching_movies));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        json = userFunction.getMovies(jsonUser.getInt("brukerID"));
                        movies.clear();
                        lentMovies.clear();
                        borrowedMovies.clear();
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject = json.getJSONObject(i);
                            Movie newMovie = new Movie(Integer.valueOf(jsonObject.getString("filmID")), jsonObject.getString("tittel").replace("\\", ""), jsonObject.getString("format"));
                            String ut = jsonObject.getString("ut");

                            String date_added = jsonObject.getString("lagtTil");
                            int year = !jsonObject.get("year").toString().isEmpty() ? Integer.parseInt(jsonObject.getString("year")) : 0;
                            int runtime = !jsonObject.get("runtime").toString().isEmpty() ? Integer.parseInt(jsonObject.getString("runtime")) : 0;
                            String genre = jsonObject.getString("genre").replace("\\", "");
                            String director = jsonObject.getString("director").replace("\\", "");
                            String writer = jsonObject.getString("writer").replace("\\", "");
                            String actor = jsonObject.getString("actor").replace("\\", "");
                            String tagline = jsonObject.getString("tagline").replace("\\", "");
                            String plot = jsonObject.getString("plot").replace("\\", "");
                            String trailer = jsonObject.getString("trailer").replace("\\", "");
                            String imdb_id = jsonObject.getString("imdb_id").replace("\\", "");
                            String poster = jsonObject.getString("poster").replace("\\", "");
                            String type = jsonObject.getString("type").replace("\\", "");
                            String dato = jsonObject.getString("dato");
                            String navn = jsonObject.getString("navn");

                            newMovie.setDate_added(date_added);
                            newMovie.setYear(year);
                            newMovie.setRuntime(runtime);
                            newMovie.setGenre(genre);
                            newMovie.setDirector(director);
                            newMovie.setWriter(writer);
                            newMovie.setActor(actor);
                            newMovie.setTagline(tagline);
                            newMovie.setPlot(plot);
                            newMovie.setTrailer(trailer);
                            newMovie.setImdb_id(imdb_id);
                            newMovie.setPoster(poster);
                            newMovie.setType(type);
                            newMovie.setNavn(navn);
                            newMovie.setDato(dato);

                            if (!ut.isEmpty()) {
                                newMovie.setUt(Integer.parseInt(ut));
                                if (newMovie.isLent()) {
                                    lentMovies.add(newMovie);
                                } else if (newMovie.isBorrowed()) {
                                    borrowedMovies.add(newMovie);
                                }
                            } else {
                                movies.add(newMovie);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //stuff that updates ui
                                lstMovies.invalidate();
                                lstMovies.invalidateViews();

                                txtTitle.setText("");
                                update_movie_count();
                                stopLoading();
                            }
                        });
                    } catch (JSONException e) {
                        Log.d("Success", "NOT FOUND");
                        e.printStackTrace();
                    }
                    stopLoading();
                }
            }).start();
        } else {
            stopLoading();
            makeToast("No internet connection");
            loadOfflineMovies();
        }
    }

    public void loadOfflineMovies() {
        final int nummovies = Integer.valueOf(preferenceHandler.getData("nummovies"));
        final int numborrowedMovies = Integer.valueOf(preferenceHandler.getData("numborrowedMovies"));
        final int numlentMovies = Integer.valueOf(preferenceHandler.getData("numlentMovies"));

        if (nummovies <=0 && numborrowedMovies <=0 && numlentMovies <=0){
            return;
        }

        movies.clear();
        for (int i = 0; i < nummovies; i++) {
            movies.add(gson.fromJson(preferenceHandler.getData("movies" + i), Movie.class));
        }
        borrowedMovies.clear();
        for (int i = 0; i < numborrowedMovies; i++) {
            borrowedMovies.add(gson.fromJson(preferenceHandler.getData("borrowedMovies" + i), Movie.class));
        }
        lentMovies.clear();
        for (int i = 0; i < numlentMovies; i++) {
            lentMovies.add(gson.fromJson(preferenceHandler.getData("lentMovies" + i), Movie.class));
        }
        refreshAllLists();
        Log.d("Loaded", nummovies + " movies locally");
        Log.d("Loaded", numborrowedMovies + " borrowedMovies locally");
        Log.d("Loaded", numlentMovies + " lentMovies locally");
    }

    private void saveMoviesOffline() {
        saveMoviesOffline("movies", movies);
        saveMoviesOffline("borrowedMovies", borrowedMovies);
        saveMoviesOffline("lentMovies", lentMovies);
        saveOfflineChanges("offlineChanges", UserFunctions.offlineChanges);
    }

    private void saveMoviesOffline(String key, ArrayList<Movie> arrayList) {
        preferenceHandler.putData("num" + key, String.valueOf(arrayList.size()));
        for (Movie movie : arrayList) {
            preferenceHandler.putData(key + arrayList.indexOf(movie), gson.toJson(movie));
        }
        Log.d("Saved ", arrayList.size() + " " + key + " locally");
    }

    private void saveOfflineChanges(String key, ArrayList<String> arrayList){
        //TODO: Fortsette her!
        preferenceHandler.putData("num" + key, String.valueOf(arrayList.size()));
        for (String change : arrayList) {
            preferenceHandler.putData(key + arrayList.indexOf(change), change);
        }
        Log.d("Saved ", arrayList.size() + " " + key + " locally");
    }

    //Longpress on movie
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.lstMovies) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            selected_movie = (Movie) lv.getItemAtPosition(acmi.position);

            if (selected_movie.isBorrowed()) {
                menu.add(getString(R.string.returnBorrowed));
                menu.add(getString(R.string.viewOnline));
                menu.add(getString(R.string.edit_movie));
            } else if (selected_movie.isLent()) {
                menu.add(getString(R.string.returnLent));
                menu.add(getString(R.string.viewOnline));
                menu.add(getString(R.string.edit_movie));
                menu.add(getString(R.string.delete));
            } else {
                menu.add(getString(R.string.borrowMovie));
                menu.add(getString(R.string.lendMovie));
                menu.add(getString(R.string.viewOnline));
                menu.add(getString(R.string.edit_movie));
                menu.add(getString(R.string.delete));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        int index = info.position;
        String s = String.valueOf(item);
        final int movieID = selected_movie.getMovieID();
        if (s.equals(getString(R.string.delete))) {
            valgtListe.remove(selected_movie);
            refreshLists(valgtListe);
            valgtListe.add(selected_movie);
            new Thread(new Runnable() {
                public void run() {
                    userFunction.delete_movie(selected_movie);
                    valgtListe.remove(selected_movie);
                    makeToast("Movie deleted");
                }
            }).start();
            return true;
        } else if (s.equals(getString(R.string.viewOnline))) {
            String url = "http://www.olavbg.com/movie_details.php?movie_id=" + String.valueOf(movieID);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
            return true;
        } else if (s.equals(getString(R.string.lendMovie))) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add lent movie");

            // Set up the input
            final EditText txtNavn = new EditText(this);
            txtNavn.setHint("Who have you lent this to?");
            // Specify the type of input expected
            txtNavn.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            builder.setView(txtNavn);

            // Set up the buttons
            builder.setPositiveButton("Lend movie", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (txtNavn.getText().toString().isEmpty()) {
                        makeToast("Please enter the person you lent this movie to");
                    } else {
                        selected_movie.setLent(txtNavn.getText().toString());
                        valgtListe.remove(selected_movie);
                        lentMovies.add(selected_movie);
                        new Thread(new Runnable() {
                            public void run() {
                                userFunction.lend_movie(selected_movie);
                            }
                        }).start();
                        refreshLists(valgtListe);
                    }
                }
            });
            builder.show();
            return true;
        } else if (s.equals(getString(R.string.edit_movie))) {
            Intent edit_movie = new Intent(getApplicationContext(), EditActivity.class);
            edit_movie.putExtra("movie_id", movieID);
            Log.e("movieID", String.valueOf(movieID));
            startActivity(edit_movie);
            return true;
        } else if (s.equals(getString(R.string.borrowMovie))) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set as borrowed movie");

            // Set up the input
            final EditText txtNavn = new EditText(this);
            txtNavn.setHint("Who have you borrowed this from?");
            // Specify the type of input expected
            txtNavn.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            builder.setView(txtNavn);

            // Set up the buttons
            builder.setPositiveButton("Add movie", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (txtNavn.getText().toString().isEmpty()) {
                        makeToast("Please enter the person you borrowed this movie from");
                    } else {
                        selected_movie.setBorrowed(txtNavn.getText().toString());
                        valgtListe.remove(selected_movie);
                        borrowedMovies.add(selected_movie);
                        new Thread(new Runnable() {
                            public void run() {
                                userFunction.borrowed_movie(selected_movie);
                            }
                        }).start();
                        refreshAllLists();
                    }
                }
            });
            builder.show();
            return true;
        } else if (s.equals(getString(R.string.returnBorrowed))) {
            valgtListe.remove(selected_movie);
            new Thread(new Runnable() {
                public void run() {
                    userFunction.returned_movie(selected_movie);
                }
            }).start();
            refreshAllLists();
            return true;
        } else if (s.equals(getString(R.string.returnLent))) {
            valgtListe.remove(selected_movie);
            selected_movie.movieReturned();
            movies.add(selected_movie);
            new Thread(new Runnable() {
                public void run() {
                    userFunction.returned_movie(selected_movie);
                }
            }).start();
            refreshAllLists();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    public void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static Movie findMovie(int movieID) {
        for (Movie movie : movies) {
            if (movie.getMovieID() == movieID) {
                return movie;
            }
        }
        for (Movie movie : lentMovies) {
            if (movie.getMovieID() == movieID) {
                return movie;
            }
        }
        for (Movie movie : borrowedMovies) {
            if (movie.getMovieID() == movieID) {
                return movie;
            }
        }
        return null;
    }

    public static Movie findMovie(String title, String format) {
        for (Movie movie : movies) {
            if (movie.getTitle().equals(title) && movie.getFormat().equals(format)) {
                return movie;
            }
        }
        for (Movie movie : lentMovies) {
            if (movie.getTitle().equals(title) && movie.getFormat().equals(format)) {
                return movie;
            }
        }
        for (Movie movie : borrowedMovies) {
            if (movie.getTitle().equals(title) && movie.getFormat().equals(format)) {
                return movie;
            }
        }
        return null;
    }

    public void add_new_movie(View view) {
        add_new_movie();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        startLoading("", "Searching for movie online...");
        //retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null && resultCode == RESULT_OK) {
            //we have a result
            final String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            new Thread(new Runnable() {
                public void run() {
                    search_res = null;
                    search_res = userFunction.search_ean(scanContent);
                    Log.d("EAN search response", search_res.toString());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //stuff that updates ui
                            try {
                                if (search_res.getString("err_msg").length() > 0) {
                                    makeToast("No movie found from barcode. Please fill in manually");
                                } else {
                                    makeToast("Movie found online, see title field");
                                    txtTitle.setText("");
                                    txtTitle.append(search_res.getString("title"));
                                    txtTitle.requestFocus();
                                    txtTitle.setSelection(txtTitle.getText().length(), txtTitle.getText().length());
                                    if (!search_res.getString("format").isEmpty()) {
                                        select_cbxItem(cbxFormats, search_res.getString("format"));
                                    }
                                }
                                stopLoading();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                stopLoading();
                            }
                        }
                    });
                }
            }).start();

            Log.d("Format", scanFormat);
            Log.d("Content", scanContent);
        } else {
            stopLoading();
            makeToast("No scan data received!");
        }
    }

    private void select_cbxItem(Spinner cbx, String text) {
        int num_formats = cbx.getCount();
        for (int i = 0; num_formats > i + 1; i++) {
            if (cbx.getItemAtPosition(i).toString().equalsIgnoreCase(text)) {
                cbx.setSelection(i);
                return;
            }
        }
    }

    public void add_new_movie() {
        startLoading("", "Adding movie..");
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        final Toast toast = Toast.makeText(context, "", duration);
        String title = String.valueOf(txtTitle.getText()).trim();
        String format = String.valueOf(cbxFormats.getSelectedItem());

        if (txtTitle.getText().length() <= 0) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }

        final Movie newMovie = new Movie(title, format);

        if (title.length() < 2) {
            toast.setText(getString(R.string.not_enough_characters_title));
            toast.show();
            stopLoading();
            return;
        }
        if (format.length() < 3) {
            toast.setText(getString(R.string.not_selected_format));
            toast.show();
            stopLoading();
            return;
        }
        if (movies.contains(newMovie) || borrowedMovies.contains(newMovie) || lentMovies.contains(newMovie)) {
            toast.setText(getString(R.string.movie_exists));
            toast.show();
            stopLoading();
            return;
        }

        if (valgtListe == borrowedMovies) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add borrowed movie");

            // Set up the input
            final EditText txtNavn = new EditText(this);
            txtNavn.setHint("Who have you borrowed this from?");
            // Specify the type of input expected
            txtNavn.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            builder.setView(txtNavn);

            // Set up the buttons
            builder.setPositiveButton("Add movie", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (txtNavn.getText().toString().isEmpty()) {
                        makeToast("Please enter the person you borrowed this movie from");
                    } else {
                        newMovie.setBorrowed(txtNavn.getText().toString());
                        new Thread(new Runnable() {
                            public void run() {
                                userFunction.borrowed_movie(newMovie);
                            }
                        }).start();
                    }
                }
            });
            builder.show();
        } else if (valgtListe == lentMovies) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add lent movie");

            // Set up the input
            final EditText txtNavn = new EditText(this);
            txtNavn.setHint("Who have you lent this to?");
            // Specify the type of input expected
            txtNavn.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            builder.setView(txtNavn);

            // Set up the buttons
            builder.setPositiveButton("Lend movie", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (txtNavn.getText().toString().isEmpty()) {
                        makeToast("Please enter the person you lent this movie to");
                    } else {
                        newMovie.setLent(txtNavn.getText().toString());
                        new Thread(new Runnable() {
                            public void run() {
                                userFunction.lend_movie(newMovie);
                            }
                        }).start();
                    }
                }
            });
            builder.show();
            newMovie.setLent(txtNavn.getText().toString());
        }

        startLoading("Adding movie", "Searching for movie details online. Please wait...");

        newMovie.setDate_added(getTodaysDate());
        valgtListe.add(newMovie);

        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = userFunction.add_movie(newMovie);
                    if (!isConnected()) {
                        stopLoading();
                        return;
                    }
                    if (json.getString("success") != null) {
                        Movie movie = valgtListe.get(valgtListe.indexOf(newMovie));
                        movie.setMovieID(Integer.parseInt(json.getString("filmID")));
                        movie.setType(json.getString("type"));
                        movie.setPoster(json.getString("poster"));
                        movie.setImdb_id(json.getString("imdb_id"));
                        movie.setActor(json.getString("actor"));
                        movie.setDirector(json.getString("director"));
                        movie.setGenre(json.getString("genre"));
                        movie.setPlot(json.getString("plot"));
                        movie.setRuntime(Integer.parseInt(json.getString("runtime")));
                        movie.setTagline(json.getString("tagline"));
                        movie.setTrailer(json.getString("trailer"));
                        movie.setWriter(json.getString("writer"));
                        movie.setYear(Integer.parseInt(json.getString("year")));
                        movie.setTitle(json.getString("tittel"));
                        Log.d("Success", "TRUE");
                    } else {
                        Log.d("Success", "FALSE");
                    }
                    stopLoading();
                } catch (JSONException e) {
                    Log.d("Success", "NOT FOUND");
                    e.printStackTrace();
                    stopLoading();
                }
            }
        }).start();

        Collections.sort(valgtListe, new Movie_comparator());

        ArrayAdapter<Movie> adapter = new ArrayAdapter<Movie>(this,
                android.R.layout.simple_list_item_1, valgtListe);
        lstMovies.setAdapter(adapter);

        lstMovies.invalidate();
        lstMovies.invalidateViews();

        txtTitle.setText("");
        update_movie_count();
    }

    public void startLoading(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                dialog.setCanceledOnTouchOutside(true);
                dialog = ProgressDialog.show(context, title,
                        message, true);
                dialog.show();
            }
        });
    }

    public void stopLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLoading();
        saveMoviesOffline();
    }

    public void refreshAllLists() {
        refreshLists(movies);
        refreshLists(borrowedMovies);
        refreshLists(lentMovies);
    }

    public void refreshLists(final ArrayList<Movie> movieList) {
        startLoading("", "Updating movie lists");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int scrollPos = lstMovies.getFirstVisiblePosition();
//                int scrollPos = valgtAdapter.getPosition(selected_movie);

                Collections.sort(movies, new Movie_comparator());
                Collections.sort(borrowedMovies, new Movie_comparator());
                Collections.sort(lentMovies, new Movie_comparator());

                ArrayAdapter<Movie> adapter;
                adapter = new ArrayAdapter<Movie>(context,
                        android.R.layout.simple_list_item_1, movieList);
                lstMovies.setAdapter(adapter);

                lstMovies.invalidate();
                lstMovies.invalidateViews();

                update_movie_count();
                lstMovies.setSelection(scrollPos);
                stopLoading();
            }
        });
    }

    public void update_movie_count() {
        Integer numMovies = movies.size() + borrowedMovies.size() + lentMovies.size();
        lblRegisteredMovies.setText(getString(R.string.registeredMovies) + " " + numMovies.toString());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                valgtListe = movies;
                valgtAdapter = adapter;
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                valgtListe = borrowedMovies;
                valgtAdapter = borrowedAdapter;
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                valgtListe = lentMovies;
                valgtAdapter = lentAdapter;
                break;
        }
        lstMovies.setAdapter(valgtAdapter);
    }

    public void restoreActionBar() {
        @SuppressLint("AppCompatMethod") ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            MenuItem searchItem = menu.findItem(R.id.menu_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Search in current movie list");
            searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    if (!s.isEmpty()) {
                        //Search..
                        ArrayList<Movie> foundMovies = new ArrayList<Movie>();
                        for (Movie movie : valgtListe) {
                            if (movie.getTitle().toLowerCase().contains(s.toLowerCase())) {
                                foundMovies.add(movie);
                            }
                        }
                        refreshLists(foundMovies);
                    } else {
                        //Don't search..
                        refreshLists(valgtListe);
                    }
                    return false;
                }
            });
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.log_out) {
            editor = sharedpreferences.edit();
            editor.remove("userdata");
            editor.apply();
            login_screen.putExtra("revokeAccess", true);
            startActivity(login_screen);
            finish();
            return true;
        }
        if (id == R.id.settings) {
            startActivity(settings_screen);
        }
        if (id == R.id.refresh) {
            update_movies();
            ArrayAdapter<Movie> adapter;
            adapter = new ArrayAdapter<Movie>(this,
                    android.R.layout.simple_list_item_1, valgtListe);
            lstMovies.setAdapter(adapter);

            lstMovies.invalidate();
            lstMovies.invalidateViews();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
