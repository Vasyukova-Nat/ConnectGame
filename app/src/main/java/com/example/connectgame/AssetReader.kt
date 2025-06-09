// Утилита для чтения файлов с вопросами
import android.content.Context
import java.io.IOException

object AssetReader {
    fun readLinesFromAsset(context: Context, fileName: String): List<String> {
        return try {
            context.assets.open(fileName)
                .bufferedReader()
                .useLines { lines ->
                    lines.filter { it.isNotBlank() } // Игнорируем пустые строки
                        .toList()
                }
        } catch (e: IOException) {
            emptyList()
        }
    }
}