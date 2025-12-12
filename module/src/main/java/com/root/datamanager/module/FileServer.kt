package com.root.datamanager.module

import fi.iki.elonen.NanoHTTPD

class FileServer(port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        return newFixedLengthResponse("Hello from inside " + android.os.Process.myPid())
    }
}
