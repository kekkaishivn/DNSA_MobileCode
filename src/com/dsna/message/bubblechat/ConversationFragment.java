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

package com.dsna.message.bubblechat;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dsna.android.main.MainActivity;
import com.dsna.contact.AddingContactFragment;
import com.dsna.entity.Message;
import com.dsna.android.main.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.demo.fragment.*;

public class ConversationFragment extends BaseFragment {

    protected ScrollView mScrollView;
  	private com.dsna.message.bubblechat.MessageArrayAdapter adapter;
  	private ListView lv;
  	private EditText editText1;
  	private final String conversationName;
  	private final List<OneMessage> msgs = new ArrayList<OneMessage>();
  	
  	public ConversationFragment(String conversationName, List<OneMessage> msgs)	{
  		this.conversationName = conversationName;
  		this.msgs.addAll(msgs);
  	}
  	
    @Override
    public int getTitleResourceId() {
        return R.string.dsna_message_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		return inflater.inflate(R.layout.activity_discuss, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    		lv = (ListView) getView().findViewById(R.id.listView1);

    		adapter = new MessageArrayAdapter(getActivity().getApplicationContext(), R.layout.listitem_discuss, msgs);

    		lv.setAdapter(adapter);

    		editText1 = (EditText) getView().findViewById(R.id.editText1);
    		editText1.setOnKeyListener(new OnKeyListener() {
    			public boolean onKey(View v, int keyCode, KeyEvent event) {
    				// If the event is a key-down event on the "enter" button
    				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
    					// Perform action on key press
    					Message msg = ((MainActivity)ConversationFragment.this.getActivity()).sendMessageToConversation(conversationName, editText1.getText().toString());
    					if (msg!=null)	{
	    					adapter.add(new OneMessage(false, editText1.getText().toString()));
	    					editText1.setText("");
	    					return true;
    					}
    				}
    				return false;
    			}
    		});

    }

    public void add(String otherMessage)	{
    		adapter.add(new OneMessage(true, otherMessage));
    }

}
