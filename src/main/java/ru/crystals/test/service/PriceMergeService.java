package ru.crystals.test.service;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import ru.crystals.test.dto.Price;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Сервис для объединения цен
 */
public class PriceMergeService {
    /**
     * Правила объединения цен:
     * если товар еще не имеет цен, или имеющиеся цены не пересекаются в периодах действия
     * c новыми, то новые цены просто добавляются к товару;
     * если имеющаяся цена пересекается в периоде действия с новой ценой, то:
     * если значения цен одинаковы, период действия имеющейся цены увеличивается
     * согласно периоду новой цены;
     * если значения цен отличаются, добавляется новая цена, а период действия старой
     * цены уменьшается согласно периоду новой цены.
     *
     * @param currentPrices текущие цены
     * @param newPrices     новые цены
     * @return объединение новых и старых цен
     */
    //Я предпологаю, что данные которые поступают валидны.
    Collection<Price> mergePrices(Collection<Price> currentPrices, Collection<Price> newPrices) {
        if (currentPrices.isEmpty()) {
            return newPrices;
        }

        var currentPricesMap = getGroupedPrices(currentPrices);
        var newPricesMap = getGroupedPrices(newPrices);
        List<Price> res = new ArrayList<>();

        for (var e : currentPricesMap.entrySet()) {
            if (newPricesMap.containsKey(e.getKey())) {
                var newPricesList = newPricesMap.get(e.getKey());
                List<Price> prices = buildPrices(e.getKey(), mergePricesLists(e.getValue(), newPricesList));
                res.addAll(prices);
                newPricesMap.remove(e.getKey());
            } else {
                res.addAll(buildPrices(e.getKey(), e.getValue()));
            }
        }
        return res;
    }

    List<PriceInterval> mergePricesLists(List<PriceInterval> current, List<PriceInterval> newPrices) {
        List<PriceInterval> res = new ArrayList<>();

        var currentIt = current.iterator();
        var newPricesIt = newPrices.iterator();

        PriceInterval currEl = null;
        PriceInterval newEl = null;

        while (true) {
            if (currEl == null) {
                if (currentIt.hasNext()) {
                    currEl = currentIt.next();
                } else {
                    break;
                }
            }

            if (newEl == null) {
                if (newPricesIt.hasNext()) {
                    newEl = newPricesIt.next();
                } else {
                    break;
                }
            }

            if (currEl.end <= newEl.begin) {
                mergeLastEl(res, currEl);
                currEl = null;
            } else if (newEl.end <= currEl.begin) {
                mergeLastEl(res, newEl);
                newEl = null;
            } else if(currEl.value == newEl.value) {
                mergeLastEl(res, new PriceInterval(Math.min(currEl.begin, newEl.begin), Math.max(currEl.end, newEl.end), currEl.value));
                currEl = null;
                newEl = null;
            } else if (currEl.begin <= newEl.begin && currEl.end >= newEl.end) {
                mergeLastEl(res, new PriceInterval(currEl.begin, newEl.begin, currEl.value));
                mergeLastEl(res, newEl);
                currEl = new PriceInterval(newEl.end, currEl.end, currEl.value);
                newEl = null;
            } else if (newEl.begin <= currEl.begin && newEl.end >= currEl.end) {
                mergeLastEl(res, new PriceInterval(newEl.begin, currEl.end, newEl.value));
                newEl = new PriceInterval(currEl.end, newEl.end, newEl.value);
                currEl = null;
            } else if (currEl.begin <= newEl.begin) {
                mergeLastEl(res, new PriceInterval(currEl.begin, newEl.begin, currEl.value));
                mergeLastEl(res, new PriceInterval(newEl.begin, currEl.end, newEl.value));
                newEl = new PriceInterval(currEl.end, newEl.end, newEl.value);
                currEl = null;
            } else {
                mergeLastEl(res, newEl);
                currEl = new PriceInterval(newEl.end, currEl.end, currEl.value);
                newEl = null;
            }
        }

        if(currEl != null) {
            mergeLastEl(res, currEl);
        }

        if(newEl != null) {
            mergeLastEl(res, newEl);
        }

        addToTail(res, currentIt);
        addToTail(res, newPricesIt);

        return res;
    }

    private void addToTail(List<PriceInterval> res, Iterator<PriceInterval> it) {
        if (it.hasNext()) {
            it.forEachRemaining(el -> mergeLastEl(res, el));
        }
    }

    private void mergeLastEl(List<PriceInterval> res, PriceInterval el) {
        if(res.isEmpty()) {
            res.add(el);
            return;
        }

        var lastEl = res.get(res.size() - 1);
        if (lastEl.end == el.begin && lastEl.value == el.value) {
            lastEl.end = el.end;
        } else {
            res.add(el);
        }
    }

    private Map<PriceId, List<PriceInterval>> getGroupedPrices(Collection<Price> prices) {
        return prices.stream().collect(
                Collectors.groupingBy(
                        p -> new PriceId(p.getProductCode(), p.getNumber(), p.getDepart()),
                        mapping(p -> new PriceInterval(p.getBegin().getTime(), p.getEnd().getTime(), p.getValue()),
                                collectingAndThen(toList(),
                                        e -> e.stream().sorted(Comparator.comparingLong(p -> p.begin))
                                                .collect(toList())))));
    }

    private List<Price> buildPrices(PriceId priceId, List<PriceInterval> intervals) {
        return intervals.stream()
                .map(i -> new Price(priceId.productCode,
                        priceId.number,
                        priceId.depart,
                        new Date(i.begin),
                        new Date(i.end),
                        i.value))
                .collect(Collectors.toList());
    }

    @EqualsAndHashCode
    @AllArgsConstructor
    private static class PriceId {
        String productCode;
        int number;
        int depart;
    }

    @AllArgsConstructor
    private static class PriceInterval {
        long begin;
        long end;
        long value;

    }
}
