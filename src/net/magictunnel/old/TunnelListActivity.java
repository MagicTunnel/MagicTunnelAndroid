/**
 * MagicTunnel DNS tunnel GUI for Android.
 * Copyright (C) 2011 Vitaly Chipounov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.magictunnel.old;

import java.util.ArrayList;
import java.util.TreeSet;

import net.magictunnel.R;
import net.magictunnel.R.id;
import net.magictunnel.R.layout;
import net.magictunnel.R.menu;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TunnelListActivity extends ListActivity {
    private TunnelListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TunnelListAdapter();

        mAdapter.addItem("Setup New DNS Tunnel", TunnelListAdapter.TYPE_CREATE_NEW);
        mAdapter.addItem("Available Tunnels", TunnelListAdapter.TYPE_SEPARATOR);

        for (int i = 1; i < 5; i++) {
            mAdapter.addItem("Tunnel " + i, TunnelListAdapter.TYPE_SETTING);
        }
        setListAdapter(mAdapter);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tunnellistpref_context, menu);
        TextView tv = (TextView)v;
        menu.setHeaderTitle(tv.getText());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
        case R.id.cfg_menu_delete:
            return true;
        case R.id.cfg_menu_change:
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    /**
     * Inspired from http://android.amberfog.com/?p=296
     * @author vitaly
     *
     */
    private class TunnelListAdapter extends BaseAdapter {

        public static final int TYPE_SETTING = 0;
        public static final int TYPE_SEPARATOR = 1;
        public static final int TYPE_CREATE_NEW = 2;
        public static final int TYPE_MAX_COUNT = TYPE_CREATE_NEW + 1;

        private LayoutInflater m_inflater;
        private ArrayList<SettingsItem> m_data = new ArrayList<SettingsItem>();

        public TunnelListAdapter() {
            m_inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(String label, int type) {
            SettingsItem item = new SettingsItem(label, type);
            m_data.add(item);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return m_data.get(position).m_type;
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }

        @Override
        public int getCount() {
            return m_data.size();
        }

        @Override
        public SettingsItem getItem(int position) {
            return m_data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            int type = getItemViewType(position);
            return (type != TYPE_SEPARATOR);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            int type = getItemViewType(position);
            System.out.println("getView " + position + " " + convertView + " type = " + type);
            if (convertView == null) {
                holder = new ViewHolder();

                switch (type) {
                case TYPE_CREATE_NEW:
                    convertView = m_inflater.inflate(R.layout.tunnelslistitem, null);
                    holder.textView = (TextView)convertView;
                    break;

                case TYPE_SETTING:
                    convertView = m_inflater.inflate(R.layout.tunnelslistitem, null);
                    holder.textView = (TextView)convertView;
                    registerForContextMenu(convertView);
                    break;

                case TYPE_SEPARATOR:
                    convertView = m_inflater.inflate(R.layout.tunnelslistitemseparator, null);
                    holder.textView = (TextView)convertView;
                    break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.textView.setText(m_data.get(position).getLabel());

            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView textView;
    }

    public static class SettingsItem implements Comparable<SettingsItem> {
        private String m_label;
        private Integer m_type;

        public String getLabel() {
            return m_label;
        }

        public void setLabel(String label) {
            this.m_label = label;
        }

        public Integer getType() {
            return m_type;
        }

        public void setType(Integer type) {
            this.m_type = type;
        }

        public SettingsItem(String label, int type) {
            m_label = label;
            m_type = type;
        }

        @Override
        public int compareTo(SettingsItem another) {
            if (m_type == another.m_type) {
                return m_label.compareTo(another.m_label);
            }
            return m_type.compareTo(another.m_type);
        }
    }

}

