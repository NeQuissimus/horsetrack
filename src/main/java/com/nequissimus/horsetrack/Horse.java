package com.nequissimus.horsetrack;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Horses - fairly self-explanatory
 */
enum Horse {
  GRAY_CAT(1, "That Darn Gray Cat", 5),
  UTOPIA(2, "Fort Utopia", 10),
  SHEEP(3, "Count Sheep", 9),
  TRAITOUR(4, "Ms Traitour", 4),
  PRINCESS(5, "Real Princess", 3),
  KETTLE(6, "Pa Kettle", 5),
  STINGER(7, "Gin Stinger", 6);

  static Optional<Horse> findByNumber(int number) {
    return Stream.of(Horse.values()).filter(h -> h.getNumber() == number).findFirst();
  }

  private final int number;
  private final String name;
  private final int odds;

  private Horse(int number, String name, int odds) {
    this.number = number;
    this.name = name;
    this.odds = odds;
  }

  int getNumber() {
    return this.number;
  }

  String getName() {
    return this.name;
  }

  int getOdds() {
    return this.odds;
  }
}
