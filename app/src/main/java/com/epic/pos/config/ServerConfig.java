package com.epic.pos.config;

import com.epic.pos.BuildConfig;

public class ServerConfig {

    //Setting base URL through the build variants
    public static String BASE_URL = BuildConfig.BASE_URL;
    public static String CONTEXT_ROOT = BuildConfig.CONTEXT_ROOT;

    public static String APPLICATION_URL = BASE_URL + CONTEXT_ROOT;

}
