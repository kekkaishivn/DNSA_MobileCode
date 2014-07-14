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

package it.gmariotti.cardslib.demo.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.dsna.util.DateTimeUtil;
import com.dsna.android.main.R;
import com.dsna.entity.Status;

import it.gmariotti.cardslib.demo.stock.ListStockAdapter;
import it.gmariotti.cardslib.demo.stock.Stock;
import it.gmariotti.cardslib.demo.stock.StockListLayout;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * This class provides a simple card as Google Now Stock
 *
 * @author Gabriele Mariotti (gabri.mariotti@gmail.com)
 */
public class NewFeedCard extends Card {
		private Status status;
		
    public NewFeedCard(Context context, Status status) {
        this(context, R.layout.card_newfeed_inner_content, status);
    }

    public NewFeedCard(Context context, int innerLayout, Status status) {
        super(context, innerLayout);
        this.status = status;
        init();
    }

    private void init() {
        //Add Header
        CardHeader header = new CardHeader(getContext());
        header.setButtonExpandVisible(true);
        header.setTitle(status.getOwnerDisplayName()); 
        addCardHeader(header);

        //Add expand
        CardExpand expand = new GoogleNowExpandCard(getContext());
        addCardExpand(expand);

        //Add onClick Listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=" , Toast.LENGTH_LONG).show();
            }
        });

        //Add swipe Listener
        setOnSwipeListener(new OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                Toast.makeText(getContext(), "Card removed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        TextView textView = (TextView) view.findViewById(R.id.card_newfeed_main_inner_lastupdate);
        textView.setText("Updated " + DateTimeUtil.getFormattedTimeStamp(status.getTimeStamp())); //should use R.string.

        TextView content = (TextView) view.findViewById(R.id.card_newfeed_main_inner_status);
        content.setText(status.getContent());

    }


    //------------------------------------------------------------------------------------------


}
