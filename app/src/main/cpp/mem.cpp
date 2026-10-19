#include <jni.h>
#include <cstdio>
#include <cstring>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/uio.h>
#include <vector>
#include <algorithm>

static bool readMem(int pid, long addr, void* buf, size_t size) {
    struct iovec l = { buf, size };
    struct iovec r = { reinterpret_cast<void*>(addr), size };
    return process_vm_readv(pid, &l, 1, &r, 1, 0)
           == static_cast<ssize_t>(size);
}

static bool writeMem(int pid, long addr, const void* buf, size_t size) {
    struct iovec l = { const_cast<void*>(buf), size };
    struct iovec r = { reinterpret_cast<void*>(addr), size };
    return process_vm_writev(pid, &l, 1, &r, 1, 0)
           == static_cast<ssize_t>(size);
}

extern "C" {

JNIEXPORT jint JNICALL
Java_com_alb_ff_MemoryBridge_findPid(JNIEnv* env, jobject, jstring jname) {
    const char* name = env->GetStringUTFChars(jname, nullptr);
    DIR* dir = opendir("/proc");
    jint result = -1;
    if (!dir) { env->ReleaseStringUTFChars(jname, name); return -1; }
    struct dirent* e;
    while ((e = readdir(dir))) {
        int pid = atoi(e->d_name);
        if (pid <= 0) continue;
        char path[64], buf[256];
        snprintf(path, sizeof(path), "/proc/%d/cmdline", pid);
        FILE* f = fopen(path, "r");
        if (!f) continue;
        if (fgets(buf, sizeof(buf), f) && strstr(buf, name))
            { result = pid; fclose(f); break; }
        fclose(f);
    }
    closedir(dir);
    env->ReleaseStringUTFChars(jname, name);
    return result;
}

JNIEXPORT jfloat JNICALL
Java_com_alb_ff_MemoryBridge_readFloat(JNIEnv*, jobject, jint pid, jlong addr) {
    float v = 0.f;
    readMem(pid, addr, &v, 4);
    return v;
}

JNIEXPORT jfloatArray JNICALL
Java_com_alb_ff_MemoryBridge_readVec3(JNIEnv* env, jobject, jint pid, jlong addr) {
    float v[3] = {};
    readMem(pid, addr, v, 12);
    jfloatArray a = env->NewFloatArray(3);
    env->SetFloatArrayRegion(a, 0, 3, v);
    return a;
}

JNIEXPORT jboolean JNICALL
Java_com_alb_ff_MemoryBridge_writeFloat(JNIEnv*, jobject, jint pid, jlong addr, jfloat val) {
    float v = val;
    return writeMem(pid, addr, &v, 4) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloatArray JNICALL
Java_com_alb_ff_MemoryBridge_readMatrix(JNIEnv* env, jobject, jint pid, jlong addr) {
    float m[16] = {};
    readMem(pid, addr, m, 64);
    jfloatArray a = env->NewFloatArray(16);
    env->SetFloatArrayRegion(a, 0, 16, m);
    return a;
}

JNIEXPORT jlong JNICALL
Java_com_alb_ff_MemoryBridge_patternScan(
    JNIEnv* env, jobject,
    jint pid, jbyteArray jpat, jstring jmask,
    jlong rStart, jlong rEnd)
{
    jsize     plen = env->GetArrayLength(jpat);
    jbyte*    pat  = env->GetByteArrayElements(jpat, nullptr);
    const char* mask = env->GetStringUTFChars(jmask, nullptr);

    const size_t CHUNK = 0x10000;
    std::vector<uint8_t> buf(CHUNK);
    jlong result = -1;

    for (jlong a = rStart; a < rEnd - plen; a += CHUNK) {
        size_t n = (size_t)std::min((jlong)CHUNK, rEnd - a);
        if (!readMem(pid, a, buf.data(), n)) continue;
        for (size_t i = 0; i < n - plen; ++i) {
            bool ok = true;
            for (int j = 0; j < plen; ++j) {
                if (mask[j] == '?') continue;
                if (buf[i+j] != (uint8_t)pat[j]) { ok = false; break; }
            }
            if (ok) { result = a + i; goto done; }
        }
    }
done:
    env->ReleaseByteArrayElements(jpat, pat, JNI_ABORT);
    env->ReleaseStringUTFChars(jmask, mask);
    return result;
}

} // extern "C"
