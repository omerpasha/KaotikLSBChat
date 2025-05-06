package com.omer.stegochat

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EncodeAndDecodeHelper {
    companion object {
        private const val a = 3.6
        private const val b = 0.4
        private var x0 = 0.1461114611461
        private val siralar = mutableListOf<Int>()

        fun siraGetir(): Int {
            x0 = chaoticMap(x0)
            val arasira = ((x0 * 10000000000000000.0).toInt() % siralar.size)
            val sira = siralar[arasira]
            siralar.removeAt(arasira)
            return sira
        }

        fun encode(bitmap: Bitmap, textToHide: String): EncodeResult {
            val pixels = IntArray(bitmap.width * bitmap.height)
            siralar.clear()
            for (i in pixels.indices) {
                siralar.add(i)
            }

            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            var sira = siraGetir()
            var pixel = 0
            var alpha = 0
            var red = 0
            var green = 0
            var blue = 0

            // Kullanılan indisleri kaydetmek için liste
            val usedIndices = mutableListOf<Int>()

            for (c in textToHide.toCharArray()) {
                var binary = Integer.toBinaryString(c.toInt())
                while (binary.length < 9) {
                    binary = "0$binary"
                }

                for (i in 0 until 9) {
                    if (i % 3 == 0) {
                        sira = siraGetir()
                        usedIndices.add(sira) // Kullanılan indisi kaydet
                        pixel = pixels[sira]
                        alpha = Color.alpha(pixel)
                        red = Color.red(pixel)
                        green = Color.green(pixel)
                        blue = Color.blue(pixel)
                    }

                    when (i % 3) {
                        0 -> red = mygetPixel(red, binary, i)
                        1 -> green = mygetPixel(green, binary, i)
                        2 -> blue = mygetPixel(blue, binary, i)
                    }

                    if (i % 3 == 2) {
                        val newColor = Color.argb(alpha, red, green, blue)
                        pixels[sira] = newColor
                    }
                }
            }

            // Flag için son indisi de kaydet
            alpha = 255
            red = 255
            green = 255
            blue = 255
            val newColor = Color.argb(alpha, red, green, blue)
            sira = siraGetir()
            usedIndices.add(sira) // Son flag için kullanılan indisi kaydet
            pixels[sira] = newColor

            val encodedBitmap = Bitmap.createBitmap(pixels, bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            return EncodeResult(encodedBitmap, usedIndices)
        }

        fun decode(bitmap: Bitmap, chaosIndices: List<Int>): String {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            var sira: Int
            var red: Int
            var green: Int
            var blue: Int
            var binary = ""

            // Kaotik indisleri kullanarak mesajı çöz
            for (index in chaosIndices) {
                sira = index
                if (Color.red(pixels[sira]) == 255 && Color.green(pixels[sira]) == 255 && 
                    Color.blue(pixels[sira]) == 255 && Color.alpha(pixels[sira]) == 255) {
                    binary += "11111111"
                    return binarySplitter(binary)
                }

                red = Color.red(pixels[sira])
                binary = mygetPixel2(red, binary, 0)
                
                green = Color.green(pixels[sira])
                binary = mygetPixel2(green, binary, 1)
                
                blue = Color.blue(pixels[sira])
                binary = mygetPixel2(blue, binary, 2)
            }
            return binarySplitter(binary)
        }

        private fun mygetPixel(pixel: Int, binary: String, i: Int): Int {
            return if (binary[i] == '1') {
                if (pixel % 2 == 0) pixel + 1 else pixel
            } else {
                if (pixel % 2 == 1) pixel - 1 else pixel
            }
        }

        private fun mygetPixel2(pixel: Int, binary: String, i: Int): String {
            val snc = pixel and 1
            return binary + if (snc == 1) "1" else "0"
        }

        private fun binarySplitter(binaryNumber: String): String {
            val binarySplitting = StringBuilder()
            val groupSize = 9
            val length = binaryNumber.length
            val numGroups = (length.toDouble() / groupSize).toInt()
            val bitGroups = Array(numGroups) { "" }

            for (i in 0 until numGroups) {
                val startIndex = i * groupSize
                val endIndex = (startIndex + groupSize).coerceAtMost(length)
                bitGroups[i] = binaryNumber.substring(startIndex, endIndex)
            }

            for (group in bitGroups) {
                if (group != "1") {
                    binarySplitting.append(binaryToChar(group))
                }
            }
            return binarySplitting.toString()
        }

        private fun binaryToChar(binary: String): String {
            val charCode = binary.toInt(2)
            return charCode.toChar().toString()
        }

        private fun chaoticMap(x: Double): Double {
            val lambda = 3.9914611461
            return lambda * x * (1 - x)
        }

        fun saveBitmapToStorage(bitmap: Bitmap, filename: String) {
            val directory = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Steganography")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)

            try {
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
} 