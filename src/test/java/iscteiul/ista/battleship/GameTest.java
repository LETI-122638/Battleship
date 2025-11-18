package iscteiul.ista.battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void fire(Compass c) {
        Ship ship = Ship.buildShip("Caravela", c, new Position(1, 1));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        // invalid shots
        assertNull(game.fire(new Position(-1, 0)));
        assertEquals(1, game.getInvalidShots());
        assertNull(game.fire(new Position(0, -1)));
        assertEquals(2, game.getInvalidShots());
        assertNull(game.fire(new Position(11, 0)));
        assertEquals(3, game.getInvalidShots());
        assertNull(game.fire(new Position(0, 11)));
        assertEquals(4, game.getInvalidShots());

        // valid miss
        assertNull(game.fire(new Position(2, 2)));
        assertEquals(0, game.getHits());
        assertEquals(0, game.getSunkShips());
        assertEquals(1, game.getShots().size());

        // repeated shot
        assertNull(game.fire(new Position(2, 2)));
        assertEquals(1, game.getRepeatedShots());
        assertEquals(1, game.getShots().size()); // should not add duplicate

        // hit and sink (ship size 1)
        IShip sunk = null;
        if(c==Compass.NORTH || c==Compass.SOUTH) {
            game.fire(new Position(1, 1));
            sunk = game.fire(new Position(2, 1));
        } else {
            game.fire(new Position(1, 1));
            sunk = game.fire(new Position(1, 2));
        }
        assertNotNull(sunk);
        assertEquals(2, game.getHits());
        assertEquals(1, game.getSunkShips());
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void getShots(Compass c) {
        Ship ship = Ship.buildShip("Barca", c, new Position(3, 3));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        game.fire(new Position(3, 3)); // miss
        List<IPosition> shots = game.getShots();
        assertEquals(1, shots.size());
        assertTrue(shots.contains(new Position(3, 3)));
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void getRepeatedShots(Compass c) {
        Ship ship = Ship.buildShip("Barca", c, new Position(4, 4));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        game.fire(new Position(4, 4));
        game.fire(new Position(4, 4));
        assertEquals(1, game.getRepeatedShots());
    }

    @Test
    void getInvalidShots() {
        Fleet fleet = new Fleet();
        Game game = new Game(fleet);

        game.fire(new Position(-5, 0));
        game.fire(new Position(-1, -1));
        assertEquals(2, game.getInvalidShots());
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void getHits(Compass c) {
        Ship ship = Ship.buildShip("Barca", c, new Position(5, 5));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        game.fire(new Position(0, 0)); // miss
        game.fire(new Position(5, 5)); // hit & sink
        assertEquals(1, game.getHits());
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void getSunkShips(Compass c) {
        Ship ship1 = Ship.buildShip("Barca", c, new Position(1, 1));
        Ship ship2 = Ship.buildShip("Barca", c, new Position(2, 2));
        Fleet fleet = new Fleet();
        fleet.addShip(ship1);
        fleet.addShip(ship2);
        Game game = new Game(fleet);

        game.fire(new Position(1, 1)); // sink s1
        assertEquals(1, game.getSunkShips());
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void getRemainingShips(Compass c) {
        Ship ship = Ship.buildShip("Barca", c, new Position(6, 6));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        assertEquals(1, game.getRemainingShips());
        game.fire(new Position(6, 6)); // sink
        assertEquals(0, game.getRemainingShips());
    }

    @Test
    void printBoard() {
        Fleet fleet = new Fleet();
        Game game = new Game(fleet);

        List<IPosition> positions = Arrays.asList(new Position(0, 0), new Position(1, 2));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            game.printBoard(positions, 'A');
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        String output = baos.toString();

        // board should contain marker 'A' at least twice
        long count = output.chars().filter(ch -> ch == 'A').count();
        assertEquals(2, count);
    }

    @Test
    void printValidShots() {
        Fleet fleet = new Fleet();
        Game game = new Game(fleet);

        game.fire(new Position(2, 3)); // valid miss -> added to shots

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            game.printValidShots();
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        String out = baos.toString();
        assertTrue(out.indexOf('X') >= 0);
    }

    @ParameterizedTest
    @EnumSource(value = Compass.class, names = {"NORTH", "SOUTH", "EAST", "WEST"})
    void printFleet(Compass c) {
        Ship ship = Ship.buildShip("Caravela", c, new Position(5, 5));
        Fleet fleet = new Fleet();
        fleet.addShip(ship);
        Game game = new Game(fleet);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try {
            game.printFleet();
        } finally {
            System.out.flush();
            System.setOut(originalOut);
        }

        String out = baos.toString();

        // two '#' markers for the two ship positions
        long count = out.chars().filter(ch -> ch == '#').count();
        assertEquals(2, count);
    }
}