package com.github.sellersj.flickrps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.sellersj.flickrps.model.MetadataTag;

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

    private static final Logger log = LoggerFactory.getLogger(SyncApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(MetadataTagRepository repository) {
        return (args) -> {
            // save a couple of customers

            MetadataTag metadata = new MetadataTag();
            metadata.setDescription("bob");
            metadata.setDirectory("exit");
            metadata.setName("something");

            repository.save(metadata);

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (MetadataTag customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("");
        };
    }
}
