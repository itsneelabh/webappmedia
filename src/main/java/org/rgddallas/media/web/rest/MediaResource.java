package org.rgddallas.media.web.rest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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
    //public static final String VIDEO_LOCATION = "/volume1/video/Intranet/";
    public static final String VIDEO_LOCATION = "C:\\temp\\";
    public static final String AUDIO_LOCATION = "/volume1/music/Arti/";
    //public static final String AUDIO_LOCATION = "C:\\temp\\";

    private VideoPlaybackRepository videoPlaybackRepo;

    @Autowired
    public MediaResource(VideoPlaybackRepository videoPlaybackRepo) {
        this.videoPlaybackRepo = videoPlaybackRepo;
    }

    /**
     * Gets the video file content from the server.
     *
     * @param fileName name of the file without extension and spaces
     * @return video content as a byte stream
     */
    @GetMapping(value = "/video/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getVideo(@PathVariable String fileName, HttpServletResponse response) {
        log.info("Got video file request for file {}", fileName);

        return getMediaAsStream(VIDEO_LOCATION, fileName, MP4_EXTN);
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
     * number stored in the DB. This number gets updated once in 23 hours. If the request is received within 23 hours
     * then same file is returned and the sequence remains unchanged.
     *
     * @return video file as byte stream
     */
    @GetMapping(value = "/video", produces = "video/mp4")
    public ResponseEntity<byte[]> getVideo() {
        log.info("Got video file request for today's lecture");

        Long sequenceToUse = 1L;
        List<VideoPlayback> videoSeqList = (List<VideoPlayback>) videoPlaybackRepo.findAll();

        VideoPlayback videoPlayback = null;
        if (!CollectionUtils.isEmpty(videoSeqList)) {
            videoPlayback = videoSeqList.get(0);
        }

        if (LocalDateTime.now().isAfter(videoPlayback.getLastPlayed().plusHours(23))) {
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

        ResponseEntity<byte[]> response = getMediaAsStream(VIDEO_LOCATION, queryStr, MP4_EXTN);
        log.debug("Response status code : {}", response.getStatusCode());

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            updateSequence(videoPlayback, sequenceToUse + 1L);
        } else {//reset the sequence to 1 to start playing from the first video
            log.info("File not found for sequence {}, defaulting to Sequence number 1", sequenceToUse);

            queryStr = "[0]*<seq>_.*";
            queryStr = queryStr.replace("<seq>", "1");

            log.debug("Regex to match is : {}", queryStr);
            response = getMediaAsStream(VIDEO_LOCATION, queryStr, MP4_EXTN);

            if (response.getStatusCode().equals(HttpStatus.OK)) {
                updateSequence(videoPlayback, 2L);
            }
        }

        return response;
    }

    @GetMapping(value = "/video2", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getTodaysVideoToPlay(OutputStream output) throws IOException {

        Long sequenceToUse = 1L;
        List<VideoPlayback> videoSeqList = (List<VideoPlayback>) videoPlaybackRepo.findAll();

        VideoPlayback videoPlayback = null;
        if (!CollectionUtils.isEmpty(videoSeqList)) {
            videoPlayback = videoSeqList.get(0);
        }

        if (LocalDateTime.now().isAfter(videoPlayback.getLastPlayed().plusHours(23))) {
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

        if (file != null) {
            updateSequence(videoPlayback, sequenceToUse + 1L);
        } else {//reset the sequence to 1 to start playing from the first video
            log.info("File not found for sequence {}, defaulting to Sequence number 1", sequenceToUse);

            queryStr = "[0]*<seq>_.*";
            queryStr = queryStr.replace("<seq>", "1");
            file = new File(getFileAsInputStream(VIDEO_LOCATION, queryStr, MP4_EXTN));
        }

        InputStream inputStream = new FileInputStream(file);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

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

        } else {
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
