package ch.epfl.sweng.project;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

/**
 *  This class represents a picture and all associated information : name, thumbnail, pictureID, author, timestamps, position, radius
 *  It is mean to be used locally, as opposed to the PhotoObjectStoredInDatabase which is used to be stored in the database
 */
public class PhotoObject {

    private final long DEFAULT_PICTURE_LIFETIME = 24*60*60*1000; // in milliseconds - 24H
    private final int THUMBNAIL_SIZE = 128; // in pixels
    private final String DATABASE_MEDIA_PATH = "MediaDirectory"; // used for Database Reference
    private final String FILESERVER_MEDIA_PATH = "gs://spoton-ec9ed.appspot.com/images";

    private final long FIVE_MEGABYTES = 5*1024*1024;

    private Bitmap mFullsizeImage;
    private String mFullsizeImageLink;   // needed for the "cache-like" behaviour of getFullsizeImage()
    private boolean mHasFullsizeImage;   // false -> no Image, but has link; true -> has image, can't access link (don't need it => no getter)
    private Bitmap mThumbnail;
    private String mPictureId;
    private String mAuthorID;
    private String mPhotoName;
    private Timestamp mCreatedDate;
    private Timestamp mExpireDate;
    private double mLatitude;
    private double mLongitude;
    private int mRadius;

    /** This constructor will be used when the user takes a photo with his device, and create the object from locally obtained information
     *  pictureId should be created by calling .push().getKey() on the DatabaseReference where the object should be stored */
    public PhotoObject(Bitmap fullSizePic, String authorID, String photoName,
                       Timestamp createdDate, double latitude, double longitude, int radius){
        mFullsizeImage = fullSizePic.copy(fullSizePic.getConfig(), true);
        mHasFullsizeImage=true;
        mFullsizeImageLink = null;  // link not avaiable yet
        mThumbnail = createThumbnail(mFullsizeImage);
        mPictureId = FirebaseDatabase.getInstance().getReference(DATABASE_MEDIA_PATH).push().getKey();   //available even offline
        mPhotoName = photoName;
        mCreatedDate = createdDate;
        mExpireDate = new Timestamp(createdDate.getTime()+DEFAULT_PICTURE_LIFETIME);
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mAuthorID = authorID;
    }

    /** This constructor is called to convert an object retrieved from the database into a PhotoObject.     */
    public PhotoObject(String fullSizeImageLink, Bitmap thumbnail, String pictureId, String authorID, String photoName, long createdDate,
                       long expireDate, double latitude, double longitude, int radius){
        mFullsizeImage = null;
        mHasFullsizeImage=false;
        mFullsizeImageLink=fullSizeImageLink;
        mThumbnail = thumbnail;
        mPictureId = pictureId;
        mPhotoName = photoName;
        mCreatedDate = new Timestamp(createdDate);
        mExpireDate = new Timestamp(expireDate);
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mAuthorID = authorID;
    }




//FUNCTIONS PROVIDED BY THIS CLASS

    /** uploads the object to our online services
     */
    public void upload(){
        // sendToFileServer calls sendToDatabase on success
        sendToFileServer();
    }

    /** return true if the coordinates in parameters are in the scope of the picture}
     */
    public boolean isInPictureCircle(LatLng position){
        return computeDistanceBetween(
                new LatLng(mLatitude, mLongitude),
                position
        ) <= mRadius;
    }

    /** retrieves the fullsizeimage from the fileserver and caches it in the object.
     *  Offers the caller to pass some listeners to trigger custom actions on download success or failure.
     *  Booleans
     */
    public void retrieveFullsizeImage(boolean hasCustomerOnSuccessListener, OnSuccessListener customerOnSuccessListener, boolean hasCustomerOnFailureListener, OnFailureListener customerOnFailureListener){
        // check for necessary conditions
        if(mFullsizeImageLink==null){
            throw new AssertionError("if there is no image stored, object should have a link to retrieve it");
        }

        // Create a file retrieval task
        Log.d("retrieving","full path in fileserver "+mFullsizeImageLink);
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(mFullsizeImageLink);
        final long ONE_MEGABYTE = 1024 * 1024;
        Task<byte[]> retrieveFullsizeImageFromFileserver = gsReference.getBytes(ONE_MEGABYTE);

        // add desired listeners
        if(hasCustomerOnSuccessListener){
            if(customerOnSuccessListener==null){
                throw new NullPointerException("this listener is specified not to be null !");
            }
            retrieveFullsizeImageFromFileserver.addOnSuccessListener(customerOnSuccessListener);
        }
        if(hasCustomerOnFailureListener){
            if(customerOnFailureListener==null){
                throw new NullPointerException("this listener is specified not to be null !");
            }
            retrieveFullsizeImageFromFileserver.addOnFailureListener(customerOnFailureListener);
        }
        addDefaultListeners(retrieveFullsizeImageFromFileserver);
    }

    /**
     * Adds default listeners, which will :
     *  - store the fullsizeImage in the object once it is retrieved
     *  - handle failures
     */
    private void addDefaultListeners(Task fileServerTask){
        fileServerTask.addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/PictureID.jpg" is returns, use this as needed
                mFullsizeImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                mHasFullsizeImage = true;
                Log.d("DownloadFromFileServer", "Downloaded full size image from FileServer");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("DownloadFromFileServer", "Exception raised by getFromFileServer()");
            }
        });
    }




//ALL THE GETTER FUNCTIONS

    public boolean hasFullSizeImage(){
        return mHasFullsizeImage;
    }
    public Bitmap getFullSizeImage(){
        if(mHasFullsizeImage){
            return mFullsizeImage.copy(mFullsizeImage.getConfig(), true);
        }else{
            throw new NoSuchElementException("PhotoObject doesn't have a fullsizeImage - you need to retrieve it with the retrieveFullSizeImage - hint : use asFullSizeImage to avoid this problem");
        }
    }
    public String getPhotoName(){
        return mPhotoName;
    }
    public Timestamp getCreatedDate(){
        return new Timestamp(mCreatedDate.getTime());
    }
    public Timestamp getExpireDate(){
        return new Timestamp(mExpireDate.getTime());
    }
    public double getLatitude(){
        return mLatitude;
    }
    public double getLongitude(){
        return mLongitude;
    }
    public int getRadius(){
        return mRadius;
    }
    public String getAuthorId(){
        return mAuthorID;
    }
    public Bitmap getThumbnail(){
        return mThumbnail.copy(mThumbnail.getConfig(), true);
    }
    public String getPictureId() {
        return mPictureId;
    }
    public String getFullsizeImageLink() { return mFullsizeImageLink; }




// PRIVATE HELPERS USED IN THE CLASS ONLY

    /** converts This into an object that we can store in the database, by converting the thumbnail into a String
     *  and leaving behing the fullsizeImage (which should be uploaded in fileserver, and retrieveable through the mFullsizeImageLink)
     */
    private PhotoObjectStoredInDatabase convertForStorageInDatabase(){
        if(mFullsizeImageLink==null){
            throw new AssertionError("the link should have been set after sending the fullsizeImage to fileserver - don't call this function on its own");
        }
        String linkToFullsizeImage = mFullsizeImageLink;
        String thumbnailAsString = encodeBitmapAsString(mThumbnail);
        return new PhotoObjectStoredInDatabase(linkToFullsizeImage, thumbnailAsString, mPictureId,mAuthorID, mPhotoName,
                mCreatedDate, mExpireDate, mLatitude, mLongitude, mRadius);
    }

    /** encodes the passed bitmap into a string
     */
    private String encodeBitmapAsString(Bitmap img){
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);
        //TODO img.recycle() ??;
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    /** creates the thumbail from this object by reducing the resolution of the fullSizeImage
     */
    private Bitmap createThumbnail(Bitmap fullSizeImage){
        return ThumbnailUtils.extractThumbnail(fullSizeImage, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
    }

    @Override
    public String toString(){
        return "PhotoObject: "+mPictureId+" lat: "+mLatitude+" long: "+mLongitude;
    }

    /** Send the full size image to the file server to be stored
     */
    private void sendToFileServer() {
        Log.d("sendToFileServer", "PictureID: "+mPictureId);
        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(FILESERVER_MEDIA_PATH);

        // Create a reference to "PictureID.jpg"
        StorageReference pictureRef = storageRef.child(mPictureId + ".jpg");

        // Create a reference to 'images/"PictureID".jpg'
        StorageReference pictureImagesRef = storageRef.child("images/" + mPictureId +  ".jpg");

        // TODO - what's the point of these 2 lines ?
        // While the file names are the same, the references point to different files
        pictureRef.getName().equals(pictureImagesRef.getName());    // true
        pictureRef.getPath().equals(pictureImagesRef.getPath());    // false

        // Convert the bitmap image to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mFullsizeImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // upload the file
        UploadTask uploadTask = pictureRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e("UploadFileToFileServer", "Exception raised by sendToFileServer()");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                // get the download link of the file
                mFullsizeImageLink = taskSnapshot.getDownloadUrl().toString();
                sendToDatabase();
            }
        });
    }

    /** Stores the object into the database (with intermediary steps : storing fullSIzeImage in the fileServer, converting the object into a PhotoObjectStoredInDatabase)
    * It is the responsibility of the sender to use the correct DBref, accordingly with the pictureId chosen, since the object will be used in a child named after the pictureId
    */
    private void sendToDatabase(){
        DatabaseReference DBref = FirebaseDatabase.getInstance().getReference(DATABASE_MEDIA_PATH);
        PhotoObjectStoredInDatabase DBobject = this.convertForStorageInDatabase();
        System.out.println(DBobject);
        DBref.child(mPictureId).setValue(DBobject);
    }
}

