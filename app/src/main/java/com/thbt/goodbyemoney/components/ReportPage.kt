package com.thbt.goodbyemoney.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thbt.goodbyemoney.components.charts.MonthlyChart
import com.thbt.goodbyemoney.components.charts.WeeklyChart
import com.thbt.goodbyemoney.components.charts.YearlyChart
import com.thbt.goodbyemoney.components.expensesList.ExpensesList
import com.thbt.goodbyemoney.models.Recurrence
import com.thbt.goodbyemoney.ui.theme.LabelSecondary
import com.thbt.goodbyemoney.ui.theme.Typography
import com.thbt.goodbyemoney.utils.formatDayForRange
import com.thbt.goodbyemoney.viewmodels.ReportPageViewModel
import com.thbt.goodbyemoney.viewmodels.viewModelFactory
import java.text.DecimalFormat
import java.time.LocalDate

@Composable
fun ReportPage(
  innerPadding: PaddingValues,
  page: Int,
  recurrence: Recurrence,
  vm: ReportPageViewModel = viewModel(
    key = "$page-${recurrence.name}",
    factory = viewModelFactory {
      ReportPageViewModel(page, recurrence)
    })
) {
  val uiState = vm.uiState.collectAsState().value

  Column(
    modifier = Modifier
      .padding(innerPadding)
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp)
      .fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        Text(
          "${
            uiState.dateStart.formatDayForRange()
          } - ${uiState.dateEnd.formatDayForRange()}",
          style = Typography.titleSmall
        )
        Row(modifier = Modifier.padding(top = 4.dp)) {
          Text(DecimalFormat("0.#").format(uiState.totalInRange), style = Typography.headlineMedium)
          Text(
            "VND",
            style = Typography.bodyMedium,
            color = LabelSecondary,
            modifier = Modifier.padding(end = 4.dp)
          )

        }
      }
      Column(horizontalAlignment = Alignment.End) {
        Text("Avg/day", style = Typography.titleSmall)
        Row(modifier = Modifier.padding(top = 4.dp)) {
          Text(DecimalFormat("0.#").format(uiState.avgPerDay), style = Typography.headlineMedium)
          Text(
            "VND",
            style = Typography.bodyMedium,
            color = LabelSecondary,
            modifier = Modifier.padding(end = 4.dp)
          )

        }
      }
    }

    Box(
      modifier = Modifier
        .height(180.dp)
        .padding(vertical = 16.dp)
    ) {
      when (recurrence) {
        Recurrence.Weekly -> WeeklyChart(expenses = uiState.expenses)
        Recurrence.Monthly -> MonthlyChart(
          expenses = uiState.expenses,
          LocalDate.now()
        )
        Recurrence.Yearly -> YearlyChart(expenses = uiState.expenses)
        else -> Unit
      }
    }

    ExpensesList(
      expenses = uiState.expenses, modifier = Modifier
        .weight(1f)
        .verticalScroll(
          rememberScrollState()
        )
    )
  }
}