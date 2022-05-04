package com.example.moviebase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class resultAdapter(var viewModel: ViewModel, var returnTo:String): RecyclerView.Adapter<resultAdapter.Holder>() {

    private var records = emptyList<movieRecord>();
    private var deletable = true;

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView){
        var Title: TextView
        var Card: CardView
        var DelBtn: ImageButton
        init{
            Title= itemView.findViewById(R.id.positionTitle)
            Card= itemView.findViewById(R.id.positionCard)
            DelBtn = itemView.findViewById(R.id.deletePosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listposition,parent,false) as View

        return  Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.Title.text=records[position].title;
        holder.Card.setOnClickListener {
            viewModel.imbdID = records[position].id;
            viewModel.returnTo=returnTo;
            it.findNavController().navigate(R.id.action_searchResult_to_movie);
        }
        if(deletable){
            holder.DelBtn.visibility=View.VISIBLE;
            //make del btn delete record
        }
        else {
            holder.DelBtn.visibility = View.GONE;
        }
    }

    fun setData(list:List<movieRecord>, deletable: Boolean = true){
        this.records=list;
        this.deletable = deletable;
        notifyDataSetChanged();
    }

    override fun getItemCount()=records.count()

}