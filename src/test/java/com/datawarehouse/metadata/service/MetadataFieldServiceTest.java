package com.datawarehouse.metadata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.datawarehouse.metadata.dto.request.FieldCreateRequest;
import com.datawarehouse.metadata.dto.request.FieldUpdateRequest;
import com.datawarehouse.metadata.entity.MetadataField;
import com.datawarehouse.metadata.exception.BusinessException;
import com.datawarehouse.metadata.mapper.MetadataFieldMapper;
import com.datawarehouse.metadata.service.impl.MetadataFieldServiceImpl;
import com.datawarehouse.metadata.vo.MetadataFieldVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MetadataFieldService单元测试
 *
 * @author System
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class MetadataFieldServiceTest {

    @Mock
    private MetadataFieldMapper fieldMapper;

    @InjectMocks
    private MetadataFieldServiceImpl fieldService;

    private MetadataField mockField;

    @BeforeEach
    void setUp() {
        mockField = new MetadataField();
        mockField.setId(1L);
        mockField.setTableId(100L);
        mockField.setFieldName("test_field");
        mockField.setFieldType("STRING");
        mockField.setFieldComment("测试字段");
        mockField.setFieldOrder(1);
        mockField.setIsPrimaryKey(0);
        mockField.setIsNullable(1);
        mockField.setCreateTime(LocalDateTime.now());
        mockField.setDeleted(0);
    }

    @Test
    void testGetFieldsByTableId_Success() {
        // given
        List<MetadataField> fields = Collections.singletonList(mockField);
        when(fieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fields);

        // when
        List<MetadataFieldVO> result = fieldService.getFieldsByTableId(100L);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test_field", result.get(0).getFieldName());
        verify(fieldMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetFieldsByTableId_EmptyResult() {
        // given
        when(fieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // when
        List<MetadataFieldVO> result = fieldService.getFieldsByTableId(999L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testBatchCreateFields_Success() {
        // given
        List<FieldCreateRequest> requests = new ArrayList<>();

        FieldCreateRequest request1 = new FieldCreateRequest();
        request1.setFieldName("id");
        request1.setFieldType("BIGINT");
        request1.setFieldComment("主键ID");
        request1.setFieldOrder(1);
        request1.setIsPrimaryKey(1);
        request1.setIsNullable(0);
        requests.add(request1);

        FieldCreateRequest request2 = new FieldCreateRequest();
        request2.setFieldName("name");
        request2.setFieldType("STRING");
        request2.setFieldComment("姓名");
        request2.setFieldOrder(2);
        request2.setIsPrimaryKey(0);
        request2.setIsNullable(1);
        requests.add(request2);

        when(fieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList()); // 无重复字段
        when(fieldMapper.insert(any(MetadataField.class))).thenReturn(1);

        // when
        fieldService.batchCreateFields(100L, requests);

        // then
        verify(fieldMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(fieldMapper, times(2)).insert(any(MetadataField.class));
    }

    @Test
    void testBatchCreateFields_EmptyList_NoOperation() {
        // given
        List<FieldCreateRequest> requests = Collections.emptyList();

        // when
        fieldService.batchCreateFields(100L, requests);

        // then
        verify(fieldMapper, never()).insert(any(MetadataField.class));
    }

    @Test
    void testBatchCreateFields_DuplicateFieldName_ThrowException() {
        // given
        List<FieldCreateRequest> requests = new ArrayList<>();

        FieldCreateRequest request = new FieldCreateRequest();
        request.setFieldName("existing_field");
        request.setFieldType("STRING");
        request.setFieldComment("字段");
        request.setFieldOrder(1);
        requests.add(request);

        MetadataField existingField = new MetadataField();
        existingField.setFieldName("existing_field");
        existingField.setTableId(100L);

        when(fieldMapper.selectList(any(LambdaQueryWrapper.class)))
            .thenReturn(Collections.singletonList(existingField)); // 字段已存在

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fieldService.batchCreateFields(100L, requests);
        });

        assertTrue(exception.getMessage().contains("已存在"));
        verify(fieldMapper, never()).insert(any(MetadataField.class));
    }

    @Test
    void testUpdateField_Success() {
        // given
        FieldUpdateRequest updateRequest = new FieldUpdateRequest();
        updateRequest.setFieldComment("更新后的注释");
        updateRequest.setSensitivityLevel("L2");

        when(fieldMapper.selectById(1L)).thenReturn(mockField);
        when(fieldMapper.updateById(any(MetadataField.class))).thenReturn(1);

        // when
        fieldService.updateField(1L, updateRequest);

        // then
        ArgumentCaptor<MetadataField> captor = ArgumentCaptor.forClass(MetadataField.class);
        verify(fieldMapper, times(1)).updateById(captor.capture());

        MetadataField updatedField = captor.getValue();
        assertEquals("更新后的注释", updatedField.getFieldComment());
        assertEquals("L2", updatedField.getSensitivityLevel());
    }

    @Test
    void testUpdateField_NotFound_ThrowException() {
        // given
        FieldUpdateRequest updateRequest = new FieldUpdateRequest();
        updateRequest.setFieldComment("更新后的注释");

        when(fieldMapper.selectById(999L)).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fieldService.updateField(999L, updateRequest);
        });

        assertTrue(exception.getMessage().contains("不存在"));
        verify(fieldMapper, never()).updateById(any(MetadataField.class));
    }

    @Test
    void testDeleteField_Success() {
        // given
        when(fieldMapper.selectById(1L)).thenReturn(mockField);
        when(fieldMapper.updateById(any(MetadataField.class))).thenReturn(1);

        // when
        fieldService.deleteField(1L);

        // then
        ArgumentCaptor<MetadataField> captor = ArgumentCaptor.forClass(MetadataField.class);
        verify(fieldMapper, times(1)).updateById(captor.capture());

        MetadataField deletedField = captor.getValue();
        assertEquals(1, deletedField.getDeleted());
    }

    @Test
    void testDeleteField_NotFound_ThrowException() {
        // given
        when(fieldMapper.selectById(999L)).thenReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            fieldService.deleteField(999L);
        });

        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    void testDeleteFieldsByTableId_Success() {
        // given
        List<MetadataField> fields = new ArrayList<>();
        fields.add(mockField);

        MetadataField field2 = new MetadataField();
        field2.setId(2L);
        field2.setTableId(100L);
        field2.setFieldName("field2");
        fields.add(field2);

        when(fieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fields);
        when(fieldMapper.updateById(any(MetadataField.class))).thenReturn(1);

        // when
        fieldService.deleteFieldsByTableId(100L);

        // then
        verify(fieldMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(fieldMapper, times(2)).updateById(any(MetadataField.class));
    }

    @Test
    void testDeleteFieldsByTableId_NoFields_NoOperation() {
        // given
        when(fieldMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // when
        fieldService.deleteFieldsByTableId(999L);

        // then
        verify(fieldMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(fieldMapper, never()).updateById(any(MetadataField.class));
    }
}
