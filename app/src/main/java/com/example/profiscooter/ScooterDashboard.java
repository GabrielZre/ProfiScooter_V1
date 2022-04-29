package com.example.profiscooter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ScooterDashboard extends AppCompatActivity implements LocationListener {

    private static boolean isButtonChecked = false;
    private static double totalDistance = 0 ;
    private static float nCurrentSpeed = 0;
    private static double avgSpeed = 0;

    private static Location locEnd = null;
    private static Location locStart = null;

    //TextView textViewSpeed;
    TextView textViewTotalDistance, textViewAvgSpeed;
    //TextView textViewLatitude,textViewLongitude;
    //ToggleButton buttonStartMeasuring;
    PointerSpeedometer speedometer;
    TextView timerText;
    ImageButton stopStartButton, saveButton;


    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    boolean timerStarted = false;

    Dialog tripdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scooter_dashboard);

        //textViewSpeed = findViewById(R.id.textViewSpeed);
        //textViewLatitude = findViewById(R.id.textViewLatitude);
        //textViewLongitude = findViewById(R.id.textViewLongitude);
        textViewTotalDistance = findViewById(R.id.textViewTotalDistance);
        textViewAvgSpeed = findViewById(R.id.textViewAvgSpeed);
        //textViewSpeed.setText(String.format("%.1f", nCurrentSpeed) +" km/h");
        textViewTotalDistance.setText(String.format("%.1f", totalDistance)+" km");
        //buttonStartMeasuring = findViewById(R.id.startMeasuring);
        //check for gps permission

        speedometer = findViewById(R.id.pointerSpeedometer);


        timerText = (TextView) findViewById(R.id.timerText);
        stopStartButton = (ImageButton) findViewById(R.id.startStopButton);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        tripdialog = new Dialog(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


        timer = new Timer();




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},100);
        }else{
            doStuff();
        }

        updateSpeed(null);
        updateLocation(null);




    }

    private void showDialog() {
        tripdialog.setContentView(R.layout.savedialog);

        tripdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tripdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        EditText tripName = tripdialog.findViewById(R.id.tvTripName);
        ImageButton buttonSave = tripdialog.findViewById(R.id.buttonSave);
        ImageButton buttonBack = tripdialog.findViewById(R.id.buttonBack);

        TextView distance = tripdialog.findViewById(R.id.tv_distanceNum);
        TextView timeNum = tripdialog.findViewById(R.id.tv_distanceTimeNum);
        TextView averageSpeed = tripdialog.findViewById(R.id.tv_averageSpeedNum);
        TextView batteryDrain = tripdialog.findViewById(R.id.tv_batteryDrainNum);
        TextView dateTime = tripdialog.findViewById(R.id.tvDateTime);

        //calculate minutes
        double rounded = (double) Math.round(time);
        double minutes = BigDecimal.valueOf(((rounded % 86400) / 3600)*60)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();

        distance.setText(String.format("%.1f",totalDistance) +" km");
        timeNum.setText(String.format("%.0f",minutes) + " min");
        averageSpeed.setText(String.format("%.1f",getAveragedSpeed()) +" km/h");
        batteryDrain.setText("15" + "%");

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                String trip_name = tripName.getText().toString();

                //time in minutes

                if(trip_name.isEmpty()){
                    tripName.setError("Trip Name is required!");
                    tripName.requestFocus();
                    return;
                }

                if(trip_name.length() > 15){
                    tripName.setError("Max Trip Name up to 15 characters!");
                    tripName.requestFocus();
                    return;
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd ' ' HH:mm");
                DateTimeFormatter dtfForDatabase = DateTimeFormatter.ofPattern("yyyy:MM:dd ' ' HH:mm");
                LocalDateTime now = LocalDateTime.now();



                Trip trip = new Trip(dtf.format(now),trip_name, (String) distance.getText(), (String) timeNum.getText(), (String) averageSpeed.getText(), (String) batteryDrain.getText());
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("trips").child(dtfForDatabase.format(now))
                        .setValue(trip);

                tripdialog.dismiss();
                Toast.makeText(ScooterDashboard.this,"Saved", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripdialog.dismiss();
                Toast.makeText(ScooterDashboard.this,"Back", Toast.LENGTH_SHORT).show();
            }
        });

        tripdialog.show();

        /*LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.savedialog, null);

        EditText tripName = view.findViewById(R.id.tvTripName);
        ImageButton saveButton = view.findViewById(R.id.saveButton);

        savealert.setView(view);
        savealert.setCancelable(false);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trip_name = tripName.getText().toString();
                Toast.makeText(getApplicationContext(), "Added: "+trip_name, Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog dialog = savealert.create();
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.show();

         */

    }


    public void resetTapped(View view)
    {
        AlertDialog.Builder resetAlert = new AlertDialog.Builder(this);
        resetAlert.setTitle("Reset Timer");
        resetAlert.setMessage("Are you sure you want to reset the timer?");
        resetAlert.setPositiveButton("Reset", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if(timerTask != null)
                {
                    timerTask.cancel();
                    setButtonUI(R.drawable.ic_baseline_play_arrow_24);
                    time = 0.0;
                    timerStarted = false;
                    timerText.setText(formatTime(0,0,0));


                    totalDistance = 0;
                    locEnd = null;
                    textViewTotalDistance.setText(String.format("%.1f", totalDistance)+" km");

                }
            }
        });

        resetAlert.setNeutralButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //do nothing
            }
        });

        resetAlert.show();

    }

    public void startStopTapped(View view)
    {
        if(timerStarted == false)
        {
            locEnd = null;
            timerStarted = true;
            setButtonUI(R.drawable.ic_baseline_pause_24);

            startTimer();
        }
        else
        {
            timerStarted = false;
            setButtonUI(R.drawable.ic_baseline_play_arrow_24);
            timerTask.cancel();
            locEnd = null;
        }
    }

    private void setButtonUI(int start)
    {
        //stopStartButton.setText(start);
        //stopStartButton.setTextColor(ContextCompat.getColor(this, color));
        stopStartButton.setImageResource(start);
    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        timerText.setText(getTimerText());
                        getDistance();
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }


    private String getTimerText()
    {
        int rounded = (int) Math.round(time);
        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }



    public void getDistance() {
        if(locEnd == null){
            locEnd = locStart;
        }else {

            if(locStart.distanceTo(locEnd) / 1000 < 1) {
                totalDistance += locStart.distanceTo(locEnd) / 1000;
                textViewTotalDistance.setText(String.format("%.1f", totalDistance)+" km");

                locEnd = locStart;
            }else{
                locEnd = locStart;
            }
        }
    }

    public double getAveragedSpeed(){

        if(totalDistance == 0 || Math.round(time) == 0) return 0;

        double rounded = (double) Math.round(time);

        double hours = BigDecimal.valueOf(((rounded % 86400) / 3600))
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();

        avgSpeed = totalDistance / hours ;

        //textViewAvgSpeed.setText(String.format("%.2f",avgSpeed) +" km/h");
        return avgSpeed;
    }






    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            CLocation myLocation = new CLocation(location);
            this.updateSpeed(myLocation);
            this.updateLocation(myLocation);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    @SuppressLint("MissingPermission")
    private void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }
        Toast.makeText(this,"Waiting GPS Connection!", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location){
        Formatter fmt = new Formatter(new StringBuilder());
        if(location!=null){
            nCurrentSpeed = location.getSpeed();
            fmt.format(Locale.US, "%.1f", nCurrentSpeed);
            String strCurrentSpeed = fmt.toString();


            //String strUnits
            speedometer.speedTo(nCurrentSpeed);
            //textViewSpeed.setText(strCurrentSpeed + " km/h");
        }

    }

    private void updateLocation(CLocation location){

        double nLatitude =0,nLongitud =0;
        if(location!=null){
            nLatitude = location.getLatitude();
            nLongitud = location.getLongitude();
        }

        //textViewLatitude.setText("Latitude: "+nLatitude);
        //textViewLongitude.setText("Longitude: "+nLongitud);

        locStart = new Location("");
        locStart.setLatitude(nLatitude);
        locStart.setLongitude(nLongitud);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.doStuff();
            } else {
                finish();
            }
        }
    }
}
