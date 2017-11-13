package wwckl.projectmiki.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import wwckl.projectmiki.R;
import wwckl.projectmiki.models.Item;

/**
 * Created by amanda on 2017-08-06.
 */

public class EditItemAdapter extends ArrayAdapter<Item> {

    public EditItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Item editItem = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.layout_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvEditItemDesc = (TextView) convertView.findViewById(R.id.tvItemDescription);
            holder.tvEditItemAmount = (TextView) convertView.findViewById(R.id.tvItemAmount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag(); // reduces number of findViewById calls
        }

        // Populate the data into the template view using the data object
        DecimalFormat df = new DecimalFormat("0.00");
        holder.tvEditItemDesc.setText(editItem.getDescription());
        holder.tvEditItemAmount.setText(df.format(editItem.getPrice()));
        
        // Return the completed view to render on screen
        return convertView;
    }

    private static class ViewHolder {
        // Lightweight inner class that holds direct references to all inner views from a row
        public TextView tvEditItemDesc;
        public TextView tvEditItemAmount;
    }

}
