package olavbg.com.mymoovs;

import static tools.Functions.getTodaysDate;

@SuppressWarnings("UnusedDeclaration")
public class Movie {
    private String title;
    private String format;
    private int movieID;
    private int ut = -1;
    private String navn;
    private String dato;

    private String date_added = "";
    private int year = 0;
    private int runtime = 0;
    private String genre = "";
    private String director = "";
    private String writer = "";
    private String actor = "";
    private String tagline = "";
    private String plot = "";
    private String trailer = "";
    private String imdb_id = "";
    private String poster = "";
    private String type = "";

    public Movie(String title, String format) {
        this.title = title;
        this.format = format;
    }

    public Movie(int movieID, String title, String format) {
        this.movieID = movieID;
        this.title = title;
        this.format = format;
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setBorrowed(String navn) {
        this.ut = 0;
        this.navn = navn;
        this.dato = getTodaysDate();
    }

    public void setLent(String navn) {
        this.ut = 1;
        this.navn = navn;
        this.dato = getTodaysDate();
    }

    public boolean isBorrowed() {
        boolean borrowed = false;
        if (ut == 0) {
            borrowed = true;
        }
        return borrowed;
    }

    public boolean isLent() {
        boolean lent = false;
        if (ut == 1) {
            lent = true;
        }
        return lent;
    }

    public void movieReturned() {
        this.ut = -1;
        this.navn = null;
        this.dato = null;
    }

    public void setUt(int ut) {
        this.ut = ut;
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    @Override
    public String toString() {
        return title + " - " + format;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (!format.equals(movie.format)) return false;
        if (!title.equals(movie.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + format.hashCode();
        return result;
    }

    public int getUt() {
        return ut;
    }

    public String getNavn() {
        return navn;
    }

    public String getDato() {
        return dato;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getGenre() {
        return genre.isEmpty() ? "N/A" : genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director.isEmpty() ? "N/A" : director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getWriter() {
        return writer.isEmpty() ? "N/A" : writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getActor() {
        return actor.isEmpty() ? "N/A" : actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getPlot() {
        return plot.isEmpty() ? "N/A" : plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getImdb_id() {
        return imdb_id;
    }

    public void setImdb_id(String imdb_id) {
        this.imdb_id = imdb_id;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
