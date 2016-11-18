// Test from the AndroidTest folder, was not ran by jenkins before, needs fixing
//
//
// package ch.epfl.sweng.spotOn.test.gui;
//
//import android.location.Location;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.espresso.intent.rule.IntentsTestRule;
//import android.support.test.runner.AndroidJUnit4;
//import android.support.test.uiautomator.UiDevice;
//import android.support.test.uiautomator.UiObject;
//import android.support.test.uiautomator.UiSelector;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import ch.epfl.sweng.spotOn.R;
//import ch.epfl.sweng.spotOn.gui.TabActivity;
//import ch.epfl.sweng.spotOn.localObjects.LocalDatabase;
//import ch.epfl.sweng.spotOn.localisation.ConcreteLocationTracker;
//import ch.epfl.sweng.spotOn.media.PhotoObject;
//import ch.epfl.sweng.spotOn.test.util.MockLocationTracker_forTest;
//import ch.epfl.sweng.spotOn.test.util.PhotoObjectTestUtils;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.swipeLeft;
//import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
///**
// * Created by olivi on 09.11.2016.
// */
//
//@RunWith(AndroidJUnit4.class)
//public class MapFragmentTest {
//
//    MockLocationTracker_forTest mMockLocationTracker;
//
//    //This will be useful when clicking on pins (not the pin for the location)
//    String mPictureId;
//    UiDevice mDevice;
//
//    @Rule
//    public IntentsTestRule<TabActivity> intentsRule = new IntentsTestRule<TabActivity>(TabActivity.class){
//        @Override
//        public void beforeActivityLaunched(){
//            Location location = new Location("testLocationProvider");
//            location.setLatitude(46.52890355757567);
//            location.setLongitude(6.569420238493345);
//            location.setAltitude(0);
//            location.setTime(System.currentTimeMillis());
//
//            mMockLocationTracker = new MockLocationTracker_forTest(location);
//            LocalDatabase.initialize(mMockLocationTracker);
//            ConcreteLocationTracker.setMockLocationTracker(mMockLocationTracker);
//
//            mPictureId = initLocalDatabase().get(0);
//            mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//        }
//    };
//
//    @Test
//    public void displayClusterTest() throws Exception {
//        PhotoObject picToAdd = PhotoObjectTestUtils.iceDivingPO();
//        LocalDatabase.getInstance().addPhotoObject(picToAdd);
//
//        onView(withId(R.id.viewpager)).perform(swipeLeft());
//        Thread.sleep(1000);
//        onView(withId(R.id.viewpager)).perform(swipeLeft());
//        Thread.sleep(1000);
//
//        Thread.sleep(1000);
//        Thread.sleep(1000);
//    }
//
//
//    @Test
//    public void clickingOnMarkerTest() throws Exception {
//        onView(withId(R.id.viewpager)).perform(swipeLeft());
//        Thread.sleep(1000);
//        onView(withId(R.id.viewpager)).perform(swipeLeft());
//        Thread.sleep(1000);
//        UiSelector myPositionUiSelecter = new UiSelector().descriptionContains("My Position");
//        if(myPositionUiSelecter==null){
//            throw new AssertionError("the \"my position\" pin should exist");
//        }
//        UiObject pin = mDevice.findObject(myPositionUiSelecter);
//        if(pin==null){
//            throw new AssertionError("pin should exist");
//        }
//        pin.click();
//        Thread.sleep(1000);
//    }
//
//    /**
//     * Initialize the local database with 2 sample pictures (useful for testing)
//     * @return the list of picture IDs pictures added in the local database
//     */
//    public static List<String> initLocalDatabase() {
//        List<String> picIDs = new ArrayList<>();
//
//        LocalDatabase.getInstance().clear();
//
//        PhotoObject po3 = PhotoObjectTestUtils.paulVanDykPO();
//        String pictureID3 = po3.getPictureId();
//        picIDs.add(pictureID3);
//        po3.upload();
//
//        PhotoObject po4 = PhotoObjectTestUtils.germaynDeryckePO();
//        String pictureID4 = po4.getPictureId();
//        picIDs.add(pictureID4);
//        po4.upload();
//
//        LocalDatabase.getInstance().addPhotoObject(po3);
//        LocalDatabase.getInstance().addPhotoObject(po4);
//        return picIDs;
//    }
//}