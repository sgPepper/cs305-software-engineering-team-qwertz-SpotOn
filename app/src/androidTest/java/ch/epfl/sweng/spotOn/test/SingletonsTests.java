package ch.epfl.sweng.spotOn.test;

import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.spotOn.singletonReferences.DatabaseRef;
import ch.epfl.sweng.spotOn.singletonReferences.StorageRef;
import ch.epfl.sweng.spotOn.test.util.DatabaseRef_test;
import ch.epfl.sweng.spotOn.test.util.StorageRef_test;

/**
 * Created by quentin on 04.11.16.
 */

@RunWith(AndroidJUnit4.class)
public class SingletonsTests {

    @Test
    public void testDatabaseRefReferences(){
        if(!(DatabaseRef.getUsersDirectory().equals(FirebaseDatabase.getInstance().getReference("UsersDirectory")))){
            throw new AssertionError();
        }
        if(!(DatabaseRef.getMediaDirectory().equals(FirebaseDatabase.getInstance().getReference("MediaDirectory")))){
            throw new AssertionError();
        }
        if(!(DatabaseRef_test.getUsersDirectory().equals(FirebaseDatabase.getInstance().getReference("UsersDirectory_test")))){
            throw new AssertionError();
        }
        if(!(DatabaseRef_test.getMediaDirectory().equals(FirebaseDatabase.getInstance().getReference("MediaDirectory_test")))){
            throw new AssertionError();
        }
    }

    @Test
    public void testStorageReferences(){
        if(!(StorageRef.getMediaDirectory().equals(FirebaseStorage.getInstance().getReference("Images")))){
            throw new AssertionError();
        }
        if(!(StorageRef_test.getMediaDirectory().equals(FirebaseStorage.getInstance().getReference("Images_test")))){
            throw new AssertionError();
        }
    }


}