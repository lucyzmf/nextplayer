package dev.anilbeesetti.nextplayer.core.serial

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SerialTriggerHandlerTest {

    private lateinit var context: Context
    private lateinit var usbManager: UsbManager
    private lateinit var usbDevice: UsbDevice
    private lateinit var usbConnection: UsbDeviceConnection
    private lateinit var usbSerialDriver: UsbSerialDriver
    private lateinit var usbSerialPort: UsbSerialPort
    private lateinit var usbSerialProber: UsbSerialProber
    private lateinit var serialTriggerHandler: SerialTriggerHandler

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        usbManager = mockk(relaxed = true)
        usbDevice = mockk(relaxed = true)
        usbConnection = mockk(relaxed = true)
        usbSerialDriver = mockk(relaxed = true)
        usbSerialPort = mockk(relaxed = true)
        usbSerialProber = mockk(relaxed = true)

        every { context.getSystemService(Context.USB_SERVICE) } returns usbManager
        
        mockkStatic(UsbSerialProber::class)
        every { UsbSerialProber.getDefaultProber() } returns usbSerialProber
        every { usbSerialProber.findAllDrivers(usbManager) } returns listOf(usbSerialDriver)
        
        every { usbSerialDriver.device } returns usbDevice
        every { usbSerialDriver.ports } returns listOf(usbSerialPort)
        every { usbManager.openDevice(usbDevice) } returns usbConnection
        
        every { usbSerialPort.open(usbConnection) } returns Unit
        every { usbSerialPort.setParameters(any(), any(), any(), any()) } returns Unit
        every { usbSerialPort.write(any(), any()) } returns 0
        every { usbSerialPort.close() } returns Unit

        serialTriggerHandler = SerialTriggerHandler(context)
    }

    @Test
    fun `initializeConnection should open port with correct parameters`() = runTest {
        // When
        serialTriggerHandler.initializeConnection()

        // Then
        verify { usbSerialPort.open(usbConnection) }
        verify { usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE) }
    }

    @Test
    fun `initializeConnection should handle empty drivers list`() = runTest {
        // Given
        every { usbSerialProber.findAllDrivers(usbManager) } returns emptyList()

        // When
        serialTriggerHandler.initializeConnection()

        // Then - should return early without errors
        verify(exactly = 0) { usbManager.openDevice(any()) }
    }

    @Test
    fun `initializeConnection should handle null connection`() = runTest {
        // Given
        every { usbManager.openDevice(usbDevice) } returns null

        // When
        serialTriggerHandler.initializeConnection()

        // Then - should return early without errors
        verify(exactly = 0) { usbSerialPort.open(any()) }
    }

    @Test
    fun `sendTrigger should write bytes to port`() = runTest {
        // Given
        val byteSlot = slot<ByteArray>()
        val timeoutSlot = slot<Int>()
        every { usbSerialPort.write(capture(byteSlot), capture(timeoutSlot)) } returns 5
        
        // Initialize connection first
        serialTriggerHandler.initializeConnection()
        
        // When
        val testTrigger = "TEST"
        serialTriggerHandler.sendTrigger(testTrigger)

        // Then
        verify { usbSerialPort.write(any(), any()) }
        assertEquals(testTrigger.toByteArray().toList(), byteSlot.captured.toList())
        assertEquals(1000, timeoutSlot.captured)
    }

    @Test
    fun `closeConnection should close port`() {
        // Given
        // Initialize connection first
        runTest { serialTriggerHandler.initializeConnection() }
        
        // When
        serialTriggerHandler.closeConnection()

        // Then
        verify { usbSerialPort.close() }
    }
}
