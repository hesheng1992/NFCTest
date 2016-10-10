package edu.feicui.nfctest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    NfcAdapter adapter;
    private Uri[] mFileUris = new Uri[10];
    private FileUriCallback fileback;
    private String mParentPath;
    private Intent mIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        initData();
        //NFC操作类。
        adapter=manager.getDefaultAdapter();
        fileback=new FileUriCallback();
        if (adapter!=null&&adapter.isEnabled()){
            Toast.makeText(this, "NFC可用", Toast.LENGTH_SHORT).show();
            adapter.setBeamPushUrisCallback(fileback,this);

        }else {
            Toast.makeText(this, "NFC不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        String transferFile = "transferimage.jpg";
        File extDir = getExternalFilesDir(null);
        File requestFile = new File(extDir, transferFile);
        requestFile.setReadable(true, false);
        Uri uri = Uri.fromFile(requestFile);
        if (uri!=null){
            mFileUris[0]=uri;
        }else {
            Toast.makeText(this, "uri不存在", Toast.LENGTH_SHORT).show();
        }

    }
    private void handleViewIntent() {
        mIntent = getIntent();
        String action = mIntent.getAction();
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent
            Uri beamUri = mIntent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(beamUri.getScheme(), "file")) {
                mParentPath = handleFileUri(beamUri);
            } else if (TextUtils.equals(
                    beamUri.getScheme(), "content")) {
                mParentPath = handleContentUri(beamUri);
            }
        }
    }

    private String handleContentUri(Uri beamUri) {
        int filenameIndex;
        // File object for the filename
        File copiedFile;
        // The filename stored in MediaStore
        String fileName;
        // Test the authority of the URI
        if (!TextUtils.equals(beamUri.getAuthority(), MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             */
            // For a MediaStore content URI
        } else {
            // Get the column that contains the file name
            String[] projection = { MediaStore.MediaColumns.DATA };
            Cursor pathCursor =
                    getContentResolver().query(beamUri, projection,
                            null, null, null);
            // Check for a valid cursor
            if (pathCursor != null &&
                    pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(
                        MediaStore.MediaColumns.DATA);
                // Get the full file name including path
                fileName = pathCursor.getString(filenameIndex);
                // Create a File object for the filename
                copiedFile = new File(fileName);
                // Return the parent directory of the file
                return copiedFile.getParent();
            } else {
                // The query didn't work; return null
                return null;
            }
        }
        return null;
    }

    private String handleFileUri(Uri beamUri) {
        String fileName = beamUri.getPath();
        // Create a File object for this filename
        File copiedFile = new File(fileName);
        // Get a string containing the file's parent directory
        String parent = copiedFile.getParent();
        return parent;
    }


    /*当Android Beam文件传输监测到用户希望与另一个支持NFC的设备发送文件时，
    系统会调用它。在该回调函数中，返回一个Uri对象数组，
    Android Beam文件传输将URI对应的文件拷贝发送给要接收的设备。*/
    private class FileUriCallback implements NfcAdapter.CreateBeamUrisCallback{
        public FileUriCallback() {
        }

        @Override
        public Uri[] createBeamUris(NfcEvent nfcEvent) {
            return mFileUris;
        }
    }
}
