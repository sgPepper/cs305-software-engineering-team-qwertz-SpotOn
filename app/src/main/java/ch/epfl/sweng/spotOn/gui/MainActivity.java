/** Use of code that can be found here:
 * https://developers.facebook.com/docs/facebook-login/android/
 *
 */

package ch.epfl.sweng.spotOn.gui;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

// Add this to the header of your file:
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import android.Manifest;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import ch.epfl.sweng.spotOn.R;
import ch.epfl.sweng.spotOn.fileDeletionServices.PassedTimestampFileDeletionService;
import ch.epfl.sweng.spotOn.fileDeletionServices.ServerDeleteExpiredPhotoReceiver;


/**
 * Your app's main activity.
 */
public final class MainActivity extends AppCompatActivity {

    private LoginButton mainLoginButton;

    private CallbackManager mCallbackManager;

    private final long TIME_BETWEEN_TWO_ALARM = 60 * 60 *1000;//one hour for now

    private final int REQUEST_FINE_LOCALISATION = 9;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        Intent deleteFileService = new Intent(this, PassedTimestampFileDeletionService.class);
        startService(deleteFileService);

        /*Create an alarm which will go off for sending queries to the Firebase server
         * to check the expiration time of the files.
         */
        AlarmManager serverDataDeletionAlarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent serverDataDeletionIntent = new Intent(this, ServerDeleteExpiredPhotoReceiver.class);
        PendingIntent serverDataDeletionPendingIntent = PendingIntent.getBroadcast(this, 0, serverDataDeletionIntent, 0);
        serverDataDeletionAlarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TIME_BETWEEN_TWO_ALARM, serverDataDeletionPendingIntent);


        setContentView(R.layout.activity_main);

        mCallbackManager = CallbackManager.Factory.create();

        // get the mainLoginButton (facebook login button)
        LoginButton mainLoginButton = (LoginButton) findViewById(R.id.mainLoginButton);

        mainLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            // Process depending on the result of the authentication
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Once the user is connected
                goToTabActivity();
            }

            @Override
            public void onCancel() {
                // Display a welcome message when user authenticates
                Context context = getApplicationContext();
                String toastMessage = "The authentication has been cancelled";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, toastMessage, duration);
                toast.show();
            }

            @Override
            public void onError(FacebookException exception) {
                //Log the exception raised by Facebook
                Log.e("FacebookException", exception.getMessage());
            }
        });

        // Test if a user is already logged on when creating the MainActivity
        if(AccessToken.getCurrentAccessToken()!= null) {
            goToTabActivity();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //check every time the MainActivity is started if we have the permission: ACCESS_FINE_LOCATION
        // and throw the user input in onRequestPermissionsResult
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity","No Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCALISATION);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void goToPictureActivity(View view){
        //launch the TakePictureFragment
        Intent pictureIntent = new Intent(this, TakePictureFragment.class);
        startActivity(pictureIntent);
    }

    /*
        This method uses the options menu when this activity is launched
         */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    public void goToTabActivity() {
        Intent intent = new Intent(this, TabActivity.class);
        startActivity(intent);
    }

    public void goToMapsActivity(View view){
        //launch the map Activity
        //TODO: migrate the MapFragment inside the fragment manager of MainActivity
        Intent mapIntent = new Intent(this, MapFragment.class);
        startActivity(mapIntent);
    }

    //read the result of the permission request, leave the app if we don't have the gps permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCALISATION: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO: When the TakePictureFragment and MapFragment will be fragment inside MainActivity, create the LocationManager here

                }
                //permission denied
                else {
                    Toast.makeText(this, getString(R.string.gps_not_permitted), Toast.LENGTH_LONG).show();

                    // leave the app
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }
}