package com.example.tonetuner_v2.ui.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun AlertDialog(
    show: Boolean,
    title: String,
    message: String,
    confirm: String,
    dismiss: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    cancelable: Boolean = true
) {
    if (show) {
        AlertDialog(
            // invoked when user clicks outside the alert
            onDismissRequest = { if (cancelable) onDismiss() },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                ) {
                    Text(
                        text = confirm,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text(
                        text = dismiss,
                        color = MaterialTheme.colors.secondaryVariant
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.surface
        )
    }
}

@Composable
fun AlertDialog(
    show: Boolean,
    title: String? = null,
    message: String? = null,
    dismiss: String = "Ok",
    onDismiss: () -> Unit,
    cancelable: Boolean = true
) {
    if (show) {
        AlertDialog(
            // invoked when user clicks outside the alert
            onDismissRequest = { if (cancelable) onDismiss() },
            title = { title?.let { Text(it) } },
            text = { message?.let { Text(it) } },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = dismiss,
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.surface,
        )
    }
}
