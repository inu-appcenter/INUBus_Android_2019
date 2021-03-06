package com.appcenter.inubus.recycler

import androidx.recyclerview.widget.RecyclerView
import com.appcenter.inubus.databinding.RecyclerArrivalSeparatorBinding

/**
 * Created by Minjae Son on 2018-08-25.
 */
class ViewHolderArrivalSection(private val mBinding : RecyclerArrivalSeparatorBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mBinding.root) {

    // recycler_arrival_separator.xml 바인딩
    fun bind(sectionText: String){
        mBinding.tvSection.text = sectionText
        if(sectionText == "통학버스"){
            mBinding.tvItemSubject1.text = "현재위치"
            mBinding.tvItemSubject2.text = "출발시간"
        }
    }
}