package com.thbt.goodbyemoney.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thbt.goodbyemoney.db
import com.thbt.goodbyemoney.models.Expense
import com.thbt.goodbyemoney.models.Recurrence
import com.thbt.goodbyemoney.utils.calculateDateRange
import io.realm.kotlin.ext.query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

data class State<T>(
  val expenses: List<Expense> = listOf(),
  val dateStart: LocalDateTime = LocalDateTime.now(),
  val dateEnd: LocalDateTime = LocalDateTime.now(),
  val avgPerDay: Double = 0.0,
  val totalInRange: Double = 0.0
)

class ReportPageViewModel(private val page: Int, val recurrence: Recurrence) :
  ViewModel() {
  private val _uiState = MutableStateFlow(State<Any>())
  val uiState: StateFlow<State<Any>> = _uiState.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      val (start, end, daysInRange) = calculateDateRange(recurrence, page)

      val filteredExpenses = db.query<Expense>().find().filter { expense ->
        (expense.date.toLocalDate().isAfter(start) && expense.date.toLocalDate()
          .isBefore(end)) || expense.date.toLocalDate()
          .isEqual(start) || expense.date.toLocalDate().isEqual(end)
      }

      val totalExpensesAmount = filteredExpenses.sumOf { it.amount }
      val avgPerDay: Double = totalExpensesAmount / daysInRange

      viewModelScope.launch(Dispatchers.Main) {
        _uiState.update { currentState ->
          currentState.copy(
            dateStart = LocalDateTime.of(start, LocalTime.MIN),
            dateEnd = LocalDateTime.of(end, LocalTime.MAX),
            expenses = filteredExpenses,
            avgPerDay = avgPerDay,
            totalInRange = totalExpensesAmount
          )
        }
      }
    }
  }
}