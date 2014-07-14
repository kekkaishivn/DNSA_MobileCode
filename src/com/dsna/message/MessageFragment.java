package com.dsna.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;

import com.dsna.android.main.R;
import it.gmariotti.cardslib.demo.fragment.BaseFragment;

public class MessageFragment extends BaseFragment {

  protected ScrollView mScrollView;
	private ListView lv;
	private List<ConversationElement> conversations;
	private final AdapterView.OnItemClickListener onItemClick;
	
	public MessageFragment(List<ConversationElement> conversations, final AdapterView.OnItemClickListener onItemClick)	{
		this.conversations = conversations;
		this.onItemClick = onItemClick;
	}

  @Override
  public int getTitleResourceId() {
      return R.string.dsna_message_title;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  		return inflater.inflate(R.layout.dsna_message_fragment, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    lv = (ListView) getView().findViewById(R.id.listview);
    
    final ConversationElementArrayAdapter adapter = new ConversationElementArrayAdapter(getActivity().getApplicationContext(), conversations);
    lv.setAdapter(adapter);
    lv.setOnItemClickListener(onItemClick);
  }	

}
