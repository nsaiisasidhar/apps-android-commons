package fr.free.nrw.commons.nearby;

import android.net.Uri;
import android.os.StrictMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.free.nrw.commons.Utils;
import fr.free.nrw.commons.location.LatLng;
import fr.free.nrw.commons.utils.FileUtils;
import timber.log.Timber;

public class NearbyMissingPictures {

    //private static final double INITIAL_RADIUS = 1.0; // in kilometers
    //private static final double MAX_RADIUS = 300.0; // in kilometers
    //private static final double RADIUS_MULTIPLIER = 1.618;
    private static final Uri WIKIDATA_QUERY_URL = Uri.parse("https://query.wikidata.org/sparql");
    private static final Uri WIKIDATA_QUERY_UI_URL = Uri.parse("https://query.wikidata.org/");
    private final String wikidataQuery;
    private double radius = INITIAL_RADIUS;
    private List<Place> places;
    
    private String GPX-Header;
    private String GPX-Footer;

    public NearbyMissingPictures() {
        try {
            wikidataQuery = FileUtils.readFromResource("/assets/queries/nearbyMissingPictures_query.rq");
            Timber.v(wikidataQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void getFromWikidataQuery(LatLng cur,
                                      double radius)
            throws IOException {

        String query = wikidataQuery
                .replace("${RAD}", String.format(Locale.ROOT, "%.2f", radius))
                .replace("${LAT}", String.format(Locale.ROOT, "%.4f", cur.getLatitude()))
                .replace("${LONG}", String.format(Locale.ROOT, "%.4f", cur.getLongitude()));

        Timber.v("# Wikidata query: \n" + query);

        // format as a URL
        Timber.d(WIKIDATA_QUERY_UI_URL.buildUpon().fragment(query).build().toString());
        String url = WIKIDATA_QUERY_URL.buildUpon()
                .appendQueryParameter("query", query).build().toString();
        URLConnection conn = new URL(url).openConnection();
        conn.setRequestProperty("Accept", "text/tab-separated-values");
        
        //Read output of query and write the output to a GPX file.
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line;
        Timber.d("Reading from query result...");
        while ((line = in.readLine()) != null) {
            Timber.v(line);
            line = line + "\n"; // to pad columns and make fields a fixed size
            if (!line.startsWith("\"Point")) {
                continue;
            }
        }
        in.close();
    }
}
