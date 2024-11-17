package com.thbt.goodbyemoney.components.expensesList

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thbt.goodbyemoney.components.ExpensesDayGroup
import com.thbt.goodbyemoney.mock.mockExpenses
import com.thbt.goodbyemoney.models.Expense
import com.thbt.goodbyemoney.models.groupedByDay
import com.thbt.goodbyemoney.ui.theme.GoodbyeMoneyTheme

@Composable
fun ExpensesList(expenses: List<Expense>, modifier: Modifier = Modifier) {
  val groupedExpenses = expenses.groupedByDay()

  Column(modifier = modifier) {
    if (groupedExpenses.isEmpty()) {
      Text("Không có dữ liệu cho phạm vi ngày đã chọn.", modifier = Modifier.padding(top = 32.dp))
    } else {
      groupedExpenses.keys.forEach { date ->
        if (groupedExpenses[date] != null) {
          ExpensesDayGroup(
            date = date,
            dayExpenses = groupedExpenses[date]!!,
            modifier = Modifier.padding(top = 24.dp)
          )
        }
      }
    }
  }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun Preview() {
  GoodbyeMoneyTheme {
    ExpensesList(mockExpenses)
  }
}