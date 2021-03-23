package cordova.plugin.filepickerv2;

import android.content.Context;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.provider.OpenableColumns;
import android.database.Cursor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;

/**
 * This class echoes a string called from JavaScript.
 */
public class FilePickerV2 extends CordovaPlugin {

    private static final String TAG = "FilePickerV2";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;



    CallbackContext callback;


    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_OPEN)) {
            String mime = inputs.getString(0);
            open(mime, callbackContext);
            return true;
        }
        return false;
    }

    private void open(String mime, CallbackContext callbackContext) {

        Intent intent = new Intent()
        .setType("*/*")
        .setAction(Intent.ACTION_GET_CONTENT);
        String[] mimetypes = {"application/pdf", "application/zip", "application/msword"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        cordova.startActivityForResult(this, Intent.createChooser(intent, "Selecione..."), PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && callback != null) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = null;
                    try {
                        path =   getFile(this.cordova.getActivity().getApplicationContext(), uri).getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (path != null) {
                        callback.success(path);
                    } else {
                        callback.error("File path was null");
                    }
                } else {
                    callback.error("File uri was null");
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                callback.error("User canceled.");
            } else {

                callback.error(resultCode);
            }
        }
    }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static File getFile(Context context, Uri uri) throws IOException {
        File destinationFilename = new File(context.getFilesDir().getPath() + File.separatorChar + queryName(context, uri));
        try (InputStream ins = context.getContentResolver().openInputStream(uri)) {
            createFileFromStream(ins, destinationFilename);
        } catch (Exception ex) {
         //   Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
        return destinationFilename;
    }

    // @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void createFileFromStream(InputStream ins, File destination) {
        try (OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = ins.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (Exception ex) {
        //    Log.e("Save File", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


}
