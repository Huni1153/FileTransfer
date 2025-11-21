package com.example.filetransfer.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainExcelParser {

    public static class Result {
        private final String systemId;
        private final Set<Integer> waferNos;

        public Result(String systemId, Set<Integer> waferNos) {
            this.systemId = systemId;
            this.waferNos = waferNos;
        }

        public String getSystemId() {
            return systemId;
        }

        public Set<Integer> getWaferNos() {
            return waferNos;
        }

        @Override
        public String toString() {
            return "Result{systemId='" + systemId + "', waferNos=" + waferNos + '}';
        }
    }

    private final Charset charset;

    public MainExcelParser() {
        this(Charset.defaultCharset());
    }

    public MainExcelParser(Charset charset) {
        this.charset = charset;
    }
    public Result parse(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath, charset);
        System.out.println("몇 라인? : "+lines.size());

        // 최소한 3행은 있어야 A3/B3를 읽을 수 있음
        if (lines.size() <= 3) {
            System.err.println("최소 3행은 있어야 읽을 수 있습니다.");
            throw new IOException("Unexpected main CSV format (need at least 3 lines): " + csvPath);
        }

        // === 1) System ID : A3 (엑셀 3행 1열) ===
        String[] row3 = splitCsvLine(lines.get(2)); // lines[2] = 엑셀 3행
        String systemId = null;
        if (row3.length > 0) {
            systemId = row3[0].trim();  // A3
        }

        // === 2) Wafer No : B3 ~ 빈칸 전까지 ===
        Set<Integer> wafers = new LinkedHashSet<>();

        // 엑셀 3행(B3)부터 아래로 쭉 내려감 → lines[2]부터
        for (int rowIndex = 2; rowIndex < lines.size(); rowIndex++) {
            String line = lines.get(rowIndex);
            String[] cols = splitCsvLine(line);

            // 컬럼이 2개 미만이면 B열이 없으니까 wafer도 없음 → 종료
            if (cols.length < 2) {
                break;
            }

            String waferStr = cols[1].trim(); // B열 = Wafer No

            // "빈칸 전까지" 조건: B열이 빈 문자열이면 여기서 끝
            if (waferStr.isEmpty()) {
                break;
            }

            try {
                int wafer = Integer.parseInt(waferStr);
                wafers.add(wafer);
            } catch (NumberFormatException e) {
                // 숫자가 아니면(이상값) -> 여기서 끊어버리는 쪽이 직관적
                break;
            }
        }

        return new Result(systemId, wafers);
    }



    private String[] splitCsvLine(String line) {
        // 간단 CSV: 콤마 기준, 따옴표 처리 없음
        return line.split(",");
    }
}
