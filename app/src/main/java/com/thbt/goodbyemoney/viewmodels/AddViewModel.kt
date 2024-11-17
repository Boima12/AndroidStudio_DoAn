package com.thbt.goodbyemoney.viewmodels

import ImageRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thbt.goodbyemoney.db
import com.thbt.goodbyemoney.models.Category
import com.thbt.goodbyemoney.models.Expense
import com.thbt.goodbyemoney.models.Recurrence
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File


import kotlinx.coroutines.flow.StateFlow

import java.io.IOException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


import com.thbt.goodbyemoney.MyApplication.Companion.context
import kotlinx.coroutines.tasks.await


data class AddScreenState(
  val amount: String = "",
  val recurrence: Recurrence = Recurrence.None,
  val date: LocalDate = LocalDate.now(),
  val note: String = "",
  val category: Category? = null,
  val categories: RealmResults<Category>? = null

)

class AddViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(AddScreenState())
  val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

  init {
    _uiState.update { currentState ->
      currentState.copy(
        categories = db.query<Category>().find()
      )
    }
  }

  fun setAmount(amount: String) {
    var parsed = amount.toDoubleOrNull()

    if (amount.isEmpty()) {
      parsed = 0.0
    }

    if (parsed != null) {
      _uiState.update { currentState ->
        currentState.copy(
          amount = amount.trim().ifEmpty { "0" },
        )
      }
    }


  }

  fun setRecurrence(recurrence: Recurrence) {
    _uiState.update { currentState ->
      currentState.copy(
        recurrence = recurrence,
      )
    }
  }

  fun setDate(date: LocalDate) {
    _uiState.update { currentState ->
      currentState.copy(
        date = date,
      )
    }
  }

  fun setNote(note: String) {
    _uiState.update { currentState ->
      currentState.copy(
        note = note,
      )
    }
  }

  fun setCategory(category: Category) {
    _uiState.update { currentState ->
      currentState.copy(
        category = category,
      )
    }
  }


  fun submitExpense() {
    if (_uiState.value.category != null) {
      viewModelScope.launch(Dispatchers.IO) {
        val now = LocalDateTime.now()
        db.write {
          this.copyToRealm(
            Expense(
              _uiState.value.amount.toDouble(),
              _uiState.value.recurrence,
              _uiState.value.date.atTime(now.hour, now.minute, now.second),
              _uiState.value.note,
              this.query<Category>("_id == $0", _uiState.value.category!!._id)
                .find().first(),
            )
          )
        }
        _uiState.update { currentState ->
          currentState.copy(
            amount = "",
            recurrence = Recurrence.None,
            date = LocalDate.now(),
            note = "",
            category = null,
            categories = null
          )
        }
      }
    }
  }

  private val _additionalText = MutableStateFlow("")
  val additionalText = _additionalText.asStateFlow()


  // New state for storing the uploaded image URI
  private val _imageUri = MutableStateFlow<Uri?>(null)
  val imageUri = _imageUri.asStateFlow()

  fun setAdditionalText(newText: String) {
    _additionalText.value = newText
  }
  fun setNoteFromAdditionalText() {
    _uiState.update { currentState ->
      currentState.copy(
        note = _additionalText.value
      )
    }
  }



  fun setAnalyzedText(text: String?) {
    _analyzedText.value = text
  }

  private val imageRepository = ImageRepository()

  private val _analyzedText = MutableStateFlow<String?>(null)
  val analyzedText: StateFlow<String?> = _analyzedText
  var a1: String = "";
  var a2: String = "";

  fun analyzeImageFromUri(context: Context, imageUri: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
          .addOnSuccessListener { visionText ->
            _analyzedText.value = visionText.text
            a1 = extractLineFromText(_analyzedText.value, 1,10) ?: "";
            a2 = extractLineFromText(_analyzedText.value, 10,20) ?: "";

            val cleanedAmountText = extractAmountFromLines(a1 ?: "")
            // Cập nhật trạng thái của các ô
            _uiState.update { currentState ->
              currentState.copy(
                amount = cleanedAmountText ?: "", // Gán dòng 1 vào amount
                      // Gán dòng 2 vào note
              )

            }
            selectCategoryFromText(a2 ?: "")
            setNoteFromAdditionalText()
          }



          .addOnFailureListener { e ->
            Log.e("MLKit", "Text recognition failed", e)
            _analyzedText.value = "Failed to recognize text."
          }
      } catch (e: IOException) {
        Log.e("MLKit", "Failed to load image", e)
        _analyzedText.value = "Could not load image for analysis."
      }
    }
  }


  fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
      val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      if (cursor.moveToFirst()) {
        fileName = cursor.getString(nameIndex)
      }
    }
    return fileName ?: uri.lastPathSegment // Fallback to last path segment if name isn't found
  }
  private suspend fun detectTextFromImage(file: File): String? {
    // Create an InputImage from the image file
    val inputImage = InputImage.fromFilePath(context, Uri.fromFile(file))
    val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    return try {
      val result: Text = recognizer.process(inputImage).await()
      // Process recognized text
      result.text
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }
//  fun extractLineFromText(text: String?, lineNumber: Int): String? {
//    if (text.isNullOrEmpty()) return null  // Return null if text is null or empty
//
//    val lines = text.split("\n") // Split the text by new lines
//    return if (lineNumber >= 0 && lineNumber < lines.size) {
//      lines[lineNumber] // Return the text at the specified line
//    } else {
//      null // Return null if the line number is out of bounds
//    }
//  }
fun extractLineFromText(text: String?, startLine: Int, endLine: Int): String? {
  if (text == null) return null
  val lines = text.lines() // Tách văn bản thành từng dòng
  return lines.subList(startLine.coerceAtLeast(0), endLine.coerceAtMost(lines.size))
    .joinToString("\n") // Nối các dòng thành chuỗi với dấu xuống dòng
}

  fun extractAmountFromLines(text: String): String? {
    val regex = """\d+([.,]?\d+)*""".toRegex() // Tìm số có thể có dấu "." hoặc ","
    val lines = text.split("\n") // Chia văn bản thành từng dòng

    for (line in lines) {
      val match = regex.find(line)
      if (match != null) {
        // Loại bỏ các dấu "." và "," để chỉ lấy số nguyên
        return match.value.replace("[.,]".toRegex(), "")
      }
    }
    return null
  }

  fun selectCategoryFromText(analyzedText: String) {
    val categories = _uiState.value.categories ?: return
    // Duyệt qua từng category
    for (category in categories) {
      // Kiểm tra xem nội dung có chứa từ khóa của category không
      if (analyzedText.contains(category.name, ignoreCase = true)) {
        // Nếu có, đặt category
        setCategory(category)
        break // Dừng sau khi tìm thấy category đầu tiên
      }
    }
  }




}







