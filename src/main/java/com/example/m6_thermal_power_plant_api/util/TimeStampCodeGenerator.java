    package com.example.m6_thermal_power_plant_api.util;

    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.atomic.AtomicInteger;

    /**
     * Tiện ích sinh mã nghiệp vụ (business code) cho các entity:
     * department_code, request_code, order_code, consumable_code...
     *
     * QUY TẮC SINH MÃ: {@code PREFIX-yyMMddHHmmss-SEQ} (ngăn cách bằng dấu gạch '-')
     *
     * 1) PREFIX = chữ cái viết HOA suy ra từ TÊN CLASS (tách theo camelCase):
     *    - Class 1 từ   → lấy 2 ký tự đầu.   VD: Tool → "TO", Employee → "EM"
     *    - Class ≥ 2 từ → lấy ký tự đầu MỖI từ. VD: WorkOrder → "WO",
     *      RepairRequest → "RR", ConsumableIssueDetail → "CID"
     *      (LƯU Ý: bản mô tả ban đầu ghi ConsumableIssueDetail → "CIS"; đây là lỗi
     *       gõ — theo đúng quy tắc "ký tự đầu mỗi từ" thì 3 từ Consumable/Issue/
     *       Detail phải ra C‑I‑D. Code này tạo "CID" cho nhất quán.)
     *
     * 2) Timestamp = thời điểm tạo, chính xác đến GIÂY, định dạng {@code yyMMddHHmmss}
     *    (YY‑năm 2 số, MM‑tháng, DD‑ngày, HH‑giờ 24h, mm‑phút, ss‑giây).
     *    VD lúc 2026‑06‑27 15:30:45 → "260627153045".
     *
     * 3) SEQ = hậu tố 3 chữ số (000–999) lấy từ một bộ đếm {@link AtomicInteger}
     *    RIÊNG THEO TỪNG PREFIX (mỗi loại entity một bộ đếm), giữ trong một
     *    {@link ConcurrentHashMap}. Mỗi prefix tăng nguyên tử (atomic) độc lập mỗi
     *    lần gọi và cuộn vòng (wrap) ở 1000. ĐÂY là phần khử trùng cho các mã CÙNG
     *    prefix tạo trong CÙNG một giây.
     *
     * Ghép lại: WorkOrder (lần gọi thứ 3 cho prefix "WO") → {@code "WO-260627153045-002"},
     * trong khi RepairRequest tính riêng → {@code "RR-260627153045-000"}.
     *
     * TẠI SAO DÙNG AtomicInteger THAY VÌ "ĐỌC MÃ MAX TRONG DB RỒI +1"?
     *  - Cách cũ phải truy vấn DB để biết số kế tiếp (phiền, thêm round-trip, dễ
     *    race khi nhiều request đồng thời). Bộ đếm trong bộ nhớ {@code getAndIncrement()}
     *    là lock-free, thread-safe, KHÔNG chạm DB.
     *  - Tách bộ đếm theo PREFIX: vì mã chỉ có thể đụng nhau khi trùng cả prefix lẫn
     *    timestamp, đếm riêng theo prefix là đủ khử trùng mà mỗi loại entity vẫn có
     *    chuỗi SEQ liên tục, dễ đọc. Hai class ra cùng prefix sẽ DÙNG CHUNG một bộ
     *    đếm — đúng mong muốn, vì chúng mới là cặp có nguy cơ trùng mã.
     *
     * GIỚI HẠN CÒN LẠI (cần lưới an toàn ở tầng service):
     *  - Nếu sinh > 1000 mã trong CÙNG một giây, SEQ cuộn vòng và có thể trùng.
     *  - Khi JVM restart, bộ đếm về 0 → nếu vô tình tạo lại trong đúng giây cũ có
     *    thể trùng. Nhiều instance (scale-out) cũng có thể trùng.
     *  → Vì các tình huống này hiếm nhưng KHÔNG bằng 0, hãy LUÔN coi unique
     *    constraint ở DB (cột active_flag — xem
     *    db/upgrade-soft-delete-unique-constraints.sql) là chốt chặn cuối, và
     *    bọc thao tác lưu bằng {@code UniqueCodeRetryExecutor} để tự sinh lại mã +
     *    thử lại khi đụng {@code DataIntegrityViolationException}.
     *
     * Class chỉ chứa static method, không cho khởi tạo.
     */
    public final class TimeStampCodeGenerator {

        private static final DateTimeFormatter TIMESTAMP_FORMAT =
                DateTimeFormatter.ofPattern("yyMMddHHmmss");

        /** Số phần tử của vòng cuộn hậu tố (000–999). */
        private static final int SEQUENCE_MODULO = 1000;

        /** Bộ đếm hậu tố khử trùng, TÁCH RIÊNG theo từng prefix (mỗi loại entity một bộ đếm). */
        private static final ConcurrentHashMap<String, AtomicInteger> SEQUENCES = new ConcurrentHashMap<>();

        private TimeStampCodeGenerator() {
            // tiện ích thuần static — không khởi tạo
        }

        /**
         * Sinh mã cho một entity theo class của nó, dùng thời điểm hiện tại và hậu
         * tố tự tăng. VD: {@code generate(WorkOrder.class)} → "WO-260627153045-007".
         */
        public static String generate(Class<?> entityClass) {
            return generate(entityClass, LocalDateTime.now());
        }

        /**
         * Sinh mã với mốc thời gian chỉ định (vẫn dùng hậu tố tự tăng).
         */
            public static String generate(Class<?> entityClass, LocalDateTime when) {
            if (entityClass == null) {
                throw new IllegalArgumentException("entityClass không được null");
            }
            return generate(entityClass.getSimpleName(), when);
        }

        /**
         * Sinh mã từ tên class dạng PascalCase, dùng thời điểm hiện tại + hậu tố tự tăng.
         * VD: {@code generate("Department")} → "DE-260627153045-012".
         */
        public static String generate(String className) {
            return generate(className, LocalDateTime.now());
        }

        /** Sinh mã từ tên class với mốc thời gian chỉ định + hậu tố tự tăng (riêng theo prefix). */
        public static String generate(String className, LocalDateTime when) {
            if (when == null) {
                throw new IllegalArgumentException("when không được null");
            }
            String prefix = prefixOf(className);
            AtomicInteger counter = SEQUENCES.computeIfAbsent(prefix, k -> new AtomicInteger(0));
            int seq = Math.floorMod(counter.getAndIncrement(), SEQUENCE_MODULO);
            return prefix + "-" + when.format(TIMESTAMP_FORMAT) + "-" + String.format("%03d", seq);
        }

        /**
         * Sinh mã với hậu tố CHỈ ĐỊNH (không dùng bộ đếm). Hữu ích cho unit test /
         * dữ liệu mẫu khi cần kết quả tất định.
         * VD: {@code generate("WorkOrder", when, 5)} → "WO-260627153045-005".
         */
        public static String generate(String className, LocalDateTime when, int sequence) {
            if (when == null) {
                throw new IllegalArgumentException("when không được null");
            }
            int seq = Math.floorMod(sequence, SEQUENCE_MODULO);
            return prefixOf(className) + "-" + when.format(TIMESTAMP_FORMAT) + "-" + String.format("%03d", seq);
        }

        /**
         * Suy ra phần tiền tố (PREFIX) viết hoa từ tên class theo quy tắc ở Javadoc class.
         * Tách public để service có thể tự ghép timestamp riêng nếu cần.
         */
        public static String prefixOf(String className) {
            if (className == null || className.isBlank()) {
                throw new IllegalArgumentException("className không được rỗng");
            }

            // Tách camelCase/PascalCase thành các từ. Regex xử lý cả ranh giới
            // thường→hoa (workO) và acronym→từ (PDFExport → PDF, Export).
            String[] words = className.split("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

            StringBuilder prefix = new StringBuilder();
            if (words.length == 1) {
                // Class 1 từ → lấy 2 ký tự đầu (Equipment → EQ).
                String word = words[0];
                prefix.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    prefix.append(Character.toUpperCase(word.charAt(1)));
                }
            } else {
                // Class ≥ 2 từ → ký tự đầu mỗi từ (WorkOrder → WO).
                for (String word : words) {
                    if (!word.isEmpty()) {
                        prefix.append(Character.toUpperCase(word.charAt(0)));
                    }
                }
            }
            return prefix.toString();
        }
    }
