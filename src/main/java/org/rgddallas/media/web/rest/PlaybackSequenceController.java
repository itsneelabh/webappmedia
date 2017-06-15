package org.rgddallas.media.web.rest;

import javafx.geometry.VPos;
import org.rgddallas.media.entity.VideoPlayback;
import org.rgddallas.media.entity.VideoPlaybackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Neelabh on 6/14/2017.
 */
@RestController
@RequestMapping("/playback")
public class PlaybackSequenceController {
    private VideoPlaybackRepository videoPlaybackRepository;

    @Autowired
    public PlaybackSequenceController(VideoPlaybackRepository videoPlaybackRepository) {
        this.videoPlaybackRepository = videoPlaybackRepository;
    }

    @PutMapping("/reset")
    public String resetPlaybackSeq() {
        List<VideoPlayback> entities = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        VideoPlayback entity = CollectionUtils.isEmpty(entities) ? null : entities.get(0);

        if (entity != null) {
            entity.setFileSequence(1L);
        } else {
            entity = new VideoPlayback();
            entity.setFileSequence(1L);
        }

        Long result = videoPlaybackRepository.save(entity).getFileSequence();

        return Long.toString(result);
    }

    @GetMapping
    public Long getSequence() {
        List<VideoPlayback> list = (List<VideoPlayback>) videoPlaybackRepository.findAll();

        return CollectionUtils.isEmpty(list) ? 0 : list.get(0).getFileSequence();
    }

}
