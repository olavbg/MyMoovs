package olavbg.com.mymoovs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static tools.Functions.get_days_since;

@SuppressWarnings("ConstantConditions")
public class MovieDetails extends Activity {
    private Movie selected_movie;
    private UserFunctions userFunctions;
    private Context context;
    MenuItem trailer;
    TextView txtTitle;
    TextView txtFormat;

    @SuppressLint("AppCompatMethod")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        context = this;
        userFunctions = new UserFunctions(context);

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtFormat = (TextView) findViewById(R.id.txtFormat);
        TextView txtTagline = (TextView) findViewById(R.id.txtTagline);
        TextView txtType = (TextView) findViewById(R.id.txtType);
        TextView txtDate_added = (TextView) findViewById(R.id.txtDate_added);
        TextView txtYear = (TextView) findViewById(R.id.txtYear);
        TextView txtRuntime = (TextView) findViewById(R.id.txtRuntime);
        TextView txtGenre = (TextView) findViewById(R.id.txtGenre);
        TextView txtDirector = (TextView) findViewById(R.id.txtDirector);
        TextView txtWriter = (TextView) findViewById(R.id.txtWriter);
        TextView txtActor = (TextView) findViewById(R.id.txtActor);
        TextView txtplot = (TextView) findViewById(R.id.txtPlot);
        TextView txtLent_Borrowed = (TextView) findViewById(R.id.txtLent_Borrowed);
        WebView imgPoster = (WebView) findViewById(R.id.imgPoster);
        WebView backgroundImage = (WebView) findViewById(R.id.backgroundImage);

        selected_movie = MainActivity.findMovie(getIntent().getIntExtra("movie_id", 0));
        if (selected_movie == null) {
            txtTitle.setText("Opps! Something went wrong! Unable to find the selected movie. Please try again, or send an email to me at 'olgulbra@gmail.com' with some information about what happened.");
            txtActor.setVisibility(View.GONE);
            txtDate_added.setVisibility(View.GONE);
            txtDirector.setVisibility(View.GONE);
            txtFormat.setVisibility(View.GONE);
            txtGenre.setVisibility(View.GONE);
            txtplot.setVisibility(View.GONE);
            txtRuntime.setVisibility(View.GONE);
            txtTagline.setVisibility(View.GONE);
            txtType.setVisibility(View.GONE);
            txtWriter.setVisibility(View.GONE);
            txtYear.setVisibility(View.GONE);
            imgPoster.setVisibility(View.GONE);
            return;
        }

        setTitle_format();
        txtTagline.setText(Html.fromHtml(selected_movie.getTagline().isEmpty() ? "" : "\"" + selected_movie.getTagline() + "\""));
        txtTagline.setVisibility(selected_movie.getTagline().isEmpty() ? View.GONE : View.VISIBLE);
        txtType.setText(Html.fromHtml("<strong>Type</strong><br>" + selected_movie.getType()));
        txtType.setVisibility(selected_movie.getType().isEmpty() ? View.GONE : View.VISIBLE);
        txtDate_added.setText(Html.fromHtml("<strong>Date added</strong><br>" + selected_movie.getDate_added()));
        txtYear.setText(Html.fromHtml("<strong>Year</strong><br>" + String.valueOf(selected_movie.getYear())));
        txtYear.setVisibility(View.GONE);
        txtRuntime.setText(Html.fromHtml(selected_movie.getRuntime() != 0 ? "<strong>Runtime</strong><br>" + String.valueOf(selected_movie.getRuntime() + " mins") : "<strong>Runtime</strong><br>N/A"));
        txtGenre.setText(Html.fromHtml("<strong>Genre</strong><br>" + selected_movie.getGenre()));
        txtDirector.setText(Html.fromHtml("<strong>Director</strong><br>" + selected_movie.getDirector()));
        txtWriter.setText(Html.fromHtml("<strong>Writer</strong><br>" + selected_movie.getWriter()));
        txtActor.setText(Html.fromHtml("<strong>Actors</strong><br>" + selected_movie.getActor()));
        txtplot.setText(Html.fromHtml("<strong>Plot</strong><br>" + selected_movie.getPlot()));
        if (!selected_movie.getImdb_id().isEmpty() && isConnected()) {
            imgPoster.loadUrl("http://www.olavbg.com/images/poster_thumbs/" + selected_movie.getMovieID() + ".jpeg");
//            backgroundImage.loadUrl("http://img.omdbapi.com/?apikey=7490539a&h="+720+"&i="+selected_movie.getImdb_id());
        } else {
            imgPoster.setVisibility(View.GONE);
        }

        if (selected_movie.isBorrowed()) {
            txtLent_Borrowed.setVisibility(View.VISIBLE);
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(selected_movie.getDato());
                txtLent_Borrowed.setText(Html.fromHtml("<strong>Borrowed from: </strong>" + selected_movie.getNavn() + ", " + get_days_since(date)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (selected_movie.isLent()) {
            txtLent_Borrowed.setVisibility(View.VISIBLE);
            txtLent_Borrowed.setText(Html.fromHtml("<strong>Lent to: </strong>" + selected_movie.getNavn()));
        }
    }

    public void setTitle_format(){
        String yearReleasedString = selected_movie.getYear() == 0 ? "" : " (" + selected_movie.getYear() + ")";
        getActionBar().setTitle(selected_movie.getTitle() + yearReleasedString);
        txtTitle.setText(Html.fromHtml("<strong>" + selected_movie.getTitle() + "</strong>" + yearReleasedString));
        txtFormat.setText(Html.fromHtml("<strong>Format</strong><br>" + selected_movie.getFormat()));
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie_details, menu);
        trailer = menu.findItem(R.id.trailer);
        if (selected_movie.getTrailer().isEmpty()) {
            trailer.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle_format();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.refresh = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.trailer) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(selected_movie.getTrailer())));
            return true;
        }
        if (id == R.id.edit) {
            Intent edit_movie = new Intent(getApplicationContext(), EditActivity.class);
            edit_movie.putExtra("movie_id", selected_movie.getMovieID());
            startActivity(edit_movie);
        }
        if (id == R.id.delete){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    userFunctions.delete_movie(selected_movie.getMovieID());
                    makeToast("Movie deleted");
                }
            });
            thread.start();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void makeToast(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
