package com.example.CafeteriaApp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.CafeteriaApp.Models.Addon;
import com.example.CafeteriaApp.R;

import java.util.List;

public class CustomProductOptionRvAdapter
        extends RecyclerView.Adapter<CustomProductOptionRvAdapter.Row> {

    // === Callback יחיד לשינוי סימון ===
    public interface OnCheckedChangeListener {
        void onCheckChanged(Addon item, int position, boolean isChecked);
    }

    private final LayoutInflater inflater;
    private final List<Addon> addons;
    private final OnCheckedChangeListener listener;

    public CustomProductOptionRvAdapter(Context c,
                                        List<Addon> addons,
                                        OnCheckedChangeListener l) {
        this.inflater = LayoutInflater.from(c);
        this.addons = addons;
        this.listener = l;
        setHasStableIds(true);
    }

    public List<Addon> getData() { return addons; }

    @Override public long getItemId(int position) { return position; }

    @NonNull
    @Override
    public Row onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.custom_lv_product_optinon, parent, false);
        v.setBackgroundResource(androidx.appcompat.R.drawable.abc_list_selector_holo_light);
        return new Row(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Row h, int position) {
        int realPosition = position + 1;

          if (realPosition >= addons.size()) return;

        Addon a = addons.get(realPosition);

        // Updated to use new getters
        h.lv_checkBox.setText("   " + a.getAddonName());
        h.tv_price.setText(a.getPriceText() + " +");

        // מניעת טריגר שווא בזמן מחזור ה-View
        h.lv_checkBox.setOnCheckedChangeListener(null);
        h.lv_checkBox.setChecked(a.isSelected());

        // שינוי מצב הסימון
        h.lv_checkBox.setOnCheckedChangeListener((button, isChecked) -> {
            a.setSelected(isChecked); // Update model via setter
            if (listener != null) {
                int pos = h.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onCheckChanged(a, pos, isChecked);
                }
            }
        });

        h.itemView.setOnClickListener(v -> h.lv_checkBox.toggle());
    }

    @Override
    public int getItemCount() {
        if (addons == null || addons.isEmpty()) return 0;
        return addons.size() - 1;
    }

    static class Row extends RecyclerView.ViewHolder {
        TextView tv_price;
        CheckBox lv_checkBox;

        Row(@NonNull View itemView) {
            super(itemView);
            tv_price    = itemView.findViewById(R.id.tv_price);
            lv_checkBox = itemView.findViewById(R.id.lv_checkBox);
        }
    }
}
