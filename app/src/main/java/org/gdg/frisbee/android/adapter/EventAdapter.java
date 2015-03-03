/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.app.App;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.adapter
 * <p/>
 * User: maui
 * Date: 23.04.13
 * Time: 03:53
 */
public class EventAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Item> mEvents;

    public EventAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mEvents = new ArrayList<>();
    }

    public void addAll(Collection<? extends SimpleEvent> items) {
        for (SimpleEvent a : items) {
            mEvents.add(new Item(a));
        }
        notifyDataSetChanged();
    }

    public void add(SimpleEvent item) {
        mEvents.add(new Item(item));
        notifyDataSetChanged();
    }

    public void clear() {
        mEvents.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public SimpleEvent getItem(int i) {
        return mEvents.get(i).getEvent();
    }

    private Item getItemInternal(int i) {
        return mEvents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        
        ViewHolder holder;
        if (convertView != null && convertView.getTag() != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.list_event_item, viewGroup, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        Item item = getItemInternal(i);
        final SimpleEvent event = item.getEvent();

        if (event.getIconUrl() != null) {
//            holder.icon.setVisibility(View.VISIBLE);
            App.getInstance().getPicasso()
                .load(Const.URL_DEVELOPERS_GOOGLE_COM + event.getIconUrl())
                .into(holder.icon);
        } else {
//            holder.icon.setVisibility(View.GONE);
            holder.icon.setImageResource(R.drawable.icon);
        }

        holder.eventTitle.setText(event.getTitle());

        final LocalDateTime dateTime = event.getStart().toLocalDateTime();
        holder.eventDate.setText(dateTime.toString(DateTimeFormat.patternForStyle("S-", mContext.getResources().getConfiguration().locale)));
        holder.eventTime.setText(dateTime.toString(DateTimeFormat.patternForStyle("-S", mContext.getResources().getConfiguration().locale)));
        
        holder.eventLocation.setText(event.getLocation());

        if (event.getStart().isBefore(DateTime.now())) {
            holder.past.setVisibility(View.VISIBLE);
//            holder.shareButton.setVisibility(View.GONE);
        } else {
//            holder.shareButton.setOnClickListener(shareClickListener);
//            holder.shareButton.setTag(event);
            holder.past.setVisibility(View.GONE);
//            holder.shareButton.setVisibility(View.VISIBLE);
        }

        // That item will contain a special property that tells if it was freshly retrieved
        if (!item.isConsumed()) {
            item.setConsumed(true);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            convertView.startAnimation(animation);
        }

        return convertView;
    }

    private void openEventLink(SimpleEvent event) {
        String link = event.getGPlusEventLink();
        if (!TextUtils.isEmpty(link)) {
            if (!link.startsWith("http")) {
                link = "https://" + link;
            }
            openEventInExternalApp(link);
        } else {
            link = event.getLink();
            if (!TextUtils.isEmpty(link)) {
                if (!link.startsWith("http")) {
                    link = Const.URL_DEVELOPERS_GOOGLE_COM + link;
                }
                openEventInExternalApp(link);
            }
        }
    }

    public void openEventInExternalApp(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(uri));
        mContext.startActivity(i);
    }

    public void sort(Comparator<Item> eventComparator) {
        Collections.sort(mEvents, eventComparator);
        notifyDataSetChanged();
    }

    public static class Item {
        private SimpleEvent mEvent;
        private boolean mConsumed = false;

        public Item(SimpleEvent a) {
            mEvent = a;
            mConsumed = false;
        }

        public boolean isConsumed() {
            return mConsumed;
        }

        public void setConsumed(boolean mConsumed) {
            this.mConsumed = mConsumed;
        }

        public SimpleEvent getEvent() {
            return mEvent;
        }

        public void setEvent(Event mEvent) {
            this.mEvent = mEvent;
        }
    }

    static class ViewHolder {
        @InjectView(R.id.event_title) TextView eventTitle;
        @InjectView(R.id.event_date) TextView eventDate;
        @InjectView(R.id.event_time) TextView eventTime;
        @InjectView(R.id.event_location) TextView eventLocation;
        @InjectView(R.id.past) TextView past;
        @InjectView(R.id.icon) ImageView icon;
//        @InjectView(R.id.event_image_header) ImageView eventHeader;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
