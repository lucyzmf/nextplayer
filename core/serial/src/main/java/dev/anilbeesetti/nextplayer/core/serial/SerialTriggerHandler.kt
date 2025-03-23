package dev.anilbeesetti.nextplayer.core.serial

import android.content.Context
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.anilbeesetti.nextplayer.core.serial.SerialPortConfig as SerialPortConfig

class SerialTriggerHandler(private val context: Context) {

    private var usbPort: UsbSerialPort? = null
    private var serialConfig: SerialPortConfig = SerialPortConfig.CONFIG_9600_8

    suspend fun initializeConnection() =
        withContext(Dispatchers.IO) {
            val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
            val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
            if (availableDrivers.isEmpty()) {
                return@withContext
            }

            val driver = availableDrivers[0]
            val connection = manager.openDevice(driver.device)
            if (connection == null) {
                // Handle permission request (not shown here)
                return@withContext
            }

            usbPort = driver.ports[0]
            usbPort?.open(connection)
            usbPort?.setParameters(serialConfig.baudRate, serialConfig.dataBits, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }

    suspend fun sendTrigger(triggerValue: Int) =
        withContext(Dispatchers.IO) {
            usbPort?.write(byteArrayOf(triggerValue.toByte()), 1000)
        }

    fun closeConnection() {
        usbPort?.close()
    }
}
