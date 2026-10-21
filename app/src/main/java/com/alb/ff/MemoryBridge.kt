package com.alb.ff

object MemoryBridge {
    init { System.loadLibrary("alb_mem") }

    external fun findPid(processName: String): Int
    external fun readFloat(pid: Int, address: Long): Float
    external fun readVec3(pid: Int, address: Long): FloatArray
    external fun writeFloat(pid: Int, address: Long, value: Float): Boolean
    external fun readMatrix(pid: Int, address: Long): FloatArray
    external fun patternScan(
        pid: Int,
        pattern: ByteArray,
        mask: String,
        regionStart: Long,
        regionEnd: Long
    ): Long
}
