package me.czvn.blelibrary.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import me.czvn.blelibrary.interfaces.IScanResultListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 2016/1/13.
 * 这个类对BluetoothLeScanner进行了封装
 */
public final class BLEScanner {

    private static final String TAG = "BBK_" + BLEScanner.class.getSimpleName();

    private WeakReference<Context> contextWeakReference;

    private IScanResultListener scanResultListener;
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters;

    private static BLEScanner instance;

    /**
     * 单例模式
     *
     * @param context  保存context的引用
     * @param listener 扫描结果的listener
     * @return BLEScanner的实例
     */
    public static BLEScanner getInstance(Context context, IScanResultListener listener) {
        Log.d(TAG, "["+
                Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        if (instance == null) {
            instance = new BLEScanner(context);
        } else {
            instance.contextWeakReference = new WeakReference<Context>(context);
        }
        instance.scanResultListener = listener;
        return instance;
    }

    /**
     * 开始扫描周围的设备
     *
     * @return 开始扫描成功返回true,否则返回false
     */
    public boolean startScan() {
        Context context = contextWeakReference.get();
        if (context == null) {
            return false;
        }

        Log.d(TAG, "["+
                Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                Thread.currentThread().getStackTrace()[2].getMethodName() + "]");

        BluetoothAdapter bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "bluetoothAdapter is null");
            return false;
        }
        scanner = bluetoothAdapter.getBluetoothLeScanner();
        if (scanner == null) {
            Log.e(TAG, "bluetoothLeScanner is null");
            return false;
        }
        scanner.startScan(filters, scanSettings, scanCallback);
        Log.i(TAG, "Start scan success");
        return true;
    }

    public void stopScan() {
        if (scanner == null || scanCallback == null) {
            return;
        }
        Log.d(TAG, "["+
                Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
        scanner.stopScan(scanCallback);
    }

    private BLEScanner(Context context) {
        contextWeakReference = new WeakReference<>(context);
        initScanData();
    }


    private void initScanData() {
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d(TAG, "["+
                        Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                        Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                        Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                Log.i(TAG, "result" + result);
                String address = result.getDevice().getAddress();
                String name;
                ScanRecord scanRecord = result.getScanRecord();
                int mRssi = result == null ? -127: result.getRssi();
                Log.i(TAG, "Rssi: " +mRssi);
                name = scanRecord == null ? "unknown" : scanRecord.getDeviceName();
                //TODO stop scan
//                if (name != null)
//                {
//                    Log.d(TAG, "["+
//                            Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
//                            Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
//                            Thread.currentThread().getStackTrace()[2].getMethodName() + "]" + "========StopScan");
//                    stopScan();
//                }

                scanResultListener.onResultReceived(name, address, mRssi);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(TAG, "["+
                        Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                        Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                        Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                Log.e(TAG, "onBatchScanResults");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG, "["+
                        Thread.currentThread().getStackTrace()[2].getFileName() + "_" +
                        Thread.currentThread().getStackTrace()[2].getLineNumber() + "_" +
                        Thread.currentThread().getStackTrace()[2].getMethodName() + "]");
                Log.e(TAG, "onScanFailed");
                scanResultListener.onScanFailed(errorCode);
            }
        };
        filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BLEProfile.UUID_SERVICE)).build());
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }
}
