package org.lst.trading.lib.backtest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lst.trading.lib.model.Order;

import java.time.Instant;

@AllArgsConstructor
@Getter
class SimpleOrder implements Order {
    private int id;
    private String instrument;
    private Instant openInstant;
    private double openPrice;
    private int amount;
}
