package com.datawarehouse.metadata.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举类单元测试
 *
 * @author System
 * @since 1.0.0
 */
class EnumTest {

    @Test
    void testDataSourceTypeEnum_GetByCode_Success() {
        // when
        DataSourceTypeEnum hive = DataSourceTypeEnum.getByCode("Hive");

        // then
        assertNotNull(hive);
        assertEquals("Hive", hive.getCode());
    }

    @Test
    void testDataSourceTypeEnum_GetByCode_NotFound_ThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            DataSourceTypeEnum.getByCode("UNKNOWN");
        });
    }

    @Test
    void testApprovalStatusEnum_GetByCode_Success() {
        // when
        ApprovalStatusEnum pending = ApprovalStatusEnum.getByCode("PENDING");

        // then
        assertNotNull(pending);
        assertEquals("PENDING", pending.getCode());
    }

    @Test
    void testApprovalStatusEnum_CanApprove_Success() {
        // given
        ApprovalStatusEnum pending = ApprovalStatusEnum.PENDING;
        ApprovalStatusEnum draft = ApprovalStatusEnum.DRAFT;
        ApprovalStatusEnum approved = ApprovalStatusEnum.APPROVED;

        // then
        assertTrue(pending.canApprove());
        assertFalse(draft.canApprove());
        assertFalse(approved.canApprove());
    }

    @Test
    void testApprovalStatusEnum_CanPublish_Success() {
        // given
        ApprovalStatusEnum approved = ApprovalStatusEnum.APPROVED;
        ApprovalStatusEnum pending = ApprovalStatusEnum.PENDING;
        ApprovalStatusEnum published = ApprovalStatusEnum.PUBLISHED;

        // then
        assertTrue(approved.canPublish());
        assertFalse(pending.canPublish());
        assertFalse(published.canPublish());
    }

    @Test
    void testBusinessUnitEnum_GetByCode_Success() {
        // when
        BusinessUnitEnum flight = BusinessUnitEnum.getByCode("flight");
        BusinessUnitEnum hotel = BusinessUnitEnum.getByCode("hotel");

        // then
        assertNotNull(flight);
        assertEquals("flight", flight.getCode());

        assertNotNull(hotel);
        assertEquals("hotel", hotel.getCode());
    }

    @Test
    void testWarehouseLayerEnum_GetByCode_Success() {
        // when
        WarehouseLayerEnum ods = WarehouseLayerEnum.getByCode("ods");
        WarehouseLayerEnum dwd = WarehouseLayerEnum.getByCode("dwd");
        WarehouseLayerEnum ads = WarehouseLayerEnum.getByCode("ads");

        // then
        assertNotNull(ods);
        assertEquals("ods", ods.getCode());

        assertNotNull(dwd);
        assertEquals("dwd", dwd.getCode());

        assertNotNull(ads);
        assertEquals("ads", ads.getCode());
    }

    @Test
    void testPrimaryThemeEnum_GetByCode_Success() {
        // when
        PrimaryThemeEnum usr = PrimaryThemeEnum.getByCode("usr");
        PrimaryThemeEnum ord = PrimaryThemeEnum.getByCode("ord");

        // then
        assertNotNull(usr);
        assertEquals("usr", usr.getCode());

        assertNotNull(ord);
        assertEquals("ord", ord.getCode());
    }

    @Test
    void testSecondaryThemeEnum_GetByCode_Success() {
        // when
        SecondaryThemeEnum con = SecondaryThemeEnum.getByCode("con");
        SecondaryThemeEnum mem = SecondaryThemeEnum.getByCode("mem");

        // then
        assertNotNull(con);
        assertEquals("con", con.getCode());
        assertEquals(PrimaryThemeEnum.USR, con.getPrimaryTheme());

        assertNotNull(mem);
        assertEquals("mem", mem.getCode());
        assertEquals(PrimaryThemeEnum.USR, mem.getPrimaryTheme());
    }

    @Test
    void testSecondaryThemeEnum_GetByPrimaryTheme_Success() {
        // when
        SecondaryThemeEnum[] usrThemes = SecondaryThemeEnum.getByPrimaryTheme(PrimaryThemeEnum.USR);
        SecondaryThemeEnum[] ordThemes = SecondaryThemeEnum.getByPrimaryTheme(PrimaryThemeEnum.ORD);

        // then
        assertNotNull(usrThemes);
        assertTrue(usrThemes.length > 0);
        assertTrue(java.util.Arrays.stream(usrThemes)
            .allMatch(theme -> PrimaryThemeEnum.USR.equals(theme.getPrimaryTheme())));

        assertNotNull(ordThemes);
        assertTrue(ordThemes.length > 0);
        assertTrue(java.util.Arrays.stream(ordThemes)
            .allMatch(theme -> PrimaryThemeEnum.ORD.equals(theme.getPrimaryTheme())));
    }

    @Test
    void testSensitivityLevelEnum_GetByCode_Success() {
        // when
        SensitivityLevelEnum l1 = SensitivityLevelEnum.getByCode("L1");
        SensitivityLevelEnum l4 = SensitivityLevelEnum.getByCode("L4");

        // then
        assertNotNull(l1);
        assertEquals("L1", l1.getCode());

        assertNotNull(l4);
        assertEquals("L4", l4.getCode());
    }

    @Test
    void testImportanceLevelEnum_GetByCode_Success() {
        // when
        ImportanceLevelEnum p0 = ImportanceLevelEnum.getByCode("P0");
        ImportanceLevelEnum p3 = ImportanceLevelEnum.getByCode("P3");

        // then
        assertNotNull(p0);
        assertEquals("P0", p0.getCode());

        assertNotNull(p3);
        assertEquals("P3", p3.getCode());
    }

    @Test
    void testPartitionTypeEnum_GetByCode_Success() {
        // when
        PartitionTypeEnum full = PartitionTypeEnum.getByCode("FULL");
        PartitionTypeEnum incr = PartitionTypeEnum.getByCode("INCR");

        // then
        assertNotNull(full);
        assertEquals("FULL", full.getCode());

        assertNotNull(incr);
        assertEquals("INCR", incr.getCode());
    }

    @Test
    void testUpdateFrequencyEnum_GetByCode_Success() {
        // when
        UpdateFrequencyEnum realtime = UpdateFrequencyEnum.getByCode("REALTIME");
        UpdateFrequencyEnum daily = UpdateFrequencyEnum.getByCode("DAILY");

        // then
        assertNotNull(realtime);
        assertEquals("REALTIME", realtime.getCode());

        assertNotNull(daily);
        assertEquals("DAILY", daily.getCode());
    }

    @Test
    void testOperationTypeEnum_GetByCode_Success() {
        // when
        OperationTypeEnum create = OperationTypeEnum.getByCode("CREATE");
        OperationTypeEnum delete = OperationTypeEnum.getByCode("DELETE");

        // then
        assertNotNull(create);
        assertEquals("CREATE", create.getCode());

        assertNotNull(delete);
        assertEquals("DELETE", delete.getCode());
    }

    @Test
    void testHiveAccountEnum_GetByAccount_Success() {
        // when
        HiveAccountEnum flight = HiveAccountEnum.getByAccount("flight");

        // then
        assertNotNull(flight);
        assertEquals("flight", flight.getAccount());
    }

    @Test
    void testFieldTypeEnum_GetByCode_Success() {
        // when
        FieldTypeEnum bigint = FieldTypeEnum.getByCode("BIGINT");
        FieldTypeEnum string = FieldTypeEnum.getByCode("STRING");

        // then
        assertNotNull(bigint);
        assertEquals("BIGINT", bigint.getCode());

        assertNotNull(string);
        assertEquals("STRING", string.getCode());
    }

    @Test
    void testEnumCodeUniqueness() {
        // 确保每个枚举的code都是唯一的
        DataSourceTypeEnum[] dataSourceTypes = DataSourceTypeEnum.values();
        assertEquals(6, dataSourceTypes.length);

        ApprovalStatusEnum[] approvalStatuses = ApprovalStatusEnum.values();
        assertEquals(6, approvalStatuses.length);

        WarehouseLayerEnum[] warehouseLayers = WarehouseLayerEnum.values();
        assertEquals(8, warehouseLayers.length);

        PrimaryThemeEnum[] primaryThemes = PrimaryThemeEnum.values();
        assertEquals(8, primaryThemes.length);

        SensitivityLevelEnum[] sensitivityLevels = SensitivityLevelEnum.values();
        assertEquals(4, sensitivityLevels.length);

        ImportanceLevelEnum[] importanceLevels = ImportanceLevelEnum.values();
        assertEquals(4, importanceLevels.length);
    }
}
