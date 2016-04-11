package com.nequissimus.horsetrack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.nequissimus.util.Money;
import com.nequissimus.util.Tuple2;

/**
 * Horse racing machine that keeps track of winning horses and can pay out
 * winnings from bets.
 */
final class Machine {
  static final int DEFAULT_BILL_COUNT = 10;
  static final Horse DEFAULT_WINNER = Horse.GRAY_CAT;
  static final Map<Money, Integer> DEFAULT_INVENTORY = new HashMap<>();
  static {
    Stream.of(1, 5, 10, 20, 100)
        .map(i -> new Money(i, 0))
        .forEach(m -> DEFAULT_INVENTORY.put(m, DEFAULT_BILL_COUNT));
  }

  private final Horse winner;
  private final Map<Money, Integer> inventory;

  /**
   * Create a Machine with the default settings
   * @return A machine
   */
  public Machine() {
    this(DEFAULT_WINNER, DEFAULT_INVENTORY);
  }

  private Machine(Horse winner, Map<Money, Integer> inventory) {
    this.winner = winner;
    this.inventory = inventory;
  }

  /**
   * Change the winner
   * @param number Number of the winning horse
   * @return Machine with the new winner set, returns this if no such horse was found
   */
  Machine setWinner(int number) {
    return Horse.findByNumber(number)
        .map(winner -> new Machine(winner, this.inventory))
        .orElse(this);
  }

  /**
   * Determine whether a horse is the winning one
   * @param h Horse to inquire about
   * @return true if the inquired horse is the winner
   */
  boolean isWinner(Horse h) {
    return h == this.winner;
  }

  /**
   * Determine amount of money won
   * @param horse Horse that was bet on
   * @param bet Amount of money bet
   * @return Amount of money won or Optional.empty if horse did not win
   */
  Optional<Money> determineWinnings(Horse horse, Money bet) {
    return Optional.of(this.winner)
        .filter(winner -> winner == horse)
        .map(Horse::getOdds)
        .map(odds -> bet.multiply(odds));
  }

  /**
   * Try to dispense money with the largest bills available
   * @param m Amount of money to be paid out
   * @return Machine after paying out the money, returns this if no payout can be made
   */
  Tuple2<Machine, Map<Money, Integer>> dispenseMoney(final Money m) {
    Money money = m;

    final Map<Money, Integer> tmpInventory = new HashMap<>(this.inventory);
    final Map<Money, Integer> removed = new HashMap<>();

    final Stream.Builder<Money> b = Stream.builder();
    this.inventory.entrySet().stream()
        .sorted((e1, e2) -> -(e1.getKey().compareTo(e2.getKey())))
        .forEach(e -> {
          IntStream.rangeClosed(1, e.getValue()).forEach(i -> b.add(e.getKey()));
        });
    final Stream<Money> s = b.build();

    for (Money bill : s.toArray(Money[]::new)) {
      if (money.compareTo(bill) >= 0) {
        tmpInventory.computeIfPresent(bill, (k, old) -> old - 1);
        removed.compute(bill, (k, old) -> (old == null ? 0 : old) + 1);
        money = money.subtract(bill).get(); // Option.get is safe here because money >= bill
      }
    }

    if (money.compareTo(Money.ZERO) == 0) {
      return new Tuple2<>(new Machine(this.winner, tmpInventory), removed);
    } else {
      return new Tuple2<>(this, Collections.emptyMap());
    }
  }

  /**
   * Current cash inventory
   * @return Denominations and number of bills available
   */
  Map<Money, Integer> getInventory() {
    return new HashMap<>(this.inventory);
  }

  /**
   * Reset the inventory to its defaults
   * @return Machine with full inventory
   */
  Machine resetInventory() {
    return new Machine(this.winner, DEFAULT_INVENTORY);
  }

  /**
   * List all horses registered to race
   * @return All horses
   */
  Set<Horse> getRegisteredHorses() {
    return new HashSet<>(Arrays.asList(Horse.values()));
  }
}
