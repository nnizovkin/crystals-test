package ru.crystals.test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data @AllArgsConstructor
public class Price {
    private String productCode; // код товара
    private int number; // номер цены
    private int depart; // номер отдела
    private Date begin; // начало действия
    private Date end; // конец действия
    private long value; // значение цены в копейках
}
