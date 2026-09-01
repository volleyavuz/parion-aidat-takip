package com.parion.aidat;

/**
 * v4.3.9 compatibility layer.
 * The old repeating setAthletePhoto watchdog is disabled because it races with
 * MainActivityV756 and resets the portrait to a placeholder before each async load.
 */
public class MainActivityV755 extends MainActivityV754 {
}
