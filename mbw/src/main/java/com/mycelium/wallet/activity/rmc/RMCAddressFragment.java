package com.mycelium.wallet.activity.rmc;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.common.base.Preconditions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mycelium.wallet.MbwManager;
import com.mycelium.wallet.R;
import com.mycelium.wallet.event.AccountChanged;
import com.mycelium.wallet.event.BalanceChanged;
import com.mycelium.wallet.event.ReceivingAddressChanged;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by elvis on 23.06.17.
 */

public class RMCAddressFragment extends Fragment {

    private View _root;
    @BindView(R.id.switcher)
    protected ViewFlipper switcher;

    @BindView(R.id.graph)
    protected GraphView graphView;

    @BindView(R.id.active_in_day_progress)
    protected ProgressBar activeProgressBar;

    @BindView(R.id.active_in_day)
    protected TextView activeInDay;

    @BindView(R.id.tvLabel)
    protected TextView tvLabel;

    @BindView(R.id.tvAddress)
    protected TextView tvAddress;

    private MbwManager _mbwManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _mbwManager = MbwManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _root = Preconditions.checkNotNull(inflater.inflate(R.layout.rmc_address_view, container, false));
        ButterKnife.bind(this, _root);
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Day");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("USD");
        return _root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graphView.addSeries(series);
        updateUi();
    }

    private void activeBtnProgress() {
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(2017, 7, 12);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(2018, 7, 12);
        int progress = (int) TimeUnit.MILLISECONDS.toDays(Calendar.getInstance().getTimeInMillis() - calendarStart.getTimeInMillis());
        int total = (int) TimeUnit.MILLISECONDS.toDays(calendarEnd.getTimeInMillis() - calendarStart.getTimeInMillis());
        activeProgressBar.setProgress(progress);
        activeProgressBar.setMax(total);
        activeInDay.setText(getString(R.string.rmc_active_in_159_days, total - progress));
    }

    @OnClick(R.id.show_graph)
    void clickShowGraph() {
        switcher.showNext();
    }

    @OnClick(R.id.show_stats)
    void clickShowStats() {
        switcher.showPrevious();
    }

    @OnClick(R.id.rmc_active_set_reminder)
    void setReminderClick() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rmc_reminder, null, false);
        new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addEventToCalendar();
                    }
                }).setNegativeButton("CANCEL", null)
                .create()
                .show();
    }

    private void addEventToCalendar() {
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues values = new ContentValues();

        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, 1);
        long dtstart = start.getTimeInMillis();
        values.put(CalendarContract.Events.DTSTART, dtstart);
        values.put(CalendarContract.Events.TITLE, "RMC activate");
        values.put(CalendarContract.Events.DESCRIPTION, "Activate RMC for maximum Return");

        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());

// Default calendar
        values.put(CalendarContract.Events.CALENDAR_ID, 1);

// Set Period for 1 Hour
        values.put(CalendarContract.Events.DURATION, "+P1H");
        values.put(CalendarContract.Events.HAS_ALARM, 1);

// Insert event to calendar
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    @Subscribe
    public void receivingAddressChanged(ReceivingAddressChanged event) {
        updateUi();
    }

    private void updateUi() {
        activeBtnProgress();
        String name = _mbwManager.getMetadataStorage().getLabelByAccount(_mbwManager.getSelectedAccount().getId());
        tvLabel.setText(name);
        tvAddress.setText(_mbwManager.getSelectedAccount().getReceivingAddress().get().toString());
    }

    @Subscribe
    public void accountChanged(AccountChanged event) {
        updateUi();
    }

    @Subscribe
    public void balanceChanged(BalanceChanged event) {
        updateUi();
    }
}