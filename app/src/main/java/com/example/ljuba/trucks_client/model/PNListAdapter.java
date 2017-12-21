package com.example.ljuba.trucks_client.model;
 import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ljuba.trucks_client.R;

import java.util.List;

/**
 * Created by VS-PC on 13.12.2017..
 */

public class PNListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<PutniNalog> putniNalozi;

    public PNListAdapter(Activity activity, List<PutniNalog> putniNalozi) {
        this.activity = activity;
        this.putniNalozi = putniNalozi;
    }

    @Override
    public int getCount() {
        return putniNalozi.size();
    }

    @Override
    public Object getItem(int location) {
        return putniNalozi.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.activity_list_view_row, null);

        //detalji putnog naloga
        TextView broj_pn = (TextView) convertView.findViewById(R.id.broj_pn);
        TextView vozilo = (TextView) convertView.findViewById(R.id.vozilo);
        TextView vozac = (TextView) convertView.findViewById(R.id.vozac);
        TextView status = (TextView) convertView.findViewById(R.id.status);

        PutniNalog pn = putniNalozi.get(position);

        // broj pn
        broj_pn.setText(pn.getBrojPN());
        vozilo.setText(pn.getVozilo());
        vozac.setText(pn.getVozac());
        status.setText(pn.getStatus());

        return convertView;
    }
}
