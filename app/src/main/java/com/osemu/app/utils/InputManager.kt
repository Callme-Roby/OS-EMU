package com.osemu.app.utils

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Manages physical gamepad / controller input mapping.
 * Supports standard Android gamepads, Xbox, PlayStation, and
 * built-in controls on handhelds like Ayn Thor and Steam Deck.
 */
class InputManager {

    data class ControllerState(
        val deviceId: Int,
        val deviceName: String,
        val buttons: MutableMap<Int, Boolean> = mutableMapOf(),
        val axes: MutableMap<Int, Float> = mutableMapOf()
    )

    private val controllers = mutableMapOf<Int, ControllerState>()

    var onInputEvent: ((port: Int, button: Int, pressed: Boolean) -> Unit)? = null
    var onAxisEvent: ((port: Int, axis: Int, value: Float) -> Unit)? = null

    fun getConnectedControllers(): List<ControllerState> {
        val gameControllerDeviceIds = mutableListOf<Int>()
        val deviceIds = InputDevice.getDeviceIds()

        for (deviceId in deviceIds) {
            InputDevice.getDevice(deviceId)?.let { device ->
                val sources = device.sources
                if (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
                    sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
                ) {
                    if (!gameControllerDeviceIds.contains(deviceId)) {
                        gameControllerDeviceIds.add(deviceId)
                        controllers[deviceId] = ControllerState(
                            deviceId = deviceId,
                            deviceName = device.name
                        )
                    }
                }
            }
        }

        return controllers.values.toList()
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val deviceId = event.deviceId
        val controller = controllers[deviceId] ?: return false

        val pressed = event.action == KeyEvent.ACTION_DOWN
        controller.buttons[event.keyCode] = pressed

        val port = controllers.keys.indexOf(deviceId)
        val retroButton = mapKeyToRetro(event.keyCode)

        if (retroButton >= 0) {
            onInputEvent?.invoke(port, retroButton, pressed)
            return true
        }

        return false
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        val deviceId = event.deviceId
        val controller = controllers[deviceId] ?: return false
        val port = controllers.keys.indexOf(deviceId)

        // Left stick
        val leftX = event.getAxisValue(MotionEvent.AXIS_X)
        val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
        controller.axes[MotionEvent.AXIS_X] = leftX
        controller.axes[MotionEvent.AXIS_Y] = leftY
        onAxisEvent?.invoke(port, RETRO_AXIS_LEFT_X, leftX)
        onAxisEvent?.invoke(port, RETRO_AXIS_LEFT_Y, leftY)

        // Right stick
        val rightX = event.getAxisValue(MotionEvent.AXIS_Z)
        val rightY = event.getAxisValue(MotionEvent.AXIS_RZ)
        controller.axes[MotionEvent.AXIS_Z] = rightX
        controller.axes[MotionEvent.AXIS_RZ] = rightY
        onAxisEvent?.invoke(port, RETRO_AXIS_RIGHT_X, rightX)
        onAxisEvent?.invoke(port, RETRO_AXIS_RIGHT_Y, rightY)

        // Triggers
        val lt = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        val rt = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        onAxisEvent?.invoke(port, RETRO_AXIS_LT, lt)
        onAxisEvent?.invoke(port, RETRO_AXIS_RT, rt)

        // D-pad via hat axis
        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
        onInputEvent?.invoke(port, RETRO_DEVICE_ID_JOYPAD_LEFT, hatX < -0.5f)
        onInputEvent?.invoke(port, RETRO_DEVICE_ID_JOYPAD_RIGHT, hatX > 0.5f)
        onInputEvent?.invoke(port, RETRO_DEVICE_ID_JOYPAD_UP, hatY < -0.5f)
        onInputEvent?.invoke(port, RETRO_DEVICE_ID_JOYPAD_DOWN, hatY > 0.5f)

        return true
    }

    companion object {
        // Retro button IDs (libretro standard)
        const val RETRO_DEVICE_ID_JOYPAD_B = 0
        const val RETRO_DEVICE_ID_JOYPAD_Y = 1
        const val RETRO_DEVICE_ID_JOYPAD_SELECT = 2
        const val RETRO_DEVICE_ID_JOYPAD_START = 3
        const val RETRO_DEVICE_ID_JOYPAD_UP = 4
        const val RETRO_DEVICE_ID_JOYPAD_DOWN = 5
        const val RETRO_DEVICE_ID_JOYPAD_LEFT = 6
        const val RETRO_DEVICE_ID_JOYPAD_RIGHT = 7
        const val RETRO_DEVICE_ID_JOYPAD_A = 8
        const val RETRO_DEVICE_ID_JOYPAD_X = 9
        const val RETRO_DEVICE_ID_JOYPAD_L = 10
        const val RETRO_DEVICE_ID_JOYPAD_R = 11
        const val RETRO_DEVICE_ID_JOYPAD_L2 = 12
        const val RETRO_DEVICE_ID_JOYPAD_R2 = 13
        const val RETRO_DEVICE_ID_JOYPAD_L3 = 14
        const val RETRO_DEVICE_ID_JOYPAD_R3 = 15

        const val RETRO_AXIS_LEFT_X = 0
        const val RETRO_AXIS_LEFT_Y = 1
        const val RETRO_AXIS_RIGHT_X = 2
        const val RETRO_AXIS_RIGHT_Y = 3
        const val RETRO_AXIS_LT = 4
        const val RETRO_AXIS_RT = 5

        fun mapKeyToRetro(keyCode: Int): Int {
            return when (keyCode) {
                KeyEvent.KEYCODE_BUTTON_A -> RETRO_DEVICE_ID_JOYPAD_B
                KeyEvent.KEYCODE_BUTTON_B -> RETRO_DEVICE_ID_JOYPAD_A
                KeyEvent.KEYCODE_BUTTON_X -> RETRO_DEVICE_ID_JOYPAD_Y
                KeyEvent.KEYCODE_BUTTON_Y -> RETRO_DEVICE_ID_JOYPAD_X
                KeyEvent.KEYCODE_BUTTON_L1 -> RETRO_DEVICE_ID_JOYPAD_L
                KeyEvent.KEYCODE_BUTTON_R1 -> RETRO_DEVICE_ID_JOYPAD_R
                KeyEvent.KEYCODE_BUTTON_L2 -> RETRO_DEVICE_ID_JOYPAD_L2
                KeyEvent.KEYCODE_BUTTON_R2 -> RETRO_DEVICE_ID_JOYPAD_R2
                KeyEvent.KEYCODE_BUTTON_THUMBL -> RETRO_DEVICE_ID_JOYPAD_L3
                KeyEvent.KEYCODE_BUTTON_THUMBR -> RETRO_DEVICE_ID_JOYPAD_R3
                KeyEvent.KEYCODE_BUTTON_START -> RETRO_DEVICE_ID_JOYPAD_START
                KeyEvent.KEYCODE_BUTTON_SELECT -> RETRO_DEVICE_ID_JOYPAD_SELECT
                KeyEvent.KEYCODE_DPAD_UP -> RETRO_DEVICE_ID_JOYPAD_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> RETRO_DEVICE_ID_JOYPAD_DOWN
                KeyEvent.KEYCODE_DPAD_LEFT -> RETRO_DEVICE_ID_JOYPAD_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> RETRO_DEVICE_ID_JOYPAD_RIGHT
                else -> -1
            }
        }
    }
}
