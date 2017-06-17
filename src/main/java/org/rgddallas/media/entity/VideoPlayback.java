package org.rgddallas.media.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Neelabh on 6/14/2017.
 */
@Entity
public class VideoPlayback {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    private Long fileSequence;

    private LocalDateTime lastPlayed;

    public Long getFileSequence() {
        return fileSequence;
    }

    public void setFileSequence(Long fileSequence) {
        this.fileSequence = fileSequence;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    @Override
    public String toString() {
        return "VideoPlayback{" +
                "fileSequence=" + fileSequence +
                '}';
    }
}
