package com.pranav.pitching;

import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // simply , just declaring the variables. No further explanations required...
    int count = 0;
    TextView textView;
    SensorManager sensorManager;// call the sensor manager->the heart of the program.
    Sensor stepCounter;// step counter
    SensorEventListener sensorEventListener;// This records the changes into the app.
    public static final String INPUT = "count";
    SharedPreferences sharedPreferences;//Analogous to SQLite->storing data.
    //Using shared preferences was the brain of this program . Shared preferences is another method
    //to store data for long time to use it again and again even when onDestroy function is called.
    SharedPreferences.Editor editor;
    ImageView foot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getPreferences(MODE_PRIVATE);// Shared pref is called.
        editor = sharedPreferences.edit();

        textView = findViewById(R.id.textView);
        //The sensor manager is called from system service.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //What is type step counter?

//        A sensor of this type returns the number of steps taken by the user since the last reboot
//     * while activated. The value is returned as a float (with the fractional part set to zero) and
//     * is reset to zero only on a system reboot. The timestamp of the event is set to the time when
//     * the last step for that event was taken. This sensor is implemented in hardware and is
//     * expected to be low power. If you want to continuously track the number of steps over a long
//     * period of time, do NOT unregister for this sensor, so that it keeps counting steps in the
//     * background even when the AP is in suspend mode and report the aggregate count when the AP
//     * is awake. Application needs to stay registered for this sensor because step counter does not
//   ->  * count steps if it is not activated. This sensor is ideal for fitness tracking applications.


//count is parsing the integer into string.
        count = Integer.parseInt(sharedPreferences.getString(INPUT, "0"));
        textView.setText(sharedPreferences.getString(INPUT, "0"));

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                //for every count , increase count by 1;Logic-> cnt=cnt+1;
                count++;
                editor.putString(INPUT, "" + count);
                editor.apply();
                textView.setText("" + count);

                if (count % 10 == 0) {// for every 10 steps counted , raise a toast-> to boost the
                    // person's motivation for short period of time !
                    Toast.makeText(MainActivity.this, "" + count + " steps walked!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
    }

    //onStart function is called.
    @Override
    protected void onStart() {
        super.onStart();
//When the app starts , fetch the previous record as fast as possible.Hence the last argument is passed(Below)
        sensorManager.registerListener(sensorEventListener, stepCounter, sensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onPause() {
        //when the app is switched by another app, unregister the listener(refer documentation if not clear)
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }
//The main activity's layout is inflated here.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);// inflate the menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // the Logic for back button-> whenever the back button is called ,there are 2 options for user-YES or NO.
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Are you sure you want to set the count to 0?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        //if the user says Yes, then put count to zero,commit the changes made so far
                        // and dismiss the dialog box opened.
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            count = 0;
                            editor.putString(INPUT, "" + count);
                            textView.setText("" + count);
                            editor.commit();
                            dialogInterface.dismiss();
                        }
                    })
                    //if the user says no, then just dismiss the dialog box and do nothing.
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();


        }
        return super.onOptionsItemSelected(item);
    }

    //logic for converting height to distance. A clinical research says that the step length of a person
//is equal to 0.5 times the number of steps taken multiplied by the person's height. So the logic is,
    //pehle height leke aao, then parse it into string ,multiply it by 0.5 and then with the no. of steps.
    public void convert(View View) {

        EditText distCalc = findViewById(R.id.distCalc);
        Double dtos = Double.parseDouble(distCalc.getText().toString());
        Double tvdist = (count * dtos) / 2;
        Toast.makeText(getApplicationContext(), tvdist.toString(), Toast.LENGTH_LONG).show();
    }

    //Fading function for the top images.Using the animate function was a tricky task here.
    public void fade(View view2) {
        ImageView foot = findViewById(R.id.foot);
        ImageView foot2 = findViewById(R.id.foot2);
        foot.animate().alpha(0f).setDuration(3000);//alpha 1f(1 float)-> it means no visibility.
        //further the next step is to set duration. This argument takes the values in millisec....
        //so 3000millisec means 3 seconds.
        foot2.animate().alpha(1f).setDuration(3000);//alpha 0f(0 float)-> it means full visibility
    }

    //logic for converting steps into calories.Again, abiding by clinical which says , pehle weight leke aao (in Kgs), then since the
    //conversion of calories requires weight to be in pounds, convert karo usse into pounds->(MF=2.205)
    // and then multiply it with the step count and a multiplier i.e 0.57.
    //The calories value is returned in the form of float datatype.
    public void calorie(View view) {
        EditText calCalc = findViewById(R.id.calCalc);//step1->fetch the weight of person (in Kgs).
        Double wtolbs = Double.parseDouble(calCalc.getText().toString());//step2->parse weight into string.
        Double tvCal = (wtolbs * count * 0.57 * 2.205);//step3->the mathematical part....Calculations!
        Toast.makeText(getApplicationContext(), tvCal.toString(), Toast.LENGTH_LONG).show();//step4->
        // Finally raise the toast and yes , you got the calories burnt by the person !
    }

    // Fading function for the bottom images.
    public void fade2(View view3) {
        ImageView walknew2 = findViewById(R.id.walknew2);
        ImageView walk = findViewById(R.id.walk);
        walknew2.animate().alpha(1f).setDuration(3000);//alpha 1f(1 float)-> it means 0 visibility.
        //further the next step is to set duration. This argument takes the values in millisec....
        //so 3000millisec means 3 seconds.
        walk.animate().alpha(0f).setDuration(3000);//alpha 0f(0 float)-> it means full visibility
    }
}
