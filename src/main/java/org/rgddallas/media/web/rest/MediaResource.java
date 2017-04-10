package org.rgddallas.media.web.rest;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Neelabh on 3/12/2017.
 */
@RestController
@RequestMapping("/api")
public class MediaResource {

    public static final Logger log = LoggerFactory.getLogger(MediaResource.class);

    public static final String MP4_EXTN = ".mp4";
    public static final String MP3_EXTN = ".mp3";
    public static final String VIDEO_LOCATION = "/volume1/video/01\\ Pravachans/RGD-EditedAll/";
    public static final String AUDIO_LOCATION = "/volume1/music/Arti/";

    @GetMapping(value = "/video/{fileName}", produces = "video/mp4")
    public ResponseEntity<byte[]> getVideo(@PathVariable String fileName) {
        log.info("Got video file request for file {}", fileName);

        ResponseEntity<byte[]> response = null;

        try {
            response = new ResponseEntity<byte[]>(getFileStreamAsByteArray(VIDEO_LOCATION, fileName, MP4_EXTN), HttpStatus.OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            response = new ResponseEntity("File Not Found", HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            response = new ResponseEntity("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @GetMapping(value = "/audio/{fileName}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getAudio(@PathVariable String fileName) {
        log.info("Got Audio file request for file ", fileName);

        ResponseEntity<byte[]> response = null;

        byte[] bytes = null;

        try {
            response = new ResponseEntity<byte[]>(getFileStreamAsByteArray(AUDIO_LOCATION, fileName, MP3_EXTN), HttpStatus.OK);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            response = new ResponseEntity("File Not Found", HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
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

        InputStream inputStream = new FileInputStream
                (fileLocation + fileName + fileExtension);

        bytes = IOUtils.toByteArray(inputStream);

        return bytes;
    }

    public byte[] getFileStreamAsByteArray
}
