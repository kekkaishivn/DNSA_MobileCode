package com.dsna.android.main;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.dsna.android.main.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements ConnectionCallbacks, 
		OnConnectionFailedListener  {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_USERNAME = "com.dsna.android.main.extra.USERNAME";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	
	public static final String defaultUsername = "dsnatest1@gmail.com";
	public static final String defaultBootIp = "192.168.1.102";
	public static final String defaultBindPort = "9001";
	public static final String defaultBootPort = "9001";
	
	// Values for booip, bootport, bindport and username at the time of the login attempt.
	private String mBootIp;
	private String mBootPort;
	private String mBindPort;
	private String mUsername;

	// UI references.
	private EditText mBootipView;
	private EditText mBootportView;
	private EditText mBindportView;
	private EditText mUsernameView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsername = defaultUsername;
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);
		
		mBootIp = defaultBootIp;
		mBootipView = (EditText) findViewById(R.id.bootip);
		mBootipView.setText(mBootIp);
		
		mBootPort = defaultBindPort;
		mBootportView = (EditText) findViewById(R.id.bootport);
		mBootportView.setText(mBootPort);
		
		mBindPort = defaultBootPort;
		mBindportView = (EditText) findViewById(R.id.bindport);
		mBindportView.setText(mBindPort);


		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						updateSettingInfo();
						attemptLogin();
					}

				});
		
    // Init cloud drive serivce	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If
	 * there are form errors (invalid email, missing fields, etc.), the errors are
	 * presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		boolean cancel = false;
		View focusView = null;

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute((Void) null);
		}
	}
	
	private void updateSettingInfo() {
		// TODO Auto-generated method stub
		mBootIp = mBootipView.getText().toString();
		mBootPort = mBootportView.getText().toString();
		mBindPort = mBindportView.getText().toString();
		mUsername = mUsernameView.getText().toString();
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate the
	 * user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		
		Activity parentActivity;
		
		public UserLoginTask(Activity parentActivity)	{
			this.parentActivity = parentActivity;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

/*			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}*/
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Intent initmain = new Intent(parentActivity, MainActivity.class);
				initmain.putExtra(MainActivity.bIp, mBootIp);
				initmain.putExtra(MainActivity.bPort, mBootPort);
				initmain.putExtra(MainActivity.biPort, mBindPort);
				initmain.putExtra(MainActivity.uName, mUsername);
				parentActivity.startActivity(initmain);
				finish();
			} else {
/*				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();*/
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
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
}
