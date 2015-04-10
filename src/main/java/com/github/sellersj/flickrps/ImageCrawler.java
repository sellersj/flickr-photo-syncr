package com.github.sellersj.flickrps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.github.sellersj.flickrps.model.MetadataTag;

/**
 * Crawls the local disk path, reading the metadata for images.
 * 
 * @author sellersj
 */
public class ImageCrawler {

    public Collection<MetadataTag> processFile(File file) {

        try {
            List<MetadataTag> tags = new ArrayList<>();

            Metadata metadata = ImageMetadataReader.readMetadata(file);
            System.out.println(metadata);

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {

                    MetadataTag metadataTag = new MetadataTag();
                    metadataTag.setDirectory(tag.getDirectoryName());
                    metadataTag.setName(tag.getTagName());
                    metadataTag.setDescription(tag.getDescription());
                    tags.add(metadataTag);
                }
            }

            return tags;
        } catch (ImageProcessingException | IOException e) {
            throw new IllegalArgumentException("Could not read file: " + file, e);
        }
    }

}
