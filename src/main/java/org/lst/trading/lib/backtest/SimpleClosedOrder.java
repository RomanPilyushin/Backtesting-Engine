package org.lst.trading.lib.backtest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lst.trading.lib.model.ClosedOrder;

import java.time.Instant;

@AllArgsConstructor
@Getter
class SimpleClosedOrder implements ClosedOrder {
    private final SimpleOrder order;
    private final double closePrice;
    private final Instant closeInstant;
    private final double pl;

    @Override
    public boolean isLong() {
        return order.isLong();
    }

    @Override
    public int getId() {
        return order.getId();
    }

    @Override
    public int getAmount() {
        return order.getAmount();
    }

    @Override
    public double getOpenPrice() {
        return order.getOpenPrice();
    }

    @Override
    public Instant getOpenInstant() {
        return order.getOpenInstant();
    }

    @Override
    public String getInstrument() {
        return order.getInstrument();
    }

    // If calculatePl is a method that needs to be executed during construction, consider calling it explicitly in the constructor.
    public SimpleClosedOrder(SimpleOrder order, double closePrice, Instant closeInstant) {
        this.order = order;
        this.closePrice = closePrice;
        this.closeInstant = closeInstant;
        this.pl = calculatePl(closePrice); // Assuming calculatePl is a method in this class
    }

    // calculatePl method (if exists)...
}
