package org.rgddallas.media.web.rest;

import org.apache.commons.lang.StringUtils;
import org.rgddallas.media.entity.VideoPlayback;
import org.rgddallas.media.entity.VideoPlaybackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides REST resources to manipulate the file sequences. File sequence is key to playing the video lecture. The
 * system automatically plays the next lecture based on this seq.
 *
 * Created by Neelabh on 6/14/2017.
 */
@RestController
@RequestMapping("/playback")
@CrossOrigin
public class PlaybackSequenceController {
    public static final Logger log = LoggerFactory.getLogger(MediaResource.class);

    private VideoPlaybackRepository videoPlaybackRepository;

    @Autowired
    public PlaybackSequenceController(VideoPlaybackRepository videoPlaybackRepository) {
        this.videoPlaybackRepository = videoPlaybackRepository;
    }

    /**
     * REST resource to get the current sequence.
     *
     * @return current file sequence
     */
    @GetMapping
    public Long getSequence() {
        List<VideoPlayback> list = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        return CollectionUtils.isEmpty(list) ? 0 : list.get(0).getFileSequence();
    }

    /**
     * REST resource to reset the sequence to 1.
     *
     * @return the seq after reset
     */
    @PutMapping("/reset")
    public String resetPlaybackSeq() {
        log.debug("Got request to reset the sequence");

        List<VideoPlayback> entities = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        VideoPlayback entity = CollectionUtils.isEmpty(entities) ? null : entities.get(0);

        if (entity != null) {
            entity.setFileSequence(1L);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        } else {
            entity = new VideoPlayback();
            entity.setFileSequence(1L);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        }

        Long result = videoPlaybackRepository.save(entity).getFileSequence();
        log.debug("Request completed - Current value of the seq is : {}", result);

        return Long.toString(result);
    }

    /**
     * REST request to increment the current seq by 1.
     *
     * @return the current seq in the db
     */
    @PutMapping("/next")
    public String incrementSeq() {
        log.debug("Got request to increment seq value");

        List<VideoPlayback> entities = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        VideoPlayback entity = CollectionUtils.isEmpty(entities) ? null : entities.get(0);

        if (entity != null) {
            entity.setFileSequence(entity.getFileSequence() + 1);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        } else {
            entity = new VideoPlayback();
            entity.setFileSequence(1L);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        }

        Long result = videoPlaybackRepository.save(entity).getFileSequence();
        log.debug("Request completed - Value in the DB for current seq is : {}", result);

        return Long.toString(result);
    }

    /**
     * REST request to decrease the current seq by 1.
     *
     * @return the current seq in the db
     */
    @PutMapping("/prev")
    public String decreaseSeq() {
        log.debug("Got request to decrement seq value");

        List<VideoPlayback> entities = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        VideoPlayback entity = CollectionUtils.isEmpty(entities) ? null : entities.get(0);

        if (entity != null) {
            entity.setFileSequence(entity.getFileSequence() - 1);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        } else {
            entity = new VideoPlayback();
            entity.setFileSequence(1L);
            entity.setLastPlayed(LocalDateTime.now().minusDays(2));
        }

        Long result = videoPlaybackRepository.save(entity).getFileSequence();
        log.debug("Request completed - Value in the DB for current seq is : {}", result);

        return Long.toString(result);
    }

    /**
     * REST resource to set a custom value to the file sequence.
     *
     * @param newSeq the seq to be set
     * @return the new seq in the db
     */
    @PutMapping("/set/{newSeq}")
    public String setCustomSeq(@PathVariable String newSeq) {
        log.debug("Got request to set custom seq value : {}", newSeq);

        Long result = 0L;

        if(StringUtils.isNotEmpty(newSeq)) {
            List<VideoPlayback> entities = (List<VideoPlayback>) videoPlaybackRepository.findAll();

            VideoPlayback entity = CollectionUtils.isEmpty(entities) ? null : entities.get(0);

            if (entity != null) {
                entity.setFileSequence(Long.parseLong(newSeq));
                entity.setLastPlayed(LocalDateTime.now().minusDays(2));
            } else {
                entity = new VideoPlayback();
                entity.setFileSequence(1L);
                entity.setLastPlayed(LocalDateTime.now().minusDays(2));
            }

            result = videoPlaybackRepository.save(entity).getFileSequence();
            log.debug("New seq successfully set in DB");
        }

        return Long.toString(result);
    }

    /**
     *
     * @return
     */
    @GetMapping("/records")
    public List<VideoPlayback> getVideoPlaybackRecords() {
        return (List<VideoPlayback>) videoPlaybackRepository.findAll();
    }
}
