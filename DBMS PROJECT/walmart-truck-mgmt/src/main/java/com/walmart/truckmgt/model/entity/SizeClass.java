package com.walmart.truckmgt.model.entity;

/**
 * Size classification for a truck based on its max weight capacity.
 * SMALL  – capacity < 10 tons
 * MEDIUM – capacity 10-25 tons
 * LARGE  – capacity > 25 tons
 */
public enum SizeClass {
    SMALL,
    MEDIUM,
    LARGE
}
