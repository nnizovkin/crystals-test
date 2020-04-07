package ru.crystals.test.service;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.test.dto.Price;

import java.util.Date;
import java.util.List;

public class PriceMergeServiceTest {
    @Test
    public void twoEmptyLists_emptyList() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(), service.mergePrices(List.of(), List.of()));
    }

    @Test
    public void currentEmptyNewWithOneEl_newList() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 3, 1)),
                service.mergePrices(List.of(), List.of(buildPrice(0, 3, 1))));
    }

    @Test
    public void currentWithOneElNewEmpty_currentList() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 3, 1)),
                service.mergePrices(List.of(buildPrice(0, 3, 1)), List.of()));
    }


    @Test
    public void intersectionNewFirst_TwoElements() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 2, 0), buildPrice(2, 3, 1)),
                service.mergePrices(List.of(buildPrice(1, 3, 1)), List.of(buildPrice(0, 2, 0))));
    }

    @Test
    public void intersectionCurrentFirst_TwoElements() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 1, 1), buildPrice(1, 3, 0)),
                service.mergePrices(List.of(buildPrice(0, 2, 1)), List.of(buildPrice(1, 3, 0))));
    }

    @Test
    public void withoutIntersectionCurrentFirst_TwoSameElements() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 2, 1), buildPrice(4, 5, 0)),
                service.mergePrices(List.of(buildPrice(0, 2, 1)), List.of(buildPrice(4, 5, 0))));
    }

    @Test
    public void withoutIntersectionNewFirst_TwoSameElements() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 2, 0), buildPrice(4, 5, 1)),
                service.mergePrices(List.of(buildPrice(4, 5, 1)), List.of(buildPrice(0, 2, 0))));
    }

    @Test
    public void enclosureNewIn_ThreeElements() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 1, 1), buildPrice(1, 2, 0), buildPrice(2, 5, 1)),
                service.mergePrices(List.of(buildPrice(0, 5, 1)), List.of(buildPrice(1, 2, 0))));
    }

    @Test
    public void enclosureCurrentIn_OneElement() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 5, 0)),
                service.mergePrices(List.of(buildPrice(1, 2, 1)), List.of(buildPrice(0, 5, 0))));
    }

    @Test
    public void intersectionTwoCurrentOneNew_ThreeElement() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 1000, 1), buildPrice(1000, 4000, 0), buildPrice(4000, 5000, 1)),
                service.mergePrices(List.of(buildPrice(0, 2000, 1), buildPrice(3000, 5000, 1)),
                        List.of(buildPrice(1000, 4000, 0))));
    }

    @Test
    public void intersectionOneCurrentTwoNew_ThreeElement() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 2000, 0), buildPrice(2000, 3000, 1), buildPrice(3000, 5000, 0)),
                service.mergePrices(List.of(buildPrice(1000, 4000, 1)),
                        List.of(buildPrice(0, 2000, 0), buildPrice(3000, 5000, 0))));
    }

    @Test
    public void enclosureTwoCurrentOneNew_OneElement() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 5, 0)),
                service.mergePrices(List.of(buildPrice(1, 2, 1), buildPrice(3, 4, 1)),
                        List.of(buildPrice(0, 5, 0))));
    }

    @Test
    public void enclosureTwoCurrentOneNew_FiveElement() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice(0, 1, 1), buildPrice(1, 2, 0),
                buildPrice(2, 3, 1), buildPrice(3, 4, 0), buildPrice(4, 5, 1)),
                service.mergePrices(List.of(buildPrice(0, 5, 1)),
                        List.of(buildPrice(1, 2, 0), buildPrice(3, 4, 0))));
    }

    @Test
    public void diffProducts_splitAndParseNewProducts() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice("1234", 1, 2, 1, 6, 1), buildPrice("1234", 2, 1, 4, 5, 1),
                buildPrice("1234", 2, 1, 5, 7, 0), buildPrice("6234", 1, 2, 0, 2, 1),
                buildPrice("6234", 1, 2, 2, 3, 0), buildPrice("6234", 1, 2, 3, 5, 1)),
                service.mergePrices(List.of(buildPrice("1234", 1, 2, 1, 5, 1), buildPrice("1234", 2, 1, 4, 6, 1),
                        buildPrice("6234", 1, 2, 0, 5, 1)),
                        List.of(buildPrice("1234", 1, 2, 4, 6, 1), buildPrice("1234", 2, 1, 5, 7, 0),
                                buildPrice("6234", 1, 2, 2, 3, 0))));
    }

    @Test
    public void newProducts_resultHasNewProducts() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice("1234", 1, 2, 1, 6, 1), buildPrice("1234", 2, 1, 4, 5, 1),
                buildPrice("1234", 2, 1, 5, 7, 0), buildPrice("6234", 1, 2, 0, 2, 1),
                buildPrice("6234", 1, 2, 2, 3, 0), buildPrice("6234", 1, 2, 3, 5, 1),
                buildPrice("5234", 1, 2, 2, 3, 0)),
                service.mergePrices(List.of(buildPrice("1234", 1, 2, 1, 5, 1), buildPrice("1234", 2, 1, 4, 6, 1),
                        buildPrice("6234", 1, 2, 0, 5, 1)),
                        List.of(buildPrice("1234", 1, 2, 4, 6, 1), buildPrice("1234", 2, 1, 5, 7, 0),
                                buildPrice("6234", 1, 2, 2, 3, 0), buildPrice("5234", 1, 2, 2, 3, 0))));
    }

    @Test
    public void twoPriceInTailWithoutIntersection_twoPriCesInTailInResult() {
        PriceMergeService service = new PriceMergeService();
        Assert.assertEquals(List.of(buildPrice("1234", 1, 2, 1, 6, 1), buildPrice("1234", 1, 2, 7, 8, 1),
                buildPrice("1234", 1, 2, 9, 10, 1)),
                service.mergePrices(List.of(buildPrice("1234", 1, 2, 1, 5, 1),
                        buildPrice("1234", 1, 2, 7, 8, 1), buildPrice("1234", 1, 2, 9, 10, 1)),
                        List.of(buildPrice("1234", 1, 2, 4, 6, 1))));
    }

    private Price buildPrice(long begin, long end, long value) {
        return new Price("", 1, 1, new Date(begin), new Date(end), value);
    }

    private Price buildPrice(String code, int number, int depart, long begin, long end, long value) {
        return new Price(code, number, depart, new Date(begin * 1000), new Date(end * 1000), value);
    }
}
