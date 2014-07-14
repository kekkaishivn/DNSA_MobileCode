package com.dsna.contact;

import java.util.ArrayList;
import java.util.List;

import com.dsna.android.main.R;
import com.dsna.entity.SocialProfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactElementArrayAdapter extends ArrayAdapter<SocialProfile> {
  private final Context context;

  public ContactElementArrayAdapter(Context context, List<SocialProfile> values) {
    super(context, R.layout.dsna_message_row, values);
    this.context = context;
    //conversations.addAll(values);
    //System.out.println(conversations.size());
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.dsna_contact_row, parent, false);
    TextView usernameView = (TextView) rowView.findViewById(R.id.secondLine);
    TextView nameView = (TextView) rowView.findViewById(R.id.firstLine);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    
    SocialProfile profile = getItem(position);
    usernameView.setText(profile.getOwnerUsername() + " - " + profile.getAge() + " - " + profile.getAbout());
    nameView.setText(profile.getOwnerDisplayName());
    // change the icon for Windows and iPhone
    imageView.setImageResource(R.drawable.ic_smile);

    return rowView;
  }
} 
