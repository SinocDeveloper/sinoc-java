package org.sinoc.config.net;

import org.sinoc.config.blockchain.*;

public class MainNetConfig extends BaseNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new SingularityConfig(new DaoHFConfig()));
        add(18000, new FutureCityConfig(new DaoHFConfig()));
    }
}
