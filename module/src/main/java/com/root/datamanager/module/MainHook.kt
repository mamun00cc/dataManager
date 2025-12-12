package com.root.datamanager.module

import android.app.Application
import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File

class MainHook : IXposedHookLoadPackage {

    // UPDATE THIS TO YOUR EXACT MT MANAGER PACKAGE NAME
    private val HOST_PACKAGE = "bin.mt.plus.canary"

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName == "android") return

        XposedHelpers.findAndHookMethod(
            Application::class.java,
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val context = param.thisObject as Context
                    val dataDir = File(context.applicationInfo.dataDir)

                    if (lpparam.packageName == HOST_PACKAGE) {
                        // Role: HOST
                        HubServer(9001).start()
                        FtpService(2121).start()
                    } else {
                        // Role: AGENT
                        AgentClient(dataDir).start()
                    }
                }
            }
        )
    }
}
