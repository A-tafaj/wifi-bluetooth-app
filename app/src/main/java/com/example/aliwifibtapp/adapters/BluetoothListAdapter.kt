package com.example.aliwifibtapp.adapters

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aliwifibtapp.R
import com.example.aliwifibtapp.databinding.BluetoothItemBinding

class BluetoothListAdapter : RecyclerView.Adapter<BluetoothListAdapter.ViewHolder>() {

    private var bluetoothsList: ArrayList<String> = ArrayList()

    fun setMyListData(bluetoothList: String) {
        bluetoothsList.add(bluetoothList)
        notifyItemInserted(itemCount - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.bluetooth_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bluetoothsList[position]
        holder.bind(item)
    }

    override fun getItemCount() = bluetoothsList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        private val binding = BluetoothItemBinding.bind(itemView)
        private var bluetoothDevice: String? = null
        fun bind(item: String) {
            bluetoothDevice = item
            binding.bluetoothTv.text = bluetoothDevice
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(this.adapterPosition, 0, 0, bluetoothDevice)
            menu?.add(this.adapterPosition, 0, 0, "Pair")
            menu?.add(this.adapterPosition, 0, 0, "Unpair")
        }
    }

}