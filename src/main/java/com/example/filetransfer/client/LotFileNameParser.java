package com.example.filetransfer.client;

public final class LotFileNameParser {

    public enum FileType {
        MAIN_CSV,
        SUB_CSV,
        MAP,
        OTHER
    }

    public static final class FileInfo {
        private final String lotId;
        private final Integer waferNo;
        private final FileType type;

        public FileInfo(String lotId, Integer waferNo, FileType type) {
            this.lotId = lotId;
            this.waferNo = waferNo;
            this.type = type;
        }

        public String getLotId() { return lotId; }
        public Integer getWaferNo() { return waferNo; }
        public FileType getType() { return type; }
    }

    private LotFileNameParser() {}

    // 메인 CSV 이름: 언더스코어 없는 .csv
    public static boolean isMainCsv(String name) {
        String lower = name.toLowerCase();
        if (!lower.endsWith(".csv")) return false;
        String base = lower.substring(0, lower.length() - 4);
        return !base.contains("_");
    }

    // "60P1T9.csv" -> "60P1T9"
    public static String extractLotIdFromMainName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(0, dot);
        }
        return name;
    }

    /** lotId를 알고 있다는 전제 하에, 서브/맵 파일에서 waferNo 및 타입 추출 */
    public static FileInfo parse(String name, String expectedLotId) {
        String lower = name.toLowerCase();

        // 메인 CSV는 여기서 굳이 안 다룸 (이미 isMainCsv로 처리)
        if (isMainCsv(name)) {
            String lotId = extractLotIdFromMainName(name);
            if (!lotId.equals(expectedLotId)) {
                return null;
            }
            return new FileInfo(lotId, null, FileType.MAIN_CSV);
        }

        if (!lower.contains(expectedLotId.toLowerCase())) {
            return null;
        }
        String lotId = expectedLotId;

        Integer waferNo = extractWaferNoAfterLot(lower, expectedLotId.toLowerCase());
        FileType type = determineType(lower);

        return new FileInfo(lotId, waferNo, type);
    }

    private static FileType determineType(String lower) {
        if (lower.endsWith(".csv")) {
            return FileType.SUB_CSV;
        } else if (lower.endsWith(".map")) {
            return FileType.MAP;
        } else {
            return FileType.OTHER;
        }
    }

    // "...60p1t9_01_..." or "...60p1t9_01.map" 에서 lotId 뒤의 "_01" → waferNo = 1
    private static Integer extractWaferNoAfterLot(String lowerName, String lowerLotId) {
        int idx = lowerName.indexOf(lowerLotId);
        if (idx < 0) {
            return null;
        }
        int i = idx + lowerLotId.length();
        // 다음 '_'까지 이동
        while (i < lowerName.length() && lowerName.charAt(i) != '_') {
            i++;
        }
        if (i >= lowerName.length() - 1) {
            return null;
        }
        i++; // '_' 뒤부터 숫자 시작

        StringBuilder sb = new StringBuilder();
        while (i < lowerName.length()
                && Character.isDigit(lowerName.charAt(i))
                && sb.length() < 2) {
            sb.append(lowerName.charAt(i));
            i++;
        }
        if (sb.length() == 0) {
            return null;
        }
        try {
            return Integer.parseInt(sb.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
