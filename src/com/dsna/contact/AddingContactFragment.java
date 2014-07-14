package com.dsna.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

import it.gmariotti.cardslib.demo.fragment.BaseFragment;

public class AddingContactFragment extends BaseFragment {

  protected ScrollView mScrollView;
	private ListView lv;
	private ContactElementArrayAdapter adapter;
	
	public AddingContactFragment()	{

	}

  @Override
  public int getTitleResourceId() {
      return R.string.dsna_message_title;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  		return inflater.inflate(R.layout.dsna_adding_contact_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    lv = (ListView) getView().findViewById(R.id.listview);
    final Button findButton = (Button) getView().findViewById(R.id.find_button);
    final EditText mUsernameView = (EditText) getView().findViewById(R.id.username);
    findButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {

      	final String lookupUsername = mUsernameView.getText().toString();
      	Toast.makeText(AddingContactFragment.this.getActivity(), "Look up to find - "+lookupUsername,Toast.LENGTH_SHORT).show();
      	((MainActivity)AddingContactFragment.this.getActivity()).lookupProfile(lookupUsername, new Continuation<PastContent, Exception>() {
          public void receiveResult(PastContent result) {
        		if (result instanceof DSNAPastContent)	{
        			final BaseEntity entity = ((DSNAPastContent)result).getContent();
        			if (entity.getType()==SocialProfile.TYPE)	{
	        				lv.post(new Runnable() {                  
	        			    @Override
	        			    public void run() {
	            				adapter.add((SocialProfile)entity);
	            				adapter.notifyDataSetChanged();
	        			    }
	        				});
        			}
        			
        		} else	{
        			System.out.println("Look up failed");
        		}
          }

    	    public void receiveException(Exception result) {
    	      //Toast.makeText(AddingContactFragment.this.getActivity(), "Look up failed - "+lookupUsername,Toast.LENGTH_SHORT).show();
    	    }
    	  });

      }
    });

    adapter = new ContactElementArrayAdapter(getActivity().getApplicationContext(), new ArrayList<SocialProfile>());
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, final View view,
          int position, long id) {
        final SocialProfile item = (SocialProfile) parent.getItemAtPosition(position);
        if (((MainActivity)AddingContactFragment.this.getActivity()).addFriend(item))	{
        	Toast.makeText(AddingContactFragment.this.getActivity(), "Adding friend - " + item.getOwnerDisplayName(), Toast.LENGTH_SHORT).show();
        	adapter.remove(item);
        	adapter.notifyDataSetChanged();
        } else	{
        	Toast.makeText(AddingContactFragment.this.getActivity(), "Already friend - " + item.getOwnerDisplayName(), Toast.LENGTH_SHORT).show();
        }
        
      }

    });
  }	

}
