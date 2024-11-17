package com.thbt.goodbyemoney.pages

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import com.thbt.goodbyemoney.components.TableRow
import com.thbt.goodbyemoney.components.UnstyledTextField
import com.thbt.goodbyemoney.models.Recurrence
import com.thbt.goodbyemoney.ui.theme.*
import com.thbt.goodbyemoney.viewmodels.AddViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Add(navController: NavController, vm: AddViewModel = viewModel()) {
  val state by vm.uiState.collectAsState()
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  val context = LocalContext.current

  // Image picker launcher
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    selectedImageUri = uri // Save the selected image URI
  }

  val recurrences = listOf(
    Recurrence.None,
    Recurrence.Daily,
    Recurrence.Weekly,
    Recurrence.Monthly,
    Recurrence.Yearly
  )

  Scaffold(topBar = {
    MediumTopAppBar(
      title = { Text("THÊM") },
      colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = TopAppBarBackground
      )
    )
  }, content = { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .clip(Shapes.large)
          .background(BackgroundElevated)
          .fillMaxWidth()
      ) {
        TableRow(label = "Số tiền", detailContent = {
          UnstyledTextField(
            value = state.amount,
            onValueChange = vm::setAmount,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0") },
            arrangement = Arrangement.End,
            maxLines = 1,
            textStyle = TextStyle(
              textAlign = TextAlign.Right,
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
            )
          )
        })
        Divider(
          modifier = Modifier.padding(start = 16.dp),
          thickness = 1.dp,
          color = DividerColor
        )
        TableRow(label = "Lặp lại", detailContent = {
          var recurrenceMenuOpened by remember {
            mutableStateOf(false)
          }
          TextButton(
            onClick = { recurrenceMenuOpened = true }, shape = Shapes.large
          ) {
            Text(state.recurrence?.name ?: Recurrence.None.name)
            DropdownMenu(expanded = recurrenceMenuOpened,
              onDismissRequest = { recurrenceMenuOpened = false }) {
              recurrences.forEach { recurrence ->
                DropdownMenuItem(text = { Text(recurrence.name) }, onClick = {
                  vm.setRecurrence(recurrence)
                  recurrenceMenuOpened = false
                })
              }
            }
          }
        })
        Divider(
          modifier = Modifier.padding(start = 16.dp),
          thickness = 1.dp,
          color = DividerColor
        )
        var datePickerShowing by remember {
          mutableStateOf(false)
        }
        TableRow(label = "Thời gian", detailContent = {
          TextButton(onClick = { datePickerShowing = true }) {
            Text(state.date.toString())
          }
          if (datePickerShowing) {
            DatePickerDialog(onDismissRequest = { datePickerShowing = false },
              onDateChange = { it ->
                vm.setDate(it)
                datePickerShowing = false
              },
              initialDate = state.date,
              title = { Text("Select date", style = Typography.titleLarge) })
          }
        })
        Divider(
          modifier = Modifier.padding(start = 16.dp),
          thickness = 1.dp,
          color = DividerColor
        )
        TableRow(label = "Ghi chú", detailContent = {
          UnstyledTextField(
            value = state.note,
            placeholder = { Text("Để lại một số ghi chú") },
            arrangement = Arrangement.End,
            onValueChange = vm::setNote,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
              textAlign = TextAlign.Right,
            ),
          )
        })
        Divider(
          modifier = Modifier.padding(start = 16.dp),
          thickness = 1.dp,
          color = DividerColor
        )
        TableRow(label = "Thể loại", detailContent = {
          var categoriesMenuOpened by remember {
            mutableStateOf(false)
          }
          TextButton(
            onClick = { categoriesMenuOpened = true }, shape = Shapes.large
          ) {
            Text(
              state.category?.name ?: "Chọn một thể loại",
              color = state.category?.color ?: Color.White
            )
            DropdownMenu(expanded = categoriesMenuOpened,
              onDismissRequest = { categoriesMenuOpened = false }) {
              state.categories?.forEach { category ->
                DropdownMenuItem(text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                      modifier = Modifier.size(10.dp),
                      shape = CircleShape,
                      color = category.color
                    ) {}
                    Text(
                      category.name, modifier = Modifier.padding(start = 8.dp)
                    )
                  }
                }, onClick = {
                  vm.setCategory(category)
                  categoriesMenuOpened = false
                })
              }
            }
          }
        })
      }

      Button(
        onClick = vm::submitExpense,
        modifier = Modifier.padding(16.dp),
        shape = Shapes.large,
        enabled = state.category != null
      ) {
        Text("Gửi chi phí")
      }
//      val analyzedText by vm.analyzedText.collectAsState()
//
//      analyzedText?.let {
//        val scrollState = rememberScrollState()
//        Box(
//          modifier = Modifier
//            .padding(16.dp) // Padding cho Box nếu cần
//            .fillMaxWidth().height(70.dp
//            ) // Đảm bảo Box chiếm hết chiều rộng
//        ) {
//          Column(
//            modifier = Modifier
//              .verticalScroll(scrollState) // Làm cho Column có thể cuộn dọc
//              .fillMaxWidth() // Đảm bảo Column chiếm hết chiều rộng
//          ) {
//            Text(text = it)
//          }
//        }
//      }



      Spacer(modifier = Modifier.height(58.dp))



        // Row for Upload and Submit Icon Buttons
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Upload Icon Button
        IconButton(
          onClick = { imagePickerLauncher.launch("image/*") },
          modifier = Modifier.padding(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Upload Image",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        // Display the selected image if available
        selectedImageUri?.let { uri ->
          Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Selected Image",
            modifier = Modifier
              .size(100.dp)
              .padding(6.dp)
              .clip(Shapes.medium),
            contentScale = ContentScale.Crop
          )
        }

          // TextField for Additional Details
          val additionalText by vm.additionalText.collectAsState()
          TextField(
            value = additionalText,
            onValueChange = vm::setAdditionalText,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 6.dp),
            label = { Text("Chi tiết bổ sung") },
            colors = TextFieldDefaults.textFieldColors(
              containerColor = Color(0xFFF5F5F5),
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent
            )
          )

          // Submit Icon Button
          IconButton(
            onClick = {
              selectedImageUri?.let { uri ->
                vm.analyzeImageFromUri(context, uri)
              }
            },
            modifier = Modifier
              .padding(16.dp)
              .clip(Shapes.large)
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Submit",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))


    }
    }
  )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewAdd() {
  GoodbyeMoneyTheme {
    val navController = rememberNavController()
    Add(navController = navController)
  }
}
