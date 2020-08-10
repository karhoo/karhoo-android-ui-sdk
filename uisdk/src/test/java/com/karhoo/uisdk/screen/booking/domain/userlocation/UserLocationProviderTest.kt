package com.karhoo.uisdk.screen.booking.domain.userlocation

import android.content.Context
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.service.address.AddressService
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserLocationProviderTest {

    internal lateinit var userLocationProvider: LocationProvider

    internal var addressService: AddressService = KarhooApi.addressService
    internal var context: Context = mock()

    @Before
    fun setUp() {
        userLocationProvider = LocationProvider(context = context, addressService = addressService)
    }

    
}