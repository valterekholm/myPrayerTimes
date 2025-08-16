package com.example.myprayertimes;

import static java.nio.file.Path.of;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
//git push -u origin main
@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    final Context context = this;

    //Added 2024-12-11
    //for clock
    private TextView textViewClock;
    private Handler handler = new Handler();
    private Runnable runnable;

    boolean testDate = false;//use a set date instead of today
    LocalDate testDate_ = LocalDate.of(2025,7,31);

    //Added 2024-12-17
    private LinearLayout settingsLayout; private Button buttonToggleSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String location = "";
        double lon = 0;
        double lat = 0;
        int timeZoneMinute = 0;
        final int MINTZMINUTE = -720;
        final int MAXTZMINUTE = 720;
        String filePath = "settings.txt";
        //File f = new File(filePath);
        File f = new File(getFilesDir(), filePath);
        boolean remakeFile = false;//TODO: see if this is needed

        if(!f.exists()){
            createInitialFile(f.getPath());
        }

        Log.i("", "file size: " + f.length());

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button buttonUpdate = findViewById(R.id.buttonUpdate);
        Button button2 = findViewById(R.id.button3);

        TextView prayerTimesTextView = findViewById(R.id.prayerTimesView);
        TextView prayerTimesTextView2 = findViewById(R.id.prayerTimesView2);

        Result result = getResult(location, lon, lat, timeZoneMinute);

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here: perform actions when the button is clicked
                // For example:
                // Toast.makeText(getApplicationContext(), "Button clicked!", Toast.LENGTH_SHORT).show();

                try {
                    String location_ = "unkn";
                    double lon_ = 0;
                    double lat_ = 0;
                    int tZMin_ = 0;
                    FileWriter fw = new FileWriter(f);
                    EditText edLoc = findViewById(R.id.editTextText);
                    location_ = edLoc.getText().toString();
                    EditText edLon = findViewById(R.id.editTextText2);
                    if(edLon.getText().toString().equals("")){
                        edLon.setText("0");
                    }
                    lon_ = Double.parseDouble(edLon.getText().toString().replaceAll(",", "."));
                    EditText edLat = findViewById(R.id.editTextText3);
                    if(edLat.getText().toString().equals("")){
                        edLat.setText("0");
                    }
                    lat_ = Double.parseDouble(edLat.getText().toString().replaceAll(",", "."));
                    EditText edTZM = findViewById(R.id.editTextTZMinute);
                    if(edTZM.getText().toString().equals("")){
                        Log.i("","tzm EMPTY!");
                        edTZM.setText("0");
                    }
                    tZMin_ = Integer.parseInt(edTZM.getText().toString());
                    //assert correct value
                    if(tZMin_ < MINTZMINUTE){
                        tZMin_ = MINTZMINUTE;
                    }
                    if(tZMin_ > MAXTZMINUTE){
                        tZMin_ = MAXTZMINUTE;
                    }
                    saveFile(fw, location_, lon_, lat_, tZMin_, false, filePath);
                    fw.close();
                    message("Have written to file," + f.length(), true);

                    Log.i("", "file size: " + f.length());

                    Toast.makeText(getApplicationContext(), getFileText(), Toast.LENGTH_SHORT).show();
                    Result result2 = getResult(location_, lon_, lat_, tZMin_);

                    PTCalc calc2 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        calc2 = new PTCalc(result2.lon, result2.lat, result2.location, result2.tZMinute);
                    }
                    PrayerTimesCollection collection2 = calc2.run();

                    if (prayerTimesTextView != null) {
                        prayerTimesTextView.setText(collection2.toString());
                        message("Have filled prayertimes", true);
                    } else {
                        Log.i("info", "it was null");
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here: perform actions when the button is clicked
                // For example:
                // Toast.makeText(getApplicationContext(), "Button clicked!", Toast.LENGTH_SHORT).show();

                try {
                    printFileText();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        //setContentView(collection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //test


        PTCalc calc = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if(testDate){
                calc = new PTCalc(testDate_, result.lon, result.lat, result.location, result.tZMinute);
            }
            else{
                calc = new PTCalc(result.lon, result.lat, result.location, result.tZMinute);
            }

        }
        PrayerTimesCollection collection = calc.run();

        //For tomorrow
        PTCalc calc2 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            calc2 = new PTCalc(calc.timeInfo, result.lon, result.lat, result.location, result.tZMinute);
        }
        calc2.stepOneDay();
        PrayerTimesCollection collection2 = calc2.run();

        if(collection.activeDST){
            Toast.makeText(getApplicationContext(), "DST on device", Toast.LENGTH_SHORT).show();
        }

        if (prayerTimesTextView != null) {
            prayerTimesTextView.setText(collection.toString());
            prayerTimesTextView2.setText(collection2.toString());
            message("Have filled prayertimes", true);
        } else {
            Log.i("info", "it was null");
        }

        /*
        if (remakeFile) {
            String location_ = "unkn";
            double lon_ = 0;
            double lat_ = 0;


            try {
                FileWriter fw = new FileWriter(f);
                EditText edLoc = findViewById(R.id.editTextText);
                location_ = edLoc.getText().toString();
                EditText edLon = findViewById(R.id.editTextText2);
                lon_ = Double.parseDouble(edLon.getText().toString());
                EditText edLat = findViewById(R.id.editTextText3);
                lat_ = Double.parseDouble(edLat.getText().toString());
                saveFile(fw, location_, lon_, lat_, false, filePath);
                fw.close();
                message("Had remakeFile", true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }*/

        //Added 2024-12-11 for clock
        textViewClock = findViewById(R.id.textViewClock); runnable = new Runnable() { @Override public void run() {
            updateClock();
            handler.postDelayed(
                    this, 1000);
        }
        };
        handler.post(runnable);
        //end added 2024-12-11

        //Added 2024-12-17
        settingsLayout = findViewById(R.id.settingsLayout); buttonToggleSettings = findViewById(R.id.buttonToggleSettings); buttonToggleSettings.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (settingsLayout.getVisibility() == View.GONE) { settingsLayout.setVisibility(View.VISIBLE); } else { settingsLayout.setVisibility(View.GONE); } } });

    }

    @NonNull
    private Result getResult(String location, double lon, double lat, int tZMinute) {
        try {
            String fromFile = getFileText();
            String[] lines = fromFile.split("\n");

            int len = lines.length;

            if (len >= 4) {
                for (int i = 0; i < len; i++) {
                    String line = lines[i];
                    char firstChar = line.charAt(0);
                    Log.i("first char", firstChar + "");

                    switch (firstChar) {
                        case '1':
                            //Log.i("", "case 1 " + line);
                            location = line.substring(2);
                            break;
                        case '2':
                            //Log.i("", "case 2 " + line);
                            //Double.valueOf(numStr.replaceAll(",", "."))
                            lon = Double.parseDouble(line.substring(2)
                                    .replaceAll(",", "."));
                            break;
                        case '3':
                            //Log.i("", "case 3 " + line);
                            lat = Double.parseDouble(line.substring(2)
                                    .replaceAll(",", "."));
                            break;
                        case '4': tZMinute = Integer.parseInt(line.substring(2));
                        break;

                    }
                }
            } else {
                message("För få rader i fil", true);
            }

            updateGUI(location, String.format("%.3f", lon), String.format("%.3f", lat), String.format("%d", tZMinute));

        } catch (FileNotFoundException e) {
            message("File not found", false);
            throw new RuntimeException(e);
        }
        Result result = new Result(location, lon, lat, tZMinute);
        return result;
    }

    private static class Result {
        public final String location;
        public final double lon;
        public final double lat;
        public final int tZMinute;

        public Result(String location, double lon, double lat, int tZMinute) {
            this.location = location;
            this.lon = lon;
            this.lat = lat;
            this.tZMinute = tZMinute;
        }
    }

    public boolean saveFile(FileWriter fileWriter, String location, double lon, double lat, int tZMinute, boolean isNewFile, String filePath) throws IOException {

        Log.i("","Will save file with tZMinute " + tZMinute);
        if (!isNewFile) {
            //clear file

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Log.i("", "Will try truncate file " + filePath);
                Path path = Paths.get(getFilesDir() + "/" + filePath);
                Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING);
                message("did truncate", true);
            } else {
                try {
                    //PrintWriter writer = new PrintWriter(f);
                    fileWriter.write(""); // Overwrite with an empty string
                    System.out.println("File cleared successfully!");
                } catch (Exception e) {
                    System.err.println("Error while clearing the file: " + e.getMessage());
                }
                message("Tried truncate the other way", true);
            }
        }
        fileWriter.write("1." + location + "\n");
        fileWriter.write("2." + String.format("%.3f", lon) + "\n");
        fileWriter.write("3." + String.format("%.3f", lat) + "\n");
        fileWriter.write("4." + String.format("%d", tZMinute) + "\n");//TODO: end with \n?
        return true;
    }

    public void message(String m, boolean append) {
        TextView messages = findViewById(R.id.editTextText4);
        if (append) {
            messages.setText(messages.getText() + "\n" + m);
        } else {
            messages.setText(m);
        }
    }

    public String getFileText() throws FileNotFoundException {
        File file = new File(getFilesDir() + "/" + "settings.txt");
        Scanner scanner = new Scanner(file);

        String ret = "";

        while (scanner.hasNextLine()) {
            ret += scanner.nextLine() + "\n";
        }

        scanner.close();

        return ret;
    }

    public void printFileText() throws FileNotFoundException {
        String path = getFilesDir() + "/" + "settings.txt";
        File file = new File(path);
        Log.i("", "printFileText: " + path);
        Scanner scanner = new Scanner(file);

        String ret = "";

        while (scanner.hasNextLine()) {
            ret += scanner.nextLine() + "\n";
        }

        scanner.close();

        Log.i("", ret);

        TextView messages = findViewById(R.id.editTextText4);
        messages.setText(ret);

    }

    public void createInitialFile(String filePath){
        try {
            // Create a FileWriter object
            FileWriter writer = new FileWriter(filePath);

            // Write a string to the file
            // location, longitude, latitude, timezone...
            writer.write("1.X\n2.0\n3.0\n4.0");

            // Close the writer
            writer.close();

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred, in createInitialFile");
            e.printStackTrace();
        }

        Log.i("","Seems I could run createInitialFile");
    }

    public void updateGUI(String loc, String lon, String lat, String tZMin) {
        EditText edLoc = findViewById(R.id.editTextText);
        edLoc.setText(loc);
        EditText edLon = findViewById(R.id.editTextText2);
        edLon.setText(lon.replaceAll(",", "."));
        EditText edLat = findViewById(R.id.editTextText3);
        edLat.setText(lat.replaceAll(",", "."));
        EditText edTZM = findViewById(R.id.editTextTZMinute);
        //Log.i("", "WILL SET edittext tzm to " + tZMin);
        edTZM.setText((tZMin));
    }

    public double assureDouble(String text) {
        return Double.valueOf(text.replaceAll(",", "."));
    }

    //Added 2024-12-11 for clock
    private void updateClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        textViewClock.setText(currentTime);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    //End added 2024-12-11
}