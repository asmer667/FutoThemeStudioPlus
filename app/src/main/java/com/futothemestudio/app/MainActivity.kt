package com.futothemestudio.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import android.graphics.BitmapFactory
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.MediaStore
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioApp() }
    }
}

private val presets = listOf(
    "Cream" to Color(0xFFF1E2C9), "Ocean" to Color(0xFF16BDEB), "Forest" to Color(0xFF26965B),
    "Rose" to Color(0xFFF28AB2), "Dragon" to Color(0xFFFF4A32), "Violet" to Color(0xFF9A69F5),
    "Gold" to Color(0xFFD9A441), "Ice" to Color(0xFFBDEEFF), "Neon" to Color(0xFF39FF88)
)

@Composable
fun StudioApp() {
    val context = LocalContext.current
    var themeName by remember { mutableStateOf("Aurora Studio") }
    var author by remember { mutableStateOf("FutoThemeStudioPlus") }
    var selectedColor by remember { mutableStateOf(presets[0].second) }
    var shape by remember { mutableStateOf("Rounded") }
    var keyImage by remember { mutableStateOf<ByteArray?>(null) }
    var backgroundImage by remember { mutableStateOf<ByteArray?>(null) }
    var chosenTarget by remember { mutableStateOf("All typing keys") }
    var exportStatus by remember { mutableStateOf("جاهز للتخصيص والتصدير") }
    val keyPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) keyImage = context.contentResolver.openBytes(uri)
    }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) backgroundImage = context.contentResolver.openBytes(uri)
    }
    val saveZip = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            try {
                val bytes = makeThemeZip(context.assets, themeName, author, selectedColor, shape, keyImage, backgroundImage)
                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                exportStatus = "تم إنشاء ZIP. جرّبه في FUTO للتحقق من توافق إصدارك."
                Toast.makeText(context, "تم حفظ ملف الثيم ZIP", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                exportStatus = "تعذّر التصدير: ${e.message ?: "خطأ غير معروف"}"
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = selectedColor, background = Color(0xFF0A0D18), surface = Color(0xFF151A2A))) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF0A0D18)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(selectedColor), contentAlignment = Alignment.Center) {
                        Text("F", color = Color(0xFF101323), fontWeight = FontWeight.Black, fontSize = 29.sp)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("FUTO THEME STUDIO+", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                        Text("استوديو تصميم الثيمات والأزرار", color = Color(0xFFB5C0D9), fontSize = 13.sp)
                    }
                    Surface(color = Color(0xFF23304A), shape = RoundedCornerShape(20.dp)) { Text("KOTLIN", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color(0xFF8DE9E0), fontSize = 11.sp) }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151A2A)), border = BorderStroke(1.dp, Color(0xFF29334B))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("معاينة مباشرة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        KeyboardPreview(selectedColor, shape, keyImage)
                        Text("تغيير شكل الزر ولونه ينعكس على المعاينة وأصل الزر المُصدّر.", color = Color(0xFFABB6D0), fontSize = 12.sp)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151A2A)), border = BorderStroke(1.dp, Color(0xFF29334B))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("بيانات الثيم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        OutlinedTextField(themeName, { themeName = it }, Modifier.fillMaxWidth(), label = { Text("اسم الثيم") }, singleLine = true)
                        OutlinedTextField(author, { author = it }, Modifier.fillMaxWidth(), label = { Text("اسم المصمم") }, singleLine = true)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151A2A)), border = BorderStroke(1.dp, Color(0xFF29334B))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("شكل الزر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Rounded", "Pill", "Hexagon", "Cut", "Diamond").forEach { s ->
                                FilterChip(selected = shape == s, onClick = { shape = s }, label = { Text(s) })
                            }
                        }
                        Text("الألوان", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            presets.forEach { (label, c) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(onClick = { selectedColor = c }, modifier = Modifier.size(38.dp), shape = RoundedCornerShape(12.dp), color = c, border = BorderStroke(if (selectedColor == c) 3.dp else 1.dp, Color.White)) {}
                                    Text(label, color = Color(0xFFB5C0D9), fontSize = 10.sp)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { keyPicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (keyImage == null) "رفع صورة زر" else "تغيير صورة الزر") }
                            OutlinedButton(onClick = { backgroundPicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (backgroundImage == null) "رفع خلفية" else "تغيير الخلفية") }
                        }
                        Text("الصورة المختارة تُضمّن داخل ملف الثيم، ولا تُرفع إلى الإنترنت.", color = Color(0xFFABB6D0), fontSize = 12.sp)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151A2A)), border = BorderStroke(1.dp, Color(0xFF29334B))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("تطبيق التصميم على", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        listOf("All typing keys", "Top row", "Bottom row", "Spacebar", "Function keys").forEach { target ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = chosenTarget == target, onClick = { chosenTarget = target })
                                Text(target, color = Color.White)
                            }
                        }
                        Text("ملاحظة: اختيار الهدف محفوظ في واجهة الاستوديو حاليًا؛ قواعد المطابقة التفصيلية لكل مفتاح تحتاج إلى ضبط واختبار مع صيغة FUTO المستهدفة.", color = Color(0xFFE9C98B), fontSize = 12.sp)
                    }
                }
                Button(onClick = { saveZip.launch("${safeName(themeName)}.zip") }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text("تصدير ثيم FUTO بصيغة ZIP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(exportStatus, color = Color(0xFF9DE6CB), fontSize = 13.sp)
                Text("تنبيه توافق: يتضمن المشروع قالب FUTO Theme Configuration Format 1.0 وأصولًا من ثيم مرجعي. لا يمكن ضمان توافق 100٪ دون استيراد الملف واختباره في إصدار FUTO لديك.", color = Color(0xFF9AA7C2), fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun KeyboardPreview(color: Color, shape: String, imported: ByteArray?) {
    val bmp = remember(imported) { imported?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0B1323)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("QWERTYUIOP", "ASDFGHJKL", "⇧ZXCVBNM⌫").forEachIndexed { row, letters ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                letters.forEach { ch ->
                    Surface(modifier = Modifier.weight(1f).height(36.dp), color = color, shape = when(shape) { "Pill" -> RoundedCornerShape(50.dp); "Hexagon" -> RoundedCornerShape(5.dp); "Cut" -> RoundedCornerShape(2.dp); "Diamond" -> RoundedCornerShape(12.dp); else -> RoundedCornerShape(8.dp) }, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)), shadowElevation = 4.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            if (bmp != null) androidx.compose.foundation.Image(bmp, null, modifier = Modifier.fillMaxSize())
                            Text(ch.toString(), color = if (color.luminanceApprox() > 0.58f) Color(0xFF172033) else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("?123" to 0.9f, "☻" to 0.6f, "English (US)" to 3.6f, "," to 0.6f, "↵" to 0.9f).forEach { (s, weight) ->
                Surface(Modifier.weight(weight).height(32.dp), color = color.copy(alpha = 0.88f), shape = RoundedCornerShape(8.dp), shadowElevation = 3.dp) { Box(contentAlignment = Alignment.Center) { Text(s, color = if (color.luminanceApprox() > 0.58f) Color(0xFF172033) else Color.White, fontSize = 10.sp) } }
            }
        }
    }
}

private fun Color.luminanceApprox(): Float = (0.2126f * red + 0.7152f * green + 0.0722f * blue)
private fun safeName(s: String) = s.trim().replace(Regex("[^A-Za-z0-9_-]+"), "-").trim('-').ifBlank { "FUTO-Theme" }
private fun android.content.ContentResolver.openBytes(uri: Uri): ByteArray? = openInputStream(uri)?.use { it.readBytes() }

private fun makeThemeZip(assets: android.content.res.AssetManager, name: String, author: String, color: Color, shape: String, keyImage: ByteArray?, bgImage: ByteArray?): ByteArray {
    val out = ByteArrayOutputStream()
    val hex = String.format("#%02X%02X%02X", (color.red*255).toInt(), (color.green*255).toInt(), (color.blue*255).toInt())
    val themeId = "studio.futoplus.${safeName(name).lowercase()}"
    ZipOutputStream(out).use { zip ->
        val files = assets.list("template")?.toList().orEmpty()
        var txt = assets.open("template/theme.txt").bufferedReader().use { it.readText() }
        txt = txt.replace(Regex("(?m)^name = .*?$"), "name = \"${name.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
            .replace(Regex("(?m)^author = .*?$"), "author = \"${author.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
            .replace(Regex("(?m)^id = .*?$"), "id = \"$themeId\"")
            .replace(Regex("(?m)^primary = \"#[0-9A-Fa-f]{6,8}\".*$"), "primary = \"${hex}FF\"")
            .replace(Regex("(?m)^keyboard_press = \"#[0-9A-Fa-f]{6,8}\".*$"), "keyboard_press = \"${hex}FF\"")
        if (bgImage != null) {
            txt = txt.replace("image = \"PetKeys-background.png\"", "image = \"Custom-background.png\"")
        }
        zip.putNextEntry(ZipEntry("theme.txt")); zip.write(txt.toByteArray(Charsets.UTF_8)); zip.closeEntry()
        for (file in files) {
            if (file == "theme.txt") continue
            if (file == "Button-default.png") {
                val customKey = keyImage ?: createKeyPng(shape, color)
                zip.putNextEntry(ZipEntry(file)); zip.write(customKey); zip.closeEntry(); continue
            }
            if (file == "PetKeys-background.png" && bgImage != null) {
                zip.putNextEntry(ZipEntry("Custom-background.png")); zip.write(bgImage); zip.closeEntry(); continue
            }
            zip.putNextEntry(ZipEntry(file)); assets.open("template/$file").use { it.copyTo(zip) }; zip.closeEntry()
        }
        if (bgImage != null && "PetKeys-background.png" !in files) { zip.putNextEntry(ZipEntry("Custom-background.png")); zip.write(bgImage); zip.closeEntry() }
        val readme = """# $name
Generated by FUTO Theme Studio Plus.

Theme configuration uses FUTO Keyboard Theme Configuration Format 1.0. Import this ZIP in FUTO Keyboard and verify the result on your installed version.

Shape selected in the studio: $shape
Target selection: configure individual selectors in theme.txt for precise per-key behavior.
"""
        zip.putNextEntry(ZipEntry("README.md")); zip.write(readme.toByteArray()); zip.closeEntry()
    }
    return out.toByteArray()
}


private fun createKeyPng(shape: String, color: Color): ByteArray {
    val size = 256
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    canvas.drawColor(AndroidColor.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    val rgb = color.toArgb()
    val top = AndroidColor.argb(255,
        (AndroidColor.red(rgb) + 34).coerceAtMost(255),
        (AndroidColor.green(rgb) + 34).coerceAtMost(255),
        (AndroidColor.blue(rgb) + 34).coerceAtMost(255))
    val bottom = AndroidColor.argb(255,
        (AndroidColor.red(rgb) - 25).coerceAtLeast(0),
        (AndroidColor.green(rgb) - 25).coerceAtLeast(0),
        (AndroidColor.blue(rgb) - 25).coerceAtLeast(0))
    val path = Path()
    when (shape) {
        "Pill" -> path.addRoundRect(RectF(18f, 28f, 238f, 228f), 95f, 95f, Path.Direction.CW)
        "Hexagon" -> { path.moveTo(65f, 20f); path.lineTo(191f, 20f); path.lineTo(238f, 128f); path.lineTo(191f, 236f); path.lineTo(65f, 236f); path.lineTo(18f, 128f); path.close() }
        "Cut" -> { path.moveTo(45f, 18f); path.lineTo(211f, 18f); path.lineTo(238f, 45f); path.lineTo(238f, 211f); path.lineTo(211f, 238f); path.lineTo(45f, 238f); path.lineTo(18f, 211f); path.lineTo(18f, 45f); path.close() }
        "Diamond" -> { path.moveTo(128f, 12f); path.lineTo(244f, 128f); path.lineTo(128f, 244f); path.lineTo(12f, 128f); path.close() }
        else -> path.addRoundRect(RectF(18f, 18f, 238f, 238f), 36f, 36f, Path.Direction.CW)
    }
    val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(120, 0, 0, 0); style = Paint.Style.FILL }
    canvas.save(); canvas.translate(0f, 9f); canvas.drawPath(path, shadow); canvas.restore()
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(0f, 20f, 0f, 238f, top, bottom, Shader.TileMode.CLAMP) }
    canvas.drawPath(path, fill)
    val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(220, 255, 255, 255); style = Paint.Style.STROKE; strokeWidth = 5f }
    canvas.drawPath(path, stroke)
    val shine = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.argb(75, 255, 255, 255); style = Paint.Style.STROKE; strokeWidth = 3f }
    canvas.save(); canvas.clipPath(path); canvas.drawLine(38f, 36f, 216f, 36f, shine); canvas.restore()
    val output = ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.PNG, 100, output); bmp.recycle()
    return output.toByteArray()
}
