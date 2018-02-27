package live.player.edge.com.playerapp.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import live.player.edge.com.playerapp.Models.Comments;
import live.player.edge.com.playerapp.Models.Winners;
import live.player.edge.com.playerapp.R;

/**
 * Created by Ashish on 27-02-2018.
 */

public class WinnerAdapter extends RecyclerView.Adapter<WinnerAdapter.MyViewHolder> {
    List<Winners> winners;
    Context context;

    public WinnerAdapter(Context context, List<Winners> winners){
        this.context = context;
        this.winners = winners;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_winner,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Picasso.with(context).load(winners.get(position).getPhotoUrl()).into(holder.winnerImage);
        holder.tvWinnerAmmount.setText(winners.get(position).getAmmount());
        holder.tvWinnerName.setText(winners.get(position).getUserName());
    }

    @Override
    public int getItemCount() {
        return winners.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView winnerImage;
        TextView tvWinnerAmmount, tvWinnerName;
        public MyViewHolder(View itemView) {
            super(itemView);
            winnerImage = itemView.findViewById(R.id.winner_image);
            tvWinnerAmmount = itemView.findViewById(R.id.tv_winner_amount);
            tvWinnerName = itemView.findViewById(R.id.tv_winner_name);
        }
    }
}
