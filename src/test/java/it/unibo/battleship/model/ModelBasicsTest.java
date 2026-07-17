package it.unibo.battleship.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;

/**
 * Tests for the basic model value objects and enums.
 */
public class ModelBasicsTest {
    
    /**
     * Checks that negatuve coordinates are rejected.
     */
    @Test
    void rejectNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Coordinate(-1, 0));
    }

    /**
     * Checks that PlayerId.other() returns the other player.
     */
    @Test
    void alternatesPlayerIds() {
        assertEquals(PlayerId.PLAYER2, PlayerId.PLAYER1.other());
        assertEquals(PlayerId.PLAYER1, PlayerId.PLAYER2.other());
    }
}

