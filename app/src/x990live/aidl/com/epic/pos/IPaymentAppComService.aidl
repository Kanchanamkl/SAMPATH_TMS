package com.epic.pos;

// Declare any non-default types here with import statements

interface IPaymentAppComService {
    // You can pass values in, out, or inout.
        // Primitive datatypes (such as int, boolean, etc.) can only be passed in.
        boolean profileUpdate(in String profileData);

        boolean autoSettlement(in boolean hasAutoSettlement);

        boolean disableTerminal(in boolean disableTerminal);
}
