package com.thbt.goodbyemoney.models

sealed class Recurrence(val name: String, val target: String) {
  object None : Recurrence("Không", "Không")
  object Daily : Recurrence("Hàng ngày", "Hôm nay")
  object Weekly : Recurrence("Hàng tuần", "Tuần này")
  object Monthly : Recurrence("Hàng tháng", "Tháng này")
  object Yearly : Recurrence("Hàng năm", "Năm nay")
}

fun String.toRecurrence(): Recurrence {
  return when(this) {
    "Không" -> Recurrence.None
    "Hàng ngày" -> Recurrence.Daily
    "Hàng tuần" -> Recurrence.Weekly
    "Hàng tháng" -> Recurrence.Monthly
    "Hàng năm" -> Recurrence.Yearly
    else -> Recurrence.None
  }
}
