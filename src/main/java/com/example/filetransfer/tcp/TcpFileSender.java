package com.example.filetransfer.tcp;

import java.io.IOException;
import java.nio.file.Path;

public interface TcpFileSender {
    void send(Path file, String serverPath) throws IOException;
}
