package com.dsna.android.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator;

import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.past.PastContent;
import rice.p2p.scribe.Topic;
import rice.pastry.JoinFailedException;

import com.dsna.contact.AddingContactFragment;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.BaseEntity;
import com.dsna.entity.Location;
import com.dsna.entity.Message;
import com.dsna.entity.Notification;
import com.dsna.entity.SocialProfile;
import com.dsna.entity.Status;
import com.dsna.entity.encrypted.EncryptedEntity;
import com.dsna.entity.encrypted.KeyInfo;
import com.dsna.service.IdBasedSecureSocialEventListener;
import com.dsna.service.IdBasedSecureSocialService;
import com.dsna.service.SocialServiceFactory;
import com.dsna.service.SocialServiceImpl;
import com.dsna.storage.cloud.CloudStorageService;
import com.dsna.storage.cloud.GoogleCloudStorageServiceImpl;
import com.dsna.util.FileUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class AndroidSocialService extends Service implements IdBasedSecureSocialEventListener, IdBasedSecureSocialService {

  public static final String BOOTIP = "130.229.152.253"; //your computer IP address should be written here
  public static final int BOOTPORT = 9001;
  public static final int BINDPORT = 9001;
  
  public static final String RECEIVE_ENTITY = "com.dsna.service.RECEIVE_ENTITY";
  public static final String ENTITY_KEY = "base.entity";
  public static final String RECEIVE_NOTIFICATION = "com.dsna.service.RECEIVE_NOTIFICATION";
  public static final String NOTIFICATION_KEY = "notification";
  public static final String RECEIVE_PROFILE = "com.dsna.service.RECEIVE_PROFILE";
  public static final String PROFILE_KEY = "profile";
  public static final String DISPLAY_TOAST = "com.dsna.service.DISPLAY_MSG";
  public static final String TOAST_MSG_KEY = "toastmsg";
  public static final String TOAST_PERIOD_KEY = "toastperiod";  
	
	private IdBasedSecureSocialService serviceHandler;
	/**
	 * The google cloud drive serve as agent for cloud interaction
	 */
	CloudStorageService googleCloudHandler;	
	private Drive drive;
	
	private String mBootIp;
	private String mBootPort;
	private String mBindPort;
	private String mUsername;
	private boolean isNewuser = false;
	private String TAG = "com.dsna.android.main.AndroidSocialService";
	
	public AndroidSocialService() {
		Log.i(TAG, "Android service created");
		serviceHandler = null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    // TODO Auto-generated method stub
	    Log.i(TAG, "In Ibinder onBind method");
      if (serviceHandler==null)	{
        Runnable ignite = new IgniteService();
      	mBindPort = intent.getStringExtra(MainActivity.biPort);
      	mBootPort = intent.getStringExtra(MainActivity.bPort);
      	mBootIp = intent.getStringExtra(MainActivity.bIp);
      	mUsername = intent.getStringExtra(MainActivity.uName);
      	if (googleCloudHandler == null) {
      		System.out.println("TRY TO CREATE SOME GOOGLE CLOUD HANLDER");
      		GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
      		credential.setSelectedAccountName(mUsername);
      		drive = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
      		System.out.println(mUsername);
      		googleCloudHandler = new GoogleCloudStorageServiceImpl(drive);
      	}      	
	      new Thread(ignite).start();
      }
	    return myBinder;
	}

  private final IBinder myBinder = new LocalBinder();

  public class LocalBinder extends Binder {
        public AndroidSocialService getService() {
            Log.i(TAG, "I am in Localbinder ");
            return AndroidSocialService.this;

        }
    }

  @Override
  public void onCreate() {
      super.onCreate();
      Log.i(TAG, "I am in on create");   
      //Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
  }
  
  @Override
  public int onStartCommand(Intent intent,int flags, int startId){
      super.onStartCommand(intent, flags, startId);
      Log.i(TAG, "I am in on start");
    //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
      if (serviceHandler==null)	{
        Runnable ignite = new IgniteService();
      	mBindPort = intent.getStringExtra(MainActivity.biPort);
      	mBootPort = intent.getStringExtra(MainActivity.bPort);
      	mBootIp = intent.getStringExtra(MainActivity.bIp);
      	mUsername = intent.getStringExtra(MainActivity.uName);
	      new Thread(ignite).start();
      }
      return START_STICKY;
  }
  
  class IgniteService implements Runnable {
  	
  	PublicKey publicKey;
  	PrivateKey privateKey;
  	
  	public byte[] RSAEncrypt(final String plain) throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
  		System.out.println("Length: " + plain.getBytes().length*8);
			
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] encryptedBytes = cipher.doFinal(plain.getBytes());
			return encryptedBytes;
		}

		public String RSADecrypt(final byte[] encryptedBytes) throws NoSuchAlgorithmException, NoSuchPaddingException,
		    InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
			Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher1.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] decryptedBytes = cipher1.doFinal(encryptedBytes);
			String decrypted = new String(decryptedBytes);
			return decrypted;
		}

    @Override
    public void run()  {   	

	    // Loads pastry configurations
	    Environment env = new Environment();
	    System.out.println("IGNITE SERVICE: ");
	    // disable the UPnP setting (in case you are testing this on a NATted LAN)
	    env.getParameters().setString("nat_search_policy","never");
	    env.getParameters().setString("firewall_test_policy","always");
	    env.getParameters().setString("firewall_test_policy","always");
	    env.getParameters().setBoolean("probe_for_external_address", false);
	    env.getParameters().setBoolean("epost_nat_support", false);
	    //env.getParameters().setString("external_address", "83.180.236.252:23456");
	    //env.getParameters().remove("external_address");
	    //env.getParameters().remove("nat_handler_class");
			SocialServiceFactory factory = new SocialServiceFactory(env);
			try {
				SocialProfile user = AndroidSocialService.this.loadUserProfile(mUsername+".dat");
				HashMap<String,Long> lastSeqs = AndroidSocialService.this.loadTopicsCache(mUsername+"_lastseq.dat");
				if (user==null || isNewuser)
					serviceHandler = factory.newDSNAIdBasedSecureSocialService(Integer.parseInt(mBindPort), Integer.parseInt(mBootPort), mBootIp, AndroidSocialService.this, mUsername);
				else
					serviceHandler = factory.newDSNAIdBasedSecureSocialService(Integer.parseInt(mBindPort), Integer.parseInt(mBootPort), mBootIp, AndroidSocialService.this, user, lastSeqs);
				
				user = serviceHandler.getUserProfile();
				System.out.println("serviceHandler: " + serviceHandler);
				System.out.println("SocialProfile: " + user.getUserId());
				System.out.println("GOOGLE_CLOUD: " + googleCloudHandler);
				//System.out.println("Do extra stuff with: " + user.getUserId());
				serviceHandler.pushProfileToDHT();
				serviceHandler.addCloudHandler(Location.GOOGLE_CLOUD, googleCloudHandler);
				serviceHandler.initSubscribe();

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//Toast.makeText(getApplicationContext(), "Cannot start DSNA social service", Toast.LENGTH_LONG).show();
				AndroidSocialService.this.stopSelf();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				//Toast.makeText(getApplicationContext(), "Cannot start DSNA social service", Toast.LENGTH_LONG).show();
				AndroidSocialService.this.stopSelf();
			} catch (JoinFailedException e1) {
				// TODO Auto-generated catch block
				//Toast.makeText(getApplicationContext(), "Cannot start DSNA social service", Toast.LENGTH_LONG).show();
				AndroidSocialService.this.stopSelf();
			} catch (Exception e)	{
				System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				e.printStackTrace();
			}

    }
  }
	
	public Message sendMessage(String friendId, String msg) {
		return serviceHandler.sendMessage(friendId, msg);
	}

	public void subscribe(String topic) {
		serviceHandler.subscribe(topic);
	}

	public void unsubscribe(String topic) {
		serviceHandler.unsubscribe(topic);
	}

	private void broadcastEntity(BaseEntity entity) {
		Intent entityDeliver = new Intent(RECEIVE_ENTITY);
		Bundle b = new Bundle();
		b.putSerializable(ENTITY_KEY, entity);
		entityDeliver.putExtras(b);
		LocalBroadcastManager.getInstance(this).sendBroadcast(entityDeliver);
	}

	@Override
	public void receiveNotification(Notification notification) {
		// TODO Auto-generated method stub
		final String googleId;
		switch (notification.getNotificationType())	{
		case NEWFEEDS:
			//JOptionPane.showMessageDialog(this, notification.getArgument("objectId"));
			System.out.println("Get an new feed " + notification);
			if (notification.getLocationSet().contains(Location.GOOGLE_CLOUD))
				fetchStatus(Location.GOOGLE_CLOUD, notification.getFileId(Location.GOOGLE_CLOUD));
			else 
				fetchStatus(Location.DHT, notification.getFileId(Location.DHT));
			break;
		case SESSION_KEY_CHANGE:
			googleId = notification.getFileId(Location.GOOGLE_CLOUD);
			if (googleId!=null)	{
				serviceHandler.lookupCloudsById(Location.GOOGLE_CLOUD, googleId, new Continuation<InputStream, Exception>() {
		      public void receiveResult(InputStream result) {
						try {
							Object obj;
							obj = FileUtil.readObject(result);
			      	if (obj instanceof BaseEntity)
			      		receiveBaseEntity((BaseEntity)obj);
						} catch (Exception e) {
								receiveException(e);
						}
		      }

			    public void receiveException(Exception result) {
			    	result.printStackTrace();
			      displayToast("Look up failed google - "+ googleId, Toast.LENGTH_SHORT);
			    }
			  });

			} else	{
				final String dhtId = notification.getFileId(Location.DHT);
				System.out.println("Lookup session key:" + dhtId);
				serviceHandler.lookupDHTById(dhtId, new Continuation<PastContent, Exception>() {
		      public void receiveResult(PastContent result) {
		    		if (result instanceof DSNAPastContent)	{
		    			BaseEntity entity = ((DSNAPastContent)result).getContent();
		    			receiveBaseEntity(entity);
		    		}
		      }

			    public void receiveException(Exception result) {
			    	displayToast("Look up failed dht - "+ dhtId, Toast.LENGTH_SHORT);
			    }
			  });
			}
			break;
		default:
		}
	}
	
	private void fetchStatus(final String location, final String statusId)	{
		
		// If location is DHT, we lookup by DHT and return 
		if (location.equalsIgnoreCase(Location.DHT))	{
			serviceHandler.lookupDHTById(statusId, new Continuation<PastContent, Exception>() {
	      public void receiveResult(PastContent result) {
	    		if (result instanceof DSNAPastContent)	{
	    			BaseEntity entity = ((DSNAPastContent)result).getContent();
	    			receiveBaseEntity(entity);
	    		}
	      }
	
		    public void receiveException(Exception result) {
		    	displayToast("Look up failed - "+statusId, Toast.LENGTH_SHORT);
		    	Log.d(TAG, location + statusId, result);
		    }
		  });
			return;
		}
		
		serviceHandler.lookupCloudsById(location, statusId, new Continuation<InputStream, Exception>() {
	      public void receiveResult(InputStream result) {
	  			Object obj;
					try {
						obj = FileUtil.readObject(result);
		  			if (obj instanceof BaseEntity)
		  				receiveBaseEntity((BaseEntity)obj);
					} catch (ClassNotFoundException e) {
						Log.d(TAG, location + statusId, e);
					} catch (IOException e) {
						Log.d(TAG, location + statusId, e);
					}
	      }
	
		    public void receiveException(Exception result) {
		    	displayToast("Look up " + location + " cloud failed - "+statusId, Toast.LENGTH_SHORT);
		    	Log.d(TAG, location + statusId, result);
		    }
		  });		

	}

	@Override
	public void receiveStatus(Status status) {
	}
	
	synchronized public void displayToast(String msg, int period) {
		Intent toastMsg = new Intent(DISPLAY_TOAST);
		Bundle b = new Bundle();
		b.putString(TOAST_MSG_KEY, msg);
		b.putInt(TOAST_PERIOD_KEY, period);
		toastMsg.putExtras(b);
		LocalBroadcastManager.getInstance(this).sendBroadcast(toastMsg);
	}

	@Override
	public void receiveSocialProfile(SocialProfile profile) {
		serviceHandler.addFriend(profile);
	}

	@Override
	public void receiveInsertException(Exception e) {
	}

	@Override
	public void receiveLookupException(Exception e) {
	}

	@Override
	public void receiveLookupNull() {
	}

	@Override
	public void subscribeFailed(Topic topic) {
	}

	@Override
	public void subscribeFailed(Collection<Topic> topics) {	
	}

	@Override
	public void subscribeSuccess(Collection<Topic> topics) {
	}
	
  @Override
  public void onDestroy() {

  	if (serviceHandler!=null)	{
    	saveUserProfile();
  		serviceHandler.logout();
  		serviceHandler = null;
  	}
  	  	
    // Tell the user we stopped.
    Toast.makeText(this, "DSNA service stopped", Toast.LENGTH_SHORT).show();
  }
	
	public void saveUserProfile()	{
		try	{
			Context context = getApplicationContext();
			SocialProfile user = serviceHandler.getUserProfile();
			String profileFileName = user.getOwnerUsername()+".dat";
			context.deleteFile(profileFileName);
			FileOutputStream fout = context.openFileOutput(profileFileName, Context.MODE_PRIVATE);
			System.out.println("Save file to "+profileFileName);
			FileUtil.writeObject(fout, user);
			
			String lastSeqFileName = user.getOwnerUsername()+"_lastseq.dat";
			System.out.println("Save last sequence to "+lastSeqFileName);
			context.deleteFile(lastSeqFileName);
			fout = context.openFileOutput(lastSeqFileName, Context.MODE_PRIVATE);
			HashMap<String,Long> topicsLastSeq = ((SocialServiceImpl)serviceHandler).getTopicsLastSeq();
			FileUtil.writeObject(fout, topicsLastSeq);
		} catch (Exception ex)	{
			ex.printStackTrace();
		}
	}
	
	public HashMap<String,Long> loadTopicsCache(String fileName)	{
		try	{
			FileInputStream fis = getApplicationContext().openFileInput(fileName);
			Object object = FileUtil.readObject(fis);
			HashMap<String,Long> lastSeqs;
			if (object!=null)	{
					lastSeqs = (HashMap<String,Long>) object;
					return lastSeqs;
			} 
			return null;
		} catch (Exception ex)	{
			return null;
		}
	}
	
	private SocialProfile loadUserProfile(String fileName)	{
		try	{
			FileInputStream fis = getApplicationContext().openFileInput(fileName);
			Object object = FileUtil.readObject(fis);
			SocialProfile profile;
			if (object!=null)	{
					profile = (SocialProfile) object;
					return profile;
			} 
			return null;
		} catch (Exception ex)	{
			return null;
		}
	}

	@Override
	public void lookupDHTById(String id, Continuation<PastContent, Exception> action) {
		serviceHandler.lookupDHTById(id, action);
	}

	@Override
	public void lookupDHTByName(String name,
		Continuation<PastContent, Exception> action) {
		serviceHandler.lookupDHTByName(name, action);
	}

	@Override
	public boolean addFriend(SocialProfile friend) {
		return serviceHandler.addFriend(friend);
	}

	@Override
	public void initSubscribe() {
		serviceHandler.initSubscribe();
	}

	@Override
	public HashMap<String, String> getFriendsContacts() {
		return serviceHandler.getFriendsContacts();
	}

	@Override
	public void updateProfile(SocialProfile edittedProfile) {
		serviceHandler.updateProfile(edittedProfile);
	}

	@Override
	public SocialProfile getUserProfile() {
		return serviceHandler.getUserProfile();
	}

	@Override
	public void pushProfileToDHT() {
		serviceHandler.pushProfileToDHT();
	}

	@Override
	public void logout() {
		serviceHandler.logout();
	}

	@Override
	public Message sendMessageToConversation(String conversationName, String msg) {
		return serviceHandler.sendMessageToConversation(conversationName, msg);
	}

  private class PostStatusTask extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... args) {
      Boolean response = false;
  		try {
  			
  			switch (args.length)	{
  			case 1:
  				serviceHandler.postStatus(args[0]); 
  				break;
  			case 2:
  				serviceHandler.postStatus(args[0], args[1]);
  				break;
  			}
  			response = true;
  		} catch (UserRecoverableAuthIOException e) {
  			Log.i(TAG, e.toString());
  			getApplication().startActivity(e.getIntent());
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
      return response;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    	String text = result ? "Post status succeed" : "Post status fail";
    	Toast.makeText(getApplication().getApplicationContext(), text,Toast.LENGTH_SHORT).show();
    }
  }	

	@Override
	public void postStatus(String status) throws UserRecoverableAuthIOException,
			IOException {
		new PostStatusTask().execute(status);
	}

	@Override
	public void postStatus(String id, String status)
			throws UserRecoverableAuthIOException, IOException {
		new PostStatusTask().execute(id, status);
	}

	@Override
	public void addCloudHandler(String cloudLocation,
			CloudStorageService cloudHandler) throws UserRecoverableAuthIOException, IOException {
		serviceHandler.addCloudHandler(cloudLocation, cloudHandler);
	}

	@Override
	public void receiveBaseEntity(BaseEntity entity) {
		switch(entity.getType())	{
		case Notification.TYPE:
			this.receiveNotification((Notification)entity);
			break;
		case SocialProfile.TYPE:
			this.receiveSocialProfile((SocialProfile)entity);
			break;
		default:
			System.out.println("BroadcastEntity " + entity);
			broadcastEntity(entity);
			break;
		}
	}


	@Override
	public void lookupCloudsById(String location, String id,
			Continuation<InputStream, Exception> action) {
			serviceHandler.lookupCloudsById(location, id, action);
	}


	@Override
	public void changeAndDistributeSessionKey(String symmetricAlgorithm, CipherParameters publicKey, String[] ids,
			Continuation<KeyInfo, Exception> action) {
		serviceHandler.changeAndDistributeSessionKey(symmetricAlgorithm, publicKey, ids, action);
	}

	@Override
	public void changeAndDistributeSessionKey(String symmetricAlgorithm, CipherParameters publicKey,
			Continuation<KeyInfo, Exception> action) {
		serviceHandler.changeAndDistributeSessionKey(symmetricAlgorithm, publicKey, action);
	}

	@Override
	public void broadcast(String topicId, BaseEntity msg, boolean isCaching) {
		serviceHandler.broadcast(topicId, msg, isCaching);
	}
	
	@Override
	public void setSessionKeyParameter(KeyInfo key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException {
		serviceHandler.setSessionKeyParameter(key);		
	}
	
	@SuppressWarnings("Unused")
	public void receiveBroadcastException(Exception e) {
	}
	
	@SuppressWarnings("Unused")
	public void receiveEncryptedEntity(EncryptedEntity e) {
	}

	@SuppressWarnings("Unused")
	public void receiveKeyInfo(KeyInfo encapsulatedKey) {	
	}
	
	@SuppressWarnings("Unused")
	public void receiveMessage(Message msg) {		
	}

	@Override
	public void setPreferEncrypted(boolean preferEncrypted) {
		serviceHandler.setPreferEncrypted(preferEncrypted);
	}
  
}
