package com.dalamarmajere.dietrecomendation

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val viewModel: APIViewModel = viewModel()
    var caloriesText by remember { mutableStateOf(SelectableItems.calories[0]) }
    var daysText by remember { mutableStateOf(SelectableItems.days[0]) }
    var mealsText by remember { mutableStateOf(SelectableItems.meals[0]) }
    Surface {
        Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
            DropdownList(SelectableItems.calories, onClick = { newDropdown -> caloriesText = newDropdown})
            DropdownList(SelectableItems.days, onClick = { newDropdown -> daysText = newDropdown})
            DropdownList(SelectableItems.meals, onClick = { newDropdown -> mealsText = newDropdown})
            SubmitButton(onClick = { viewModel.sendRequestToChatGPT(caloriesText, daysText, mealsText)
                Log.e("MyViewModel", "Main launcing")} )
            ResponseList(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownList(
    items: Array<String>,
    onClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(items[0]) }
        Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange  = { expanded = !expanded },
        ) {

            TextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedText = item
                            expanded = false
                            onClick(item)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputField(text: String, onTextChange: (String) -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Enter text") },
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun ResponseList(viewModel: APIViewModel) {
    val responses = viewModel.responses.collectAsState()
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(responses.value) { response ->
            ResponseCard(response)
        }
    }
}

@Composable
fun ResponseCard(response: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { /* handle click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = response,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun SubmitButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text("Submit")
    }
}