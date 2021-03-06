package com.appcenter.inubus.fragment

import android.content.Context
import android.content.Intent
import androidx.databinding.Observable
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.appcenter.inubus.R
import com.appcenter.inubus.activity.MainActivity
import com.appcenter.inubus.recycler.RecyclerAdapterArrival
import com.appcenter.inubus.util.LocalIntent
import com.appcenter.inubus.util.Singleton
import kotlinx.android.synthetic.main.fragment_swipepull_recycler.*


/**
 * Created by Minjae Son on 2018-08-07.
 * Updated by ByoungMean on 2019-07-27.
 */

class ArrivalFragmentTab : androidx.fragment.app.Fragment(){

    private var isShowing = false
    private lateinit var mStrBusStop: String
    private lateinit var mContext : Context
    private val mBroadcastManager by lazy { androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(mContext) }
    private val mAdapter by lazy { RecyclerAdapterArrival() }

    companion object {
        fun newInstance(context: Context, stopName: String): ArrivalFragmentTab {
            val fragment = ArrivalFragmentTab()
            fragment.mStrBusStop = stopName
            fragment.mContext = context
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_swipepull_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_fragment_node_arrival_recycler.adapter = mAdapter
        // 프래그먼트 준비 알림 Broadcast 시작
        mBroadcastManager.sendBroadcast(Intent(LocalIntent.NOTIFY_FRAGMENT_READY.value))
        // 프래그먼트를 당기면 데이터 리프레시 Broadcast 시작

        fragment_node_arrival_swipeRefreshLayout.setOnRefreshListener {
            mBroadcastManager.sendBroadcast(Intent(LocalIntent.ARRIVAL_DATA_REFRESH_REQUEST.value))
        }
        Singleton.arrivalFromInfo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                dataRefresh()
            }
        })
        refreshLoading()
        dataRefresh()
    }
    // 하교용 정류장 정보를 다시 받아 어댑터에 적용
    fun dataRefresh(){
        fragment_node_arrival_swipeRefreshLayout?.isRefreshing = false
        Singleton.arrivalFromInfo.get()?.let{
            val checked = it.filter{ subIt->  subIt.name == mStrBusStop }
            if(checked.isEmpty()) return
            val filtered = checked[0].data

            if(!(activity as MainActivity).firstDBload){
                (activity as MainActivity).setDB()
            }

            filtered.forEach {
                (activity as MainActivity).favList.forEachIndexed { index, favorite ->
                    if (it.no == favorite)
                        it.favorite = true
                }
            }
            mAdapter.applyDataSet(filtered,(activity as MainActivity).favList)
            mAdapter.notifyDataSetChanged()
        }
    }

    // 당겨서 새로고침 이미지 설정
    private fun refreshLoading(){
        val mSwipeRefreshLayout = fragment_node_arrival_swipeRefreshLayout
        mSwipeRefreshLayout.setProgressViewOffset(true,0,130)
        mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#0061f4"))

        val f = mSwipeRefreshLayout.javaClass.getDeclaredField("mCircleDiameter")
        f.isAccessible = true
        f.setInt(mSwipeRefreshLayout,130)
        val f2 = mSwipeRefreshLayout.javaClass.getDeclaredField("mProgress")
        f2.isAccessible = true
        var prog = f2.get(mSwipeRefreshLayout) as androidx.swiperefreshlayout.widget.CircularProgressDrawable
        prog.centerRadius = 30f
        prog.strokeWidth = 9f
        val f3 = mSwipeRefreshLayout.javaClass.getDeclaredField("mCircleView")
        f3.isAccessible = true
        var img = f3.get(mSwipeRefreshLayout) as ImageView
        img.setBackgroundResource(R.drawable.refresh_loading)
    }

    // ViewPager에서 현재 페이지가 보이고 있는지 오는 콜백.
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mAdapter.isShowing.set(isVisibleToUser)
        isShowing = isVisibleToUser
    }

    // 생명주기에 맞춤
    override fun onPause() {
        super.onPause()
        mAdapter.isShowing.set(false)
    }

    // 재시작되면서 현재 보이는 탭만 Ticker를 실행하도록.
    override fun onResume() {
        super.onResume()
        mAdapter.isShowing.set(isShowing)
    }

}
