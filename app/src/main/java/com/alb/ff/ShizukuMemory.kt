package com.alb.ff

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.IBinder
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ShizukuMemory {

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) { false }
    }

    // chạy lệnh shell qua Shizuku
    private fun exec(cmd: String): String {
        return try {
            val process = ShizukuRemoteProcess(
                Shizuku.getBinder(),
                arrayOf("sh", "-c", cmd),
                null, null
            )
            val out = process.inputStream.bufferedReader().readText().trim()
            process.destroy()
            out
        } catch (e: Exception) { "" }
    }

    fun findPid(packageName: String): Int {
        val out = exec("pidof $packageName")
        return out.split(" ").firstOrNull()?.toIntOrNull() ?: -1
    }

    fun readBytes(targetPid: Int, address: Long, size: Int): ByteArray? {
        return try {
            val cmd = "dd if=/proc/$targetPid/mem" +
                " ibs=1 skip=$address count=$size 2>/dev/null | base64 -w 0"
            val b64 = exec(cmd)
            if (b64.isEmpty()) null
            else android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
        } catch (e: Exception) { null }
    }

    fun readFloat(targetPid: Int, address: Long): Float {
        val b = readBytes(targetPid, address, 4) ?: return 0f
        if (b.size < 4) return 0f
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).float
    }

    fun readVec3(targetPid: Int, address: Long): FloatArray {
        val b = readBytes(targetPid, address, 12) ?: return FloatArray(3)
        if (b.size < 12) return FloatArray(3)
        val buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        return floatArrayOf(buf.float, buf.float, buf.float)
    }

    fun readMatrix(targetPid: Int, address: Long): FloatArray {
        val b = readBytes(targetPid, address, 64) ?: return FloatArray(16)
        if (b.size < 64) return FloatArray(16)
        val buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        return FloatArray(16) { buf.float }
    }

    fun writeFloat(targetPid: Int, address: Long, value: Float): Boolean {
        return try {
            val bytes = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(value).array()
            val escaped = bytes.joinToString("") { "\\x%02x".format(it) }
            exec("printf '$escaped' | dd of=/proc/$targetPid/mem" +
                " bs=1 seek=$address conv=notrunc 2>/dev/null")
            true
        } catch (e: Exception) { false }
    }

    fun patternScan(
        targetPid: Int,
        pattern: ByteArray,
        mask: String,
        regionStart: Long,
        regionEnd: Long
    ): Long {
        val chunkSize = 0x8000L
        var addr = regionStart
        while (addr < regionEnd - pattern.size) {
            val toRead = minOf(chunkSize, regionEnd - addr).toInt()
            val buf = readBytes(targetPid, addr, toRead)
            if (buf != null) {
                for (i in 0..buf.size - pattern.size) {
                    var match = true
