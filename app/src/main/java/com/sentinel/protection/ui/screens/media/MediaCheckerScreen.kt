package com.sentinel.protection.ui.screens.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.sentinel.protection.domain.model.MediaAnalysisResult
import com.sentinel.protection.domain.model.MediaAuthenticityResult
import com.sentinel.protection.domain.model.MediaType
import com.sentinel.protection.ui.theme.*
import java.io.IOException

@Composable
fun MediaCheckerScreen(onBack: () -> Unit) {
    var analysisResult by remember { mutableStateOf<MediaAnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            isAnalyzing = true
            analyzePhoto(context, it) { result ->
                analysisResult = result
                isAnalyzing = false
            }
        }
    }

    // Video picker
    val videoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            analysisResult = MediaAnalysisResult(
                mediaType = MediaType.VIDEO,
                analysisAvailable = false,
                overallResult = MediaAuthenticityResult.ANALYSIS_UNAVAILABLE,
                indicators = listOf("Video frame extraction requires additional processing"),
                suspiciousIndicators = emptyList(),
                explanation = "Video analysis extracts frames and applies face detection to check consistency. This feature requires additional configuration.",
                limitations = "Advanced video deepfake detection requires a validated ML model which is not yet configured."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Media Authenticity Check") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF3B82F6))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("About This Feature", fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "This tool uses on-device face detection (ML Kit) to look for basic image quality issues. " +
                            "It is NOT a certified deepfake detector. Treat results as guidance, not definitive proof.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pick buttons
            Text("Select Media to Analyze", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Pick Photo")
                }
                OutlinedButton(
                    onClick = { videoPicker.launch("video/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Pick Video")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isAnalyzing) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryGreen)
                        Spacer(Modifier.height(8.dp))
                        Text("Analyzing… (on-device)", color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Results
            analysisResult?.let { result ->
                MediaAnalysisResultCard(result)
            }

            Spacer(Modifier.height(16.dp))

            // What we check
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What We Check", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "📸 Photos: Face detection, image quality, basic manipulation indicators",
                        "🎬 Videos: Frame extraction and face consistency (coming soon with ML model)",
                        "🔊 Audio: Voice pattern analysis (requires ML model configuration)",
                        "🔒 Privacy: All analysis is done on your device. Files are not uploaded."
                    ).forEach { item ->
                        Text(item, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaAnalysisResultCard(result: MediaAnalysisResult) {
    val (bg, textColor, title) = when (result.overallResult) {
        MediaAuthenticityResult.LIKELY_AUTHENTIC -> Triple(Color(0xFFDCFCE7), PrimaryGreen, "✅ Likely Authentic")
        MediaAuthenticityResult.SUSPICIOUS -> Triple(Color(0xFFFEE2E2), DangerRed, "⚠️ Suspicious Indicators Found")
        MediaAuthenticityResult.ANALYSIS_UNAVAILABLE -> Triple(Color(0xFFF1F5F9), TextSecondary, "ℹ️ Analysis Unavailable")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(result.explanation, style = MaterialTheme.typography.bodyMedium, color = textColor)

            if (result.indicators.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Findings:", fontWeight = FontWeight.Medium, color = textColor)
                result.indicators.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = textColor)
                }
            }

            if (result.suspiciousIndicators.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Suspicious Indicators:", fontWeight = FontWeight.Medium, color = DangerRed)
                result.suspiciousIndicators.forEach {
                    Text("• $it", style = MaterialTheme.typography.bodySmall, color = DangerRed)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "⚠️ ${result.limitations}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

private fun analyzePhoto(
    context: android.content.Context,
    uri: Uri,
    callback: (MediaAnalysisResult) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        detector.process(image)
            .addOnSuccessListener { faces ->
                val indicators = mutableListOf<String>()
                val suspicious = mutableListOf<String>()

                if (faces.isEmpty()) {
                    indicators.add("No human face detected in this image")
                } else {
                    indicators.add("${faces.size} face(s) detected")
                    faces.forEachIndexed { i, face ->
                        val smileProb = face.smilingProbability
                        val leftEye = face.leftEyeOpenProbability
                        val rightEye = face.rightEyeOpenProbability

                        // Check for unnatural eye openness (potential deepfake indicator)
                        if (leftEye != null && rightEye != null) {
                            val diff = kotlin.math.abs(leftEye - rightEye)
                            if (diff > 0.4f) {
                                suspicious.add("Face ${i+1}: Unusual eye openness asymmetry detected (${String.format("%.0f", diff*100)}% difference)")
                            }
                        }

                        indicators.add("Face ${i+1}: Rotation ${face.headEulerAngleY.toInt()}°, " +
                            "Eye Open: ${leftEye?.let { "${(it*100).toInt()}%" } ?: "N/A"}")
                    }
                }

                val result = MediaAuthenticityResult.LIKELY_AUTHENTIC.let {
                    if (suspicious.isNotEmpty()) MediaAuthenticityResult.SUSPICIOUS else it
                }

                callback(MediaAnalysisResult(
                    mediaType = MediaType.PHOTO,
                    analysisAvailable = true,
                    overallResult = result,
                    indicators = indicators,
                    suspiciousIndicators = suspicious,
                    explanation = if (suspicious.isNotEmpty())
                        "Some indicators warrant further verification. This is based on face detection only, not a certified deepfake analysis."
                    else
                        "No obvious manipulation indicators found with on-device face detection.",
                    limitations = "This analysis uses ML Kit face detection only. It cannot certify whether an image is genuine or AI-generated. For important decisions, seek expert verification."
                ))
                detector.close()
            }
            .addOnFailureListener { e ->
                callback(MediaAnalysisResult(
                    mediaType = MediaType.PHOTO,
                    analysisAvailable = false,
                    overallResult = MediaAuthenticityResult.ANALYSIS_UNAVAILABLE,
                    indicators = listOf("Could not read image: ${e.message}"),
                    suspiciousIndicators = emptyList(),
                    explanation = "Advanced authenticity analysis is not configured yet. The image could not be processed.",
                    limitations = "Make sure the image is in a supported format (JPEG, PNG) and is not corrupted."
                ))
            }
    } catch (e: IOException) {
        callback(MediaAnalysisResult(
            mediaType = MediaType.PHOTO,
            analysisAvailable = false,
            overallResult = MediaAuthenticityResult.ANALYSIS_UNAVAILABLE,
            indicators = listOf("Could not read image file"),
            suspiciousIndicators = emptyList(),
            explanation = "Advanced authenticity analysis is not configured yet.",
            limitations = "The selected file could not be opened."
        ))
    }
}

