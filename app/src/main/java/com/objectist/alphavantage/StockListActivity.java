package com.objectist.alphavantage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.TimeSeriesResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * An activity representing a list of Stocks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link StockDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class StockListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private static final String TAG = StockListActivity.class.getSimpleName();
    public static final String [] tickers = {"UNH", "MA", "AMZN", "ADP", "BBVA"};
    private static String apiKey = "GJLF3CX2MZ6ATP09";
    private static int timeout = 5000;
    private static AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
    private static TimeSeries stockTimeSeries = new TimeSeries(apiConnector);
    private static SimpleItemRecyclerViewAdapter mSimpleItemRecyclerViewAdapter;
    private List<TimeSeriesResponse> timeSeriesResponseList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeSeriesResponseList.clear();
                StockContent.ITEMS.clear();
                StockContent.ITEM_MAP.clear();
                mSimpleItemRecyclerViewAdapter.notifyDataSetChanged();
                try {
                    new RetrieveStocksTask().execute(tickers).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        if (findViewById(R.id.stock_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = findViewById(R.id.stock_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);
        try {
            new RetrieveStocksTask().execute(tickers).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mSimpleItemRecyclerViewAdapter = new SimpleItemRecyclerViewAdapter(this, StockContent.ITEMS, mTwoPane);
        recyclerView.setAdapter(mSimpleItemRecyclerViewAdapter);
    }

    private static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final StockListActivity mParentActivity;
        private final List<StockContent.StockItem> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StockContent.StockItem item = (StockContent.StockItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(StockDetailFragment.ARG_ITEM_ID, item.id);
                    StockDetailFragment fragment = new StockDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.stock_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, StockDetailActivity.class);
                    intent.putExtra(StockDetailFragment.ARG_ITEM_ID, item.id);

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(StockListActivity parent,
                                      List<StockContent.StockItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.stock_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).id);
            holder.mContentView.setText(mValues.get(position).content);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = view.findViewById(R.id.id_text);
                mContentView = view.findViewById(R.id.content);
            }
        }
    }

    private class RetrieveStocksTask extends AsyncTask<String, Void, List<TimeSeriesResponse>> {

        @Override
        protected List<TimeSeriesResponse> doInBackground(String... tickers) {
            Arrays.sort(tickers);
            for (String ticker : tickers) {
                try {
                    timeSeriesResponseList.add(stockTimeSeries.daily(ticker, OutputSize.COMPACT));
                } catch (AlphaVantageException e) {
                    //TODO AV error processing
                }
            }
            return timeSeriesResponseList;
        }

        @Override
        protected void onPostExecute(List<TimeSeriesResponse> responses) {
            super.onPostExecute(responses);
            StockContent.update(responses);
            mSimpleItemRecyclerViewAdapter.notifyDataSetChanged();
        }
    }
}
