import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.Feature.Type
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.thbt.goodbyemoney.R
import java.io.InputStream
import java.io.ByteArrayOutputStream

class ImageRepository {

    suspend fun analyzeImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Load credentials from JSON file in res/raw
                val inputStream: InputStream = context.resources.openRawResource(R.raw.abc)
                val credentials = GoogleCredentials.fromStream(inputStream)

                // Convert image URI to bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                Log.d("ImageRepository", "Bitmap Width: ${bitmap.width}, Height: ${bitmap.height}")

                // Convert bitmap to bytes
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = ByteString.copyFrom(byteArrayOutputStream.toByteArray())
                Log.d("ImageRepository", "Image bytes prepared")

                // Prepare the image request for the Vision API
                val img = Image.newBuilder().setContent(imageBytes).build()
                val feature = Feature.newBuilder().setType(Type.TEXT_DETECTION).build()
                val request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(img)
                    .build()

                // Process the image using Google Vision API
                ImageAnnotatorClient.create().use { client ->
                    val response = client.batchAnnotateImages(listOf(request))

                    // Log the API response
                    Log.d("ImageRepository", "API Response: $response")

                    // Handle any API response errors
                    val firstResponse = response.responsesList.getOrNull(0)
                    if (firstResponse == null) {
                        Log.e("ImageRepository", "API response list is empty")
                        return@withContext null
                    } else if (firstResponse.hasError()) {
                        Log.e("ImageRepository", "API Error: ${firstResponse.error.message}")
                        return@withContext null
                    }

                    // Extract and log text from response if available
                    val textAnnotations = firstResponse.textAnnotationsList
                    if (textAnnotations.isNotEmpty()) {
                        val extractedText = textAnnotations[0].description
                        Log.d("ImageRepository", "Extracted Text: $extractedText")
                        return@withContext extractedText
                    } else {
                        Log.d("ImageRepository", "No text found in the image.")
                        return@withContext null
                    }
                }
            } catch (e: Exception) {
                Log.e("ImageRepository", "Error in analyzeImage function", e)
                null
            }
        }
    }
}
