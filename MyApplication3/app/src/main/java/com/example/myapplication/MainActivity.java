package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    Button save_btn;
    Button saveas_btn;
    Button load_btn;
    Button tts_btn;
    EditText content_txt;
    String path;
    static Uri saveURI;
    static Uri newURI;


    private static final int CODE_READ = Intent.FLAG_GRANT_READ_URI_PERMISSION;
    private static final int CODE_WRITE = Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts_btn = findViewById(R.id.tts_btn);
        save_btn = findViewById(R.id.save_btn);
        saveas_btn = findViewById(R.id.saveas_btn);
        load_btn = findViewById(R.id.load_btn);
        content_txt = findViewById(R.id.content_txt);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status)
            {
                if(status != TextToSpeech.ERROR)
                {
                    tts.setLanguage(Locale.US);
                }
            }
        });
        path = getExternalFilesDir(null).toString() + "/NewFile.txt";

        if(saveURI == null) {
            saveURI = Uri.parse(path);
            newURI = saveURI;
        }

    }
/* Depricated, before I swapped to Android's File framework...

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.OutputStreamWriter;
    public void save(String fullPath)
    {
        // save to fullPath...
        File writer = new File(fullPath);

        if(!(writer.exists()))
        {
            try {
                writer.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(writer)) {
            try {
                fileOut.write(content_txt.getText().toString().getBytes());
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return;
    }
*/
    public void onClick(android.view.View vw)
    {
        if(vw == save_btn)
        {
            // use currentFileUri to save the new contents of the file back
            // to the current file
            final ContentResolver cr = getContentResolver();
            cr.takePersistableUriPermission(saveURI, CODE_WRITE);
            OutputStream outFile = null;
            try {
                outFile = cr.openOutputStream(saveURI);
                outFile.write(content_txt.getText().toString().getBytes());
                outFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(vw == saveas_btn) {
            Intent fileBrowse = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            fileBrowse.addCategory(Intent.CATEGORY_OPENABLE);
            fileBrowse.setType("text/*");
            startActivityForResult(fileBrowse, CODE_WRITE);
        }
        else if(vw == load_btn)
        {

            /* Open file Dialogue using android's framework method */
            Intent fileBrowse = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            fileBrowse.addCategory(Intent.CATEGORY_OPENABLE);
            fileBrowse.setType("text/*"); // all text type mime (includes cpp and java source, if not enough see next line for further options)
            // fileBrowse.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "text/plain", "application/java-vm"}); // Look up mime types on google for more */
            startActivityForResult(fileBrowse, CODE_READ);
        }
        else if(vw == tts_btn)
        {
            String textToSay = content_txt.getText().toString();
            tts.speak(textToSay, TextToSpeech.QUEUE_FLUSH, null);
        }

        return;
    }

    public String readStreamToText(InputStream inFile) throws UnsupportedEncodingException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        StringBuilder contents = new StringBuilder();

        while (true) {
            try {
                if (!((length = inFile.read(buffer)) != -1)) break;

                result.write(buffer, 0, length);
                contents.append(result.toString("UTF-8"));
                result.reset();
            } catch (IOException e) {
                e.printStackTrace();
                content_txt.setText("Failed file Load: " + e.toString());
                break;
                // return "Failed to read File";
            }
        }
        return contents.toString();
    }

    public void saveStringToStream(OutputStream temp)
    {

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        /* Needed to override this to do nothing,
            otherwise it wanted to exit to desktop
            and when coming back the entire contents
            would be cleared.

            There must be an onResume() opr something similar that
            I can use instead, but for now I will leave blank.
         */
    }

    // use the following to get results back from launched activities that used startActivityForResult
    // It's a callback function that we need to override.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final ContentResolver cr = getContentResolver();
        if(data != null)
        {
            if (resultCode == RESULT_OK)
            {
                final Uri uri = data.getData();
                saveURI = uri;
                if(requestCode == CODE_READ)
                {
                    // read File from cr.openInputStream(uri);
                    // close file
                    cr.takePersistableUriPermission(uri, CODE_READ);
                    InputStream ifile = null;
                    try {
                        ifile = cr.openInputStream(uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        content_txt.setText(readStreamToText(ifile));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            ifile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // write to file...
                if(requestCode == CODE_WRITE)
               {
                    cr.takePersistableUriPermission(uri, CODE_WRITE);
                    OutputStream outFile = null;
                    try {
                        outFile = cr.openOutputStream(uri);
                        outFile.write(content_txt.getText().toString().getBytes());
                        outFile.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

}

