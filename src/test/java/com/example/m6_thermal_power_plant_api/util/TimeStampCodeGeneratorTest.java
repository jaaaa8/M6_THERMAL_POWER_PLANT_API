package com.example.m6_thermal_power_plant_api.util;

import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.Employee;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeStampCodeGeneratorTest {

    @Test
    void prefixOf_twoWordClass_takesFirstLetterOfEachWord() {
        assertThat(TimeStampCodeGenerator.prefixOf("WorkOrder")).isEqualTo("WO");
        assertThat(TimeStampCodeGenerator.prefixOf("RepairRequest")).isEqualTo("RR");
        assertThat(TimeStampCodeGenerator.prefixOf("SparePartsIssue")).isEqualTo("SPI");
    }

    @Test
    void prefixOf_singleWordClass_takesFirstTwoLetters() {
        assertThat(TimeStampCodeGenerator.prefixOf("Equipment")).isEqualTo("EQ");
        assertThat(TimeStampCodeGenerator.prefixOf("Employee")).isEqualTo("EM");
        assertThat(TimeStampCodeGenerator.prefixOf("Unit")).isEqualTo("UN");
    }

    @Test
    void prefixOf_threeWordClass_takesThreeLetters() {
        // Consumable + Issue + Detail -> C, I, D (KHÔNG phải "CIS").
        assertThat(TimeStampCodeGenerator.prefixOf("ConsumableIssueDetail")).isEqualTo("CID");
    }

    @Test
    void prefixOf_blankOrNull_throws() {
        assertThatThrownBy(() -> TimeStampCodeGenerator.prefixOf(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TimeStampCodeGenerator.prefixOf("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generate_withFixedTimeAndSequence_isDeterministic() {
        LocalDateTime t = LocalDateTime.of(2026, 6, 27, 15, 30, 45);
        assertThat(TimeStampCodeGenerator.generate("WorkOrder", t, 5)).isEqualTo("WO-260627153045-005");
        assertThat(TimeStampCodeGenerator.generate(Equipment.class.getSimpleName(), t, 0)).isEqualTo("EQ-260627153045-000");
    }

    @Test
    void generate_sequenceWrapsAtThousand() {
        LocalDateTime t = LocalDateTime.of(2026, 6, 27, 15, 30, 45);
        // 1000 -> 000, 1234 -> 234, -1 -> 999 (floorMod).
        assertThat(TimeStampCodeGenerator.generate("WorkOrder", t, 1000)).endsWith("-000");
        assertThat(TimeStampCodeGenerator.generate("WorkOrder", t, 1234)).endsWith("-234");
        assertThat(TimeStampCodeGenerator.generate("WorkOrder", t, -1)).endsWith("-999");
    }

    @Test
    void generate_byClass_matchesExpectedShape() {
        // PREFIX + "-" + 12 chữ số timestamp + "-" + 3 chữ số seq.
        assertThat(TimeStampCodeGenerator.generate(WorkOrder.class)).matches("WO-\\d{12}-\\d{3}");
        assertThat(TimeStampCodeGenerator.generate(ConsumableIssueDetail.class)).matches("CID-\\d{12}-\\d{3}");
        assertThat(TimeStampCodeGenerator.generate(Employee.class)).matches("EM-\\d{12}-\\d{3}");
    }

    @Test
    void generate_consecutiveCalls_produceDifferentCodes() {
        // Bộ đếm AtomicInteger đảm bảo 2 lần gọi liên tiếp khác nhau dù cùng giây.
        String a = TimeStampCodeGenerator.generate(WorkOrder.class);
        String b = TimeStampCodeGenerator.generate(WorkOrder.class);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void generate_separateCounterPerPrefix_eachSequenceIncrementsIndependently() {
        LocalDateTime t = LocalDateTime.of(2026, 6, 27, 15, 30, 45);
        // Hai prefix riêng biệt phải đếm độc lập, không ăn chung số.
        int first = seqOf(TimeStampCodeGenerator.generate("Unit", t));
        int firstOther = seqOf(TimeStampCodeGenerator.generate("Garage", t));
        int second = seqOf(TimeStampCodeGenerator.generate("Unit", t));
        // Bộ đếm "UN" tăng 1 bất kể có gọi prefix "GA" xen vào giữa.
        assertThat(second).isEqualTo(first + 1);
        // Lần đầu của prefix "GA" không bị ảnh hưởng bởi bộ đếm của "UN".
        assertThat(firstOther).isZero();
    }

    /** Lấy phần SEQ (3 số cuối) từ mã sinh ra. */
    private static int seqOf(String code) {
        return Integer.parseInt(code.substring(code.lastIndexOf('-') + 1));
    }
}
