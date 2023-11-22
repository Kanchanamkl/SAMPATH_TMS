package com.epic.pos.device.data;

/*
    01 - plain pin by ICC
    02 - encrypted pin online
    03 - plain pin by ICC  + signature
    04 - encrypted by icc
    05 - encrypted by icc  + signature
     */
public  enum CVMResult {
    SIGNATURE,
    PLAIN_PIN_BY_ICC,
    ENCTRYPTED_PIN_ONLINE,
    PLAIN_PIN_ICC_AND_SIGNATURE,
    ENCRYPTED_PIN_BY_ICC,
    ENCRYPTED_PIN_BY_ICC_ABD_SIGNATURE,
    NO_CMV_REQUIRED,
    UNKNOWN
}