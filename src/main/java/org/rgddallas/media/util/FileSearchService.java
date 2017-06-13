package org.rgddallas.media.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileSearchService {
    private static Logger log = LoggerFactory.getLogger(FileSearchService.class);

    private String fileNameToSearch;
    private List<String> result = new ArrayList<String>();

    public String getFileNameToSearch() {
        return fileNameToSearch;
    }

    public void setFileNameToSearch(String fileNameToSearch) {
        this.fileNameToSearch = fileNameToSearch;
    }

    public List<String> getResult() {
        return result;
    }

  /*  public static void main(String[] args) {

        FileSearch fileSearch = new FileSearch();

        //try different directory and filename :)
        fileSearch.searchDirectory(new File("C:\\temp\\"), "aaja_piya.mp3");

        int count = fileSearch.getResult().size();
        if(count ==0){
            System.out.println("\nNo result found!");
        }else{
            System.out.println("\nFound " + count + " result!\n");
            for (String matched : fileSearch.getResult()){
                System.out.println("Found : " + matched);
            }
        }
    }*/

    /**
     * Finds a matching file in the specified directory and its sub-directories.
     *
     * @param directory        directory to search
     * @param fileNameToSearch file name to search, including extension
     */
    public List<String> searchDirectory(File directory, String fileNameToSearch) {
        setFileNameToSearch(fileNameToSearch);

        if (directory.isDirectory()) {
            search(directory);
        } else {
            log.debug("{} is not a directory!", directory.getAbsoluteFile());
        }

        return result;
    }

    private void search(File file) {

        if (file.isDirectory()) {
            log.debug("Searching directory ... {}", file.getAbsoluteFile());

            //do you have permission to read this directory?
            if (file.canRead()) {
                for (File temp : file.listFiles()) {
                    if (temp.isDirectory()) {
                        search(temp);
                    } else {
                        if (getFileNameToSearch().equals(temp.getName().toLowerCase())) {
                            result.add(temp.getAbsoluteFile().toString());
                        }
                    }
                }
            } else {
                log.debug("{} - Permission Denied.", file.getAbsoluteFile());
            }
        }
    }
}
