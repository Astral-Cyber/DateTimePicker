package com.loper7.date_time_picker.controller

import android.util.Log
import com.loper7.date_time_picker.DateTimeConfig
import com.loper7.date_time_picker.DateTimeConfig.DAY
import com.loper7.date_time_picker.DateTimeConfig.HOUR
import com.loper7.date_time_picker.DateTimeConfig.MIN
import com.loper7.date_time_picker.DateTimeConfig.MONTH
import com.loper7.date_time_picker.DateTimeConfig.SECOND
import com.loper7.date_time_picker.DateTimeConfig.YEAR
import com.loper7.date_time_picker.ext.*
import com.loper7.date_time_picker.ext.getMaxDayInMonth
import com.loper7.date_time_picker.ext.isSameDay
import com.loper7.date_time_picker.ext.isSameMonth
import com.loper7.date_time_picker.ext.isSameYear
import com.loper7.date_time_picker.number_picker.NumberPicker
import java.util.*
import kotlin.math.min

/**
 *
 * @CreateDate:     2020/9/11 13:36
 * @Description:    日期/时间逻辑控制器
 * @Author:         LOPER7
 * @Email:          loper7@163.com
 */
class DateTimeController : BaseDateTimeController() {
    private var mYearSpinner: NumberPicker? = null
    private var mMonthSpinner: NumberPicker? = null
    private var mDaySpinner: NumberPicker? = null
    private var mHourSpinner: NumberPicker? = null
    private var mMinuteSpinner: NumberPicker? = null
    private var mSecondSpinner: NumberPicker? = null

    private lateinit var calendar: Calendar
    private lateinit var minCalendar: Calendar
    private lateinit var maxCalendar: Calendar

    private var global = DateTimeConfig.GLOBAL_LOCAL

    private var mOnDateTimeChangedListener: ((Long) -> Unit)? = null

    private var wrapSelectorWheel = true
    private var wrapSelectorWheelTypes: MutableList<Int>? = null


    override fun bindPicker(type: Int, picker: NumberPicker?): DateTimeController {
        when (type) {
            YEAR -> mYearSpinner = picker
            MONTH -> mMonthSpinner = picker
            DAY -> mDaySpinner = picker
            HOUR -> mHourSpinner = picker
            MIN -> mMinuteSpinner = picker
            SECOND -> mSecondSpinner = picker
        }
        return this
    }

    override fun bindGlobal(global: Int): DateTimeController {
        this.global = global
        return this
    }

    override fun build(): DateTimeController {
        calendar = Calendar.getInstance()
        minCalendar = Calendar.getInstance()
        minCalendar.set(Calendar.YEAR, 1900)
        minCalendar.set(Calendar.MONTH, 0)
        minCalendar.set(Calendar.DAY_OF_MONTH, 1)
        minCalendar.set(Calendar.HOUR_OF_DAY, 0)
        minCalendar.set(Calendar.MINUTE, 0)
        minCalendar.set(Calendar.SECOND, 0)

        maxCalendar = Calendar.getInstance()
        maxCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1900)
        maxCalendar.set(Calendar.MONTH, 11)
        maxCalendar.set(Calendar.DAY_OF_MONTH, maxCalendar.getMaxDayInMonth())
        maxCalendar.set(Calendar.HOUR_OF_DAY, 23)
        maxCalendar.set(Calendar.MINUTE, 59)
        maxCalendar.set(Calendar.SECOND, 59)

        mYearSpinner?.run {
            maxValue = maxCalendar.get(Calendar.YEAR)
            minValue = minCalendar.get(Calendar.YEAR)
            value = calendar.get(Calendar.YEAR)
            isFocusable = true
            isFocusableInTouchMode = true
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS //设置NumberPicker不可编辑
            setOnValueChangedListener(onChangeListener)
        }


        mMonthSpinner?.run {
            maxValue = maxCalendar.get(Calendar.MONTH)
            minValue = minCalendar.get(Calendar.MONTH)
            value = calendar.get(Calendar.MONTH) + 1
            isFocusable = true
            isFocusableInTouchMode = true

            formatter = if (DateTimeConfig.showChina(global))
                DateTimeConfig.formatter //格式化显示数字，个位数前添加0
            else
                DateTimeConfig.globalMonthFormatter

            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener(onChangeListener)
        }

        mDaySpinner?.run {
            maxValue = maxCalendar.get(Calendar.DAY_OF_MONTH)
            minValue = minCalendar.get(Calendar.DAY_OF_MONTH)
            value = calendar.get(Calendar.DAY_OF_MONTH)
            isFocusable = true
            isFocusableInTouchMode = true
            formatter = DateTimeConfig.formatter
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener(onChangeListener)
        }

        mHourSpinner?.run {
            maxValue = maxCalendar.get(Calendar.HOUR_OF_DAY)
            minValue = minCalendar.get(Calendar.HOUR_OF_DAY)
            isFocusable = true
            isFocusableInTouchMode = true
            value = calendar.get(Calendar.HOUR_OF_DAY)
            formatter = DateTimeConfig.formatter
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener(onChangeListener)
        }

        mMinuteSpinner?.run {
            maxValue = maxCalendar.get(Calendar.MINUTE)
            minValue = minCalendar.get(Calendar.MINUTE)
            isFocusable = true
            isFocusableInTouchMode = true
            value = calendar.get(Calendar.MINUTE)
            formatter = DateTimeConfig.formatter
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener(onChangeListener)
        }

        mSecondSpinner?.run {
            maxValue = maxCalendar.get(Calendar.SECOND)
            minValue = minCalendar.get(Calendar.SECOND)
            isFocusable = true
            isFocusableInTouchMode = true
            value = calendar.get(Calendar.SECOND)
            formatter = DateTimeConfig.formatter
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            setOnValueChangedListener(onChangeListener)
        }
        return this
    }


    private val onChangeListener = NumberPicker.OnValueChangeListener { view, old, new ->
        limitMaxAndMin()
        onDateTimeChanged()
    }

    /**
     * 同步数据
     */
    private fun syncDateData() {
        mYearSpinner?.apply { calendar.set(Calendar.YEAR, value) }
        mMonthSpinner?.apply { calendar.set(Calendar.MONTH, value - 1) }
        mDaySpinner?.apply { calendar.set(Calendar.DAY_OF_MONTH, value) }
        mHourSpinner?.apply { calendar.set(Calendar.HOUR_OF_DAY, value) }
        mMinuteSpinner?.apply { calendar.set(Calendar.MINUTE, value) }
        mSecondSpinner?.apply { calendar.set(Calendar.SECOND, value) }
    }

    /**
     * 日期发生变化
     */
    private fun onDateTimeChanged() {
        syncDateData()
        if (mOnDateTimeChangedListener != null) {
            mOnDateTimeChangedListener?.invoke(calendar.timeInMillis)
        }
    }

    /**
     * 设置允许选择的区间
     */
    private fun limitMaxAndMin() {
        syncDateData()

        var maxDayInMonth = getMaxDayInMonth(mYearSpinner?.value, (mMonthSpinner?.value ?: 0) - 1)

        mMonthSpinner?.apply {
            minValue =
                if (calendar.isSameYear(minCalendar)) minCalendar.get(Calendar.MONTH) + 1 else 1
            maxValue =
                if ((calendar.isSameYear(maxCalendar))) maxCalendar.get(Calendar.MONTH) + 1 else 12
        }
        mDaySpinner?.apply {
            minValue =
                if (calendar.isSameMonth(minCalendar)) minCalendar.get(Calendar.DAY_OF_MONTH) else 1
            maxValue =
                if (calendar.isSameMonth(maxCalendar)) maxCalendar.get(Calendar.DAY_OF_MONTH) else maxDayInMonth
        }
        mHourSpinner?.apply {
            minValue =
                if (calendar.isSameDay(minCalendar)) minCalendar.get(Calendar.HOUR_OF_DAY) else 0
            maxValue =
                if (calendar.isSameDay(maxCalendar)) maxCalendar.get(Calendar.HOUR_OF_DAY) else 23
        }
        mMinuteSpinner?.apply {
            minValue = if (calendar.isSameHour(minCalendar)) minCalendar.get(Calendar.MINUTE) else 0
            maxValue =
                if (calendar.isSameHour(maxCalendar)) maxCalendar.get(Calendar.MINUTE) else 59
        }
        mSecondSpinner?.apply {
            minValue =
                if (calendar.isSameMinute(minCalendar)) minCalendar.get(Calendar.SECOND) else 0
            maxValue =
                if (calendar.isSameMinute(maxCalendar)) maxCalendar.get(Calendar.SECOND) else 59
        }

        if (mDaySpinner?.value ?: 0 >= maxDayInMonth) {
            mDaySpinner?.value = maxDayInMonth
            onDateTimeChanged()
        }
        setWrapSelectorWheel(wrapSelectorWheelTypes, wrapSelectorWheel)

    }


    override fun setDefaultMillisecond(time: Long) {
        if (time == 0L) return
        if (time < minCalendar?.timeInMillis ?: 0) return
        if (time > maxCalendar?.timeInMillis ?: 0) return

        calendar.timeInMillis = time

        mYearSpinner?.value = calendar.get(Calendar.YEAR)
        mMonthSpinner?.value = calendar.get(Calendar.MONTH) + 1
        mDaySpinner?.value = calendar.get(Calendar.DAY_OF_MONTH)
        mHourSpinner?.value = calendar.get(Calendar.HOUR_OF_DAY)
        mMinuteSpinner?.value = calendar.get(Calendar.MINUTE)
        mSecondSpinner?.value = calendar.get(Calendar.SECOND)

        limitMaxAndMin()
        onDateTimeChanged()
    }

    override fun setMinMillisecond(time: Long) {

        if (time == 0L) return
        if (maxCalendar?.timeInMillis ?: 0 in 1 until time) return
        if (minCalendar == null)
            minCalendar = Calendar.getInstance()
        minCalendar?.timeInMillis = time

        mYearSpinner?.minValue = minCalendar?.get(Calendar.YEAR) ?: 1900

        limitMaxAndMin()
        setWrapSelectorWheel(wrapSelectorWheelTypes, wrapSelectorWheel)
        if (calendar < minCalendar) setDefaultMillisecond(minCalendar?.timeInMillis ?: 0)
    }

    override fun setMaxMillisecond(time: Long) {
        if (time == 0L) return
        if (minCalendar?.timeInMillis ?: 0 > 0L && time < minCalendar?.timeInMillis ?: 0) return
        if (maxCalendar == null)
            maxCalendar = Calendar.getInstance()
        maxCalendar?.timeInMillis = time

        mYearSpinner?.maxValue =
            maxCalendar?.get(Calendar.YEAR) ?: calendar.get(Calendar.YEAR) + 100
        limitMaxAndMin()
        setWrapSelectorWheel(wrapSelectorWheelTypes, wrapSelectorWheel)
        if (calendar > maxCalendar) setDefaultMillisecond(maxCalendar?.timeInMillis ?: 0)
    }


    override fun setWrapSelectorWheel(types: MutableList<Int>?, wrapSelector: Boolean) {
        this.wrapSelectorWheelTypes = types
        this.wrapSelectorWheel = wrapSelector
        if (wrapSelectorWheelTypes == null || wrapSelectorWheelTypes!!.isEmpty()) {
            wrapSelectorWheelTypes = mutableListOf()
            wrapSelectorWheelTypes!!.add(YEAR)
            wrapSelectorWheelTypes!!.add(MONTH)
            wrapSelectorWheelTypes!!.add(DAY)
            wrapSelectorWheelTypes!!.add(HOUR)
            wrapSelectorWheelTypes!!.add(MIN)
            wrapSelectorWheelTypes!!.add(SECOND)
        }

        wrapSelectorWheelTypes!!.apply {
            for (type in this) {
                when (type) {
                    YEAR -> mYearSpinner?.run { wrapSelectorWheel = wrapSelector }
                    MONTH -> mMonthSpinner?.run { wrapSelectorWheel = wrapSelector }
                    DAY -> mDaySpinner?.run { wrapSelectorWheel = wrapSelector }
                    HOUR -> mHourSpinner?.run { wrapSelectorWheel = wrapSelector }
                    MIN -> mMinuteSpinner?.run { wrapSelectorWheel = wrapSelector }
                    SECOND -> mSecondSpinner?.run { wrapSelectorWheel = wrapSelector }
                }
            }
        }
    }


    override fun setOnDateTimeChangedListener(callback: ((Long) -> Unit)?) {
        mOnDateTimeChangedListener = callback
        onDateTimeChanged()
    }

    override fun getMillisecond(): Long {
        return calendar.timeInMillis
    }

}
