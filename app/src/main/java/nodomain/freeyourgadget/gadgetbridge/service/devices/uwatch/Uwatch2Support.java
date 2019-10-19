/*  Copyright (C) 2016-2019 Andreas Shimokawa, Carsten Pfeiffer, Sebastian
    Kranz

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
package nodomain.freeyourgadget.gadgetbridge.service.devices.uwatch;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.net.Uri;

import java.time.Clock;
import java.time.Instant;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.database.DBHandler;
import nodomain.freeyourgadget.gadgetbridge.database.DBHelper;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventBatteryInfo;
import nodomain.freeyourgadget.gadgetbridge.deviceevents.GBDeviceEventVersionInfo;
import nodomain.freeyourgadget.gadgetbridge.devices.uwatch.UwatchSampleProvider;
import nodomain.freeyourgadget.gadgetbridge.devices.uwatch.UwatchService;
import nodomain.freeyourgadget.gadgetbridge.entities.DaoSession;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.entities.User;
import nodomain.freeyourgadget.gadgetbridge.entities.UwatchActivitySample;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.Alarm;
import nodomain.freeyourgadget.gadgetbridge.model.CalendarEventSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.CannedMessagesSpec;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;
import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
import nodomain.freeyourgadget.gadgetbridge.service.btle.GattService;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.btle.actions.SetDeviceStateAction;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.IntentListener;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfoProfile;
import nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfoProfile;

public class Uwatch2Support extends AbstractBTLEDeviceSupport {

    private static final Logger LOG = LoggerFactory.getLogger(Uwatch2Support.class);
    private final DeviceInfoProfile<Uwatch2Support> deviceInfoProfile;
    private final BatteryInfoProfile<Uwatch2Support> batteryInfoProfile;
    private final GBDeviceEventVersionInfo versionCmd = new GBDeviceEventVersionInfo();
    private final GBDeviceEventBatteryInfo batteryCmd = new GBDeviceEventBatteryInfo();
    private final IntentListener mListener = new IntentListener() {
        @Override
        public void notify(Intent intent) {
            String s = intent.getAction();
            if (s.equals(DeviceInfoProfile.ACTION_DEVICE_INFO)) {
                handleDeviceInfo((nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo) intent.getParcelableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO));
            } else if (s.equals(BatteryInfoProfile.ACTION_BATTERY_INFO)) {
                handleBatteryInfo((nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfo) intent.getParcelableExtra(BatteryInfoProfile.EXTRA_BATTERY_INFO));
            }
        }
    };

    public Uwatch2Support() {
        super(LOG);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ACCESS);
        addSupportedService(GattService.UUID_SERVICE_GENERIC_ATTRIBUTE);
        addSupportedService(GattService.UUID_SERVICE_DEVICE_INFORMATION);
        addSupportedService(GattService.UUID_SERVICE_BATTERY_SERVICE);
        addSupportedService(GattService.UUID_SERVICE_HEART_RATE);
        addSupportedService(UwatchService.UUID_SERVICE_UWATCH_SERVICE);

        deviceInfoProfile = new DeviceInfoProfile<>(this);
        deviceInfoProfile.addListener(mListener);
        batteryInfoProfile = new BatteryInfoProfile<>(this);
        batteryInfoProfile.addListener(mListener);
        addSupportedProfile(deviceInfoProfile);
        addSupportedProfile(batteryInfoProfile);
    }

    private void handleBatteryInfo(nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.battery.BatteryInfo info) {
        batteryCmd.level = (short) info.getPercentCharged();
        handleGBDeviceEvent(batteryCmd);
    }

    @Override
    protected TransactionBuilder initializeDevice(TransactionBuilder builder) {
        onSetTime();
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZING, getContext()));
        builder.notify(getCharacteristic(UwatchService.UUID_CHARACTERISTIC_STEPS_INFO),true);
        builder.notify(getCharacteristic(UwatchService.UUID_CHARACTERISTIC_UNK2),true);
        requestDeviceInfo(builder);
        setInitialized(builder);
        batteryInfoProfile.requestBatteryInfo(builder);


        return builder;
    }

    private void requestDeviceInfo(TransactionBuilder builder) {
        LOG.debug("Requesting Device Info!");
        deviceInfoProfile.requestDeviceInfo(builder);
    }

    private void setInitialized(TransactionBuilder builder) {
        builder.add(new SetDeviceStateAction(getDevice(), GBDevice.State.INITIALIZED, getContext()));
    }


    @Override
    public boolean useAutoConnect() {
        return true;
    }

    private void handleDeviceInfo(nodomain.freeyourgadget.gadgetbridge.service.btle.profiles.deviceinfo.DeviceInfo info) {
        LOG.warn("Device info: " + info);
        versionCmd.hwVersion = info.getHardwareRevision();
        versionCmd.fwVersion = info.getFirmwareRevision();
        handleGBDeviceEvent(versionCmd);
    }

    @Override
    public void onNotification(NotificationSpec notificationSpec) {
    }

    @Override
    public void onDeleteNotification(int id) {

    }

    @Override
    public void onSetTime() {
        LOG.info("set time fired");
//        Long ts = System.currentTimeMillis()/1000;
        Long ts = Instant.now().toEpochMilli()/1000;
        //ts -= 5*60*60;
        LOG.info("time: " + ts.toString());

        getQueue().clear();
        BluetoothGattCharacteristic cmdChar = getCharacteristic(UwatchService.UUID_CHARACTERISTIC_CONTROL_POINT);

        TransactionBuilder builder = new TransactionBuilder("findDevice");
        builder.write(cmdChar, new byte[]{(byte)0xfe, (byte)0xea, 0x10, 0x0a, 0x31,
                (byte)((ts>>>24)&0xff),
                (byte)((ts>>>16)&0xff),
                (byte)((ts>>>8 )&0xff),
                (byte)((ts>>>0 )&0xff),
                0x03});
        builder.queue(getQueue());
    }

    @Override
    public void onSetAlarms(ArrayList<? extends Alarm> alarms) {
        LOG.info("set alarms fired");

    }

    @Override
    public void onSetCallState(CallSpec callSpec) {

    }

    @Override
    public void onSetCannedMessages(CannedMessagesSpec cannedMessagesSpec) {

    }

    @Override
    public void onSetMusicState(MusicStateSpec stateSpec) {

    }

    @Override
    public void onSetMusicInfo(MusicSpec musicSpec) {

    }

    @Override
    public void onEnableRealtimeSteps(boolean enable) {

    }

    @Override
    public void onInstallApp(Uri uri) {

    }

    @Override
    public void onAppInfoReq() {

    }

    @Override
    public void onAppStart(UUID uuid, boolean start) {

    }

    @Override
    public void onAppDelete(UUID uuid) {

    }

    @Override
    public void onAppConfiguration(UUID appUuid, String config, Integer id) {

    }

    @Override
    public void onAppReorder(UUID[] uuids) {

    }

    @Override
    public void onFetchRecordedData(int dataTypes) {

    }

    @Override
    public void onReset(int flags) {

    }

    @Override
    public void onHeartRateTest() {

    }

    @Override
    public void onEnableRealtimeHeartRateMeasurement(boolean enable) {

    }

    @Override
    public void onFindDevice(boolean start) {
        getQueue().clear();
        BluetoothGattCharacteristic cmdChar = getCharacteristic(UwatchService.UUID_CHARACTERISTIC_CONTROL_POINT);

        TransactionBuilder builder = new TransactionBuilder("findDevice");
        builder.write(cmdChar, new byte[]{(byte)0xfe, (byte)0xea, 0x10, 0x05, 0x61});
        builder.queue(getQueue());


    }

    @Override
    public void onSetConstantVibration(int intensity) {
//        getQueue().clear();
//        BluetoothGattCharacteristic characteristic2 = getCharacteristic(UUID.fromString("00001526-1212-efde-1523-785feabcd123"));
//        BluetoothGattCharacteristic characteristic1 = getCharacteristic(UUID.fromString("00001524-1212-efde-1523-785feabcd123"));
//
//        TransactionBuilder builder = new TransactionBuilder("vibration");
//        builder.write(characteristic1, new byte[]{0x03, (byte) 0x80});
//
//        builder.write(characteristic2, new byte[]{(byte) intensity, 0x00});
//        builder.queue(getQueue());
    }

    @Override
    public void onScreenshotReq() {

    }

    @Override
    public void onEnableHeartRateSleepSupport(boolean enable) {

    }

    @Override
    public void onSetHeartRateMeasurementInterval(int seconds) {

    }

    @Override
    public void onAddCalendarEvent(CalendarEventSpec calendarEventSpec) {

    }

    @Override
    public void onDeleteCalendarEvent(byte type, long id) {

    }


    @Override
    public boolean onCharacteristicChanged(BluetoothGatt gatt,
                                           BluetoothGattCharacteristic characteristic) {
        if (super.onCharacteristicChanged(gatt, characteristic)) {
            return true;
        }

        UUID characteristicUUID = characteristic.getUuid();


        if (UwatchService.UUID_CHARACTERISTIC_STEPS_INFO.equals(characteristicUUID)) {
            byte[] data = characteristic.getValue();
            if (data.length != 9) {
                LOG.info("Wrong packet length");
                return false;
            }

            int steps = data[0] + (data[1] << 8) + (data[2] << 16);
            int distance = data[3] + (data[4] << 8) + (data[5] << 16);
            int calories = data[6] + (data[7] << 8) + (data[8] << 16);
handleActivityData(steps, distance, calories);
        }

        LOG.info("Unhandled characteristic changed: " + characteristicUUID + " data " + Arrays.toString(characteristic.getValue()));
        return false;
    }

    @Override
    public boolean onCharacteristicRead(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic, int status) {
        if (super.onCharacteristicRead(gatt, characteristic, status)) {
            return true;
        }
        UUID characteristicUUID = characteristic.getUuid();

        LOG.info("Unhandled characteristic read: " + characteristicUUID + " data " + Arrays.toString(characteristic.getValue()));
        return false;
    }

    @Override
    public void onSendConfiguration(String config) {

    }

    @Override
    public void onReadConfiguration(String config) {

    }

    @Override
    public void onTestNewFunction() {

    }

    @Override
    public void onSendWeather(WeatherSpec weatherSpec) {

    }

    private void handleActivityData(int steps, int distance, int calories) {
        try (DBHandler handler = GBApplication.acquireDB()) {
            DaoSession session = handler.getDaoSession();

            Device device = DBHelper.getDevice(getDevice(), session);
            User user = DBHelper.getUser(session);
            int ts = (int) (System.currentTimeMillis() / 1000);
            UwatchSampleProvider provider = new UwatchSampleProvider(gbDevice, session);
            UwatchActivitySample sample = new UwatchActivitySample();
            sample.setDevice(device);
            sample.setUser(user);
            sample.setTimestamp((int) (System.currentTimeMillis() / 1000));
            sample.setProvider(provider);
            sample.setSteps(steps);
            sample.setDistanceMeters(distance);
            sample.setCaloriesBurnt(calories);

            provider.addGBActivitySample(sample);

            Intent intent = new Intent(DeviceService.ACTION_REALTIME_SAMPLES)
                    .putExtra(DeviceService.EXTRA_REALTIME_SAMPLE, sample);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        } catch (Exception e) {
            LOG.warn("Unable to acquire db for saving realtime samples", e);
        }
    }

    private void sendCmd(int cmd) {
        sendCmd(cmd, new byte[0]);
    }

    private void sendCmd(int cmd, byte[] payload) {
        getQueue().clear();
        BluetoothGattCharacteristic cmdChar = getCharacteristic(UwatchService.UUID_CHARACTERISTIC_CONTROL_POINT);
        TransactionBuilder builder = new TransactionBuilder("findDevice");
        builder.write(cmdChar, new byte[]{(byte)0xfe, (byte)0xea, 0x10, (byte)(payload.length + 5), (byte)cmd});
        builder.queue(getQueue());
    }
}
