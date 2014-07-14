package com.dsna.status;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import rice.Continuation;
import rice.p2p.past.PastContent;

import com.dsna.android.main.MainActivity;
import com.dsna.desktop.client.ui.ClientFrame;
import com.dsna.dht.past.DSNAPastContent;
import com.dsna.entity.BaseEntity;
import com.dsna.entity.SocialProfile;
import com.dsna.message.ConversationElement;
import com.dsna.message.bubblechat.ConversationFragment;
import com.dsna.message.bubblechat.OneMessage;
import com.dsna.service.SocialService;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.dsna.android.main.R;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import it.gmariotti.cardslib.demo.fragment.BaseFragment;

public class PostStatusFragment extends BaseFragment {

  protected ScrollView mScrollView;
	private ListView lv;
	
	public PostStatusFragment()	{

	}

  @Override
  public int getTitleResourceId() {
      return R.string.dsna_new_feeds_title;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  		return inflater.inflate(R.layout.dsna_post_status_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    final Button findButton = (Button) getView().findViewById(R.id.post_button);
    final EditText mStatusView = (EditText) getView().findViewById(R.id.status);
    findButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {

      	final String myStatus = mStatusView.getText().toString();
      	Toast.makeText(PostStatusFragment.this.getActivity(), "Post status - "+myStatus,Toast.LENGTH_SHORT).show();

				try {
					((MainActivity)PostStatusFragment.this.getActivity()).postStatus(myStatus);
				} catch (UserRecoverableAuthIOException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
      	mStatusView.setText("");

      }
    });

  }	

}

