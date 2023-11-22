// IAdditionService.aidl
package com.epic.eatmca;

// Declare any non-default types here with import statements

interface IInterAppComService {
    // You can pass values in, out, or inout.
        // Primitive datatypes (such as int, boolean, etc.) can only be passed in.
        String getProfileData(in String packageName, in int applicationId);

        boolean profileUpdateSuccess(in String packageName, in int applicationId);

        boolean settlementSuccess(in String packageName, in int applicationId);

        boolean checkUpdates(in String packageName, in int applicationId);

}
