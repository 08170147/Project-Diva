
package dev.pjdvm39.android

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = AssetResourceManager(this)

        // Read the existing test resource.
        val resourceResult = manager.readText("port_test.txt")
        val resourceText = resourceResult.fold(
            onSuccess = { it.trimEnd() },
            onFailure = { error ->
                "Resource read error: ${error.javaClass.simpleName}"
            }
        )

        // Verify every entry listed in resource_manifest.txt.
        val manifestResult = manager.verifyManifest()
        val manifestResults = manifestResult.getOrNull().orEmpty()

        val manifestReadable = manifestResult.isSuccess
        val manifestNonEmpty = manifestResults.isNotEmpty()

        val passedCount = manifestResults.count { it.matches }
        val errorCount = manifestResults.count { it.error != null }
        val failedCount = manifestResults.count {
            !it.matches && it.error == null
        }

        val allPassed =
            resourceResult.isSuccess &&
            manifestReadable &&
            manifestNonEmpty &&
            manifestResults.all { it.matches }
        val manifestStatus = when {
            !manifestReadable -> {
                val error = manifestResult.exceptionOrNull()
                "MANIFEST: ERROR (${error?.javaClass?.simpleName ?: "Unknown"})"
            }
            !manifestNonEmpty -> "MANIFEST: EMPTY"
            else -> "MANIFEST: LOADED"
        }

        val verificationLines = if (manifestResults.isEmpty()) {
            if (manifestReadable) {
                "No resources found in manifest."
            } else {
                "Could not load resource_manifest.txt."
            }
        } else {
            manifestResults.joinToString("\n\n") { item ->
                val itemStatus = when {
                    item.error != null -> "ERROR"
                    item.matches -> "PASS"
                    else -> "FAIL"
                }

                buildString {
                    append("[$itemStatus] ")
                    append(item.path)

                    if (item.error != null) {
                        append("\nReason: ")
                        append(item.error)
                    } else if (!item.matches) {
                        append("\nExpected: ")
                        append(item.expectedHash)
                        append("\nActual: ")
                        append(item.actualHash ?: "unavailable")
                    }
                }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(18, 20, 30))
        }

        val title = TextView(this).apply {
            text = "PJDVM39 · Android Port"
            textSize = 25f
            setTextColor(Color.rgb(235, 238, 255))
            gravity = Gravity.CENTER
        }

        val status = TextView(this).apply {
            text = buildString {
                appendLine("ANDROID SHELL 0.2.0")
                appendLine()
                appendLine("Asset Resource Manager")
                appendLine()
                appendLine(
                    if (resourceResult.isSuccess) {
                        "RESOURCE READ: SUCCESS"
                    } else {
                        "RESOURCE READ: FAILED"
                    }
                )
                appendLine()
                appendLine(manifestStatus)
                appendLine()
                appendLine(
                    if (allPassed) {
                        "ALL CHECKS PASSED"
                    } else {
                        "CHECKS NEED ATTENTION"
                    }
                )
                appendLine()
                appendLine("SUMMARY")
                appendLine("Passed: $passedCount")
                appendLine("Failed: $failedCount")
                appendLine("Errors: $errorCount")
                appendLine()
                appendLine("MANIFEST VERIFICATION")
                appendLine(verificationLines)
                appendLine()
                appendLine("TEST RESOURCE: port_test.txt")
                appendLine(resourceText)
                appendLine()
                append("This is a test resource, not game content.")
            }

            textSize = 15f
            setTextColor(Color.rgb(210, 215, 230))
            gravity = Gravity.CENTER
            setLineSpacing(5f, 1f)
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 28
            }
        )

        root.addView(
            status,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(18, 20, 30))
            addView(
                root,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        setContentView(scrollView)
    }
}
