package com.example.filetransfer.server;

import com.example.filetransfer.server.model.IncomingFileInfo;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;

public class ServerProcessor implements Runnable {

    private final Socket socket;
    private final Path inboxRoot;

    public ServerProcessor(Socket socket, Path inboxRoot) {
        this.socket = socket;
        this.inboxRoot = inboxRoot;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("[SERVER] Client connected: " + remote);

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()))) {

            // 1) 헤더: serverPath 길이 + serverPath 문자열 + 파일 크기
            int pathLen = in.readInt();
            if (pathLen <= 0 || pathLen > 10_000) {
                throw new IOException("Invalid path length: " + pathLen);
            }
            byte[] pathBytes = in.readNBytes(pathLen);
            if (pathBytes.length != pathLen) {
                throw new EOFException("Unexpected EOF while reading path");
            }
            String serverPath = new String(pathBytes, java.nio.charset.StandardCharsets.UTF_8);

            long size = in.readLong();
            if (size < 0) {
                throw new IOException("Invalid file size: " + size);
            }

            System.out.println("[SERVER] Header received: path=" + serverPath + ", size=" + size);

            // 2) 저장할 실제 경로 계산
            Path target = resolveTargetPath(serverPath);

            // 디렉토리 생성
            Files.createDirectories(target.getParent());

            // 3) 파일 바디 수신
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(
                    target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {

                byte[] buf = new byte[8192];
                long remaining = size;

                while (remaining > 0) {
                    int read = in.read(buf, 0, (int) Math.min(buf.length, remaining));
                    if (read == -1) {
                        throw new EOFException("Unexpected EOF while reading file body");
                    }
                    out.write(buf, 0, read);
                    remaining -= read;
                }
                out.flush();
            }

            IncomingFileInfo info = new IncomingFileInfo(serverPath, size, target);
            System.out.println("[SERVER] File received: " + info);

            // TODO: 여기서 lotId, 파일타입 등을 분석해서
            //       lot 단위로 다 모였는지 판단하고,
            //       L01 생성 + FTP 업로드 등으로 넘기는 로직을 얹을 예정.

        } catch (IOException e) {
            System.err.println("[SERVER] Error while handling client " + remote + " : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            System.out.println("[SERVER] Client disconnected: " + remote);
        }
    }

    private Path resolveTargetPath(String serverPath) {
        // serverPath 예: "60P1T9/60P1T9.csv"
        // 결과: INBOX_ROOT/60P1T9/60P1T9.csv
        Path relative = Paths.get(serverPath);
        Path target = inboxRoot.resolve(relative).normalize();

        // 보너스: INBOX_ROOT 바깥으로 나가려는 경로 방어 (../ 같은 것)
        if (!target.startsWith(inboxRoot)) {
            throw new IllegalArgumentException("Invalid serverPath (escaping root): " + serverPath);
        }
        return target;
    }
}
