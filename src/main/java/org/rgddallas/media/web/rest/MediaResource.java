package org.rgddallas.media.web.rest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jni.Local;
import org.rgddallas.media.entity.VideoPlayback;
import org.rgddallas.media.entity.VideoPlaybackRepository;
import org.rgddallas.media.util.FileSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * This class contains resources the serves the media content - audio and video as REST controllers.
 * <p>
 * Created by Neelabh on 3/12/2017.
 */

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MediaResource {
    public static final Logger log = LoggerFactory.getLogger(MediaResource.class);

    public static final String MP4_EXTN = ".mp4";
    public static final String MP3_EXTN = ".mp3";
    public static final String KIRTAN = ".mov";
    public static final String VIDEO_LOCATION = "/volume1/video/Intranet/";
    public static final String VIDEO_KIRTAN_LOCATION = "/volume1/video/Intranet/video_kirtan/";
    //public static final String VIDEO_KIRTAN_LOCATION = "\\\\192.168.1.144\\video\\Intranet\\video_kirtan\\";
    //public static final String VIDEO_LOCATION = "\\\\192.168.1.144\\video\\Intranet\\";
    public static final String AUDIO_LOCATION = "/volume1/music/Arti/";
    //public static final String AUDIO_LOCATION = "C:\\temp\\";

    private VideoPlaybackRepository videoPlaybackRepo;

    @Autowired
    public MediaResource(VideoPlaybackRepository videoPlaybackRepo) {
        this.videoPlaybackRepo = videoPlaybackRepo;
    }

    /**
     * Gets the audio file content from the server.
     *
     * @param fileName file to return, with no spaces
     * @return audio content as a byte stream
     */
    @GetMapping(value = "/audio/{fileName}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getAudio(@PathVariable String fileName) {
        log.info("Got Audio file request for file {}", fileName);

        return getMediaAsStream(AUDIO_LOCATION, fileName, MP3_EXTN);
    }

    /**
     * REST controller to return the content of the video file to be played. This is based on the current sequence
     * number stored in the DB. This number gets updated once in a day. If the request is received on same day
     * then same file is returned and the sequence remains unchanged.
     *
     */
    @GetMapping(value = "/video2", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getTodaysVideoToPlay(OutputStream output) throws IOException {

        Long sequenceToUse = 1L;
        List<VideoPlayback> videoSeqList = (List<VideoPlayback>) videoPlaybackRepo.findAll();

        VideoPlayback videoPlayback = null;
        if (!CollectionUtils.isEmpty(videoSeqList)) {
            videoPlayback = videoSeqList.get(0);
        }

        if (LocalDateTime.now().getDayOfMonth() != videoPlayback.getLastPlayed().getDayOfMonth()) {
            sequenceToUse = videoPlayback.getFileSequence();
        } else {
            sequenceToUse = videoPlayback.getFileSequence() - 1;
        }

        //String queryStr = "[0]*<seq>[a-zA-Z_-].*";
        String queryStr = "[0]*<seq>_.*";
        if (videoPlayback != null) {
            String seq = Long.toString(sequenceToUse);

            //prepare the regex for matching
            if (StringUtils.isNotEmpty(seq)) {
                queryStr = queryStr.replace("<seq>", seq);
                log.debug("Regex to match is : {}", queryStr);
            }
        }

        log.debug("File pattern to search : {}", queryStr);


        File file = new File(getFileAsInputStream(VIDEO_LOCATION, queryStr, MP4_EXTN));

        if (file.canRead()) {
            updateSequence(videoPlayback, sequenceToUse + 1L);
        } else {//reset the sequence to 1 to start playing from the first video
            log.info("File not found for sequence {}, defaulting to Sequence number 1", sequenceToUse);

            queryStr = "[0]*<seq>_.*";
            queryStr = queryStr.replace("<seq>", "1");
            file = new File(getFileAsInputStream(VIDEO_LOCATION, queryStr, MP4_EXTN));
        }

        InputStream inputStream = new FileInputStream(file);

        IOUtils.copy(inputStream, output);
    }

    /**
     * REST Endpoint to send the video kirtna file stream.
     *
     * @param output
     * @throws IOException
     */
    @GetMapping(value = "/video-kirtan", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getVideoToPlay(OutputStream output) throws IOException {

        String pathToFetch = VIDEO_KIRTAN_LOCATION;

        File folder = new File(pathToFetch);
        File[] files = folder.listFiles();

        int totalFiles = files.length;
        log.info("Total files inside the folder - {}", totalFiles);

        Random random = new Random();
        int index = random.nextInt(totalFiles);
        log.info("File - {}", index);

        InputStream inputStream = new FileInputStream(files[index]);

        log.info("Got file input stream");

        IOUtils.copy(inputStream, output);
    }

    /**
     * Increments the current sequence and save into the DB.
     *
     * @param videoPlayback the video playback entity
     * @param sequenceToSet optional sequence to set
     * @return the updated sequence
     */
    public Long updateSequence(VideoPlayback videoPlayback, Long sequenceToSet) {
        if (sequenceToSet == null) {
            sequenceToSet = videoPlayback.getFileSequence() + 1L;
        }

        videoPlayback.setFileSequence(sequenceToSet);
        videoPlayback.setLastPlayed(LocalDateTime.now());

        videoPlayback = videoPlaybackRepo.save(videoPlayback);

        log.debug("Incremented Sequence number and saved : {}", videoPlayback.getFileSequence());

        return videoPlayback.getFileSequence();
    }


    /**
     * Utility method to get the media content from file system and prepare a response entity.
     *
     * @param location      directory to search into
     * @param fileName      file name
     * @param fileExtension file extension - mp3 or mp4
     * @return the response entity with file content, if found
     */
    public ResponseEntity<byte[]> getMediaAsStream(String location, String fileName, String fileExtension) {
        ResponseEntity<byte[]> response;

        try {
            byte[] byteStream = getFileStreamAsByteArray(location, fileName, fileExtension);
            if (byteStream != null) {
                response = new ResponseEntity<byte[]>(byteStream, HttpStatus.OK);
            } else {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            log.error("No file found ", e);
            response = new ResponseEntity("File Not Found", HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("IO Exception ", e);
            response = new ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    /**
     * Utility method.
     *
     * @param fileLocation
     * @param fileName
     * @param fileExtension
     * @return
     * @throws IOException
     */
    public byte[] getFileStreamAsByteArray(String fileLocation, String fileName, String fileExtension) throws IOException {
        byte[] bytes = null;
        FileSearchService fileSearchService = new FileSearchService();

        InputStream inputStream = null;

        String fileToSearch = fileName + fileExtension;
        log.debug("file to search {}", fileToSearch);
        List<String> filesFound = null;

        if (fileExtension == MP4_EXTN) {
            filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileToSearch, true);

        } else {
            filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileToSearch, false);

        }

        log.debug("files found : {}", filesFound);

        filesFound.forEach(file -> log.debug("List of files found {}", file));

        if (!CollectionUtils.isEmpty(filesFound)) {
            inputStream = new FileInputStream(filesFound.get(0));
        }

        if (inputStream != null) {
            bytes = IOUtils.toByteArray(inputStream);
        }

        return bytes;
    }

    /**
     * @param fileLocation
     * @param fileName
     * @param fileExtension
     * @return
     * @throws IOException
     */
    public String getFileAsInputStream(String fileLocation, String fileName, String fileExtension) throws IOException {
        byte[] bytes = null;
        FileSearchService fileSearchService = new FileSearchService();

        String fileFound = "";

        String fileToSearch = fileName + fileExtension;
        log.debug("file to search {}", fileToSearch);
        List<String> filesFound = null;

        if (fileExtension == MP4_EXTN) {
            filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileToSearch, true);

        } else if (fileExtension == KIRTAN) {
            filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileLocation, true);
        }

        else {
            filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileToSearch, false);
        }

        log.debug("files found : {}", filesFound);

        filesFound.forEach(file -> log.debug("List of files found {}", file));

        if (!CollectionUtils.isEmpty(filesFound)) {
            fileFound = filesFound.get(0);

        }

        return fileFound;
    }
}
