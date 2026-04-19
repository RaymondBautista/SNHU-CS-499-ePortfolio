/**
 * Events Mobile Application
 *
 * File: SearchViewModel.java
 *
 * Handles the search filtering,
 * sorting, and querying logic
 * for the search screen.
 *
 * Last Modified: 2026-04-04
 *
 * Author: Raymond Bautista
 */

package com.snhu.events.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.snhu.events.model.Event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchViewModel extends AndroidViewModel {

    // MutableLiveData list of events that updates in real time based on user search input
    private final MutableLiveData<List<Event>> searchResults = new MutableLiveData<>();

    /**
     * Optimized LinkedHashMap to achieve O(1) time complexity
     * for search and delete while maintaining insertion order
     */
    private final Map<String, Event> eventMap = new LinkedHashMap<>();

    // Stores the current search query to refresh the list if the data changes
    private String currentQuery = "";

    public SearchViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Event>> getSearchResults() {
        return searchResults;
    }

    /**
     * Updates the local cache from the database observer.
     * Complexity: O(n) to populate the map.
     */
    public void updateRawData(List<Event> events) {
        eventMap.clear();
        // Insert all events from the list in the Hash Map
        if (events != null) {
            for (Event e : events) {
                eventMap.put(e.id, e);
            }
        }
        // Refresh the search results based on the new data
        performSearch(currentQuery);
    }

    /**
     * O(1) Optimistic Delete.
     * Removes item from local cache instantly for a smooth UI experience.
     */
    public void deleteEventOptimistically(String eventId) {
        if (eventMap.containsKey(eventId)) {
            eventMap.remove(eventId);
            performSearch(currentQuery);
        }
    }

    /**
     * Core Search Logic
     * Filtering: O(n * m) using KMP
     * Sorting: O(n log n) using Merge Sort
     */
    public void performSearch(String query) {
        // Creates a new empty list if there are no events
        this.currentQuery = (query == null) ? "" : query.trim();
        if (currentQuery.isEmpty()) {
            searchResults.setValue(new ArrayList<>());
            return;
        }

        List<Event> filtered = new ArrayList<>();

        // Use the Map values to iterate and filter using KMP algorithm
        for (Event e : eventMap.values()) {
            if (isMatch(e.name, currentQuery) || isMatch(e.description, currentQuery)) {
                filtered.add(e);
            }
        }

        // Apply Merge Sort for chronological ascending order
        if (!filtered.isEmpty()) {
            mergeSort(filtered, 0, filtered.size() - 1);
        }

        searchResults.setValue(filtered);
    }

    // Knuth-Morris-Pratt (KMP) Pattern Search Algorithm Implementation
    private boolean isMatch(String text, String pattern) {
        // Return false if input text is empty
        if (text == null) return false;

        // Normalize input text by converting to lower-case
        String t = text.toLowerCase();
        String p = pattern.toLowerCase();


        int n = t.length();
        int m = p.length();
        if (m == 0) return true;
        if (m > n) return false;    // Exit if the patter is larger than the text

        int[] lps = computeLPSArray(p);
        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            // Move to the next character if they match
            if (p.charAt(j) == t.charAt(i)) {
                i++; j++;
            }
            // Match Found. All characters on the pattern are equal to the same length substring
            if (j == m) return true;
            else if (i < n && p.charAt(j) != t.charAt(i)) {
                if (j != 0) j = lps[j - 1]; // Use LPS to skip unnecessary comparisons
                else i++;
            }
        }
        return false;
    }

    // Implement Longest Proper Prefix Array
    private int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        while (i < pattern.length()) {
            // If characters match, increment size of LPS and move to the next position
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                // If there is a mismatch
                // Update len to the previous lps to avoid unnecessary comparisons
                if (len != 0) len = lps[len - 1];
                // If doesn't match, set lps to compare from the beginning of the pattern
                else { lps[i] = 0; i++; }
            }
        }
        return lps;
    }

    // Merge Sort Algorithm Implementation
    private void mergeSort(List<Event> list, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;

            // Divide: Recursively split the list into halves
            mergeSort(list, left, mid);
            mergeSort(list, mid + 1, right);

            // Conquer: Merge the sorted halves
            merge(list, left, mid, right);
        }
    }

    private void merge(List<Event> list, int left, int mid, int right) {
        // Create temporary lists for the two halves
        List<Event> leftSide = new ArrayList<>(list.subList(left, mid + 1));
        List<Event> rightSide = new ArrayList<>(list.subList(mid + 1, right + 1));

        int i = 0, j = 0, k = left;

        // Compare elements and merge in ascending order
        while (i < leftSide.size() && j < rightSide.size()) {
            // Using String compareTo on date format "yyyy/MM/dd"
            if (leftSide.get(i).date.compareTo(rightSide.get(j).date) <= 0) {
                list.set(k++, leftSide.get(i++));
            } else {
                list.set(k++, rightSide.get(j++));
            }
        }

        // Clean up remaining elements
        while (i < leftSide.size()) list.set(k++, leftSide.get(i++));
        while (j < rightSide.size()) list.set(k++, rightSide.get(j++));
    }
}
