package com.github.sellersj.flickrps;

import java.io.File;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.github.sellersj.flickrps.model.MetadataTag;

public class ImageCrawlerTest {

    // TODO the things that will uniqually identify a file include:
    // [File] File Name
    // timestamp (which one?)
    // resolution
    // camera ?

    @Test
    public void processFileGoodFile() {
        String userhome = System.getProperty("user.home");
        // ? File f = new File(System.getProperty("user.home") +
        // "/Downloads/16411933417_a711d12d77_o.jpg");
        File f = new File(userhome + "/Documents/pictures/2015-01-31_dads_birthday/DSC_0916.JPG");
        ImageCrawler c = new ImageCrawler();
        Collection<MetadataTag> processFile = c.processFile(f);

        for (MetadataTag metadataTag : processFile) {
            System.out.println(metadataTag);

            if ("File Modified Date".equals(metadataTag.getName())) {
                String time = metadataTag.getDescription();

            }
        }
    }

}
