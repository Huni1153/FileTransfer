package com.example.filetransfer.server;

import com.example.filetransfer.client.LotFileNameParser;
import com.example.filetransfer.ftp.FtpFileSender;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ServerProcessor implements Runnable {

    private final Socket socket;
    private final Path serverRoot;
    private final FtpFileSender ftpSender;

    public ServerProcessor(Socket socket, Path serverRoot, FtpFileSender ftpSender) {
        this.socket = socket;
        this.serverRoot = serverRoot;
        this.ftpSender = ftpSender;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("[SERVER] Connected: " + remote);

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()))) {

            // 1️⃣ 헤더 수신
            int pathLen = in.readInt();
            byte[] pathBytes = in.readNBytes(pathLen);
            String serverPath = new String(pathBytes, StandardCharsets.UTF_8);

            long size = in.readLong();

            // 2️⃣ 실제 저장 경로
            Path target = resolveTargetPath(serverPath);
            Files.createDirectories(target.getParent());

            // 3️⃣ 파일 수신
            try (OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                byte[] buf = new byte[8192];
                long remaining = size;
                while (remaining > 0) {
                    int read = in.read(buf, 0, (int) Math.min(buf.length, remaining));
                    if (read == -1) throw new EOFException("Unexpected EOF");
                    out.write(buf, 0, read);
                    remaining -= read;
                }
            }

            System.out.println("[SERVER] File received: " + target);

            // 4️⃣ 메인 엑셀(.csv, 언더스코어 없는 경우)인지 판단 후 L01 생성
            String fileName = target.getFileName().toString();
            if (LotFileNameParser.isMainCsv(fileName)) {
                Path l01Path = createL01(target);
                List<Path> filesForFtp = collectLotFiles(target.getParent(), fileName);
                filesForFtp.add(l01Path);

                // FTP 전송
                sendLotToFtp(target.getParent(), filesForFtp);
            }

        } catch (Exception e) {
            System.err.println("[SERVER] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }

    private Path resolveTargetPath(String serverPath) {
        Path relative = Paths.get(serverPath);
        Path target = serverRoot.resolve(relative).normalize();
        if (!target.startsWith(serverRoot)) {
            throw new IllegalArgumentException("Invalid serverPath: " + serverPath);
        }
        return target;
    }

    // L01 생성 (메인.csv 기준)
    private Path createL01(Path mainCsv) throws IOException {
        String base = mainCsv.getFileName().toString().replace(".csv", "");
        Path l01 = mainCsv.getParent().resolve(base + ".L01");
        String content = "SOURCE=" + mainCsv.toString() + "\n"
                + "CREATED=" + java.time.LocalDateTime.now();
        Files.writeString(l01, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("[SERVER] L01 created: " + l01);
        return l01;
    }

    // 동일 폴더 내 lot 세트 수집
    private List<Path> collectLotFiles(Path lotDir, String mainFileName) throws IOException {
        List<Path> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lotDir)) {
            for (Path p : stream) {
                if (!Files.isRegularFile(p)) continue;
                String name = p.getFileName().toString();
                if (name.endsWith(".csv") || name.endsWith(".MAP")) {
                    list.add(p);
                }
            }
        }
        System.out.println("[SERVER] Collected lot files: " + list.size());
        return list;
    }

    private void sendLotToFtp(Path lotDir, List<Path> files) throws IOException {
        String systemId = lotDir.getFileName().toString();
        String remoteDir = ServerConfig.FTP_BASE_DIR + "/" + systemId;

        for (Path f : files) {
            String remotePath = remoteDir + "/" + f.getFileName();
            ftpSender.upload(f, remotePath);
        }
        System.out.println("[SERVER] FTP transfer complete: " + systemId);
    }
}
