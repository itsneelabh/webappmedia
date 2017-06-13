package org.rgddallas.media.web.rest;

import org.apache.commons.io.IOUtils;
import org.rgddallas.media.util.FileSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;

/**
 * This class contains resources the serves the media content - audio and video as REST controllers.
 *
 * Created by Neelabh on 3/12/2017.
 */

@RestController
@RequestMapping("/api")
public class MediaResource {
    public static final Logger log = LoggerFactory.getLogger(MediaResource.class);

    public static final String MP4_EXTN = ".mp4";
    public static final String MP3_EXTN = ".mp3";
    //public static final String VIDEO_LOCATION = "/volume1/video/01\\ Pravachans/RGD-EditedAll/";
    public static final String VIDEO_LOCATION = "/volume1/video/01\\ Pravachans/RGD-EditedAll/";
    //public static final String AUDIO_LOCATION = "/volume1/music/Arti/";
    public static final String AUDIO_LOCATION = "C:\\temp\\";


    private FileSearchService fileSearchService;

    @Autowired
    public MediaResource(FileSearchService fileSearchService) {
        this.fileSearchService = fileSearchService;
    }
    /**
     * Gets the video file content from the server.
     *
     * @param fileName
     * @return video content as a byte stream
     */
    @GetMapping(value = "/video/{fileName}", produces = "video/mp4")
    public ResponseEntity<byte[]> getVideo(@PathVariable String fileName) {
        log.info("Got video file request for file {}", fileName);

        return getMediaAsStream(VIDEO_LOCATION, fileName, MP4_EXTN);
    }

    /**
     * Gets the audio file content from the server.
     *
     * @param fileName
     * @return audio content as a byte stream
     */
    @GetMapping(value = "/audio/{fileName}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getAudio(@PathVariable String fileName) {
        log.info("Got Audio file request for file {}", fileName);

        return getMediaAsStream(AUDIO_LOCATION, fileName, MP3_EXTN);
    }

    /**
     * Utility method to get the media content from file system and prepare a response entity.
     *
     * @param location directory to search into
     * @param fileName file name
     * @param fileExtension file extension - mp3 or mp4
     * @return the response entity with file content, if found
     */
    public ResponseEntity<byte[]> getMediaAsStream(String location, String fileName, String fileExtension) {
        ResponseEntity<byte[]> response;

        try {
            response = new ResponseEntity<byte[]>(getFileStreamAsByteArray(location, fileName, fileExtension), HttpStatus.OK);
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
     *
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

        InputStream inputStream = null;

        String fileToSearch = fileName + fileExtension;
        List<String> filesFound = fileSearchService.searchDirectory(new File(fileLocation), fileName);

        filesFound.forEach(file -> log.debug("List of files found {}", file));

        if (!CollectionUtils.isEmpty(filesFound)) {
            inputStream = new FileInputStream(filesFound.get(0));
        }

        bytes = IOUtils.toByteArray(inputStream);

        return bytes;
    }
}