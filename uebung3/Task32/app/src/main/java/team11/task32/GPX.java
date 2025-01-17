package team11.task32;

import android.location.Location;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPX {
    private final String TAG = GPX.class.getName();
    private FileWriter writer;

    public GPX(File file, String n) {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + n + "</name><trkseg>\n";
        Log.i(TAG, "Opening File");
        try {
            writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error Opening/Writing Path",e);
        }
    }
    public void addPoint(Location l){
        Log.i(TAG, "Add Point to File");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String segment = "<trkpt lat=\"" + l.getLatitude() + "\" lon=\"" + l.getLongitude() + "\"><time>" + df.format(new Date(l.getTime())) + "</time></trkpt>\n";
        try {
            writer.append(segment);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, "Error Writing Path",e);
        }
    }

    public void close() {
        Log.i(TAG, "Close File");
        String footer = "</trkseg></trk></gpx>";
        try {
            writer.append(footer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "Error Closing File",e);
        }
    }
}
