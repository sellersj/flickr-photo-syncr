package com.github.sellersj.flickrps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuth1Token;

public class AlbumMirror {

    private String USER_ID = null;

    private String API_KEY = null;

    private String SHARED_SECRET = null;

    private String CACHE_DIRECTORY = null;

    private String SET_TO_SYNC_DOWN = "";

    /** This is the size of each fetch to the sets on flickr. */
    private static final int PAGE_SIZE = 200;

    /** The image size we want the pictures to be. */
    // Size.ORIGINAL; -- use this if you're using a projector?
    private static final int DEFAULT_IMAGE_SIZE = Size.LARGE_2048;

    private Flickr flickr;

    private PhotosInterface photoInterface;

    private PhotosetsInterface photoSets;

    private AuthStore authStore = null;

    public static void main(String[] args) throws Exception {
        if (1 != args.length) {
            throw new IllegalArgumentException("A properties file must be passed in");
        }
        Properties props = parsePropertiesString(args[0]);
        System.out.println(props);

        // do all the work
        AlbumMirror pull = new AlbumMirror();

        long t1 = System.currentTimeMillis();

        pull.USER_ID = props.getProperty("flickr.user.id");
        pull.API_KEY = props.getProperty("flickr.api.apiKey");
        pull.SHARED_SECRET = props.getProperty("flickr.api.sharedSecret");
        pull.authStore = new FileAuthStore(new File(props.getProperty("flickr.authStoreLocation")));

        // albumn syncing
        pull.SET_TO_SYNC_DOWN = props.getProperty("sync.album.id");
        pull.CACHE_DIRECTORY = props.getProperty("sync.album.cacheDir");

        pull.flickr = new Flickr(pull.API_KEY, pull.SHARED_SECRET, new REST());
        pull.photoInterface = pull.flickr.getPhotosInterface();

        pull.photoSets = pull.flickr.getPhotosetsInterface();

        // for write access, we need to authorize
        pull.authorizeIfNeeded();

        // TODO remove
        // pull.getAuthUrl();

        // TODO remove this
        // pull.addPhotosToSet(pictureFrameSetId);

        // pull.runSearch();

        // List<Photo> photos = pull.getPhotoIdsByTag(tags);

        System.err.println("Not doing the pic frame sync!");
        // put back the sync code
        pull.syncPhotos(pull.SET_TO_SYNC_DOWN);

        long t2 = System.currentTimeMillis();
        System.out.println("total time took was: " + (t2 - t1) + " milliseconds.");

    }

    public static Properties parsePropertiesString(String s) throws IOException {
        final Properties p = new Properties();
        p.load(new FileInputStream(s));
        return p;
    }

    // TODO refactor so we don't need the constructor to throw an exception
    public AlbumMirror() throws Exception {

        // authStore = new FileAuthStore("/Users/sellersj/Downloads/flickr_frame/");

        // // because of a design decision to throw exceptions on a constructor,
        // I
        // // have do init the class in the same way.
        // Flickr flickr = new Flickr(API_KEY, SHARED_SECRET, new REST());
        //
        // photoInterface = flickr.getPhotosInterface();
        // photoSets = flickr.getPhotosetsInterface();
    }

    public void findDuplicates() throws Exception {
        SearchParameters params = new SearchParameters();

        params.setUserId(USER_ID);

        // TODO get the photo count so we can figure out the number of pages

        PhotoList<Photo> photoList = photoInterface.search(params, PAGE_SIZE, 1);

        int setSize = photoList.size();

        ArrayList<Photo> photos = new ArrayList<Photo>(setSize);

        for (int i = 1; i <= (setSize / PAGE_SIZE) + 1; i++) {
            // photoList.get

            // then when the photo count size is figured out, go though all the
            // pages and get the info (photoId, dateTaken, filename, url, view
            // count) and
            // stream it to a file as you go.
            // tab seperated file???
            // PhotoList photoList = photoSets.getPhotos(setId, PAGE_SIZE, i);

            // after that is done, we can load up a seperate method that will
            // look at that list and figure out the ones that might be dupes

            // hopefully not too many and they can be manually deleted

            photos.addAll(photoList);
        }

    }

    public void authorizeIfNeeded() throws Exception {
        if (this.authStore != null) {
            Auth auth = this.authStore.retrieve(USER_ID);
            if (auth == null)
                this.authorize();
            else
                RequestContext.getRequestContext().setAuth(auth);
        }
    }

    private void authorize() throws IOException, SAXException, FlickrException {
        // String frob = this.flickr.getAuthInterface().getFrob();
        //
        // URL authUrl = this.flickr.getAuthInterface().buildAuthenticationUrl(Permission.READ,
        // frob);
        // System.out.println("Please visit: " + authUrl.toExternalForm() + " then, hit enter.");
        //
        // System.in.read();
        //
        // Auth token = this.flickr.getAuthInterface().getToken(frob);
        // RequestContext.getRequestContext().setAuth(token);
        // authStore.store(token);
        // System.out.println("Thanks. You probably will not have to do this every time. Now
        // starting backup.");
        // above code used in old lib

        AuthInterface authInterface = flickr.getAuthInterface();
        OAuth1RequestToken accessToken = authInterface.getRequestToken();
        String url = authInterface.getAuthorizationUrl(accessToken, Permission.READ);
        System.out.println("Follow this URL to authorise yourself on Flickr");
        System.out.println(url);
        System.out.println("Paste in the token it gives you:");
        System.out.print(">>");
        try (Scanner scanner = new Scanner(System.in)) {
            String tokenKey = scanner.nextLine();
            OAuth1Token requestToken = authInterface.getAccessToken(accessToken, tokenKey);
            Auth auth = authInterface.checkToken(requestToken);
            RequestContext.getRequestContext().setAuth(auth);
            this.authStore.store(auth);
            System.out.println("Thanks. You probably will not have to do this every time. Now starting backup.");
        }
    }

    public void syncPhotos(String photosetId) throws Exception {
        // generate a file cache
        Map<String, File> cachedFiles = findFiles();

        // get the info of the photos from the set
        List<Photo> photos = getMinimallyFilledPhotos(photosetId);

        // remove any files that have been removed from the set
        removeFilesThatAreNoLongerInSet(cachedFiles, photos);

        // add this back in
        downloadFileDiff(cachedFiles, photos);
    }

    public Map<String, File> findFiles() {
        Map<String, File> cache = new HashMap<String, File>();

        String[] extensions = { "jpg" };
        boolean recursive = true;

        Collection<File> files = FileUtils.listFiles(new File(CACHE_DIRECTORY), extensions, recursive);

        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File file = iterator.next();

            String photoId = FilenameUtils.removeExtension(file.getName());
            cache.put(photoId, file);

            // System.out.println("File = " + photoId + " at location: " +
            // file.getAbsolutePath());
        }

        System.out.println("There are currently " + cache.size() + " files cached.");

        return cache;
    }

    public void downloadFileDiff(Map<String, File> cachedFiles, List<Photo> photos) throws Exception {

        int downloadedFiles = 1;
        // // print a blank line before we enter the loop
        // System.out.println("");
        int numberOfFilesToDownload = photos.size() - cachedFiles.size();
        System.out.println("Going to download " + numberOfFilesToDownload + " photos");

        for (Photo photo : photos) {

            String photoId = photo.getId();

            // if the photoId does not exist in the cache, download it
            if (!cachedFiles.containsKey(photoId)) {
                // download a full copy of the photo object so we can use the
                // proper metadata
                Photo fullPhoto = photoInterface.getPhoto(photoId);

                downloadPhoto(fullPhoto, photoId);

                // backslash r repositions the curser at the start of the line
                System.out.println("Downloading file " + downloadedFiles++ + " of " + numberOfFilesToDownload);
            } else {
                // TODO put in some better logging here
                // System.out.println("Caching file with id: " + photoId);
            }
        }
    }

    public void downloadPhoto(Photo photo, String photoId) throws Exception {

        try {
            Size sizeToUse = getSizeToUse(photoId);

            // download to a temp file
            File outputfile = downloadPhotoToTempSpace(photo, photoId, sizeToUse);

            BufferedImage bi = ImageIO.read(outputfile);

            // figure out if the image is port or landscape
            String subDir;
            if (bi.getHeight() > bi.getWidth()) {
                subDir = "vert/";
            } else {
                subDir = "horz/";
            }

            // now move to the final file
            File targetFile = new File(CACHE_DIRECTORY + subDir + photoId + ".jpg");

            FileUtils.moveFile(outputfile, targetFile);

            // set the last mod date on the final file
            setTimeIfPossible(photo, targetFile);

        } catch (IOException e) {
            // this might fail for some of the files we remove? Not sure what's
            // wrong
            e.printStackTrace();
        }
    }

    public void setTimeIfPossible(Photo photo, File targetFile) {
        Date lastModDate = photo.getDateTaken();
        if (null != lastModDate) {
            targetFile.setLastModified(lastModDate.getTime());
        }
    }

    public File downloadPhotoToTempSpace(Photo photo, String photoId, int sizeToUse) throws Exception {

        // download to a temp file
        File outputfile = File.createTempFile(photoId, "jpg");
        BufferedImage bi = photoInterface.getImage(photo, sizeToUse);
        ImageIO.write(bi, "jpg", outputfile);

        if (outputfile.length() < 10000) {
            System.err.println("had trouble downloading: " + photoId);
        }

        return outputfile;
    }

    public File downloadPhotoToTempSpace(Photo photo, String photoId, Size size) throws Exception {

        // download to a temp file
        File outputfile = File.createTempFile(photoId, "jpg");

        String target = size.getSource();
        System.out.println("The url we're trying to use is: " + target);

        URL url = new URL(target);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.connect();
        BufferedImage bi = ImageIO.read(conn.getInputStream());

        ImageIO.write(bi, "jpg", outputfile);

        if (outputfile.length() < 10000) {
            System.err.println("had trouble downloading: " + photoId);
        }

        return outputfile;
    }

    private Size getSizeToUse(String photoId) throws IOException, SAXException, FlickrException {
        Size sizeToUse = null;
        Size original = null;

        // look for a large size, if they don't have it, download the
        // original
        Collection<Size> sizes = photoInterface.getSizes(photoId);
        for (Size size : sizes) {
            if (Size.ORIGINAL == size.getLabel()) {
                original = size;
            }

            if (DEFAULT_IMAGE_SIZE == size.getLabel()) {
                sizeToUse = size;
                break;
            }
        }

        // if we had not found the size we were looking for, use the original
        if (null == sizeToUse) {
            System.err.println("Using the original size for photoId: " + photoId);

            sizeToUse = original;
        }

        return sizeToUse;
    }

    /**
     * By design the calls used here will only return a
     * <a href="http://flickrj.sourceforge.net/faq.php?faq_id=4">minimally filled out object</a>.
     */
    // because of the photos.addAll(photoList);
    public List<Photo> getMinimallyFilledPhotos(String setId) throws Exception {

        Photoset info = photoSets.getInfo(setId);

        int setSize = info.getPhotoCount();

        ArrayList<Photo> photos = new ArrayList<Photo>(setSize);

        for (int i = 1; i <= (setSize / PAGE_SIZE) + 1; i++) {

            PhotoList<Photo> photoList = photoSets.getPhotos(setId, PAGE_SIZE, i);

            photos.addAll(photoList);
        }

        System.out.println("Total number of photos in set: " + photos.size());

        return photos;
    }

    /**
     * If the files exist locally, but are not in the set, we can assume that they should not be
     * part of the set any more.
     */
    public void removeFilesThatAreNoLongerInSet(Map<String, File> cachedFiles, List<Photo> photos) throws Exception {
        // make a quick set of all the photo ids
        HashSet<String> idsInFlickrSet = new HashSet<String>();
        for (Photo photo : photos) {
            idsInFlickrSet.add(photo.getId());
        }

        // create a defensive copy since we're going to be removing things from
        // the set
        HashSet<String> localIds = new HashSet<String>(cachedFiles.keySet());

        // whatever is left in the set should be the ones to be removed
        localIds.removeAll(idsInFlickrSet);

        System.out.println("There are " + localIds.size() + " local files to remove.");
        for (String idToRemove : localIds) {
            File file = cachedFiles.get(idToRemove);
            file.delete();
            System.out.println("Removing file " + file.getAbsolutePath());
        }

    }

    public static String getCleanFileName(File file) {
        String fname = FilenameUtils.removeExtension(file.getName());

        // remove anything like a tildy
        return fname.replace("~", "");
    }

    public void getAuthUrl() throws Exception {
        // TODO fix this
        throw new RuntimeException("Commenting out until got new lib downloading properly");
        // AuthInterface auth = new AuthInterface(API_KEY, SHARED_SECRET, new REST());
        // flickr.getAuthInterface().getFrob();
        //
        // // TODO put this back
        // // URL url = auth.buildAuthenticationUrl(Permission.READ,
        // // auth.getFrob());
        // URL url = auth.buildAuthenticationUrl(Permission.WRITE, auth.getFrob());
        // System.out.println(url);
    }

    public List<Photo> getPhotoIdsByTag(String[] tags) throws Exception {
        List<Photo> photoIdsToAdd = new ArrayList<Photo>();

        SearchParameters params = new SearchParameters();

        params.setText("grandmap");
        params.setUserId(USER_ID);

        params.setTags(tags);

        // dumb big ish number
        int total = Integer.MAX_VALUE;

        for (int page = 0; photoIdsToAdd.size() < total; page++) {

            PhotoList<Photo> photoList = photoInterface.search(params, 10, page);
            total = photoList.getTotal();

            // System.out.println("The photos size " + photoList.size());

            for (Object object : photoList) {
                Photo photo = (Photo) object;

                // System.out.println(photo.getUrl());

                // pull down the info for the actual photo
                Photo fullPhoto = photoInterface.getPhoto(photo.getId());

                photoIdsToAdd.add(fullPhoto);
            }
        }

        System.out.println("total photos are: " + photoIdsToAdd.size());
        return photoIdsToAdd;
    }

}
