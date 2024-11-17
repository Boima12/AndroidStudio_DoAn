package com.thbt.goodbyemoney.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thbt.goodbyemoney.models.Expense
import com.thbt.goodbyemoney.ui.theme.LabelSecondary
import com.thbt.goodbyemoney.ui.theme.Typography
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@Composable
fun ExpenseRow(expense: Expense, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        expense.note ?: expense.category!!.name,
        style = Typography.headlineMedium
      )
      Text(
        " ${DecimalFormat("0.#").format(expense.amount)} VND",
        style = Typography.headlineMedium
      )
    }
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      expense.category?.let {
        CategoryBadge(category = it)
      }
      Text(
        expense.date.format(DateTimeFormatter.ofPattern("HH:mm")),
        style = Typography.bodyMedium,
        color = LabelSecondary
      )
    }
  }
}