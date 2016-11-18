package ch.epfl.sweng.spotOn.test.gui;

import android.content.Intent;
import android.location.Location;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.spotOn.R;
import ch.epfl.sweng.spotOn.gui.ViewFullsizeImageActivity;
import ch.epfl.sweng.spotOn.localObjects.LocalDatabase;
import ch.epfl.sweng.spotOn.media.PhotoObject;
import ch.epfl.sweng.spotOn.singletonReferences.DatabaseRef;
import ch.epfl.sweng.spotOn.singletonReferences.StorageRef;
import ch.epfl.sweng.spotOn.test.util.MockLocationTracker_forTest;
import ch.epfl.sweng.spotOn.test.util.PhotoObjectTestUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Alexis Dewaele on 09/11/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewFullSizeImageActivityTest {

    @Rule
    public ActivityTestRule<ViewFullsizeImageActivity> mActivityTestRule = new ActivityTestRule<>(ViewFullsizeImageActivity.class,true,false);
    public String pictureID;
    public Intent displayFullSizeImageIntent;

    @Before
    public void initLocalDatabase(){
        Location location = new Location("testLocationProvider");
        location.setLatitude(46.52890355757567);
        location.setLongitude(6.569420238493345);
        location.setAltitude(0);
        location.setTime(System.currentTimeMillis());

        MockLocationTracker_forTest mlt = new MockLocationTracker_forTest(location);
        LocalDatabase.initialize(mlt);

        PhotoObject po = PhotoObjectTestUtils.paulVanDykPO();
        pictureID = po.getPictureId();
        // po.upload(); not  usefull in this test
        LocalDatabase.getInstance().addPhotoObject(po);

        displayFullSizeImageIntent = new Intent();
        displayFullSizeImageIntent.putExtra(ViewFullsizeImageActivity.WANTED_IMAGE_PICTUREID, pictureID);

    }

    @Test
    public void launchFullPictureActivity() throws Exception{
        mActivityTestRule.launchActivity(displayFullSizeImageIntent);
        Thread.sleep(1000);
        onView(withId(R.id.upvoteButton)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.downvoteButton)).perform(click());

        Thread.sleep(10000);
    }

    @Test
    public void swipeBetweenPicturesTest() throws InterruptedException{
        mActivityTestRule.launchActivity(displayFullSizeImageIntent);
        Thread.sleep(1000);
        onView(withId(R.id.pager)).perform(swipeLeft());
    }

    @After
    public void deletePhotoObject(){
        DatabaseRef.deletePhotoObjectFromDB(pictureID);
        StorageRef.deletePictureFromStorage(pictureID);
        LocalDatabase.getInstance().removePhotoObject(pictureID);
    }

    /**
     * Initialize the local database with 2 sample pictures (useful for testing)
     * @return the list of picture IDs pictures added in the local database
     */
    /*public static List<String> initLocalDatabase() {
        List<String> picIDs = new ArrayList<>();
        Location location = new Location("testLocationProvider");
        location.setLatitude(46.52890355757567);
        location.setLongitude(6.569420238493345);
        location.setAltitude(0);
        location.setTime(System.currentTimeMillis());
        LocalDatabase.clearData();
        LocalDatabase.setLocation(location);

        PhotoObject po1 = TestPhotoObjectUtils.paulVanDykPO();
        po1.setRadiusMax();
        String pictureID3 = po1.getPictureId();
        picIDs.add(pictureID3);
        po1.upload(true, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
        PhotoObject po2 = TestPhotoObjectUtils.germaynDeryckePO();
        po1.setRadiusMax();
        String pictureID4 = po2.getPictureId();
        picIDs.add(pictureID4);
        po2.upload(true, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        LocalDatabase.addPhotoObject(po1);
        LocalDatabase.addPhotoObject(po2);
        return picIDs;
    }*/
}
