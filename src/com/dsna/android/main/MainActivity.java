/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package com.dsna.android.main;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;

import rice.Continuation;
import rice.p2p.past.PastContent;

import com.dsna.android.database.DatabaseHandler;
import com.dsna.android.main.AndroidSocialService;
import com.dsna.contact.AddingContactFragment;
import com.dsna.crypto.asn1.exception.InvalidCertificateException;
import com.dsna.crypto.asn1.exception.UnsupportedFormatException;
import com.dsna.crypto.ibbe.cd07.IBBECD07;
import com.dsna.crypto.ibbe.cd07.params.CD07DecryptionParameters;
import com.dsna.crypto.ibbe.cd07.params.CD07SecretKeyParameters;
import com.dsna.crypto.signature.ps06.PS06;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.BaseEntity;
import com.dsna.entity.Location;
import com.dsna.entity.Message;
import com.dsna.entity.Notification;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;
import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.entity.encrypted.KeyHeader;
import com.dsna.entity.encrypted.KeyInfo;
import com.dsna.message.ConversationElement;
import com.dsna.message.MessageFragment;
import com.dsna.message.bubblechat.ConversationFragment;
import com.dsna.message.bubblechat.OneMessage;
import com.dsna.service.IdBasedSecureSocialEventListener;
import com.dsna.service.IdBasedSecureSocialService;
import com.dsna.status.PostStatusFragment;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.storage.cloud.GoogleCloudStorageServiceImpl;
import com.dsna.util.ASN1Util;
import com.dsna.util.FileUtil;
import com.dsna.util.NetworkUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dsna.android.main.R;

import it.gmariotti.cardslib.demo.fragment.BaseFragment;
import it.gmariotti.cardslib.demo.fragment.NewFeedsFragment;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.util.Arrays;

public class MainActivity extends Activity implements ConnectionCallbacks, 
		OnConnectionFailedListener {
		
		static final int REQUEST_AUTHORIZATION = 0;
		public static final String publicKeyURL = "https://130.237.20.200:8080/DSNA_privatekeygenerator/SystemPublic.txt";
		public static final String secretKeyURL = "https://130.237.20.200:8080/DSNA_privatekeygenerator/KeyExtract.jsp";
		public static final String idParams = "clientid";
	
    private ListView mDrawerList;
    private DrawerLayout mDrawer;
    private CustomActionBarDrawerToggle mDrawerToggle;
    private int mCurrentTitle=R.string.app_name;
    private int mSelectedFragment;
    private BaseFragment mBaseFragment;

  	/**
  	 * String parameter name to passing params in init intent
  	 */    
    static final String bIp = "BOOTIP";
    static final String bPort = "BOOTPORT";
    static final String biPort = "BINDPORT";
    static final String uName = "USERNAME";
    
  	/**
  	 * Booting information for android social service
  	 */    
    private String mBootIp;
    private String mBootPort;
    private String mBindPort;
    private String mUsername;
    
  	/**
  	 * The social service which run on background maintain p2p connection
  	 */
  	AndroidSocialService service;
  	
  	/**
  	 * Database helper to store consistent data
  	 */
  	DatabaseHandler dbHelper;
  	
  	/**
  	 * Database helper to store consistent data
  	 */
  	StringBuilder logBatteryAndProcessResult = new StringBuilder();
    
    protected ActionMode mActionMode;
    
    /*
     * Cipher parameter engine and id
     */
    private CipherParameters[] publicKeys;
    private CipherParameters[] secretKeys;
    private PS06 ps06;
		private IBBECD07 cd07;

    private static String TAG= "MainActivity";
    private ArrayList<Status> feeds = new ArrayList<Status>();
    
    //Used in savedInstanceState
    private static String BUNDLE_SELECTEDFRAGMENT = "BDL_SELFRG";
    private static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 1;
    
    private static final int CASE_ACCINFO = 0;
    private static final int CASE_NEWFEEDS = 1;
    private static final int CASE_MESSAGE = 2;
    private static final int CASE_CONTACT = 3;
    private static final int CASE_POSTSTATUS = 4;
/*    private static final int CASE_HEADER = 0;
    private static final int CASE_SHADOW = 1;
    private static final int CASE_THUMBNAIL = 2;
    private static final int CASE_CARD = 3;
    private static final int CASE_CARD_EXPAND = 4;
    private static final int CASE_BIRTH = 5;
    private static final int CASE_GPLAY = 6;
    private static final int CASE_STOCK = 7;
    private static final int CASE_MISC = 8;
    private static final int CASE_CHG_VALUE = 9;
    private static final int CASE_LIST_BASE = 10;
    private static final int CASE_LIST_BASE_INNER = 11;
    private static final int CASE_LIST_EXPAND = 12;
    private static final int CASE_LIST_GPLAY = 13;
    private static final int CASE_LIST_GPLAY_UNDO = 14;
    private static final int CASE_GRID_BASE = 15;
    private static final int CASE_GRID_GPLAY = 16;
    private static final int CASE_LIST_COLORS = 17;
    private static final int CASE_CURSOR_LIST = 18;
    private static final int CASE_CURSOR_GRID = 19;
    private static final int CASE_LIST_GPLAY_CAB = 20;
    private static final int CASE_GRID_GPLAY_CAB = 21;
    private static final int CASE_OVERFLOW_ANIM = 22;*/

  //Your activity will respond to this action String

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(AndroidSocialService.RECEIVE_ENTITY)) {
        	BaseEntity entity = (BaseEntity)intent.getExtras().getSerializable(AndroidSocialService.ENTITY_KEY);
        	receiveBaseEntity(entity);
        }
        
        if (intent.getAction().equals(AndroidSocialService.DISPLAY_TOAST))	{
        	Toast.makeText(MainActivity.this, intent.getStringExtra(AndroidSocialService.TOAST_MSG_KEY), intent.getIntExtra(AndroidSocialService.TOAST_PERIOD_KEY, Toast.LENGTH_SHORT)).show();
        }
        

      }
    };    
    
    private Intent batteryStatus;    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_SELECTEDFRAGMENT, mSelectedFragment);
    }
    
    @Override
    protected void onResume() {
      super.onResume();   	     
      
      Intent intent= new Intent(this, AndroidSocialService.class);
      intent.putExtra(bIp, mBootIp);
      intent.putExtra(bPort, mBootPort);
      intent.putExtra(biPort, mBindPort);
      intent.putExtra(uName, mUsername);
      LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(AndroidSocialService.RECEIVE_ENTITY);
      intentFilter.addAction(AndroidSocialService.DISPLAY_TOAST);
      bManager.registerReceiver(bReceiver, intentFilter);   
      
      System.out.println("Prepare to bind service");
      bindService(intent, mConnection,
          Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
      super.onPause();
      unbindService(mConnection);
      LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
      bManager.unregisterReceiver(bReceiver);
    }	

		private ServiceConnection mConnection = new ServiceConnection() {

      public void onServiceConnected(ComponentName className, 
          IBinder binder) {
      	AndroidSocialService.LocalBinder b = (AndroidSocialService.LocalBinder) binder;
        service = b.getService();
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
            .show();
      }

      public void onServiceDisconnected(ComponentName className) {
        service = null;
      }
    };
    
    public void handleUserRecoverableAuthIOException(UserRecoverableAuthIOException ex)	{
    	this.startActivityForResult(ex.getIntent(), REQUEST_AUTHORIZATION);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_main);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        _initMenu();
        mDrawerToggle = new CustomActionBarDrawerToggle(this, mDrawer);
        mDrawer.setDrawerListener(mDrawerToggle);

        //-----------------------------------------------------------------
        //BaseFragment baseFragment = null;
        if (savedInstanceState != null) {
            mSelectedFragment = savedInstanceState.getInt(BUNDLE_SELECTEDFRAGMENT);

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (fragmentManager.findFragmentById(R.id.fragment_main)==null)
                mBaseFragment = selectFragment(mSelectedFragment);
            //if (mBaseFragment==null)
            //    mBaseFragment = selectFragment(mSelectedFragment);
        } else {
            mBaseFragment = new NewFeedsFragment(feeds);
            openFragment(mBaseFragment);
        }

        // Store the booting information to pass to the service
        mBootIp = getIntent().getStringExtra(bIp);
        mBootPort = getIntent().getStringExtra(bPort);
        mBindPort = getIntent().getStringExtra(biPort);
        mUsername = getIntent().getStringExtra(uName);
        
        // Initiate database helper
        dbHelper = new DatabaseHandler(this, mUsername);
        
        // Initiate cipher parameters
        publicKeys = null;
        secretKeys = null;
        ps06 = new PS06();
        cd07 = new IBBECD07();	
        
    		GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, java.util.Arrays.asList(DriveScopes.DRIVE));
    		credential.setSelectedAccountName(mUsername);
    		new googleCloudAuthorizationRequestTask().execute(credential);
              
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawer.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
		 * The action bar home/up should open or close the drawer.
		 * ActionBarDrawerToggle will take care of this.
		 */
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {

            //About
            case R.id.menu_about:
                //Utils.showAbout(this);
                return true;
            case R.id.menu_beer:
                //IabUtil.showBeer(this, mHelper);
                return true;
            default:
                break;
        }


        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {

        public CustomActionBarDrawerToggle(Activity mActivity, DrawerLayout mDrawerLayout) {
            super(
                    mActivity,
                    mDrawerLayout,
                    R.drawable.ic_navigation_drawer,
                    R.string.app_name,
                    mCurrentTitle);
        }

        @Override
        public void onDrawerClosed(View view) {
            getActionBar().setTitle(getString(mCurrentTitle));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            getActionBar().setTitle(getString(R.string.app_name));
            invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            // Highlight the selected item, update the title, and close the drawer
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mBaseFragment = selectFragment(position);
            mSelectedFragment = position;

            if (mBaseFragment != null)
                openFragment(mBaseFragment);
            mDrawer.closeDrawer(mDrawerList);
        }
    }
    
    synchronized public CipherParameters[] getPublicKeys()	{
  		if (publicKeys==null)	{
  			try {
  				getPublicKeysFromServer();
				} catch (Exception e) {
					e.printStackTrace();
				} 
  		} 
  		return publicKeys;
    }
    
    synchronized private CipherParameters[] getSecretKeys()	{
  		if (secretKeys==null)	{
  			try {
  				getSecretKeysFromServer();
				} catch (Exception e) {
					e.printStackTrace();
				} 
  		} 
  		return secretKeys;
    }
    
    private void getPublicKeysFromServer() throws InvalidCertificateException, IOException, KeyManagementException, NoSuchAlgorithmException	{
    	KeyStore dsnaKeyStore = loadLocalTrustKeystore();   
      HttpsURLConnection urlConnection = NetworkUtil.establishHttpsConnection(publicKeyURL, dsnaKeyStore);
    	String encodedPublicKeys = FileUtil.readString(urlConnection.getInputStream());
    	urlConnection.disconnect();
    	publicKeys = ASN1Util.extractPublicKey(ASN1Util.decodeIBESysPublicParams(encodedPublicKeys));
    }
    
    private void getSecretKeysFromServer() throws InvalidCertificateException, IOException, UnsupportedFormatException, KeyManagementException, NoSuchAlgorithmException {
    	// Load publickey from file
    	String mySecretKeyUrl = secretKeyURL+"?"+idParams+"="+mUsername;
    	KeyStore dsnaKeyStore = loadLocalTrustKeystore();   
      HttpsURLConnection urlConnection = NetworkUtil.establishHttpsConnection(mySecretKeyUrl, dsnaKeyStore);
    	String encodedPrivateKeys = FileUtil.readString(urlConnection.getInputStream());
    	urlConnection.disconnect();
      secretKeys = ASN1Util.extractClientSecretKey(ASN1Util.decodeIBEClientSecretParams(encodedPrivateKeys), getPublicKeys());
    }    
    
    private void getPublicKeysFromFile() throws InvalidCertificateException, IOException {
    	// Load publickey from file
      String encodedPublicKeys = getResources().getString(R.string.encoded_publickeys_certificate);
      publicKeys = ASN1Util.extractPublicKey(ASN1Util.decodeIBESysPublicParams(encodedPublicKeys));
    }
    
    private void getSecretKeysFromFile() throws InvalidCertificateException, IOException, UnsupportedFormatException {
    	// Load publickey from file
      String encodedPrivateKeys = getResources().getString(R.string.encoded_client_certificate);
      secretKeys = ASN1Util.extractClientSecretKey(ASN1Util.decodeIBEClientSecretParams(encodedPrivateKeys), getPublicKeys());
    }

    private BaseFragment selectFragment(int position) {
        BaseFragment baseFragment = null;

        switch (position) {
	        case CASE_ACCINFO:
	        	break;
	        case CASE_NEWFEEDS:
	          baseFragment = new NewFeedsFragment(feeds);
	          break;
		      case CASE_MESSAGE:
		      	List<ConversationElement> conversations = dbHelper.getAllConversations(mUsername);
		      	AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

		          @Override
		          public void onItemClick(AdapterView<?> parent, final View view,
		              int position, long id) {
		            final ConversationElement item = (ConversationElement) parent.getItemAtPosition(position);
		            String conversationName = item.getName();
		            List<OneMessage> msgs = dbHelper.getAllMessages(mUsername, conversationName);
		            mBaseFragment = new ConversationFragment(conversationName, msgs);
		            openFragment(mBaseFragment);
		          }

		        };
	          baseFragment = new MessageFragment(conversations, onItemClickListener);
	          break;
		      case CASE_CONTACT:
		      	baseFragment = new AddingContactFragment();
		      	break;
		      case CASE_POSTSTATUS:
		      	this.changeSessionKey();
		      	baseFragment = new PostStatusFragment();
		      	break;
/*            case CASE_HEADER:
                baseFragment = new HeaderFragment();
                break;
            case CASE_SHADOW:
                baseFragment = new ShadowFragment();
                break;
            case CASE_THUMBNAIL:
                baseFragment = new ThumbnailFragment();
                break;
            case CASE_CARD:
                baseFragment = new CardFragment();
                break;
            case CASE_CARD_EXPAND:
                baseFragment = new CardExpandFragment();
                break;
            case CASE_BIRTH:
                baseFragment = new BirthDayCardFragment();
                break;
            case CASE_GPLAY:
                baseFragment = new GPlayCardFragment();
                break;
            case CASE_STOCK:
                baseFragment = new StockCardFragment();
                break;
            case CASE_MISC:
                baseFragment = new MiscCardFragment();
                break;
            case CASE_CHG_VALUE:
                baseFragment = new ChangeValueCardFragment();
                break;
            case CASE_LIST_BASE:
                baseFragment = new ListBaseFragment();
                break;
            case CASE_LIST_BASE_INNER:
                baseFragment = new ListDifferentInnerBaseFragment();
                break;
            case CASE_LIST_EXPAND:
                baseFragment = new ListExpandCardFragment();
                break;
            case CASE_LIST_GPLAY:
                baseFragment = new ListGplayCardFragment();
                break;
            case CASE_LIST_GPLAY_UNDO:
                baseFragment = new ListGplayUndoCardFragment();
                break;
            case CASE_GRID_BASE:
                baseFragment = new GridBaseFragment();
                break;
            case CASE_GRID_GPLAY:
                baseFragment = new GridGplayFragment();
                break;
            case CASE_LIST_COLORS:
                baseFragment = new ListColorFragment();
                break;
            case CASE_CURSOR_LIST:
                baseFragment = new ListCursorCardFragment();
                break;
            case CASE_CURSOR_GRID:
                baseFragment = new GridCursorCardFragment();
                break;
            case CASE_LIST_GPLAY_CAB:
                baseFragment = new ListGplayCardCABFragment();
                break;
            case CASE_GRID_GPLAY_CAB:
                baseFragment = new GridGplayCABFragment();
                break;
            case CASE_OVERFLOW_ANIM:
                baseFragment = new OverflowAnimFragment();
                break;*/
            default:
                break;
        }

        return baseFragment;
    }
    
  	public void lookupProfile(final String username, final Continuation<PastContent, Exception> resultHandler)	{	
  		service.lookupDHTByName(username, resultHandler);
  	}
  	
  	public boolean addFriend(final SocialProfile newFriend)	{
  		if(service.addFriend(newFriend))	{
  			Message welcomeMessage = newFriend.createMessage(newFriend.getOwnerUsername() + " is added to your friend list");
  			welcomeMessage.setConversation(newFriend.getOwnerUsername());
  			dbHelper.addMessage(welcomeMessage);
  			return true;
  		}
  		return false;
  	}
  	
  	public void postStatus(final String status) throws UserRecoverableAuthIOException, IOException	{
  		service.postStatus(status);
  	}
  	
  	public Message sendMessageToConversation(String conversationName, String msg)	{
  		Message result = service.sendMessageToConversation(conversationName, msg);
  		if (result!=null)	{
  			String sendingConversation = result.getConversation();
  			result.setConversation(conversationName);
  			dbHelper.addMessage(result);
  			result.setConversation(sendingConversation);
  			return result;
  		}
  		return null;
  	}
  	
  	public void changeSessionKey()	{
  		new changeSessionKeyTask().execute();
  	}

    private void openFragment(BaseFragment baseFragment) {
        if (baseFragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.fragment_main, baseFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            if (baseFragment.getTitleResourceId()>0)
                mCurrentTitle = baseFragment.getTitleResourceId();

        }
    }


    public static final String[] options = {
    				"Account info",
    				"Newfeeds",
    				"Message",
    				"Adding contact", 
    				"Post status"
/*            "CardHeader",
            "CardShadow",
            "CardThumbnail",
            "Card",
            "Card Expand",
            "Google Birthday",
            "Google Play",
            "Google Stock",
            "Misc",
            "Refresh Card",
            "List base",
            "List base with different Inner Layouts" ,
            "List and expandable card",
            "List Google Play",
            "List with swipe and undo",
            "Grid base",
            "Grid Google Play",
            "List colored cards",
            "List with Cursor",
            "Grid with Cursor",
            "List with MultiChoice",
            "Grid with MultiChoice",
            "Overflow Animation (exp)"*/
    };   

    private void _initMenu() {
        mDrawerList = (ListView) findViewById(R.id.drawer);

        if (mDrawerList != null) {
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, options));

            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        }

    }

		@Override
		public void onConnectionFailed(ConnectionResult arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnected(Bundle connectionHint) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnectionSuspended(int cause) {
			// TODO Auto-generated method stub
			
		}
		
		private class googleCloudAuthorizationRequestTask extends AsyncTask<GoogleAccountCredential, Void, UserRecoverableAuthIOException>	{
			 @Override
		    protected UserRecoverableAuthIOException doInBackground(GoogleAccountCredential... credentials) {

	      	if (credentials.length>0 && credentials[0] != null) {
	      		try	{
	          	CloudStorageService googleCloudHandler = null;	
	          	Drive drive;
	          	System.out.println("TRY TO AUTHENTICATE SOME GOOGLE CLOUD HANLDER");
	          	System.out.println(credentials[0].getSelectedAccountName());
          		drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credentials[0]).build();
          		googleCloudHandler = new GoogleCloudStorageServiceImpl(drive);	
          		googleCloudHandler.initializeDSNAFolders();
	      		} catch(UserRecoverableAuthIOException urai)	{
	      			return urai;
	      		} catch(IOException ioe)	{
	      			return null;
	      		}
	      	} 
				 return null;
		    }

		    @Override
		    protected void onPostExecute(UserRecoverableAuthIOException urai) {
		    	if (urai!=null)	{
		    		MainActivity.this.startActivityForResult(urai.getIntent(), MainActivity.REQUEST_AUTHORIZATION);
		    	}
		    }			
		}
		
		private class decapsulateNewSessionKeyTask extends AsyncTask<KeyHeader, Void, String> {
	    @Override
	    protected String doInBackground(KeyHeader... arg) {
	      String result = null;
  			
  			try {
  				long beginTime = System.currentTimeMillis();
  				KeyHeader keyHeader = arg[0];
	  			byte[] encapsulatedHeader = keyHeader.header;
	  			String[] ids = arg[0].getIds();
	  			for (int i=0; i<ids.length; i++)
	  				System.out.println(ids[i]);
	  			CipherParameters[] publicKeys = getPublicKeys();
	  			CipherParameters[] secretKeys = getSecretKeys();
	  			Element[] cd07Ids = cd07.map(publicKeys[1], ids);
	  			CipherParameters cd07DecryptionKey = new CD07DecryptionParameters((CD07SecretKeyParameters)secretKeys[1], cd07Ids);	  			
	  			byte[] key = cd07.decaps(cd07DecryptionKey, encapsulatedHeader);
					
					byte[] keyMaterial = Arrays.copyOf(key, 16);
					dbHelper.addSessionKeys(keyHeader.getKeyId(), keyHeader.getOwnerUsername(), keyHeader.getAlgorithm(), keyMaterial, keyHeader.getTimeStamp());
					//result = "Got session key: " + keyHeader.getKeyId() + " - " + keyHeader.getOwnerUsername();
					//logBatteryLevelAndProcessTime(System.currentTimeMillis()-beginTime);
  			} catch (Exception e)	{
  				e.printStackTrace();
  			}
	      return result;
	    }
	    
	    volatile boolean firstTime = true;

	    @Override
	    protected void onPostExecute(String result) {
	    	if (result!=null)	{
	    		Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
	    	}
	    }
	  }
		

		
		private class changeSessionKeyTask extends AsyncTask<KeyHeader, Void, String> {
	    @Override
	    protected String doInBackground(KeyHeader... arg) {
	      String result = null;
  			
  			try {
  	  		CipherParameters[] myPublicKeys = getPublicKeys();

  	  		service.changeAndDistributeSessionKey(IdBasedSecureSocialService.AESAlgorithm, myPublicKeys[1], new Continuation<KeyInfo, Exception>() {
  	        public void receiveResult(KeyInfo result) {          
  	      		dbHelper.addSessionKeys(result.getKeyId(), mUsername, result.getAlgorithm(), result.getValues(), result.getTimeStamp());
  	      		service.displayToast("Distribute session key complete", Toast.LENGTH_SHORT);
  	        }

  	        public void receiveException(Exception result) {
  	        	service.displayToast("Distribute session key fail", Toast.LENGTH_SHORT);
  	        	Log.d(TAG, "changeSessionKey exception", result);
  	        }
  				});
  			} catch (Exception e)	{
  				e.printStackTrace();
  			}
  			return null;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	if (result!=null)
	    		Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
	    }
	  }
		
		private KeyStore loadLocalTrustKeystore()	{
			KeyStore localTrustStore;
			try {
				localTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				InputStream in = getResources().openRawResource(R.raw.dsnatrustkeystore);
				try {
					//System.out.println(FileUtil.readString(in));
					localTrustStore.load(in, "kthdsna".toCharArray());
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return localTrustStore;
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			return null;
		}
		
		private void receiveBaseEntity(BaseEntity entity) {
			switch(entity.getType())	{
			case Status.TYPE:
				long beginTime = System.currentTimeMillis();
				receiveStatus((Status) entity);
				logBatteryLevelAndProcessTime(System.currentTimeMillis()-beginTime);
				break;
			case Message.TYPE:
				receiveMessage((Message) entity);
				break;
			case EncryptedEntity.TYPE:
				receiveEncryptedEntity((EncryptedEntity) entity);
				break;
			case KeyHeader.TYPE:
				receiveKeyHeader((KeyHeader) entity);
				break;
			case Notification.TYPE:
				service.receiveNotification((Notification) entity);
			default:
				break;
			}
		}

		public void receiveStatus(Status newStatus) {
			feeds.add(newStatus);
      if (mBaseFragment instanceof NewFeedsFragment)	{
      	NewFeedsFragment nff = (NewFeedsFragment)mBaseFragment;
      	nff.addNewFeed(newStatus);
      }
      //Do something with the string
      //Toast.makeText(MainActivity.this, newStatus.toString(), Toast.LENGTH_LONG).show();
		}		
		
		private void receiveKeyHeader(KeyHeader encapsulatedKey) {	
			System.out.println("Receive Key Header");
			new decapsulateNewSessionKeyTask().execute(encapsulatedKey);
		}
		
		public void receiveMessage(Message newMessage) {		
			//Do something with the string
      //Toast.makeText(MainActivity.this, newMessage.toString(), Toast.LENGTH_LONG).show();          
      dbHelper.addMessage(newMessage);
      
      if (mBaseFragment instanceof ConversationFragment)	{
      	ConversationFragment mf = (ConversationFragment)mBaseFragment;
      	mf.add(newMessage.getContent());
      }   
		}
		
		public void logBatteryLevelAndProcessTime(long processedTime)	{
      IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
      batteryStatus = getApplicationContext().registerReceiver(null, ifilter); 
    	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    	float batteryPct = level/(float)scale;
    	batteryPct = Math.max(Math.min(batteryPct, 1), 0);
    	logBatteryAndProcessResult.append(System.currentTimeMillis());
    	logBatteryAndProcessResult.append(',');
    	logBatteryAndProcessResult.append(batteryPct);
    	logBatteryAndProcessResult.append(',');
    	logBatteryAndProcessResult.append(processedTime);
    	logBatteryAndProcessResult.append('\n');		
		}
		
		public void receiveEncryptedEntity(final EncryptedEntity encryptedEntity) {
			
			/*
			 * The logic of the case when receive Encrypted Entity:
			 * 1. Lookup sessionKey in database
			 * 2. If we already have session, try to decrypt it, get the base entity and return
			 * 3. Otherwise, fetch the key from the internet, try to decrypt it, get the base entity 
			 * 4. If cannot fetch the sessionkey from the internet, drop the encryptedEntity 
			 * since we might not have permission to view it
			 */
			long beginTime = System.currentTimeMillis();
			KeyInfo sessionKey = dbHelper.getSessionKey(encryptedEntity.getKeyId(), encryptedEntity.getOwnerUsername());
			if (sessionKey!=null)	{
				try {
					receiveBaseEntity(encryptedEntity.getEntity(sessionKey.getValues(), sessionKey.getAlgorithm()));
					long endTime = System.currentTimeMillis();
					//logBatteryLevelAndProcessTime(endTime-beginTime);
					return;
				} catch (Exception e1)	{
					e1.printStackTrace();
					Log.d(TAG, "receiveEncryptedEntity", e1);
				}
			}
			
			System.out.println("Fetching new session Key for encryptedEntity");
			/* To-do: 
			 * should handle case when cannot then fetch the KeyHeader from the internet 
			 * (Internet is down)
			 */
			for (String location : encryptedEntity.getLocationSet())	{
				if (!location.equalsIgnoreCase(Location.DHT))
					service.lookupCloudsById(location, encryptedEntity.getKeyHeaderFileId(location), new Continuation<InputStream, Exception>() {
			      public void receiveResult(InputStream result) {
			      	try {
								Object obj;
								obj = FileUtil.readObject(result);
				      	if (obj instanceof KeyHeader)	{
				      		receiveBaseEntity((BaseEntity)obj);
				      		receiveEncryptedEntity(encryptedEntity);
				      	}
							} catch (Exception e1) {
									Log.d(TAG, "receiveEncryptedEntity", e1);
									receiveException(e1);
							}
			      }
		
				    public void receiveException(Exception result) {
				    	service.displayToast("Fetch key failed", Toast.LENGTH_SHORT);
				    }
				  });
			}				

		}

}

