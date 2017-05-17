package com.bizconnectivity.gino.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bizconnectivity.gino.R;
import com.bizconnectivity.gino.adapters.PulseRecyclerListAdapter;
import com.bizconnectivity.gino.services.EventbriteAPI;
import com.bizconnectivity.gino.models.EventList;
import com.bizconnectivity.gino.models.PulseList;
import com.bizconnectivity.gino.models.Pulses;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bizconnectivity.gino.Constant.EVENTBRITE_API_URL;
import static com.bizconnectivity.gino.Constant.EVENTBRITE_EXPAND;
import static com.bizconnectivity.gino.Constant.EVENTBRITE_LOCATION;
import static com.bizconnectivity.gino.Constant.EVENTBRITE_SORT_BY;
import static com.bizconnectivity.gino.Constant.EVENTBRITE_START_DATE;
import static com.bizconnectivity.gino.Constant.EVENTBRITE_TOKEN;

public class PulseFragment extends Fragment implements PulseRecyclerListAdapter.AdapterCallBack{

    @BindView(R.id.pulses_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    Retrofit retrofit;
    EventbriteAPI eventbriteAPI;
    PulseRecyclerListAdapter pulseListAdapter;
    List<PulseList> pulseList;
    Pulses pulses;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    Realm realm;

    public PulseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pulse, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Layout Binding
        ButterKnife.bind(this, view);

        // Initial Retrofit with Eventbrite API
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(EVENTBRITE_API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        eventbriteAPI = retrofit.create(EventbriteAPI.class);

        mSwipeRefreshLayout.setRefreshing(true);

        // Call Eventbrite API
        Call<Pulses> call = eventbriteAPI.getEvents(
                EVENTBRITE_TOKEN,
                EVENTBRITE_SORT_BY,
                EVENTBRITE_LOCATION,
                EVENTBRITE_START_DATE,
                EVENTBRITE_EXPAND);

        call.enqueue(new Callback<Pulses>() {
            @Override
            public void onResponse(Call<Pulses> call, Response<Pulses> response) {

                pulses = response.body();
                retrieveEventFromEventbrite(pulses);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<Pulses> call, Throwable t) {

                Log.d("TAG", "onFailure: " + t);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Call<Pulses> call = eventbriteAPI.getEvents(
                        EVENTBRITE_TOKEN,
                        EVENTBRITE_SORT_BY,
                        EVENTBRITE_LOCATION,
                        EVENTBRITE_START_DATE,
                        EVENTBRITE_EXPAND);

                call.enqueue(new Callback<Pulses>() {
                    @Override
                    public void onResponse(Call<Pulses> call, Response<Pulses> response) {

                        pulses = response.body();
                        retrieveEventFromEventbrite(pulses);

                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<Pulses> call, Throwable t) {

                        Log.d("TAG", "onFailure: " + t);
                    }
                });
            }
        });
    }

    private void retrieveEventFromEventbrite(Pulses pulses) {

        pulseList = new ArrayList<>();

        for (EventList event : pulses.getEvents()) {

            PulseList pulse = new PulseList();

            String pulseID = event.getId() != null ? event.getId() : "";
            pulse.setPulseID(pulseID);

            String pulseTitle = event.getName() != null ? event.getName().getText() : "";
            pulse.setPulseTitle(pulseTitle);

            String pulseDescription = event.getDescription() != null ? event.getDescription().getText() : "";
            pulse.setPulseDescription(pulseDescription);

            String pulseLocation = event.getVenue() != null ? event.getVenue().getName() : "";
            pulse.setPulseLocation(pulseLocation);

            String pulseDatetime = event.getEnd() != null ? simpleDateFormat.format(event.getEnd().getLocal()) : "";
            pulse.setPulseDatetime(pulseDatetime);

            String pulseImage = event.getLogo() != null ? event.getLogo().getUrl() : "";
            pulse.setPulseImage(pulseImage);

            String pulseOrganizer = event.getOrganizer() != null ? event.getOrganizer().getName() : "";
            pulse.setPulseOrganizer(pulseOrganizer);

            String pulseURL = event.getUrl() != null ? event.getUrl() : "";
            pulse.setPulseURL(pulseURL);

            pulseList.add(pulse);
        }

        // Pulse RecyclerView
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        pulseListAdapter = new PulseRecyclerListAdapter(getContext(), pulseList, this);
        mRecyclerView.setAdapter(pulseListAdapter);
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_pulse, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.action_search:
//
//                break;
//
//            case R.id.action_filter:
//
//                break;
//
//            default:
//                break;
//        }
//
//        return true;
//    }

    @Override
    public void adapterOnClick(int adapterPosition) {

    }
}
