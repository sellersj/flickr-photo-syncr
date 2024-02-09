# flickr-photo-syncr

There are a couple of uses for this library.
* To find all the photos that are _just_ on flickr
* Download files missing from the local computer
* Sync a folder with a set (used for a picture frame)

Future plans would include downloading / backuping up all metadata.

## Sync to removable drive
```
rsync  -avz --delete /Users/sellersj/Downloads/flickr_frame/ /Volumes/my\ drive
```

## Check for new plugins and dependencies
`versions:display-plugin-updates`
`versions:display-dependency-updates -DprocessDependencyManagement=false`
