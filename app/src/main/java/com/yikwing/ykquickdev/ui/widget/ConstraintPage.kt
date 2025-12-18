package com.example.jetpackcompose.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet

@Composable
fun ConstraintPage(modifier: Modifier = Modifier) {
    Column(
        modifier =
            Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                ).then(modifier),
    ) {
        ConstraintLayout(
            constraintSet = decoupledConstraints(8.dp),
        ) {
            Button(
                onClick = { /* Do something */ },
                // Assign reference "button" to the Button composable
                // and constrain it to the top of the ConstraintLayout
                modifier = Modifier.layoutId("button"),
            ) {
                Text("Button")
            }

            // Assign reference "text" to the Text composable
            // and constrain it to the bottom of the Button composable
            Text(
                "Text",
                Modifier.layoutId("text"),
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        TwoTexts(text1 = "Text1", text2 = "Text2")

        Spacer(
            modifier = Modifier.height(16.dp),
        )

        HelloScreen()
    }
}

@Composable
fun HelloScreen() {
    var name by remember { mutableStateOf("") }
    HelloContent(name = name, onNameChange = { name = it })
}

@Composable
fun HelloContent(
    name: String,
    onNameChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (name.isNotEmpty()) {
            Text(
                text = "Hello, $name!",
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
        )
    }
}

@Composable
fun TwoTexts(
    modifier: Modifier = Modifier,
    text1: String,
    text2: String,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Text(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .wrapContentWidth(Alignment.Start),
            text = text1,
        )
        VerticalDivider(
            color = Color.LightGray,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(1.dp),
        )
        Text(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .wrapContentWidth(Alignment.End),
            text = text2,
        )
    }
}

private fun decoupledConstraints(margin: Dp): ConstraintSet =
    ConstraintSet {
        val button = createRefFor("button")
        val text = createRefFor("text")

        constrain(button) {
            top.linkTo(parent.top, margin = margin)
        }

        constrain(text) {
            top.linkTo(button.bottom, margin)
            start.linkTo(button.start)
            end.linkTo(button.end)
        }
    }
