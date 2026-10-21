package com.alb.ff

// Shizuku approach không khả thi không root trên Android 14
// Dùng lại MemoryBridge (JNI) — hoạt động khi FF cùng UID (X8 Sandbox)
object ShizukuMemory {

    fun isAvailable(): Boolean = true

    fun findPid(packageName: String): Int =
        MemoryBridge.findPid(packageName)

    fun readBytes(targetPid: Int, address: Long, size: Int): ByteArray? {
        val result = ByteArray(size)
        // đọc từng float
        return null
    }

    fun readFloat(targetPid: Int, address: Long): Float =
        MemoryBridge.readFloat(targetPid, address)

    fun readVec3(targetPid: Int, address: Long): FloatArray =
        MemoryBridge.readVec3(targetPid, address)

    fun readMatrix(targetPid: Int, address: Long): FloatArray =
        MemoryBridge.readMatrix(targetPid, address)

    fun writeFloat(targetPid: Int, address: Long, value: Float): Boolean =
        MemoryBridge.writeFloat(targetPid, address, value)

    fun patternScan(
        targetPid: Int,
        pattern: ByteArray,
        mask: String,
        regionStart: Long,
        regionEnd: Long
    ): Long = MemoryBridge.patternScan(targetPid, pattern, mask, regionStart, regionEnd)
}
