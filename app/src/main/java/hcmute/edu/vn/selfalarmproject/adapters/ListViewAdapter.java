package hcmute.edu.vn.selfalarmproject.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.SongModel;

public class ListViewAdapter extends BaseAdapter {
    private List<SongModel> songs;
    public ListViewAdapter(List<SongModel> songs) {
        this.songs = songs;
    }

    @Override
    public int getCount() {
//        return Player.getPlayers().size();
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
//        return Player.getPlayers().get(position);
        return songs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewProduct;
        if (convertView == null) {
            viewProduct = View.inflate(parent.getContext(), R.layout.list_item, null);
        } else viewProduct = convertView;

        SongModel player = (SongModel) getItem(position);
        ((ImageView) viewProduct.findViewById(R.id.playerImg)).setImageResource(player.getImgID());
        ((TextView) viewProduct.findViewById(R.id.playerName)).setText(player.getTitle());
        ((TextView) viewProduct.findViewById(R.id.playerCountry)).setText(String.format(player.getAuthor()));
        ((TextView) viewProduct.findViewById(R.id.playerBirth)).setText(String.format(player.getDuration()));


        return viewProduct;
    }
}
