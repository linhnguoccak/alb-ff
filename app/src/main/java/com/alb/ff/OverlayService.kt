package com.alb.ff

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.WindowManager
import kotlin.concurrent.thread

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var menuView: MenuView
    private lateinit var cheatView: CheatView
    private var running = true

    private val FF_PKG      = "com.dts.freefireth"
    private val BONE_HEAD   = 0x1C8L
    private val ENTITY_STEP = 0x2A0L
    private val MAX_ENT     = 32
    private val VIEW_MATRIX = 0L

    private var pid        = -1
    private var entityBase = -1L

    // vị trí menu
    private var menuX = 30
    private var menuY = 200
    private lateinit var menuParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotif())
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        addViews()
        thread { loop() }
    }

    private fun dpToPx(dp: Float): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()

    private fun addViews() {
        menuView  = MenuView(this)
        cheatView = CheatView(this, menuView)

        // ESP: full screen, không nhận touch
        val espParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        // Menu: đúng kích thước menu, nhận touch chỉ trong vùng đó
        menuParams = WindowManager.LayoutParams(
            dpToPx(menuView.menuW),
            dpToPx(menuView.menuH),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = menuX
            y = menuY
        }

        wm.addView(cheatView, espParams)
        wm.addView(menuView, menuParams)

        // kéo menu → cập nhật vị trí WM
        menuView.onDrag = { dx, dy ->
            menuX = (menuX + dx.toInt()).coerceIn(0, 900)
            menuY = (menuY + dy.toInt()).coerceIn(0, 1700)
            menuParams.x = menuX
            menuParams.y = menuY
            wm.updateViewLayout(menuView, menuParams)
        }
    }

    private fun loop() {
        while (pid == -1 && running) {
            pid = MemoryBridge.findPid(FF_PKG)
            Thread.sleep(1000)
        }

        entityBase = MemoryBridge.patternScan(
            pid,
            byteArrayOf(
                0xA0.toByte(), 0x00, 0x00, 0x00,
                0x00, 0x00, 0x80.toByte(), 0x3F
            ),
            "x???xxxx",
            0x70000000L,
            0x90000000L
        )

        while (running) {
            val f        = menuView.features
            val aimbotOn = f[0].enabled
            val espAny   = f[1].enabled || f[2].enabled || f[3].enabled

            if ((aimbotOn || espAny) && entityBase != -1L) {
                val list     = mutableListOf<CheatView.Entity>()
                var closestD = Float.MAX_VALUE
                var aimT: Pair<Float, Float>? = null

                for (i in 0 until MAX_ENT) {
                    val hAddr = entityBase + i * ENTITY_STEP + BONE_HEAD
                    val head  = MemoryBridge.readVec3(pid, hAddr)
                    if (head[0] == 0f && head[1] == 0f) continue

                    val screen = w2s(head[0], head[1], head[2]) ?: continue
                    val dist   = 80f
                    val h      = (300f / dist * 10f).coerceIn(40f, 400f)
                    list.add(CheatView.Entity(screen.first, screen.second, h, dist))

                    if (aimbotOn) {
                        val dx = screen.first  - cheatView.width  / 2f
                        val dy = screen.second - cheatView.height * 0.45f
                        val d  = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                        if (d < 350f && d < closestD) {
                            closestD = d
                            aimT     = screen
                        }
                    }
                }

                cheatView.entities  = list
                cheatView.aimTarget = aimT
                cheatView.postInvalidate()
            }

            Thread.sleep(50)
        }
    }

    private fun w2s(wx: Float, wy: Float, wz: Float): Pair<Float, Float>? {
        if (VIEW_MATRIX == 0L) return null
        val m = MemoryBridge.readMatrix(pid, VIEW_MATRIX)
        if (m.size < 16) return null
        val cx = m[0]*wx + m[4]*wy + m[8]*wz  + m[12]
        val cy = m[1]*wx + m[5]*wy + m[9]*wz  + m[13]
        val cw = m[3]*wx + m[7]*wy + m[11]*wz + m[15]
        if (cw <= 0.01f) return null
        val sw = cheatView.width.toFloat()
        val sh = cheatView.height.toFloat()
        val sx = (cx / cw + 1f) * 0.5f * sw
        val sy = (1f - cy / cw) * 0.5f * sh
        if (sx < -200 || sx > sw + 200) return null
        if (sy < -200 || sy > sh + 200) return null
        return sx to sy
    }

    private fun buildNotif(): Notification {
        val ch = NotificationChannel(
            "alb", "ALB",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(ch)
        return Notification.Builder(this, "alb")
            .setContentTitle("System Service")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .build()
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        running = false
        try { wm.removeView(cheatView) } catch (_: Exception) {}
        try { wm.removeView(menuView)  } catch (_: Exception) {}
    }
}
