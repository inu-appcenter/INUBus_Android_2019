package com.inu.bus.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.provider.FontsContract
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.FontResourcesParserCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.inu.bus.R
import com.ms_square.etsyblur.BlurringView
import kotlinx.android.synthetic.main.custom_popup_container.view.*
import java.lang.ref.WeakReference

class FirstPopUp : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)
    companion object {
        var mInstance : WeakReference<FirstPopUp>? = null
    }
    private val mMessage : TextView
    private val mBtnConfirm: Button
    private val mPopupWindow: PopupWindow
    private var mRefDimView: BlurringView? = null
    private var mDpIvSize: Float
    private var mShowDuration: Long?
    private var mHandler: Handler
    private var mWindow: Window? = null
    private val mAnimationDuration = 1000L
    private var mDismissCallback : (()->Unit)? = null


    init {
        val v = LayoutInflater.from(context).inflate(R.layout.custom_popup_first, this, false)
        addView(v)
        mMessage = v.findViewById(R.id.tv_first_popup_message)
        mBtnConfirm = v.findViewById(R.id.btn_first_popup_check)
        mBtnConfirm.setOnClickListener { dismiss() }
        mPopupWindow = PopupWindow(this, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mDpIvSize = 76f
        mShowDuration = null
        mHandler = Handler()
        mInstance = WeakReference(this)
    }

    // 텍스트뷰에 html 스타일 적용
    @Suppress("DEPRECATION")
    private fun makeSpanText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            Html.fromHtml(text);
        }
    }

    // TextView 특정 문자열 폰트 변경
    fun SpanText(){
        val inubus = "INU BUS"
        val start = mMessage.text.indexOf(inubus)
        val end = start + inubus.length
        val ssb = SpannableString(mMessage.text)
//        ssb.setSpan(ForegroundColorSpan(Color.parseColor("#00FF00")),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ssb.setSpan(TextAppearanceSpan(context,R.style.inubus_jalnan),start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        mMessage.setText(ssb)

    }

    // 확인버튼 콜백
    fun setOnConfirmButtonClickListener(callback: (FirstPopUp) -> Unit): FirstPopUp {
        mBtnConfirm.setOnClickListener {
            callback(this)
        }
        return this
    }

    // 블러처리하는 뷰
    fun setDimBlur(blurView: BlurringView): FirstPopUp {
        mRefDimView = blurView
        // blurredView를 미리 바인드하고 호출. 바인드를 나중에하다가 null 상태에서 사라지면 에러남
        return this
    }

    // show 애니메이션 시간
    fun setShowDuration(duration: Long): FirstPopUp {
        mShowDuration = duration
        return this
    }

    // 팝업 보여주기
    fun show() {
        mPopupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.animationStyle = R.style.AppTheme_PopupAnimation
        mPopupWindow.showAtLocation(this, Gravity.CENTER, 0, 0)
        SpanText()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateStatusBarColor(500,
                    ContextCompat.getColor(this.context, R.color.colorActionBar),
                    ContextCompat.getColor(this.context, R.color.colorAccent))
        }

        mRefDimView?.alpha = 0f
        mRefDimView?.visibility = View.VISIBLE
        mRefDimView
                ?.animate()
                ?.alpha(1f)
                ?.setDuration(500)
                ?.setListener(emptyAnimationListener)
                ?.start()
        mShowDuration?.let {
            mHandler.postDelayed({
                dismiss()
            }, it)
        }
    }

    // 팝업 사라질때 호출
    fun setDismissListener(callback: () -> Unit): FirstPopUp {
        mDismissCallback = callback
        return this
    }

    fun dismiss() {
        // 팝업이 보여지고 있으면 사라짐
        if(mPopupWindow.isShowing){
            mPopupWindow.dismiss()
        }
        mDismissCallback?.invoke()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateStatusBarColor(500,
                    ContextCompat.getColor(this.context, R.color.colorAccent),
                    ContextCompat.getColor(this.context, R.color.colorActionBar))
        }
        // 블러처리 뷰 서서히 사라짐
        mRefDimView
                ?.animate()
                ?.alpha(0f)
                ?.setDuration(500)
                ?.setListener(dismissAnimationListener)
                ?.start()
    }

    // 상단바 색상을 맞추기 위해 window를 받아옴
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setWindow(window: Window) {
        mWindow = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//        window.statusBarColor = ContextCompat.getColor(window.context, R.color.colorPrimaryDim)
        window.statusBarColor = ContextCompat.getColor(window.context,R.color.colorActionBar)
//        window.decorView.systemUiVisibility = 1
    }

    // 상단바 색상 변화 애니메이션
    private fun animateStatusBarColor(duration: Long, from: Int, to: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var blended: Int
            var position: Float
            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.addUpdateListener { animation ->
                position = animation.animatedFraction
                blended = blendColors(from, to, position)

                mWindow!!.statusBarColor = blended

            }
            anim.setDuration(duration).start()
        }
    }

    // Back키를 눌렀을 때 처리
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("test", "$keyCode ${event?.action}")
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mPopupWindow.isShowing){
                dismiss()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // RGB 색상 설정
    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(to) * ratio + Color.red(from) * inverseRatio
        val g = Color.green(to) * ratio + Color.green(from) * inverseRatio
        val b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
    // dp -> px 사이즈 변환
    private fun dp2px(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }
    // 이미지를 비트맵 형식으로 처리
    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val radius: Int =
                    if (drawable.intrinsicWidth > 0)
                        drawable.intrinsicWidth
                    else
                        (dp2px(mDpIvSize) / 2).toInt()

            val bitmap = Bitmap.createBitmap(radius,
                    radius, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private val emptyAnimationListener =
            object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            }
    // 애니메이션이 종료되면 호출
    private val dismissAnimationListener = object : Animator.AnimatorListener {
        override fun onAnimationEnd(animation: Animator?) {
            mRefDimView?.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mWindow?.statusBarColor = ContextCompat.getColor(mWindow?.context!!, R.color.colorActionBar)
            }
            mInstance = null
        }
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) {}
    }
}