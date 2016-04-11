package com.nequissimus.horsetrack;

import java.io.Console;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.nequissimus.util.Money;
import com.nequissimus.util.Tuple2;

final class Main {
  static final String NOT_ENOUGH_MONEY = "Insufficient funds: %d";
  static final String NO_PAYOUT = "No payout: %s";
  static final String INVALID_HORSE_NUMBER = "Invalid Horse Number: %d";
  static final String INVALID_BET = "Invalid Bet: %s";
  static final String INVALID_COMMAND = "Invalid Command: %s";

  static final String WON = "won";
  static final String LOST = "lost";
  static final String HORSES = "Horses:";
  static final String HORSES_OUT = "%d,%s,%d,%s";
  static final String INVENTORY = "Inventory:";
  static final String INVENTORY_OUT = "$%d,%d";
  static final String DISPENSING = "Dispensing:";
  static final String PAYOUT = "Payout: %s, %s";

  static void printInventory(String headline, Map<Money, Integer> inventory, final PrintStream out) {
    out.println(headline);
    inventory.entrySet().stream()
        .sorted((e1, e2) -> (e1.getKey().compareTo(e2.getKey())))
        .map(e -> String.format(INVENTORY_OUT, e.getKey().getDollars(), e.getValue()))
        .forEach(out::println);
  }

  static void printHorses(Set<Horse> horses, Function<Horse, Boolean> isWinner, PrintStream out) {
    out.println(HORSES);
    horses.stream()
        .sorted(Comparator.comparing(Horse::getNumber))
        .map(h -> String.format(HORSES_OUT, h.getNumber(), h.getName(), h.getOdds(), (isWinner.apply(h) ? WON : LOST)))
        .forEach(out::println);
  }

  static void processBet(int horseNum, int amount, Machine machine) {
    final Optional<Horse> horse = Horse.findByNumber(horseNum);

    if (horse.map(machine::isWinner).filter(Boolean::booleanValue).isPresent()) {
      final Machine tmpMachine = machine;
      final Optional<Money> winnings = horse
          .flatMap(h -> tmpMachine.determineWinnings(h, new Money(amount, 0)));

      final Optional<Tuple2<Machine, Map<Money, Integer>>> newMachine = winnings
          .map(tmpMachine::dispenseMoney)
          .filter(t -> t.getT1() != tmpMachine);

      if (newMachine.isPresent()) {
        final Map<Money, Integer> dispensed = newMachine.get().getT2();
        machine = newMachine.get().getT1();
        System.out.println(String.format(PAYOUT, horse.get().getName(), winnings.get().toString()));
        printInventory(DISPENSING, dispensed, System.out);
      } else {
        System.out.println(String.format(NOT_ENOUGH_MONEY, amount));
      }
    } else {
      if (horse.isPresent()) {
        System.out.println(String.format(NO_PAYOUT, horse.get().getName()));
      } else {
        System.out.println(String.format(INVALID_HORSE_NUMBER, horseNum));
      }
    }
  }

  static Optional<Integer> parseInteger(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  public static void main(String[] args) {
    final Console stdin = System.console();

    Machine machine = new Machine();
    boolean running = true;

    while (running) {
      printInventory(INVENTORY, machine.getInventory(), System.out);
      printHorses(machine.getRegisteredHorses(), machine::isWinner, System.out);

      final String c = new String(stdin.readPassword()); // This will prevent printing the input
      final String[] cmds = c.split(" +");
      final String cmd = cmds[0].trim();
      final String param = (cmds.length > 1) ? cmds[1].trim() : "";

      final Optional<Integer> cmdInt = parseInteger(cmd);
      final Optional<Integer> paramInt = parseInteger(param);

      switch (cmd) {
        case "R":
        case "r": machine = machine.resetInventory(); break;
        case "Q":
        case "q": running = false; break;
        case "w":
        case "W": if (paramInt.isPresent()) {
                    machine = machine.setWinner(paramInt.get());
                  } else {
                    System.out.println(String.format(INVALID_HORSE_NUMBER, param));
                  }; break;
        default:  if (cmdInt.isPresent()) {
                    if (paramInt.isPresent()) {
                      processBet(cmdInt.get(), paramInt.get(), machine);
                    } else {
                      System.out.println(String.format(INVALID_BET, param));
                    }
                  } else {
                    System.out.println(String.format(INVALID_COMMAND, cmd));
                  }

      }
    }
  }
}
