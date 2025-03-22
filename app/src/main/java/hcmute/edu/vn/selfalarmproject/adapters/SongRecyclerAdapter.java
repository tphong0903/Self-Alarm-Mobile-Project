package hcmute.edu.vn.selfalarmproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import hcmute.edu.vn.selfalarmproject.R;
import hcmute.edu.vn.selfalarmproject.models.SongModel;

public class SongRecyclerAdapter extends RecyclerView.Adapter<SongRecyclerAdapter.SongViewHolder> {
    Context context;
    List<SongModel> songList;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SongRecyclerAdapter(Context context, List<SongModel> songList, OnItemClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        SongModel songModel = songList.get(position);
        Glide.with(context).load(songModel.getImageURL()).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image);
        holder.title.setText(songModel.getTitle());
        holder.artist.setText(songModel.getAuthor());
        holder.duration.setText("99:99");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder{
        TextView title, artist, duration;
        ImageView image;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.songImage);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            duration = itemView.findViewById(R.id.songDuration);
        }
    }
}
