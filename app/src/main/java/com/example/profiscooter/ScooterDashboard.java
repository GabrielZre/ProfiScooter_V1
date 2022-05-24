package com.example.profiscooter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ScooterDashboard extends AppCompatActivity implements LocationListener {

    static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static boolean isButtonChecked = false;
    private static double totalDistance = 0;
    private static float nCurrentSpeed = 0;
    private static double avgSpeed = 0;

    private volatile boolean stopThread = true;
    private volatile boolean isBluetoothSuccess = false;

    private static boolean batterySettingsDefined = false;
    private static double batteryAhX, batteryVoltageX, motorPowerX, bottomCutOffX, upperCutOffX;
    private static double currentBatteryVoltage;
    private static double batteryPercentage, batteryDistance;
    private static double batteryPercentageStart = 0;

    private static Location locEnd = null;
    private static Location locStart = null;

    private DatabaseReference reference;


    //TextView textViewSpeed;
    TextView textViewTotalDistance, textViewAvgSpeed;
    TextView textViewBatteryPercentage, textViewBatteryDistance;
    //TextView textViewLatitude,textViewLongitude;
    //ToggleButton buttonStartMeasuring;
    PointerSpeedometer speedometer;
    TextView timerText;
    ImageButton stopStartButton, saveButton;
    ImageView batBluetooth;


    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    boolean timerStarted = false;

    Dialog tripdialog, batteryDialog;


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
        textViewTotalDistance.setText(String.format("%.1f", totalDistance) + " km");
        //buttonStartMeasuring = findViewById(R.id.startMeasuring);
        //check for gps permission
        textViewBatteryDistance = findViewById(R.id.textViewBatteryDistance);
        textViewBatteryPercentage = findViewById(R.id.textViewBatteryPercentage);

        new ScooterDashboard().runOnUiThread(() -> {

//put your UI access code here

        });


        batBluetooth = findViewById(R.id.startBatteryMeasurement);


        batBluetooth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(ScooterDashboard.this, "Disconnect...", Toast.LENGTH_SHORT).show();
                stopThread(view);
                return true;
            }
        });

        batBluetooth.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View view) {
                if (!stopThread) {
                    Toast.makeText(ScooterDashboard.this, "Options...", Toast.LENGTH_SHORT).show();
                    showBatteryDialog();
                }
                if (stopThread) {
                    Toast.makeText(ScooterDashboard.this, "Connecting...", Toast.LENGTH_SHORT).show();
                    startThread(view);
                }

            }
        });

        speedometer = findViewById(R.id.pointerSpeedometer);


        timerText = (TextView) findViewById(R.id.timerText);
        stopStartButton = (ImageButton) findViewById(R.id.startStopButton);
        saveButton = (ImageButton) findViewById(R.id.saveButton);
        tripdialog = new Dialog(this);
        batteryDialog = new Dialog(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


        timer = new Timer();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            doStuff();
        }

        updateSpeed(null);
        updateLocation(null);


    }

    private void showDialog() {
        tripdialog.setContentView(R.layout.savedialog);

        tripdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tripdialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        EditText tripName = tripdialog.findViewById(R.id.etBatteryAh);
        ImageButton buttonSave = tripdialog.findViewById(R.id.buttonSave);
        ImageButton buttonBack = tripdialog.findViewById(R.id.buttonBack);

        TextView distance = tripdialog.findViewById(R.id.tv_distanceNum);
        TextView timeNum = tripdialog.findViewById(R.id.tv_distanceTimeNum);
        TextView averageSpeed = tripdialog.findViewById(R.id.tv_averageSpeedNum);
        TextView batteryDrain = tripdialog.findViewById(R.id.tv_batteryDrainNum);
        TextView dateTime = tripdialog.findViewById(R.id.tvDateTime);

        //calculate minutes
        double rounded = (double) Math.round(time);
        double minutes = BigDecimal.valueOf(((rounded % 86400) / 3600) * 60)
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();
        if(batteryPercentageStart != 0) {
            double batteryPercentageDrain = batteryPercentageStart - batteryPercentage;
            batteryDrain.setText(String.format("%.0f", batteryPercentageDrain) + "%");
        } else {
            batteryDrain.setText("-");
        }
        distance.setText(String.format("%.1f", totalDistance) + " km");
        timeNum.setText(String.format("%.0f", minutes) + " min");
        averageSpeed.setText(String.format("%.1f", getAveragedSpeed()) + " km/h");


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                String trip_name = tripName.getText().toString();

                //time in minutes

                if (trip_name.isEmpty()) {
                    tripName.setError("Trip Name is required!");
                    tripName.requestFocus();
                    return;
                }

                if (trip_name.length() > 15) {
                    tripName.setError("Max Trip Name up to 15 characters!");
                    tripName.requestFocus();
                    return;
                }

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd ' ' HH:mm");
                DateTimeFormatter dtfForDatabase = DateTimeFormatter.ofPattern("yyyy:MM:dd ' ' HH:mm");
                LocalDateTime now = LocalDateTime.now();


                Trip trip = new Trip(dtf.format(now), trip_name, (String) distance.getText(), (String) timeNum.getText(), (String) averageSpeed.getText(), (String) batteryDrain.getText());
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("trips").child(dtfForDatabase.format(now))
                        .setValue(trip);

                tripdialog.dismiss();
                Toast.makeText(ScooterDashboard.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tripdialog.dismiss();
                Toast.makeText(ScooterDashboard.this, "Back", Toast.LENGTH_SHORT).show();
            }
        });

        tripdialog.show();
    }


    private void showBatteryDialog() {
        batteryDialog.setContentView(R.layout.batterysettings);

        batteryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        batteryDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        EditText batteryAh = batteryDialog.findViewById(R.id.etBatteryAh);
        EditText batteryVoltage = batteryDialog.findViewById(R.id.etBatteryVoltage);
        EditText motorWatt = batteryDialog.findViewById(R.id.etMotorPower);
        EditText bottomCutOff = batteryDialog.findViewById(R.id.etBottomCutOff);
        EditText upperCutOff = batteryDialog.findViewById(R.id.etUpperCutOff);

        ImageButton buttonSave = batteryDialog.findViewById(R.id.buttonSave);
        ImageButton buttonBack = batteryDialog.findViewById(R.id.buttonBack);


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                String battery_Ah = batteryAh.getText().toString();
                String battery_Voltage = batteryVoltage.getText().toString();
                String motor_Watt = motorWatt.getText().toString();
                String bottom_CutOff = bottomCutOff.getText().toString();
                String upper_CutOff = upperCutOff.getText().toString();

                //time in minutes

                if (battery_Ah.isEmpty()) {
                    batteryAh.setError("Battery Ah is required!");
                    batteryAh.requestFocus();
                    return;
                }
                if (battery_Voltage.isEmpty()) {
                    batteryVoltage.setError("Battery Voltage is required!");
                    batteryVoltage.requestFocus();
                    return;
                }
                if (motor_Watt.isEmpty()) {
                    motorWatt.setError("Motor Watt is required!");
                    motorWatt.requestFocus();
                    return;
                }
                if (bottom_CutOff.isEmpty()) {
                    bottomCutOff.setError("Bottom CutOff is required!");
                    bottomCutOff.requestFocus();
                    return;
                }
                if (upper_CutOff.isEmpty()) {
                    upperCutOff.setError("Upper CutOff is required!");
                    upperCutOff.requestFocus();
                    return;
                }

                ScooterInfo scooterInfo = new ScooterInfo(battery_Ah, battery_Voltage, motor_Watt, bottom_CutOff, upper_CutOff);
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("scooter")
                        .setValue(scooterInfo);


                batteryDialog.dismiss();
                Toast.makeText(ScooterDashboard.this, "Saved", Toast.LENGTH_SHORT).show();
                tryPreviousSettings();
            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                batteryDialog.dismiss();
                Toast.makeText(ScooterDashboard.this, "Back", Toast.LENGTH_SHORT).show();
            }
        });

        batteryDialog.show();

    }


    public void resetTapped(View view) {
        AlertDialog.Builder resetAlert = new AlertDialog.Builder(this);
        resetAlert.setTitle("Reset Timer");
        resetAlert.setMessage("Are you sure you want to reset the timer?");
        resetAlert.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (timerTask != null) {
                    timerTask.cancel();
                    setButtonUI(R.drawable.ic_baseline_play_arrow_24);
                    time = 0.0;
                    timerStarted = false;
                    timerText.setText(formatTime(0, 0, 0));


                    totalDistance = 0;
                    locEnd = null;
                    batteryPercentageStart = 0;
                    if(stopThread == false) {
                        stopThread(view);
                    }
                    textViewTotalDistance.setText(String.format("%.1f", totalDistance) + " km");

                }
            }
        });

        resetAlert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });

        resetAlert.show();

    }

    public void startStopTapped(View view) {
        if (timerStarted == false) {
            locEnd = null;
            timerStarted = true;
            setButtonUI(R.drawable.ic_baseline_pause_24);

            startTimer();
        } else {
            timerStarted = false;
            setButtonUI(R.drawable.ic_baseline_play_arrow_24);
            timerTask.cancel();
            locEnd = null;
        }
    }

    private void setButtonUI(int start) {
        //stopStartButton.setText(start);
        //stopStartButton.setTextColor(ContextCompat.getColor(this, color));
        stopStartButton.setImageResource(start);
    }

    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timerText.setText(getTimerText());
                        getDistance();
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }


    private String getTimerText() {
        int rounded = (int) Math.round(time);
        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        if(seconds % 10 == 0){
            rateEcoDriving();
        }

        return formatTime(seconds, minutes, hours);
    }



    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }


    public void getDistance() {
        if (locEnd == null) {
            locEnd = locStart;
        } else {

            if (locStart.distanceTo(locEnd) / 1000 < 1) {
                totalDistance += locStart.distanceTo(locEnd) / 1000;
                textViewTotalDistance.setText(String.format("%.1f", totalDistance) + " km");

                locEnd = locStart;
            } else {
                locEnd = locStart;
            }
        }
    }

    public double getAveragedSpeed() {

        if (totalDistance == 0 || Math.round(time) == 0) return 0;

        double rounded = (double) Math.round(time);

        double hours = BigDecimal.valueOf(((rounded % 86400) / 3600))
                .setScale(3, RoundingMode.HALF_UP)
                .doubleValue();

        avgSpeed = totalDistance / hours;

        //textViewAvgSpeed.setText(String.format("%.2f",avgSpeed) +" km/h");
        return avgSpeed;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
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
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        Toast.makeText(this, "Waiting GPS Connection!", Toast.LENGTH_SHORT).show();
    }

    private void updateSpeed(CLocation location) {
        Formatter fmt = new Formatter(new StringBuilder());
        if (location != null) {
            nCurrentSpeed = location.getSpeed();
            fmt.format(Locale.US, "%.1f", nCurrentSpeed);
            String strCurrentSpeed = fmt.toString();


            //String strUnits
            speedometer.speedTo(nCurrentSpeed);
            //textViewSpeed.setText(strCurrentSpeed + " km/h");
        }

    }

    private void updateLocation(CLocation location) {

        double nLatitude = 0, nLongitud = 0;
        if (location != null) {
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


    public void tryPreviousSettings() {
        //get batterySettings in Scooter if exist
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("scooter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ScooterInfo userProfile = snapshot.getValue(ScooterInfo.class);

                if (userProfile != null) {


                    batteryAhX = Double.parseDouble(userProfile.batteryAh);
                    batteryVoltageX = Double.parseDouble(userProfile.batteryVoltage);
                    motorPowerX = Double.parseDouble(userProfile.motorWatt);
                    bottomCutOffX = Double.parseDouble(userProfile.bottomCutOff);
                    upperCutOffX = Double.parseDouble(userProfile.upperCutOff);

                    batterySettingsDefined = true;
                }
                if (userProfile == null) {
                    Toast.makeText(ScooterDashboard.this, "Click battery to set up!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScooterDashboard.this, "Something wrong happened!", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void startThread(View view) {
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable();
        new Thread(runnable).start();

    }

    public void stopThread(View view) {
        stopThread = true;
        batBluetooth.setImageResource(R.drawable.batbluetooth);
        textViewBatteryPercentage.setVisibility(View.INVISIBLE);
        textViewBatteryDistance.setVisibility(View.INVISIBLE);
    }


    class ExampleRunnable implements Runnable {


        @Override
        public void run() {
            if (!batterySettingsDefined) {
                tryPreviousSettings();
            }
            while (!stopThread) {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                System.out.println(btAdapter.getBondedDevices());

                BluetoothDevice hc05 = btAdapter.getRemoteDevice("C8:C9:A3:D1:9E:22");
                System.out.println(hc05.getName());

                BluetoothSocket btSocket = null;
                int counter = 0;
                do {
                    try {
                        btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                        System.out.println(btSocket);
                        btSocket.connect();
                        System.out.println(btSocket.isConnected());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    counter++;
                } while (!btSocket.isConnected() && counter < 3);
                if (btSocket.isConnected()) {
                    System.out.println("ITS  OKAYYYYY");
                    isBluetoothSuccess = true;
                }else{
                    isBluetoothSuccess = false;
                }

                try {
                    OutputStream outputStream = btSocket.getOutputStream();
                    outputStream.write(48);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                InputStream inputStream = null;
                try {
                    inputStream = btSocket.getInputStream();
                    inputStream.skip(inputStream.available());
                    char[] byteArray = new char[4];
                    String voltageString = "";

                    for (int i = 0; i < 4; i++) {

                        char b = (char) inputStream.read();
                        byteArray[i] = b;
                        voltageString += b;


                    }
                    System.out.println(Arrays.toString(byteArray));
                    System.out.println(voltageString);
                    double voltage = Double.parseDouble(voltageString);
                    currentBatteryVoltage = voltage;
                    System.out.println(currentBatteryVoltage);
                    //calculate the % and km, and change image of battery while currentVoltage is > 0

                    if (currentBatteryVoltage > 0 && batterySettingsDefined) {
                        setTextBattery();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    btSocket.close();
                    System.out.println(btSocket.isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setTextBattery() {
        double percentage = ((currentBatteryVoltage - bottomCutOffX) * 100) / (upperCutOffX - bottomCutOffX);
        double distance = ((((batteryAhX * batteryVoltageX) / motorPowerX) * 30) * percentage) / 100 ; // default average 30 km/h

        if(batteryPercentageStart == 0){
            batteryPercentageStart = percentage;
        }

        if (percentage > 90) {
            batBluetooth.setImageResource(R.drawable.bat100);
        }
        if (percentage <= 90 && percentage > 80) {
            batBluetooth.setImageResource(R.drawable.bat90);
        }
        if (percentage <= 80 && percentage > 70) {
            batBluetooth.setImageResource(R.drawable.bat80);
        }
        if (percentage <= 70 && percentage > 60) {
            batBluetooth.setImageResource(R.drawable.bat70);
        }
        if (percentage <= 60 && percentage > 50) {
            batBluetooth.setImageResource(R.drawable.bat60);
        }
        if (percentage <= 50 && percentage > 40) {
            batBluetooth.setImageResource(R.drawable.bat50);
        }
        if (percentage <= 40 && percentage > 30) {
            batBluetooth.setImageResource(R.drawable.bat40);
        }
        if (percentage <= 30 && percentage > 20) {
            batBluetooth.setImageResource(R.drawable.bat30);
        }
        if (percentage <= 20 && percentage > 10) {
            batBluetooth.setImageResource(R.drawable.bat20);
        }
        if (percentage <= 10 && percentage > 0) {
            batBluetooth.setImageResource(R.drawable.bat10);
        }
        if (percentage <= 0) {
            batBluetooth.setImageResource(R.drawable.bat0);
        }

        batteryPercentage = percentage;
        batteryDistance = distance;

        runOnUiThread(() -> {
            textViewBatteryPercentage.setText(String.format("%.0f", batteryPercentage) + "%");
            textViewBatteryPercentage.setVisibility(View.VISIBLE);
            textViewBatteryDistance.setText(String.format("%.0f", batteryDistance) + "KM");
            textViewBatteryDistance.setVisibility(View.VISIBLE);
        });
    }


    private void rateEcoDriving() {

    }
}


