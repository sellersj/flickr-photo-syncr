package com.github.sellersj.flickrps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Used as the starting point at ensuring that we have a local copy of all photos in flickr.
 * 
 * @author sellersj
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }
}
