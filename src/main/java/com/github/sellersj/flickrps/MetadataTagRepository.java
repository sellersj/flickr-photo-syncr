package com.github.sellersj.flickrps;

import org.springframework.data.repository.CrudRepository;

import com.github.sellersj.flickrps.model.MetadataTag;

/** How to save the metadata from the scanned files on the system. */
public interface MetadataTagRepository extends CrudRepository<MetadataTag, Long> {

}
