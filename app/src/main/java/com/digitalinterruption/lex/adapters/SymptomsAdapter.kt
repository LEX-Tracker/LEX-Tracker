package com.digitalinterruption.lex.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.models.SymptomModel
import com.digitalinterruption.lex.ui.main.SymptomsFragment

class SymptomsAdapter(private val list: ArrayList<SymptomModel>, private val context: Context, val listener: MyItemSelected?) :
    RecyclerView.Adapter<SymptomsAdapter.MyViewHolder>() {

    var oldLowView: ImageView? = null
    var oldMedView: ImageView? = null
    var oldHighView: ImageView? = null


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val lowIntensity: TextView = itemView.findViewById(R.id.txt_low)
        val medIntensity: TextView = itemView.findViewById(R.id.txt_med)
        val highIntensity: TextView = itemView.findViewById(R.id.txt_high)
        val low: ImageView = itemView.findViewById(R.id.low)
        val med: ImageView = itemView.findViewById(R.id.medium)
        val high: ImageView = itemView.findViewById(R.id.high)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val viewHolder =
            LayoutInflater.from(parent.context).inflate(R.layout.item_symptom, parent, false)
        return MyViewHolder(viewHolder)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = list[position].symptom
        val intensity = list[position].intensity
        holder.lowIntensity.tag = "low$position"
        holder.medIntensity.tag = "med$position"
        holder.highIntensity.tag = "high$position"

        val yellow = R.color.darkYellow
        val green = R.color.green
        var color = if (list[position].symptom == "Bleeding") {
            yellow
        } else {
            green
        }

        if (intensity != "") {
            when (intensity) {
                "low" -> {
                    holder.low.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                }
                "med" -> {
                    holder.med.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                }
                "high" -> {
                    holder.high.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                }
                else -> {
                    holder.low.imageTintList = null
                    holder.med.imageTintList = null
                    holder.high.imageTintList = null
                }
            }
        } else {
            holder.low.imageTintList = null
            holder.med.imageTintList = null
            holder.high.imageTintList = null
        }

        SymptomsFragment.listSymptomsSelected.forEach {
            if (it.intensity != "") {
                if (it.symptom == list[position].symptom) {
                    when (it.intensity) {
                        "low" -> {
                            holder.low.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                        }
                        "med" -> {
                            holder.med.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                        }
                        "high" -> {
                            holder.high.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                        }
                        else -> {
                            holder.low.imageTintList = null
                            holder.med.imageTintList = null
                            holder.high.imageTintList = null
                        }
                    }
                }
            } else {
                holder.low.imageTintList = null
                holder.med.imageTintList = null
                holder.high.imageTintList = null
            }
        }



        holder.lowIntensity.setOnClickListener {
            oldLowView = holder.low
            oldMedView = holder.med
            oldHighView = holder.high

            setColors(holder.lowIntensity, position, context, color)
            if (position >= 0 && position < list.size) {
                listener?.myItemClicked(holder.lowIntensity, position, list)
            }
        }

        holder.medIntensity.setOnClickListener {
            oldLowView = holder.low
            oldMedView = holder.med
            oldHighView = holder.high

            setColors(holder.medIntensity, position, context, color)
            if (position >= 0 && position < list.size) {
                listener?.myItemClicked(holder.medIntensity, position, list)
            }
        }


        holder.highIntensity.setOnClickListener {
            oldLowView = holder.low
            oldMedView = holder.med
            oldHighView = holder.high

            setColors(holder.highIntensity, position, context, color)
            if (position >= 0 && position < list.size) {
                listener?.myItemClicked(holder.highIntensity, position, list)
            }

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setColors(currentView: TextView, position: Int, context: Context, color: Int) {
        when (currentView.tag) {
            "low$position" -> {
                oldMedView?.imageTintList = null
                oldHighView?.imageTintList = null
                oldLowView?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))


            }
            "med$position" -> {
                oldMedView?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                oldHighView?.imageTintList = null
                oldLowView?.imageTintList = null

            }
            else -> {
                oldMedView?.imageTintList = null
                oldHighView?.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, color))
                oldLowView?.imageTintList = null

            }
        }

    }

}

interface MyItemSelected {
    fun myItemClicked(currentView: TextView, position: Int, list: ArrayList<SymptomModel>)
}

