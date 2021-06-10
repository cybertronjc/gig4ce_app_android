package com.gigforce.common_image_picker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Rohit
 * ImagePicker class to open camera to pick images
 * and handle their exif info
 */
public class ImagePicker {


    private static final int DEFAULT_MIN_WIDTH_QUALITY = 600;        // min pixels
    private static final String TAG = "ImagePicker";
    private static final String TEMP_IMAGE_NAME = String.valueOf(System.currentTimeMillis());
    public static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;


    @Nullable
    public static Intent getImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);

        Uri uriForFile = FileProvider.getUriForFile(
                context.getApplicationContext(),
                context.getApplicationContext().getPackageName() + ".provider",
                getTempFile(context)
        );

        if (uriForFile != null) {
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
            intentList = addIntentsToList(context, intentList, pickIntent);
            intentList = addIntentsToList(context, intentList, takePhotoIntent);

            if (intentList.size() > 0) {
                chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                        context.getString(R.string.pick_image_intent_text));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            }
        }

        return chooserIntent;
    }

    /**
     * method to return the intent for gallery or camera
     *
     * @param context context of the activity where you want to receive result
     * @return intent of camera or gallery
     */


    @Nullable
    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        Uri tempUri = FileProvider.getUriForFile(
                context.getApplicationContext(),
                context.getApplicationContext().getPackageName() + ".provider",
                getTempFile(context)
        );
        if (tempUri != null) {
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
            intentList = addIntentsToList(context, intentList, pickIntent);
            intentList = addIntentsToList(context, intentList, takePhotoIntent);

            if (intentList.size() > 0) {
                chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                        context.getString(R.string.pick_image_intent_text));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (takePhotoIntent != null) {
                    takePhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            } else {
                ClipData clip =
                        ClipData.newUri(context.getContentResolver(), "whatevs", tempUri);

                if (takePhotoIntent != null) {
                    takePhotoIntent.setClipData(clip);
                }
                if (takePhotoIntent != null) {
                    takePhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            return chooserIntent;
        }
        return null;
    }

    @Nullable
    public static Intent getPickImageIntentsOnly(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Uri tempUri = FileProvider.getUriForFile(
                context.getApplicationContext(),
                context.getApplicationContext().getPackageName() + ".provider",
                getTempFile(context)
        );
        if (tempUri != null) {
            intentList = addIntentsToList(context, intentList, pickIntent);

            if (intentList.size() > 0) {
                chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                        context.getString(R.string.pick_image_intent_text));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            }

            return chooserIntent;
        }
        return null;
    }

    public static Intent getCaptureImageIntentsOnly(Context context) {
        Intent chooserIntent = null;
        List<Intent> intentList = new ArrayList<>();
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        Uri tempUri = FileProvider.getUriForFile(
                context.getApplicationContext(),
                context.getApplicationContext().getPackageName() + ".provider",
                getTempFile(context)
        );
        if (tempUri != null) {
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
            intentList = addIntentsToList(context, intentList, takePhotoIntent);

            if (intentList.size() > 0) {
                chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                        context.getString(R.string.pick_image_intent_text));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (takePhotoIntent != null) {
                    takePhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            } else {
                ClipData clip =
                        ClipData.newUri(context.getContentResolver(), "whatevs", tempUri);

                if (takePhotoIntent != null) {
                    takePhotoIntent.setClipData(clip);
                }
                if (takePhotoIntent != null) {
                    takePhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
            }
            return chooserIntent;
        }
        return null;
    }

    /**
     * method to collect the activities whose intent filter matches the required type
     *
     * @param context context of the activity where you want to receive result
     * @param list    list of the app's which have intent filter which can handle your content
     * @param intent  your Intent
     * @return List of the supported app's (which can handle your intent)
     */
    public static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
            Log.d(TAG, "Intent: " + intent.getAction() + " package: " + packageName);
        }
        return list;
    }

    /**
     * Method for the handling the result which you will get from gallery or camera
     *
     * @param context             context of the activity where you want to receive result
     * @param resultCode          result code which you will use in onActivityResult
     * @param imageReturnedIntent the intent object which will get returned
     * @return Bitmap which will be created using the intent object
     */
    public static Uri getImageFromResult(Context context, int resultCode,
                                                 Intent imageReturnedIntent) {
        Log.d(TAG, "getImageFromResult, resultCode: " + resultCode);
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        if (imageFile == null)
            return null;
            Uri selectedImage = null;
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            if (isCamera) {     /* CAMERA */
                selectedImage = FileProvider.getUriForFile(
                        context.getApplicationContext(),
                        context.getApplicationContext().getPackageName() + ".provider",
                        getTempFile(context)
                );
            } else {            /* ALBUM */
                selectedImage = imageReturnedIntent.getData();
            }
            Log.d(TAG, "selectedImage: " + selectedImage);

            bm = getImageResized(context, selectedImage);
            int rotation = getRotation(context, selectedImage, isCamera);
            if (bm != null) {
                bm = rotate(bm, rotation);
            }
        }


        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.getMessage();

        }


        return selectedImage;
    }

    /**
     * method for creating a temp file which will be stored in our cache
     *
     * @param context context of the activity where you want to receive result
     * @return File object which we will use
     */
    @Nullable
    public static File getTempFile(Context context) {
//        File imageFile = new File(Environment.getExternalStorageDirectory() + File.separator + context.getPackageName(), TEMP_IMAGE_NAME);
        File folder = context.getExternalFilesDir("photos");
        if (folder != null && !folder.exists()) {
            folder.mkdir();
        }
        return new File(folder, TEMP_IMAGE_NAME + ".png");
    }

    /**
     * Method to make a smaller bitmap
     *
     * @param context    context of the activity where you want to receive result
     * @param theUri     Uri of the selected image
     * @param sampleSize sample size(If set to a value > 1, requests the decoder to subsample
     *                   the original image, returning a smaller image to save memory.)
     * @return smaller bitmap
     */
    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;


        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage() == null ? "No file found" : e.getMessage());
        }

        Bitmap actuallyUsableBitmap = null;
        if (fileDescriptor != null) {
            actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.getFileDescriptor(), null, options);
        }

//        Log.d(TAG, options.inSampleSize + " sample method bitmap ... " +
//                actuallyUsableBitmap.getWidth() + " " + actuallyUsableBitmap.getHeight());

        return actuallyUsableBitmap;
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        int size = 0;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            if (bm != null) {
                size = bm.getWidth();
            }
            i++;
        } while (size < minWidthQuality && i < sampleSizes.length);
        return bm;
    }

    /**
     * method to get rotation from image returned from camera or gallery
     *
     * @param context  context of the activity where you want to receive result
     * @param imageUri uri of the image to get the rotation
     * @param isCamera boolean to check whether clicked image is from camera or gallery
     * @return orientation of the image
     */
    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        Log.d(TAG, "Image rotation: " + rotation);
        return rotation;
    }

    /**
     * getting the rotated image using Exif if clicked from camera
     *
     * @param context   context of the activity where you want to receive result
     * @param imageFile uri of the file to be rotated
     * @return
     */
    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            InputStream inputStream = context.getContentResolver().openInputStream(imageFile);
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    /**
     * getting the rotated image using Exif taken from gallery
     *
     * @param context  context of the activity where you want to receive result
     * @param imageUri uri of the file to be rotated
     * @return orientation of the image
     */
    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    /**
     * method to rotate image
     *
     * @param bm       bitmap to be rotated
     * @param rotation rotation angle
     * @return rotated bitmap
     */
    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }


    public static class PICKER_TYPE {
        public static final int PHOTO = 0;
        public static final int VIDEO = 1;
    }
}