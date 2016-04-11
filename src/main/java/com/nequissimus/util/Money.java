package com.nequissimus.util;

import java.util.Optional;

public final class Money implements Comparable<Money> {
  public static final Money ZERO = new Money(0, 0);

  private final int dollars;
  private final int cents;

  public Money(int dollars, int cents) {
    this.dollars = dollars + cents / 100;
    this.cents = cents % 100;
  }

  @Override
  public int compareTo(Money that) {
    if (this.dollars > that.dollars) {
      return 1;
    } else if (this.dollars < that.dollars) {
      return -1;
    } else if (this.cents > that.cents) {
      return 1;
    } else if (this.cents < that.cents) {
      return -1;
    } else {
      return 0;
    }
  }

  public int getDollars() {
    return this.dollars;
  }

  public int getCents() {
    return this.cents;
  }

  @Override
  public String toString() {
    return String.format("$%d", this.dollars);
  }

  public Money add(Money that) {
    return new Money(this.dollars + that.dollars, this.cents + that.cents);
  }

  public Optional<Money> subtract(Money that) {
    if (this.compareTo(that) == -1) {
      return Optional.empty();
    } else {
      if (this.cents >= that.cents) {
        return Optional.of(new Money(this.dollars - that.dollars, this.cents - that.cents));
      } else {
        return Optional.of(new Money(this.dollars - that.dollars - 1, this.cents - that.cents + 100));
      }
    }
  }

  public Money multiply(int factor) {
    return new Money(this.dollars * factor, this.cents * factor);
  }
}
