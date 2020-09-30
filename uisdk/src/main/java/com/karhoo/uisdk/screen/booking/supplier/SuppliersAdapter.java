package com.karhoo.uisdk.screen.booking.supplier;

import android.content.Context;
import android.view.ViewGroup;

import com.karhoo.sdk.api.model.Quote;
import com.karhoo.uisdk.base.BaseRecyclerAdapter;
import com.karhoo.uisdk.base.BaseRecyclerView;
import com.karhoo.uisdk.screen.booking.domain.supplier.PriceSort;
import com.karhoo.uisdk.screen.booking.domain.supplier.QtaSort;
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod;

import java.util.Collections;
import java.util.List;

public class SuppliersAdapter extends BaseRecyclerAdapter<Quote, SupplierListItemView> {

    private Context context;
    private boolean isPrebook;
    private static SortMethod sortMethod = SortMethod.ETA;

    public SuppliersAdapter(Context context) {
        this.context = context;
    }

    @Override
    protected SupplierListItemView onCreateItemView(ViewGroup parent, int viewType) {
        return new SupplierListItemView(context);
    }

    @Override
    public void onBindViewHolder(BaseRecyclerView<SupplierListItemView> holder, int position) {
        final SupplierListItemView view = holder.getView();
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

}
