package com.example.integradora2.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.integradora2.DataUser
import com.example.integradora2.Model.Assistencias
import com.example.integradora2.databinding.AssistanceUserBinding


class AsistenciaAdapter (val datos: List<Assistencias>)
    : RecyclerView.Adapter<AsistenciaAdapter.ViewHolder>() {

        var onItemClick:((Assistencias)-> Unit)? = null

    class ViewHolder(var binding: AssistanceUserBinding)
        : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AssistanceUserBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return datos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = datos[position]
        with(holder.binding){
            txtFecha.text = element.entryDate
            txtEntrada.text = element.entrance
            txtSalida.text = element.outside
            txtDuarcion.text = element.visitDuration
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(element)
        }
    }
}