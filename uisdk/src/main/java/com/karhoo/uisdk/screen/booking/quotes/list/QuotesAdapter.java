package com.karhoo.uisdk.screen.booking.quotes.list;

import android.content.Context;
import android.view.ViewGroup;

import com.karhoo.sdk.api.model.Quote;
import com.karhoo.uisdk.base.BaseRecyclerAdapter;
import com.karhoo.uisdk.base.BaseRecyclerView;
import com.karhoo.uisdk.screen.booking.domain.quotes.PriceSort;
import com.karhoo.uisdk.screen.booking.domain.quotes.QtaSort;
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod;
import com.karhoo.uisdk.screen.booking.quotes.list.QuotesListItemView;

import java.util.Collections;
import java.util.List;

public class QuotesAdapter extends BaseRecyclerAdapter<Quote, QuotesListItemView> {

    private Context context;
    private boolean isPrebook;
    private static SortMethod sortMethod = SortMethod.PRICE;

    public QuotesAdapter(Context context) {
        this.context = context;
    }

    @Override
    protected QuotesListItemView onCreateItemView(ViewGroup parent, int viewType) {
        return new QuotesListItemView(context);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerView<QuotesListItemView> holder, int position) {
        final QuotesListItemView view = holder.getView();
        final Quote vehicleDetails = getItems().get(position);

        view.bind(position, vehicleDetails, isPrebook, getItemClickListener());
    }

    public void setSelectedSortMethod(final SortMethod selectedSortMethod) {
        sortMethod = selectedSortMethod;
        notifyDataSetChanged();
    }

    public void prebook(boolean isPrebook) {
        this.isPrebook = isPrebook;
    }

    @Override
    public void setItems(List<Quote> items) {
        switch (sortMethod) {
            case ETA:
                Collections.sort(items, new QtaSort());
                break;
            case PRICE:
                Collections.sort(items, new PriceSort());
                break;
        }
        super.setItems(items);
    }

    public void refreshItemsAndAnimateNewOnes(List<Quote> allQuotes, List<String> newQuoteIds) {
        switch (sortMethod) {
            case ETA:
                Collections.sort(allQuotes, new QtaSort());
                break;
            case PRICE:
                Collections.sort(allQuotes, new PriceSort());
                break;
        }

        super.setItemsWithoutRefreshing(allQuotes);

        // In case the list is empty, then refresh it to delete the potential previous shown items
        if(allQuotes.size() == 0) {
            notifyDataSetChanged();
        } else {
            for (int i = 0; i < allQuotes.size(); i++) {
                if (newQuoteIds.contains(allQuotes.get(i).getId())) {
                    notifyItemInserted(i);
                }
            }
        }


    }

}
