package com.example.filetransfer.tcp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleTcpFileSender implements TcpFileSender {

    private final String host;
    private final int port;

    public SimpleTcpFileSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void send(Path file, String serverPath) throws IOException {
        long size = Files.size(file);
        System.out.println("[TCP-SEND] " + file + " -> " + host + ":" + port
                + " (" + serverPath + "), size=" + size);

        try (Socket socket = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             InputStream in = Files.newInputStream(file)) {

            byte[] pathBytes = serverPath.getBytes(StandardCharsets.UTF_8);
            out.writeInt(pathBytes.length);
            out.write(pathBytes);
            out.writeLong(size);

            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            out.flush();
        }
    }
}
