package com.dsna.message;

import java.util.ArrayList;
import java.util.List;

import com.dsna.android.main.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationElementArrayAdapter extends ArrayAdapter<ConversationElement> {
  private final Context context;

  public ConversationElementArrayAdapter(Context context, List<ConversationElement> values) {
    super(context, R.layout.dsna_message_row, values);
    this.context = context;
    //conversations.addAll(values);
    //System.out.println(conversations.size());
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.dsna_message_row, parent, false);
    TextView msgView = (TextView) rowView.findViewById(R.id.secondLine);
    TextView nameView = (TextView) rowView.findViewById(R.id.firstLine);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    msgView.setText(getItem(position).getLastMsg());
    nameView.setText(getItem(position).getName());
    // change the icon for Windows and iPhone
    imageView.setImageResource(R.drawable.ic_smile);

    return rowView;
  }
} 
