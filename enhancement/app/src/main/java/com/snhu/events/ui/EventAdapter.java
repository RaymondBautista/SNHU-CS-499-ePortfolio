/**
 * Events Mobile Application
 *
 * File: EventAdapter.java
 *
 * Transforms Event data model
 * to UI manageable card items
 * and enable "Delete" and
 * "Tap to Edit" logic
 * Separated into two lists:
 * today's and upcoming events
 * and past events under the dropdown
 *
 * Last Modified: 2026-03-22
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.snhu.events.R;
import com.snhu.events.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Separated upcoming items from past items to leverage different lists
    private List<ListItem> upcomingItems = new ArrayList<>();
    private List<ListItem> pastItems = new ArrayList<>();

    // State of the past items dropdown
    private boolean isPastExpanded = false;
    private final OnEventClickListener listener;

    // Interface to handle clicks back in MainActivity
    public interface OnEventClickListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    // Updated set items to adapt to two distinct lists
    public void setData(List<ListItem> upcoming, List<ListItem> past) {
        this.upcomingItems = upcoming;
        this.pastItems = past;
        notifyDataSetChanged();
    }

    // Return the event type element int constant
    @Override
    public int getItemViewType(int position) {
        ListItem item;

        // Safety: If position is the toggle row
        if (!pastItems.isEmpty() && position == upcomingItems.size()) {
            return ListItem.TYPE_COLLAPSIBLE;
        }

        // Identify which list to pull from
        if (position < upcomingItems.size()) {
            item = upcomingItems.get(position);
        } else {
            // This only hits if isPastExpanded is true and position > upcomingItems.size()
            item = pastItems.get(position - upcomingItems.size() - 1);
        }

        return (item.event != null) ? ListItem.TYPE_EVENT : ListItem.TYPE_HEADER;
    }

    // Create view holder based on element type
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ListItem.TYPE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_date_header, parent, false));
            case ListItem.TYPE_COLLAPSIBLE:
                return new CollapsibleViewHolder(inflater.inflate(R.layout.item_past_events_header, parent, false));
            default:
                return new EventViewHolder(inflater.inflate(R.layout.item_event, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = null;

        // 1. Identify the Item
        if (pastItems.isEmpty() || position < upcomingItems.size()) {
            item = upcomingItems.get(position);
        } else if (position == upcomingItems.size()) {
            if (holder instanceof CollapsibleViewHolder) bindCollapsibleRow((CollapsibleViewHolder) holder);
            return;
        } else {
            int pastIndex = position - upcomingItems.size() - 1;
            if (pastIndex < pastItems.size()) item = pastItems.get(pastIndex);
        }

        // 2. Safe Binding
        if (item == null) return;

        if (holder instanceof EventViewHolder && item.event != null) {
            bindEventRow((EventViewHolder) holder, item.event);
        } else if (holder instanceof HeaderViewHolder && item.headerDate != null) {
            ((HeaderViewHolder) holder).txtHeader.setText(item.headerDate);
        }
    }

    // Bind Event row and listen user click to edit or delete
    private void bindEventRow(EventViewHolder evh, Event e) {
        evh.title.setText(e.name);
        evh.desc.setText(e.description);
        evh.time.setText(String.format("%s - %s", e.startTime, e.endTime));

        evh.itemView.setOnClickListener(v -> listener.onEdit(e));
        evh.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    // Bind dropdown list event row
    private void bindCollapsibleRow(CollapsibleViewHolder cvh) {
        // Rotate arrow based on state (180 degrees if expanded)
        cvh.imgArrow.setRotation(isPastExpanded ? 180f : 0f);

        // Show/Hide the entire past section on click
        cvh.itemView.setOnClickListener(v -> {
            isPastExpanded = !isPastExpanded;
            notifyDataSetChanged();
        });
    }

    // Return the number of items
    @Override
    public int getItemCount() {
        // If pastItems is empty, don't reserve a slot for the toggle row
        if (pastItems == null || pastItems.isEmpty()) {
            return (upcomingItems != null) ? upcomingItems.size() : 0;
        }

        // If past items exist, show them only if the toggle is expanded
        if (isPastExpanded) {
            return upcomingItems.size() + 1 + pastItems.size();
        } else {
            // Just the upcoming list + the toggle row itself
            return upcomingItems.size() + 1;
        }
    }

    // --- VIEW HOLDERS ---

    // Event date or today header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeader;
        HeaderViewHolder(View v) {
            super(v);
            txtHeader = v.findViewById(R.id.txtDateHeader);
        }
    }

    // Event card viewholder for the recycler view
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

    // Dropdown event card viewholder
    static class CollapsibleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgArrow;
        CollapsibleViewHolder(View v) {
            super(v);
            imgArrow = v.findViewById(R.id.imgArrow);
        }
    }
}
