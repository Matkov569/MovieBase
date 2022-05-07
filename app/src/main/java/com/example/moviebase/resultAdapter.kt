package com.example.moviebase

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class resultAdapter(var viewModel: ViewModel, var returnTo:String, var context:Context): RecyclerView.Adapter<resultAdapter.Holder>() {

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
            if(deletable){
                it.findNavController().navigate(R.id.action_main_to_movie);
            }
            else {
                it.findNavController().navigate(R.id.action_searchResult_to_movie);
            }
        }
        if(deletable){
            holder.DelBtn.visibility=View.VISIBLE;
            //make del btn delete record
            holder.DelBtn.setOnClickListener {
                var builder = AlertDialog.Builder(context);
                builder.setTitle("Potwierdzenie usunięcia");
                builder.setMessage("Czy na pewno chcesz usunąć pozycję \"${records[position].title}\"?");
                builder.setPositiveButton("Tak"){ dialog, which ->
                    var ref = Firebase.database.getReference("movies");
                    ref.child("${FirebaseAuth.getInstance().currentUser?.uid}").child(records[position].id).removeValue();
                    Toast.makeText(context,"Usunięto",Toast.LENGTH_LONG).show();
                    notifyDataSetChanged();
                }
                builder.setNegativeButton("Nie"){diadol, which -> }
                builder.show();
            }
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