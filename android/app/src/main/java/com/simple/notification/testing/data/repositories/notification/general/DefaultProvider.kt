package com.simple.notification.testing.data.repositories.notification.general

import com.simple.notification.testing.data.repositories.notification.ODEProvider

interface DefaultProvider : ODEProvider {

    override fun accept(): Boolean = true
}
