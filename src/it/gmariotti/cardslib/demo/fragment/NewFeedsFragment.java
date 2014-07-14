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

package it.gmariotti.cardslib.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;

import java.io.File;

import com.dsna.android.main.R;

import it.gmariotti.cardslib.demo.cards.GoogleNowStockCard;
import it.gmariotti.cardslib.demo.cards.NewFeedCard;
import it.gmariotti.cardslib.library.utils.BitmapUtils;
import it.gmariotti.cardslib.library.view.CardView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import com.dsna.android.main.R;
import com.dsna.entity.Status;

import it.gmariotti.cardslib.demo.cards.CustomExpandCard;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * List of expandable cards Example
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class NewFeedsFragment extends BaseFragment {
		
		private ArrayList<Status> feeds;
		private ArrayList<Card> cards;
		private CardArrayAdapter mCardArrayAdapter;
		
		public NewFeedsFragment(ArrayList<Status> feeds)	{
			this.feeds = feeds;
		}
	
    @Override
    public int getTitleResourceId() {
        return R.string.dsna_new_feeds_title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.demo_fragment_list_expand, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCards();
    }


    private void initCards() {

        //Init an array of Cards
        cards = new ArrayList<Card>();
        for (Status status : feeds){
        		Card card= new NewFeedCard(getActivity(), status);
            cards.add(0, card);
        }

        mCardArrayAdapter = new CardArrayAdapter(getActivity(),cards);
        
        CardListView listView = (CardListView) getActivity().findViewById(R.id.carddemo_list_expand);
        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }
    }
    
    public void addNewFeed(Status feed)	{
    	Card card= new NewFeedCard(getActivity(), feed);
    	mCardArrayAdapter.insert(card, 0);
    }


    /**
     * This method builds a standard header with a custom expand/collpase
     */
    private Card init_standard_header_with_expandcollapse_button_custom_area(String titleHeader,int i) {

        //Create a Card
        Card card = new Card(getActivity());

        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());

        //Set the header title
        header.setTitle(titleHeader);

        //Set visible the expand/collapse button
        header.setButtonExpandVisible(true);

        //Add Header to card
        card.addCardHeader(header);

        //This provides a simple (and useless) expand area
        CustomExpandCard expand = new CustomExpandCard(getActivity(),i);
        //Add Expand Area to Card
        card.addCardExpand(expand);

        //Just an example to expand a card
        if (i==2 || i==7 || i==9)
            card.setExpanded(true);

        //Swipe
        card.setSwipeable(true);

        //Animator listener
        card.setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
            @Override
            public void onExpandEnd(Card card) {
                Toast.makeText(getActivity(),"Expand "+card.getCardHeader().getTitle(),Toast.LENGTH_SHORT).show();
            }
        });

        card.setOnCollapseAnimatorEndListener(new Card.OnCollapseAnimatorEndListener() {
            @Override
            public void onCollapseEnd(Card card) {
                Toast.makeText(getActivity(),"Collpase " +card.getCardHeader().getTitle(),Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }

}

