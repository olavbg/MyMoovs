package olavbg.com.mymoovs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class EditActivity extends Activity {
    private UserFunctions userFunction;
    private Spinner cbxEditFormat;
    private EditText txtEditTitle;
    private Context context;
    private ProgressDialog dialog;
    Movie selected_movie;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        txtEditTitle = (EditText) findViewById(R.id.txtEditTitle);
        cbxEditFormat = (Spinner) findViewById(R.id.cbxEditFormat);
        context = this;
        userFunction = new UserFunctions(context);
        dialog = new ProgressDialog(getApplicationContext());
        selected_movie = MainActivity.selected_movie;

        getActionBar().setTitle("Editing "+selected_movie.getTitle());

        addFormatsToSpinner();

        txtEditTitle.setText("");
        txtEditTitle.append(selected_movie.getTitle());
        txtEditTitle.requestFocus();
        txtEditTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                updateMovie(new View(context));
                return false;
            }
        });

    }

    private void addFormatsToSpinner() {
        if (MainActivity.userFunction.isConnected()){
            new Thread(new Runnable() {
                public void run() {
                    JSONArray movieFormats = userFunction.getMovieFormats();
                    final ArrayList<String> movieFormatsList = new ArrayList<String>();
                    for (int i = 0; i < movieFormats.length(); i++) {
                        try {
                            JSONObject movieFormat = movieFormats.getJSONObject(i);
                            movieFormatsList.add(movieFormat.getString("beskrivelse"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!movieFormatsList.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                                        android.R.layout.simple_spinner_item, movieFormatsList);
                                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cbxEditFormat.setAdapter(dataAdapter);
                                cbxEditFormat.invalidate();
                                select_cbxItem(cbxEditFormat,selected_movie.getFormat());
                            }
                        });
                    }
                }
            }).start();
        }else{
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.formats, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            cbxEditFormat.setAdapter(adapter);
            cbxEditFormat.invalidate();
            select_cbxItem(cbxEditFormat,selected_movie.getFormat());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMovie(View view) {
        final String newTitle = txtEditTitle.getText().toString().trim();
        final String newFormat = cbxEditFormat.getSelectedItem().toString();

        startLoading("Updating movie", "Validating input");

        if (newTitle.length() < 2) {
            makeToast(getString(R.string.not_enough_characters_title));
            stopLoading();
            return;
        }
        if (newFormat.isEmpty()) {
            makeToast(getString(R.string.not_selected_format));
            stopLoading();
            return;
        }

        Movie updatedMovie = new Movie(newTitle, newFormat);
        if (MainActivity.movies.contains(updatedMovie) || MainActivity.borrowedMovies.contains(updatedMovie) || MainActivity.lentMovies.contains(updatedMovie)) {
            makeToast(getString(R.string.movie_exists));
            stopLoading();
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (userFunction.userHasMovie(newTitle, newFormat)) {
                        makeToast(getString(R.string.movie_exists_in_cloud));
                        stopLoading();
                    } else {
                        startLoading("Updating movie", "Saving updates");
                        userFunction.updateMovie(selected_movie.getMovieID(), newTitle, newFormat);
                        selected_movie.setTitle(newTitle);
                        selected_movie.setFormat(newFormat);
                        stopLoading();
                        makeToast("Movie updated");
                        MainActivity.refresh = true;
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    public void startLoading(final String title, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                dialog.setCanceledOnTouchOutside(true);
                dialog = ProgressDialog.show(context, title, message, true);
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

    public void makeToast(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();
            }
        });
    }
}