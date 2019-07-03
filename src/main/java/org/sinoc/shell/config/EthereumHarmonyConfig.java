package org.sinoc.shell.config;

import org.sinoc.config.CommonConfig;
import org.sinoc.config.NoAutoscan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * Override default EthereumJ config to apply custom configuration.
 * This is entry point for starting EthereumJ core beans.
 */
@Configuration
@ComponentScan(
        basePackages = "org.sinoc",
        excludeFilters = @ComponentScan.Filter(NoAutoscan.class))
public class EthereumHarmonyConfig extends CommonConfig {
}
