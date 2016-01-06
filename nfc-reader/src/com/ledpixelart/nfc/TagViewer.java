/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2011 Adam Nybäck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ledpixelart.nfc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import alt.android.os.CountDownTimer;
import android.view.*;

import com.ledpixelart.nfc.record.ParsedNdefRecord;
import com.ledpixelart.nfc.record.TextRecord;
import com.ledpixelart.pixel.hardware.Pixel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device just discovered.
 */
public class TagViewer extends IOIOActivity {

    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private LinearLayout mTagContent;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    private AlertDialog mDialog;
    private String TagPayloadString;
    
    //******* Al added code ********
    
    private TextView textView_;
	//private TextView scrollSpeedtextView_;	
	private SeekBar scrollSpeedSeekBar_;	
	private SeekBar fontSizeSeekBar_;	
	//private ToggleButton toggleButton_;	
	private static EditText textField;	
	private SharedPreferences prefs;
	private SharedPreferences savePrefs;
	private Editor mEditor;
	private Resources resources;
	private String app_ver;	
	private int matrix_model;
	private final String tag = "";	
	private final String LOG_TAG = "PixelText";
	private static int resizedFlag = 0;
	private ConnectTimer connectTimer; 	
	private static ScrollingTextTimer scrollingtextTimer_;
  	private static boolean deviceFound = false;
  	private boolean noSleep = false;	
	private int countdownCounter;
	private static final int countdownDuration = 30;
	private int scrollSpeed = 1;
    
    private static ioio.lib.api.RgbLedMatrix matrix_;
	private static ioio.lib.api.RgbLedMatrix.Matrix KIND;  //have to do it this way because there is a matrix library conflict
	private static android.graphics.Matrix matrix2;
	private static short[] frame_;
  	public static final Bitmap.Config FAST_BITMAP_CONFIG = Bitmap.Config.RGB_565;
  	private static byte[] BitmapBytes;
  	private static InputStream BitmapInputStream;
  	private static Bitmap canvasBitmap;
  	private static Bitmap IOIOBitmap;
  	private static Bitmap originalImage;
  	private static int width_original;
  	private static int height_original; 	  
  	private static float scaleWidth; 
  	private static float scaleHeight; 	  	
  	private static Bitmap resizedBitmap; 
	private Canvas canvas;
	private static Canvas canvasIOIO;
	
	private String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    private String basepath = extStorageDirectory;
    private static Context context;
    private Context frameContext;
	private boolean debug_;
	private static int appAlreadyStarted = 0;
	//private int scrollSpeedProgress = 1;
	private static int scrollSpeedValue = 50;
	private int fontSizeValue = 26;
	
	private Typeface selectedFont;
	private String fontlist[];
	private int prefFontPosition;
	public long frame_length;
	private static int currentResolution;
	private static String pixelFirmware = "Not Connected";
	private static String pixelBootloader = "Not Connected";
	private static String pixelHardwareID = "Not Connected";
	private static String IOIOLibVersion = "Not Connected";
	private static VersionType v;
    private volatile static Timer timer;
    private static Pixel pixel;
    //private RgbLedMatrix ledMatrix;
    private static int scrollingKeyFrames_ = 1;
	private static final int REQUEST_PAIR_DEVICE = 10;
	private  ProgressDialog progress;
	private int yCenter;  //TO DO this center doesn't work all the time, add a way for the user to override up or down
	private static final int WENT_TO_PREFERENCES = 1;
	private int prefYoffset_;
	private int yOffset = 0;
	//private String prefScrollSpeed_;
	private int fontSizeStepper = 8;
	
	private static int ColorWheel;
	private static Paint paint;
	private Typeface tf;
	private static String scrollingText; //used for scrolling text
	private static Rect bounds;
	private static int resetX;
	private static int messageWidth;
	private static int x  = 0;	
	private int stepSize = 6;
	private String prefFontSize;
	private int prefColor;
	private String prefScrollingText;
	private int color_;
	private int scrollingSpeed_;
	
	private boolean AutoSelectPanel_ = false;
	
	//*****************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_viewer);
        mTagContent = (LinearLayout) findViewById(R.id.list);
        resolveIntent(getIntent());

        mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }

        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[] { newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true) });
        
        bounds = new Rect();
        paint = new Paint();
        
        ///********** Al added code *******************
        connectTimer = new ConnectTimer(30000,5000); //pop up a message if it's not connected by this timer
 		connectTimer.start(); //this timer will pop up a message box if the device is not found
 		
 		 //******** preferences code
        resources = this.getResources();
        setPreferences();
        //***************************
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (noSleep == true) {        	      	
        	this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //disables sleep mode
        }	
        
       //***************these are not used right now
        savePrefs = getSharedPreferences("appSave", MODE_PRIVATE);
       prefFontSize = savePrefs.getString("fontKey", "14");
       prefYoffset_ = prefs.getInt("prefYoffset", KIND.height/2); //default to in the middle
        
        prefYoffset_ = savePrefs.getInt("prefYoffset", KIND.height/2); //default to in the middle
        yOffset = prefYoffset_ - KIND.height/2; //16 - 32/2 = 0 or 20 - 16 = 4
        
        //prefScrollSpeed_ = savePrefs.getString("prefScrollSpeed", "1");
        //prefScrollingText = savePrefs.getString("scrollingTextKey","TYPE TEXT HERE");
        //prefColor = savePrefs.getInt("colorKey", 333333);
        prefFontPosition = savePrefs.getInt("fontPositionKey", 0);
        //*****************
        
      
        
        context = getApplicationContext();
        enableUi(true);
        
        
        
    	//TO DO fix later
        //if (prefColor != 333333) {   //let's set the last color from prefs
    	//	ColorWheel = prefColor;
    	//	paint.setColor(prefColor); 
    	//}
    	//else {
    		//ColorWheel = Color.GREEN;
        	//paint.setColor(ColorWheel);
    	//}
        
        
    }

    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        
    	byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter != null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }
            mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
        }
        
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updatePrefs();
        //setPreferences();
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.nfc_disabled);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.create().show();
        return;
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                
               
             //**** Al's code here *********************   
                
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                byte[] array = null;
                array = msg.getRecords()[0].getPayload();
                
                TagPayloadString = "";
                
                try {
                	TagPayloadString = new String(array, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // for UTF-8 encoding
                
               //showToast(TagPayloadString); //the payload is the language code + string so in our case "enValue" where en is english and Value is what we want
               //so now let's take out the en part
                
                String[] separated = TagPayloadString.split("en");
                TagPayloadString = separated[1]; 
                //showToast(TagPayloadString);
                
                scrollText(TagPayloadString, false); //false means don't write, just stream the text to the Smart LED
             
               //****************************************************
               
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
            // Setup the views
            buildTagViews(msgs);
            
        }
    }

    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                case MifareClassic.TYPE_CLASSIC:
                    type = "Classic";
                    break;
                case MifareClassic.TYPE_PLUS:
                    type = "Plus";
                    break;
                case MifareClassic.TYPE_PRO:
                    type = "Pro";
                    break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                case MifareUltralight.TYPE_ULTRALIGHT:
                    type = "Ultralight";
                    break;
                case MifareUltralight.TYPE_ULTRALIGHT_C:
                    type = "Ultralight C";
                    break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout content = mTagContent;

        
        //NdefRecord[] recs = msgs[0].getRecords();
       
        
        // Parse the first message in the list
        // Build views for all of the sub records
        Date now = new Date();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
    
        final int size = records.size();
        for (int i = 0; i < size; i++) {
           
        	TextView timeView = new TextView(this);
            timeView.setText(TIME_FORMAT.format(now));
            content.addView(timeView, 0);
            ParsedNdefRecord record = records.get(i);
            content.addView(record.getView(this, inflater, content, i), 1 + i);
            content.addView(inflater.inflate(R.layout.tag_divider, content, false), 2 + i);
        }
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	   if (item.getItemId() == R.id.menu_instructions) {
    	    	AlertDialog.Builder alert=new AlertDialog.Builder(this);
    	      	alert.setTitle(getResources().getString(R.string.setupInstructionsStringTitle)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.setupInstructionsString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();
    	   }
       	
   	  
         if (item.getItemId() == R.id.menu_btPair)
         {
   			
   		if (pixelHardwareID.substring(0,4).equals("MINT")) { //then it's a PIXEL V1 unit
   			showToast("Bluetooth Pair to PIXEL using code: 4545");
   		}
   		else { //we have a PIXEL V2 unit
   			showToast("Bluetooth Pair to PIXEL using code: 0000");
   		}
   		
   	  Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
         startActivityForResult(intent, REQUEST_PAIR_DEVICE);
          
         }
         
         if (item.getItemId() == R.id.menu_about) {
   		  
   		    AlertDialog.Builder alert=new AlertDialog.Builder(this);
   	      	alert.setTitle(getString(R.string.menu_about_title)).setIcon(R.drawable.icon).setMessage(getString(R.string.menu_about_summary) + "\n\n" + getString(R.string.versionString) + " " + app_ver + "\n"
   	      			+ getString(R.string.FirmwareVersionString) + " " + pixelFirmware + "\n"
   	      			+ getString(R.string.HardwareVersionString) + " " + pixelHardwareID + "\n"
   	      			+ getString(R.string.BootloaderVersionString) + " " + pixelBootloader + "\n"
   	      			+ getString(R.string.LibraryVersionString) + " " + IOIOLibVersion).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
   	   }
       	
       	if (item.getItemId() == R.id.menu_prefs)
          {
       		
       		appAlreadyStarted = 0;    		
       		Intent intent = new Intent()
          				.setClass(this,
          						preferences.class);   
       				//this.startActivityForResult(intent, 0);
       				this.startActivity(intent);
          }
       	
       	if (item.getItemId() == R.id.menu_main_clear)
        {
     		
       		menuMainClearClick();
        }
    	
        return true;
    	
    	
    	/*// Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_main_clear:
              menuMainClearClick();
            default:
                return super.onOptionsItemSelected(item);
        }*/
    }

    private void menuMainClearClick() {
        for (int i = mTagContent.getChildCount() -1; i >= 0 ; i--) {
            View view = mTagContent.getChildAt(i);
            if (view.getId() != R.id.tag_viewer_text) {
                mTagContent.removeViewAt(i);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
    
    public void showToast(final String msg) {
	 		runOnUiThread(new Runnable() {
	 			@Override
	 			public void run() {
	 				Toast toast = Toast.makeText(TagViewer.this, msg, Toast.LENGTH_LONG);
	                 toast.show();
	 			}
	 		});
	 } 
    
    //******* Al Added Code *******
    
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) //we'll go into a reset after this
    {
    	super.onActivityResult(reqCode, resCode, data);    	
    	setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
    
    	if (resCode == WENT_TO_PREFERENCES)  {
    		setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
    		//showToast("returned from preferences");
    	}	
    	
    } 
    
    private void setPreferences() //here is where we read the shared preferences into variables
    {
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);   
     
     noSleep = prefs.getBoolean("pref_noSleep", false);
     debug_ = prefs.getBoolean("pref_debugMode", false);
     
     scrollingKeyFrames_ = Integer.valueOf(prefs.getString(   //how smooth the scrolling, essentially the keyframes
 	        resources.getString(R.string.scrollingKeyFrames),
 	        resources.getString(R.string.scrollingKeyFramesDefault))); 
     
     matrix_model = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
    	        resources.getString(R.string.selected_matrix),
    	        resources.getString(R.string.matrix_default_value))); 
     
     color_ = Integer.valueOf(prefs.getString(   
 	        resources.getString(R.string.selected_color),
 	        resources.getString(R.string.color_default_value))); 
     
     scrollingSpeed_ = Integer.valueOf(prefs.getString(   
  	        resources.getString(R.string.scrollSpeed),
  	        resources.getString(R.string.scrollSpeed_default_value))); 
     
     switch (scrollingSpeed_) {  //get this from the preferences
     case 0:
    	 scrollSpeedValue = 100;
    	 break;
     case 1:
    	 scrollSpeedValue = 50;
    	 break;
     case 2:
    	 scrollSpeedValue = 1;
    	 break;
     default:	    		 
    	 scrollSpeedValue = 50;
     }
     
     switch (color_) {  //get this from the preferences
     case 0:
    	 ColorWheel = Color.GREEN;
    	 break;
     case 1:
    	 ColorWheel = Color.BLUE;
    	 break;
     case 2:
    	 ColorWheel = Color.RED;
    	 break;
     case 3:
    	 ColorWheel = Color.YELLOW;
    	 break;
     case 4:
    	 ColorWheel = Color.MAGENTA;
    	 break;
     case 5:
    	 ColorWheel = Color.CYAN;
    	 break;	 
     case 6:
    	 ColorWheel = Color.GRAY; 
    	 break;	 	 
     case 7: //this one doesn't work and we don't use it rigth now
    	 ColorWheel = Color.DKGRAY;
    	 break;
     default:	    		 
    	 ColorWheel = Color.GREEN;
     }
     	
     paint.setColor(ColorWheel);
 	
     
     
    /* twitterSearchString = prefs.getString(   
 	        resources.getString(R.string.pref_twitterSearchString),
 	        resources.getString(R.string.twitterSearchStringDefault)); 
     
     twitterMode = prefs.getBoolean("pref_twitterMode", false);
     
     filterTweets = prefs.getBoolean("pref_twitterFilter", true);
     
     twitterInterval = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
 	        resources.getString(R.string.twitterRefresh),
 	        resources.getString(R.string.twitterRefreshDefault))); */
     
   
     
     if (AutoSelectPanel_ && pixelHardwareID.substring(0,4).equals("PIXL") && !pixelHardwareID.substring(4,5).equals("0")) { // PIXL0008 or PIXL0009 is the normal so if it's just a 0 for the 5th character, then we don't go here
	    	
 	 	//let's first check if we have a matching firmware to auto-select and if not, we'll just go what the matrix from preferences
	  
	  		if (pixelHardwareID.substring(4,5).equals("Q")) {
 	 		matrix_model = 11;
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
		    	BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	frame_length = 2048;
		    	currentResolution = 32; 
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("T")) {
 	 		matrix_model = 14;
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_64x64;
		    	BitmapInputStream = getResources().openRawResource(R.raw.select64by64);
		    	frame_length = 8192;
		    	currentResolution = 128; 
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("I")) {
 	 		matrix_model = 1; 
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
		    	BitmapInputStream = getResources().openRawResource(R.raw.selectimage16);
		    	frame_length = 1024;
		    	currentResolution = 16;
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("L")) { //low power
 	 		matrix_model = 1; 
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
		    	BitmapInputStream = getResources().openRawResource(R.raw.selectimage16);
		    	frame_length = 1024;
		    	currentResolution = 16;
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("C")) {
 	 		matrix_model = 12; 
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32_ColorSwap;
		    	BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	frame_length = 2048;
		    	currentResolution = 32; 
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("R")) {
 	 		matrix_model = 13; 
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_64x32;
		    	BitmapInputStream = getResources().openRawResource(R.raw.select64by32);
		    	frame_length = 4096;
		    	currentResolution = 64; 
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("M")) { //low power
 	 		 matrix_model = 3;
 	 		 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //pixel v2
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32;
 	 	}
 	 	else if (pixelHardwareID.substring(4,5).equals("N")) { //low power
 	 		 matrix_model = 11;
 	 		 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32; //pixel v2.5
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32; 
 	 	}
 	 	else {  //in theory, we should never go here
 	 		KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
		    	BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	frame_length = 2048;
		    	currentResolution = 32; 
 	 	}
		}	

    else {
	     switch (matrix_model) {  //get this from the preferences
		     case 0:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x16;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage16);
		    	 frame_length = 1024;
		    	 currentResolution = 16;
		    	 break;
		     case 1:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x16;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage16);
		    	 frame_length = 1024;
		    	 currentResolution = 16;
		    	 break;
		     case 2:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32_NEW; //v1, this matrix was never used
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32;
		    	 break;
		     case 3:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32;
		    	 break;
		     case 4:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_64x32; 
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select64by32);
		    	 frame_length = 8192;
		    	 currentResolution = 64; 
		    	 break;
		     case 5:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x64; 
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select32by64);
		    	 frame_length = 8192;
		    	 currentResolution = 64; 
		    	 break;	 
		     case 6:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_2_MIRRORED; 
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select32by64);
		    	 frame_length = 8192;
		    	 currentResolution = 64; 
		    	 break;	 	 
		     case 7: //this one doesn't work and we don't use it rigth now
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_4_MIRRORED;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select32by64);
		    	 frame_length = 8192; //original 8192
		    	 currentResolution = 128; //original 128
		    	 break;
		     case 8:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_128x32; //horizontal
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select128by32);
		    	 frame_length = 8192;
		    	 currentResolution = 128;  
		    	 break;	 
		     case 9:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x128; //vertical mount
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select32by128);
		    	 frame_length = 8192;
		    	 currentResolution = 128; 
		    	 break;	 
		     case 10:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_64x64;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select64by64);
		    	 frame_length = 8192;
		    	 currentResolution = 128; 
		    	 break;
		     case 11:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32; 
		    	 break;	 
		     case 12:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x32_ColorSwap;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32; 
		    	 break;	 	 
		     case 13:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_64x32;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select64by32);
		    	 frame_length = 4096;
		    	 currentResolution = 64; 
		    	 break;	
		     case 14:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_64x64;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select64by64);
		    	 frame_length = 8192;
		    	 currentResolution = 128; 
		    	 break;
		     case 15:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_128x32;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select128by32);
		    	 frame_length = 8192;
		    	 currentResolution = 128; 
		    	 break;	 	 	
		     case 16:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_32x128;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.select32by128);
		    	 frame_length = 8192;
		    	 currentResolution = 128; 
		    	 break;	 
		     case 17:
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.ADAFRUIT_64x16;
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32; 
		    	 break;	 	 	
		     default:	    		 
		    	 KIND = ioio.lib.api.RgbLedMatrix.Matrix.SEEEDSTUDIO_32x32; //v2 as the default
		    	 BitmapInputStream = getResources().openRawResource(R.raw.selectimage32);
		    	 frame_length = 2048;
		    	 currentResolution = 32;
		     }
 	 }
      
     frame_ = new short [KIND.width * KIND.height];
	 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
	 
	 loadRGB565(); //load the select pic raw565 file
 }
    
    private void updatePrefs() {
    	setPreferences();
    	//stopTimers(); //only need this if using Twitter feeds
    	
    	//now we need to start twitter and scrolling text timers here
    	
    	/*if (scrollingtextTimer_ != null) {
    		 	resetScrolling();
    	}*/
    	
    	/*if (isNetworkAvailable()) {
	    	if (twitterMode) {
		    	twitterTimer_ = new twitterTimer (100000,twitterInterval*1000);  //the prefs is in seconds so we'll need to convert to milliseconds
		    	twitterTimer_.start();
	    	}
    	}	*/
    }
    
 private void resetScrolling() {
    	
    	if (scrollingtextTimer_ != null) {
    		scrollingtextTimer_.cancel();
    	}
    	
    	
    	 paint.setColor(ColorWheel); //let's get the color the user has specified from the color wheel widget
         //TO DO fix later
    	 //scrollingText = textField.getText().toString(); //let's get the text the user has mentioned
    	 scrollingText = "Fix Later";
     	 paint.getTextBounds(scrollingText, 0, scrollingText.length(), bounds);
     	 yCenter = (KIND.height / 2) + ((bounds.height())/2 + yOffset);
     	 messageWidth = bounds.width(); 
        // showToast(Integer.toString(messageWidth));
      	
       if (messageWidth < KIND.width) { //then it means we don't need to scroll
       	   
    	   
    	   x = 0;
    	   try {
					pixel.writeMessageToPixel(x, scrollingText, paint, yCenter);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //let's write the text
          }
       
       else {
	    	 //x = 0; //having this works one scrolling sequence but then the next one doesn't come
	       		x=KIND.width *2 ; //like this so the scrolling start at the edge
	       		//scrollingtextTimer_.cancel();
	       		paint.setColor(ColorWheel); //let's get the color the user has specified from the color wheel widget
	            //To Do Fix Later
	       		//scrollingText = textField.getText().toString(); //let's get the text the user has mentioned
	       		scrollingText = "Fix Later";
	            paint.getTextBounds(scrollingText, 0, scrollingText.length(), bounds);
	       		yCenter = (KIND.height / 2) + ((bounds.height())/2 + yOffset);
	    	    scrollingtextTimer_ = new ScrollingTextTimer (100000,scrollSpeedValue);
	    	    //scrollingtextTimer_ = new ScrollingTextTimer (100000,scrollingSpeed_);
		 		scrollingtextTimer_.start();
       }
    	
    	
    	//scrollingtextTimer_ = new ScrollingTextTimer (100000,scrollSpeedValue);  //scrollSpeedValue was changed, we hard code at 100
 		//scrollingtextTimer_.start();
    }
    
    
    private static void loadRGB565() {
	 	   
		try {
   			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
   																				// the
   																				// input
   																				// stream
   																				// into
   																				// a
   																				// byte
   																				// array
   			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
   		} catch (IOException e) {
   			e.printStackTrace();
   		}

   		int y = 0;
   		for (int i = 0; i < frame_.length; i++) {
   			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
   			y = y + 2;
   		}
   }
    
    private void scrollText(final String msg, boolean writeMode) 
	 
    {
	   if(scrollingtextTimer_ != null)
		   scrollingtextTimer_.cancel();
	
	 // stopExistingTimer(); //
		
		if (pixelHardwareID.substring(0,4).equals("PIXL") && writeMode == true) {  //in write mode, we don't need a timer because we don't need a delay in between frames, we will first put PIXEL in write mode and then send all frames at once
				
			//TO DO this appliation will not write so we don't need this code, clean up later
			
				/*pixel.interactiveMode();
				float textFPS = 1000.f / scrollSpeedValue;  //TO DO need to do the math so the scrollig speed is right, need to change this formula
			
				pixel.writeMode(textFPS);
					
				stopTimers(); //stop the twitter timer if it's running	
				
				writePixelAsync loadApplication = new writePixelAsync();
    			loadApplication.execute();*/
		}
		else {   //we're not writing so let's just stream
      
		   if (scrollingtextTimer_ != null)
				   scrollingtextTimer_.cancel();
         	 		
					runOnUiThread(new Runnable() 
					{
						public void run() 
						{	
		 	            	 
		 	            	paint.setColor(ColorWheel); //let's get the color the user has specified from the color wheel widget
		 	                //scrollingText = textField.getText().toString(); //let's get the text the user has mentioned
		 	                scrollingText = "Welcome " + msg; //let's get the text the user has mentioned
		 	                
		 	                
		 	                paint.getTextBounds(scrollingText, 0, scrollingText.length(), bounds);
		 	            	yCenter = (KIND.height / 2) + ((bounds.height())/2 + yOffset);
		 	            	messageWidth = bounds.width(); 
		 	  	            //showToast(Integer.toString(messageWidth));
		 	            	 
		 	  	            if (messageWidth < KIND.width) { //then it means we don't need to scroll 
		 	  	            
			 	  	            x =0;
		 	  	            	try {
			 	 						pixel.writeMessageToPixel(x, scrollingText, paint, yCenter);
			 	 					} catch (ConnectionLostException e) {
			 	 						// TODO Auto-generated catch block
			 	 						e.printStackTrace();
			 	 					} //let's write the text
		 	 	           }
		 	             
			 	             else {
								scrollingtextTimer_ = new ScrollingTextTimer (100000,scrollSpeedValue);
						 		scrollingtextTimer_.start();
			 	             }
		 	             
		 	             
						}
					});
		    	}
    }
    
 private class IOIOThread extends BaseIOIOLooper {
			
		
		@Override
		public void setup() throws ConnectionLostException 
		{
			try 
			{
				
				
				deviceFound = true; //set this flag so the pop up doesn't come
				
				//**** let's get IOIO version info for the About Screen ****
	  			pixelFirmware = ioio_.getImplVersion(v.APP_FIRMWARE_VER);
	  			pixelBootloader = ioio_.getImplVersion(v.BOOTLOADER_VER);
	  			pixelHardwareID = ioio_.getImplVersion(v.HARDWARE_VER); 
	  			IOIOLibVersion = ioio_.getImplVersion(v.IOIOLIB_VER);
	  			//**********************************************************
				
	  			//if (!pixelHardwareID.substring(0,4).equals("PIXL"))  //don't show the write button if it's not a PIXEL V2 board
	  			  //  hideWriteButton(); //have to do this as runnable or we'll get a crash
	  			
	  				 if (AutoSelectPanel_ && pixelHardwareID.substring(0,4).equals("PIXL") && !pixelHardwareID.substring(4,5).equals("0")) { //only go here if we have a firmware that is set to auto-detect, otherwise we can skip this
	 	  			runOnUiThread(new Runnable() 
	 	  			{
	 	  			   public void run() 
	 	  			   {
	 	  				  
	 	  				  // updatePrefs();
	 	  				  
	 	  				   try { //had to add here or was crashing
							matrix_ = ioio_.openRgbLedMatrix(KIND);
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	 	  				   
	 	  				   pixel = new Pixel(matrix_, KIND);
	 	  				   try {
							matrix_.frame(frame_);
						} catch (ConnectionLostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //stream "select image" text to PIXEL
	 	  			   }
	 	  			}); 
	   			}
	   		   
	   		   else { //we didn't auto-detect so just go the normal way
	   			  
	   			   try { //had to add here or was crashing
						matrix_ = ioio_.openRgbLedMatrix(KIND);
					} catch (ConnectionLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	   			   
	   			  //matrix_ = ioio_.openRgbLedMatrix(KIND);
	   			  pixel = new Pixel(matrix_, KIND);
	   			  matrix_.frame(frame_);
	   		   }
	  			
				System.out.println("PIXEL found, Hardware ID: " + pixelHardwareID);
				
				enableUi(true);
				//no need to scroll anything here as in this app, we won't trigger scrolling until an NFC is tagged, before we had a twitter text
				//scrollText(false); //start scrolling text, false means we stream and not write. User can write if they press write buttonl start scrolling when the app starts
			
			} 
			catch (ConnectionLostException e) 
			{
				enableUi(false);
				throw e;
			}
		}
	}

	@Override
  	protected IOIOLooper createIOIOLooper() {
  		return new IOIOThread();
  	}

	private void enableUi(final boolean enable) 
	{
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				//scrollSpeedSeekBar_.setEnabled(enable);
				//writeButton_.setEnabled(enable);
			}
		});
	}
	
	

    public class ConnectTimer extends CountDownTimer
    	{

    		public ConnectTimer(long startTime, long interval)
    			{
    				super(startTime, interval);
    			}

    		@Override
    		public void onFinish()
    			{
    				if (!deviceFound) {
    					showNotFound(); 					
    				}
    				
    			}

    		@Override
    		public void onTick(long millisUntilFinished)				{
    			//not used
    		}
    	}
     
     public class ScrollingTextTimer extends CountDownTimer
     	{

     		public ScrollingTextTimer(long startTime, long interval)
     			{
     				super(startTime, interval); 
     				
     			}

     		@Override
     		public void onFinish()
     			{
     			scrollingtextTimer_.start(); //restart the timer to keep is going
     				
     			}

     		@Override
     		public void onTick(long millisUntilFinished)  {
    	            	
            	
                try {
    				pixel.writeMessageToPixel(x, scrollingText, paint, yCenter);
    			} catch (ConnectionLostException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} //let's write the text
    	                        
    	            messageWidth = bounds.width(); 
    	            //showToast(Integer.toString(messageWidth));
    	            
    	           /* System.out.println("resetX is:" + " " + resetX);
    	            System.out.println("x is:" + " " + x);*/
    	            
    	            resetX = 0 - messageWidth;
    	            
    	            if(x < resetX)
    	            {
    	                x = KIND.width *2;
    	            }
    	            else
    	            {
    	                //TO DO fix this later , need to add scroll bar or a prefs for it
    	                //x = x - (scrollSpeedSeekBar_.getProgress() + 1);
    	            	x = x - (1 + 1);
    	            }
     		}
     	}
     
	     private void showNotFound() {	
	 		AlertDialog.Builder alert=new AlertDialog.Builder(this);
	 		alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
	     }
	 	
	 	private void setText(final String str) 
	 	{
	 		runOnUiThread(new Runnable() 
	 		{
	 			public void run() 
	 			{
	 				textView_.setText(str);
	 			}
	 		});
	 	}
     
     
     
     //********************
    
 

    
    
    
    
    
}

