/**
 * Events Mobile Application
 *
 * File: EventAdapter.java
 *
 * Transforms Event data model
 * to UI manageable card items
 * and enable "Delete" and
 * "Tap to Edit" logic
 *
 * Last Modified: 2026-02-21
 * Version: 1.0.0
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.snhu.events.R;
import com.snhu.events.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> items = new ArrayList<>();
    private final OnEventClickListener listener;

    // Interface to handle clicks back in MainActivity
    public interface OnEventClickListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Returns 0 for Header, 1 for Event card
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == ListItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).txtHeader.setText(item.headerDate);
        } else if (holder instanceof EventViewHolder) {
            EventViewHolder evh = (EventViewHolder) holder;
            Event e = item.event;

            evh.title.setText(e.name);
            evh.desc.setText(e.description);
            evh.time.setText(e.startTime + " - " + e.endTime);

            // Handle Tapping the card to Edit
            evh.itemView.setOnClickListener(v -> listener.onEdit(e));

            // Handle Tapping the Delete icon
            evh.btnDelete.setOnClickListener(v -> listener.onDelete(e));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- VIEW HOLDERS ---

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeader;
        HeaderViewHolder(View v) {
            super(v);
            txtHeader = v.findViewById(R.id.txtDateHeader);
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, time;
        ImageButton btnDelete;

        EventViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.txtEventTitle);
            desc = v.findViewById(R.id.txtEventDesc);
            time = v.findViewById(R.id.txtEventTime);
            btnDelete = v.findViewById(R.id.btnDeleteEvent);
        }
    }
}
