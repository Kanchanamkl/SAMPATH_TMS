// SystemService.aidl
package com.vfi.smartpos.system_service.aidl;

import android.os.IBinder;
import com.vfi.smartpos.system_service.aidl.IAppInstallObserver;
import com.vfi.smartpos.system_service.aidl.IAppDeleteObserver;

interface ISystemManager {
	/**
	 * Install an apk <br/>
	 * @param apkPath - apk's absolute url.
	 * @param observer - callback handler.
	 * @param installerPackageName - packagename of installer apk.
	 */
    void installApp(String apkPath, IAppInstallObserver observer, String installerPackageName);

	/**
	 * remove an apk <br/>
	 * @param packageName - package name which need to remove.
	 * @param observer - callback handler.
	 */
    void uninstallApp(String packageName, IAppDeleteObserver observer);
    void reboot();

    void isMaskHomeKey(boolean state);
    void isMaskStatusBard(boolean state);

    boolean chekcK21Update(String sysBin, String appBin);
    void updateROM(String zipPath);
}
