package com.example.filetransfer.ftp;

import java.io.IOException;
import java.nio.file.Path;

public interface FtpFileSender {

    /**
     * localFile을 remotePath(예: "/IMP8-03/60P1T9_IMP8-03.csv")로 업로드.
     */
    void upload(Path localFile, String remotePath) throws IOException;
}
