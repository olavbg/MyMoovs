package olavbg.com.mymoovs_beta;

import java.util.Comparator;

/**
 * Created by Olav on 06.03.14.
 */
public class Movie_comparator implements Comparator<Movie> {
    @Override
    public int compare(Movie movie, Movie movie2) {
        return movie.getTitle().compareTo(movie2.getTitle());
    }
}
