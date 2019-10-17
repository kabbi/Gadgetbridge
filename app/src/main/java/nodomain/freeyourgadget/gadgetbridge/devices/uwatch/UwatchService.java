/*  Copyright (C) 2015-2019 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Kasha

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nodomain.freeyourgadget.gadgetbridge.devices.uwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;

import static nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport.BASE_UUID;

public class UwatchService {


    public static final UUID UUID_SERVICE_UWATCH_SERVICE = UUID.fromString(String.format(BASE_UUID, "FEEA"));
    public static final UUID UUID_SERVICE_HEART_RATE = GattService.UUID_SERVICE_HEART_RATE;
    public static final UUID UUID_SERVICE_BATTERY = GattService.UUID_SERVICE_BATTERY_SERVICE;

    public static final UUID UUID_CHARACTERISTIC_STEPS_INFO = UUID.fromString(String.format(BASE_UUID, "FEE1"));
    public static final UUID UUID_CHARACTERISTIC_CONTROL_POINT = UUID.fromString(String.format(BASE_UUID, "FEE2"));
    public static final UUID UUID_CHARACTERISTIC_UNK2 = UUID.fromString(String.format(BASE_UUID, "FEE3"));
    public static final UUID UUID_CHARACTERISTIC_WATCHFACE_TRANSFER = UUID.fromString(String.format(BASE_UUID, "FEE5"));
    public static final UUID UUID_CHARACTERISTIC_FIRMWARE_TRANSFER = UUID.fromString(String.format(BASE_UUID, "FEE6"));

    public static final UUID UUID_CHARACTERISTIC_BATTERY_LEVEL = GattCharacteristic.UUID_CHARACTERISTIC_BATTERY_LEVEL;
    public static final UUID UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT = GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT;
    public static final UUID UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT = GattCharacteristic.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT;


    /*NOTIFICATIONS: usually received on the UUID_CHARACTERISTIC_NOTIFICATION characteristic */

    public static final byte NOTIFY_NORMAL = 0x0;

    public static final byte NOTIFY_FIRMWARE_UPDATE_FAILED = 0x1;

    public static final byte NOTIFY_FIRMWARE_UPDATE_SUCCESS = 0x2;

    public static final byte NOTIFY_CONN_PARAM_UPDATE_FAILED = 0x3;

    public static final byte NOTIFY_CONN_PARAM_UPDATE_SUCCESS = 0x4;

    public static final byte NOTIFY_AUTHENTICATION_SUCCESS = 0x5;

    public static final byte NOTIFY_AUTHENTICATION_FAILED = 0x6;

    public static final byte NOTIFY_FITNESS_GOAL_ACHIEVED = 0x7;

    public static final byte NOTIFY_SET_LATENCY_SUCCESS = 0x8;

    public static final byte NOTIFY_RESET_AUTHENTICATION_FAILED = 0x9;

    public static final byte NOTIFY_RESET_AUTHENTICATION_SUCCESS = 0xa;

    public static final byte NOTIFY_FW_CHECK_FAILED = 0xb;

    public static final byte NOTIFY_FW_CHECK_SUCCESS = 0xc;

    public static final byte NOTIFY_STATUS_MOTOR_NOTIFY = 0xd;

    public static final byte NOTIFY_STATUS_MOTOR_CALL = 0xe;

    public static final byte NOTIFY_STATUS_MOTOR_DISCONNECT = 0xf;

    public static final byte NOTIFY_STATUS_MOTOR_SMART_ALARM = 0x10;

    public static final byte NOTIFY_STATUS_MOTOR_ALARM = 0x11;

    public static final byte NOTIFY_STATUS_MOTOR_GOAL = 0x12;

    public static final byte NOTIFY_STATUS_MOTOR_AUTH = 0x13;

    public static final byte NOTIFY_STATUS_MOTOR_SHUTDOWN = 0x14;

    public static final byte NOTIFY_STATUS_MOTOR_AUTH_SUCCESS = 0x15;

    public static final byte NOTIFY_STATUS_MOTOR_TEST = 0x16;

    // 0x18 is returned when we cancel data sync, perhaps is an ack for this message

    public static final byte NOTIFY_UNKNOWN = -0x1;

    public static final int NOTIFY_PAIR_CANCEL = 0xef;

    public static final int NOTIFY_DEVICE_MALFUNCTION = 0xff;


    /* MESSAGES: unknown */

    public static final byte MSG_CONNECTED = 0x0;

    public static final byte MSG_DISCONNECTED = 0x1;

    public static final byte MSG_CONNECTION_FAILED = 0x2;

    public static final byte MSG_INITIALIZATION_FAILED = 0x3;

    public static final byte MSG_INITIALIZATION_SUCCESS = 0x4;

    public static final byte MSG_STEPS_CHANGED = 0x5;

    public static final byte MSG_DEVICE_STATUS_CHANGED = 0x6;

    public static final byte MSG_BATTERY_STATUS_CHANGED = 0x7;

    /* COMMANDS: usually sent to UUID_CHARACTERISTIC_CONTROL_POINT characteristic */

    public static final byte COMMAND_SET_TIMER = 0x4;

    public static final byte COMMAND_SET_FITNESS_GOAL = 0x5;

    public static final byte COMMAND_FETCH_DATA = 0x6;

    public static final byte COMMAND_SEND_FIRMWARE_INFO = 0x7;

    public static final byte COMMAND_SEND_NOTIFICATION = 0x8;

    public static final byte COMMAND_FACTORYRESET = 0x9;

    public static final byte COMMAND_CONFIRM_ACTIVITY_DATA_TRANSFER_COMPLETE = 0xa;

    public static final byte COMMAND_SYNC = 0xb;

    public static final byte COMMAND_REBOOT = 0xc;

    public static final byte COMMAND_SET_WEAR_LOCATION = 0xf;

    public static final byte COMMAND_STOP_SYNC_DATA = 0x11;

    public static final byte COMMAND_STOP_MOTOR_VIBRATE = 0x13;

    public static final byte COMMAND_SET_REALTIME_STEPS_NOTIFICATION = 0x3;

    public static final byte COMMAND_SET_REALTIME_STEP = 0x10;

    // Test HR
    public static final byte COMMAND_SET_HR_SLEEP = 0x0;
    public static final byte COMMAND_SET__HR_CONTINUOUS = 0x1;
    public static final byte COMMAND_SET_HR_MANUAL = 0x2;

    public static final byte COMMAND_GET_SENSOR_DATA = 0x12;

    private static final Map<UUID, String> UWATCH_DEBUG;

    static {
        UWATCH_DEBUG = new HashMap<>();
        UWATCH_DEBUG.put(UUID_SERVICE_UWATCH_SERVICE, "Uwatch Service");
        UWATCH_DEBUG.put(UUID_SERVICE_HEART_RATE, "Uwatch HR Service");
        UWATCH_DEBUG.put(UUID_SERVICE_BATTERY, "Uwatch Battery Service");

        UWATCH_DEBUG.put(UUID_CHARACTERISTIC_CONTROL_POINT, "Control Point");
        UWATCH_DEBUG.put(UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT, "Heart Rate Control Point");
        UWATCH_DEBUG.put(UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT, "Heart Rate Measure");
        UWATCH_DEBUG.put(UUID_CHARACTERISTIC_BATTERY_LEVEL, "Battery Level");
    }

    public static String lookup(UUID uuid, String fallback) {
        String name = UWATCH_DEBUG.get(uuid);
        if (name == null) {
            name = fallback;
        }
        return name;
    }
}
