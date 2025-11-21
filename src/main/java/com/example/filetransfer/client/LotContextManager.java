package com.example.filetransfer.client;

import com.example.filetransfer.backup.BackupService;
import com.example.filetransfer.tcp.TcpFileSender;
import com.example.filetransfer.client.LotFileNameParser.FileInfo;
import com.example.filetransfer.client.LotFileNameParser.FileType;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LotContextManager {

    private final Path watchDir;
    private final BackupService backupService;
    private final TcpFileSender tcpSender;
    private final MainExcelParser mainExcelParser = new MainExcelParser();

    private final Map<String, LotContext> lotMap = new ConcurrentHashMap<>();

    public LotContextManager(Path watchDir, BackupService backupService, TcpFileSender tcpSender) {
        this.watchDir = watchDir;
        this.backupService = backupService;
        this.tcpSender = tcpSender;
    }

    public void onMainExcelStable(Path mainExcelPath) {
        String fileName = mainExcelPath.getFileName().toString();
        String lotId = LotFileNameParser.extractLotIdFromMainName(fileName);
        System.out.println("[LOT] Main Excel stable: lot=" + lotId + ", path=" + mainExcelPath);

        LotContext ctx = lotMap.computeIfAbsent(lotId, LotContext::new);
        ctx.setMainExcelPath(mainExcelPath);

        try {
            MainExcelParser.Result parsed = mainExcelParser.parse(mainExcelPath);
            ctx.setSystemId(parsed.getSystemId());
            ctx.setExpectedWafers(parsed.getWaferNos());

            System.out.println("[LOT] Parsed main: systemId=" + parsed.getSystemId() + ", wafers=" + parsed.getWaferNos());

            scanLotDirectory(ctx);
            backupAndSend(ctx);

        } catch (Exception e) {
            System.err.println("[LOT] onMainExcelStable error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // v1: 메인엑셀 안정화 시점에 디렉토리를 스캔하므로 여기서는 로그만
    public void onSubFileDetected(Path path) {
        System.out.println("[LOT] Sub file detected: " + path);
    }

    private void scanLotDirectory(LotContext ctx) throws IOException {
        Path mainExcelPath = ctx.getMainExcelPath();
        if (mainExcelPath == null) {
            return;
        }
        Path lotDir = mainExcelPath.getParent();
        String lotId = ctx.getLotId();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(lotDir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) continue;
                if (p.equals(mainExcelPath)) continue;

                String name = p.getFileName().toString();

                FileInfo info = LotFileNameParser.parse(name, lotId);
                if (info == null || info.getWaferNo() == null) {
                    // lotId/waferNo 없는 파일 = 실패 or 기타
                    continue;
                }

                int waferNo = info.getWaferNo();
                if (!ctx.getExpectedWafers().contains(waferNo)) {
                    // 메인엑셀에 없는 wafer → 실패/불필요
                    continue;
                }

                // 서브파일 얕은 안정 체크 (마지막 수정 후 N초)
                if (!FileStabilityChecker.isOlderThan(p, ClientConfig.SUBFILE_STABLE_MILLIS)) {
                    System.out.println("[LOT] Subfile too new, skip for now: " + p);
                    continue;
                }

                WaferContext wctx = ctx.getOrCreateWafer(waferNo);
                if (info.getType() == FileType.SUB_CSV) {
                    wctx.addSubCsv(p);
                } else if (info.getType() == FileType.MAP) {
                    wctx.addMap(p);
                } else {
                    wctx.addOther(p);
                }
            }
        }
    }

    private void backupAndSend(LotContext ctx) throws IOException {
        if (ctx.isSent()) {
            System.out.println("[LOT] Already sent, skip: " + ctx.getLotId());
            return;
        }

        String lotId = ctx.getLotId();
        List<Path> toBackup = new ArrayList<>();

        if (ctx.getMainExcelPath() != null) {
            toBackup.add(ctx.getMainExcelPath());
        }
        for (WaferContext wctx : ctx.getWaferContexts()) {
            toBackup.addAll(wctx.getSubCsvFiles());
            toBackup.addAll(wctx.getMapFiles());
            toBackup.addAll(wctx.getOtherFiles());
        }

        if (toBackup.isEmpty()) {
            System.out.println("[LOT] Nothing to backup/send for lot: " + lotId);
            return;
        }

        List<Path> backupFiles = new ArrayList<>();
        for (Path src : toBackup) {
            Path backupFile = backupService.backupLotFile(lotId, src);
            backupFiles.add(backupFile);
        }

        for (Path backupFile : backupFiles) {
            String serverPath = lotId + "/" + backupFile.getFileName().toString();
            tcpSender.send(backupFile, serverPath);
        }

        ctx.markSent();
        System.out.println("[LOT] Backup + send complete for lot=" + lotId
                + ", files=" + backupFiles.size());
    }
}
