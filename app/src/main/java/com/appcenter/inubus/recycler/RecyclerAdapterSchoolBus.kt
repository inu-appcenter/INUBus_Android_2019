package com.appcenter.inubus.recycler

import androidx.databinding.ObservableBoolean
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.appcenter.inubus.databinding.RecyclerArrivalHeaderBinding
import com.appcenter.inubus.databinding.RecyclerArrivalItemBinding
import com.appcenter.inubus.databinding.RecyclerArrivalSeparatorBinding
import com.appcenter.inubus.model.RecyclerArrivalItem
import com.appcenter.inubus.util.ArrivalInfoDiffUtil


/**
 * Created by Minjae Son on 2018-08-07.
 * Updated by ByoungMean on 2019-09-24.
 */

class RecyclerAdapterSchoolBus : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>(){

    private var isShowing = ObservableBoolean(false)
    private val mArrivalItems = arrayListOf<RecyclerArrivalItem>()

    // ItemType에 따라 생성할 뷰홀더 객체 선택
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val type = RecyclerArrivalItem.ItemType.findByOrdinal(viewType)
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(type){
            RecyclerArrivalItem.ItemType.Header->
                ViewHolderArrivalHeader(RecyclerArrivalHeaderBinding.inflate(layoutInflater, parent, false))
            RecyclerArrivalItem.ItemType.SectionHeader->
                ViewHolderArrivalSection(RecyclerArrivalSeparatorBinding.inflate(layoutInflater, parent, false))
            RecyclerArrivalItem.ItemType.ArrivalInfo->
                ViewHolderArrivalItem(RecyclerArrivalItemBinding.inflate(layoutInflater, parent, false), isShowing, true)
        }
    }
    // ItemType에 따라 각 뷰 홀더에 데이터 연결
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when(mArrivalItems[position].itemType){
            RecyclerArrivalItem.ItemType.Header->{ }
            RecyclerArrivalItem.ItemType.SectionHeader->{
                (holder as ViewHolderArrivalSection).bind(mArrivalItems[position].sectionHeader!!)
            }
            RecyclerArrivalItem.ItemType.ArrivalInfo->{
//                val customInfo = mArrivalItems[position].arrivalInfo!!
                (holder as ViewHolderArrivalItem).bind(mArrivalItems[position].arrivalInfo!!)
            }
        }
    }
    // 아이템 개수 리턴
    override fun getItemCount(): Int = mArrivalItems.size
    // 아이템 타입 리턴
    override fun getItemViewType(position: Int): Int = mArrivalItems[position].itemType.ordinal

    fun applyDataSet(items: ArrayList<RecyclerArrivalItem>,favList : ArrayList<String?>) {
        mArrivalItems.clear()
        val newDataSet = ArrayList<RecyclerArrivalItem>()
        var count = 1
        var favOnOff = false
        val favsorted = favList.sortedWith(Comparator {o1, o2 ->
            o1!!.compareTo(o2!!)
        })

        items.forEach {
            for(element in favsorted){
                if (it.arrivalInfo!!.no == element) {
                    if(!favOnOff){
                        newDataSet.add(0,RecyclerArrivalItem("즐겨찾기"))
                        favOnOff = true
                    }
                    newDataSet.add(count, it)
                    count++
                }
            }
        }

        newDataSet.add(RecyclerArrivalItem("통학버스"))
        newDataSet.addAll(items)
        newDataSet.add(0,RecyclerArrivalItem())


        // 데이터 목록 업데이트
        val diffUtil = ArrivalInfoDiffUtil(mArrivalItems, newDataSet)
        val result = DiffUtil.calculateDiff(diffUtil)
        mArrivalItems.addAll(newDataSet)
        result.dispatchUpdatesTo(this)
    }

    // 아이템이 화면에 보일때만 Ticker가 작동하도록 설정
    override fun onViewAttachedToWindow(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        if(holder is ViewHolderArrivalItem){
            if(isShowing.get()){
                holder.startTick()
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if(holder is ViewHolderArrivalItem){
            holder.stopTick()
        }
    }
}