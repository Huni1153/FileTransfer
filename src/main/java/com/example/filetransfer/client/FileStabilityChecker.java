package com.example.filetransfer.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

public final class FileStabilityChecker {

    private FileStabilityChecker() {}

    /**
     * lastModified 시간이 stableMillis 동안 변하지 않으면 "안정"으로 간주.
     * 전체 대기 시간은 maxWaitMillis를 넘지 않음.
     */
    public static boolean waitUntilStable(Path file, long stableMillis, long maxWaitMillis) throws IOException, InterruptedException {

        if (!Files.exists(file)) {
            return false;
        }

        long start = System.currentTimeMillis();
        FileTime lastMod = Files.getLastModifiedTime(file);
        long lastChangeTime = start;

        while (true) {
            Thread.sleep(200L); // 0.2초마다 확인

            if (!Files.exists(file)) {
                return false;
            }

            long now = System.currentTimeMillis();
            FileTime curMod = Files.getLastModifiedTime(file);

            if (!curMod.equals(lastMod)) {
                lastMod = curMod;
                lastChangeTime = now;
            }

            if (now - lastChangeTime >= stableMillis) {
                return true; // stableMillis 동안 변경 없음
            }

            if (now - start >= maxWaitMillis) {
                return false; // 최대 대기 시간 초과
            }
        }
    }

    /** 마지막 수정 시점이 minAgeMillis 이상 지났는지만 단발성 체크 */
    public static boolean isOlderThan(Path file, long minAgeMillis) throws IOException {
        if (!Files.exists(file)) {
            return false;
        }
        long lastMod = Files.getLastModifiedTime(file).toMillis();
        long now = System.currentTimeMillis();
        return now - lastMod >= minAgeMillis;
    }
}
