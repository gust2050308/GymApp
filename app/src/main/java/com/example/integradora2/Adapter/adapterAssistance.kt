package com.example.integradora2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.integradora2.Model.Assistencias
import com.example.integradora2.databinding.AssistanceUserBinding

class adapterAssistance(private var items: MutableList<Assistencias>):
    RecyclerView.Adapter<adapterAssistance.ViewHolder>() {

    var onItemClick: ((Assistencias) -> Unit)? = null

    class ViewHolder(val binding: AssistanceUserBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AssistanceUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assis = items[position]
        with(holder.binding) {
            txtFecha.text = assis.entryDate
            txtEntrada.text = assis.entrance.substring(0, 5)
            txtSalida.text = assis.outside.substring(0, 5)
            txtDuarcion.text = assis.visitDuration
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(assis)
        }
    }

    fun addItems(newItems: List<Assistencias>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
