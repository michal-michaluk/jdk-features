package dev.bottega.jdkfeatures.flightcontrol.step14.domain;

/**
 * Threat classification of an aircraft relative to a sector and an alarm speed.
 *
 * | Category | Meaning |
 * |----------|---------|
 * | `ALARM`  | fast **and** inside the sector |
 * | `SECTOR` | inside the sector, not over the alarm speed |
 * | `NORMAL` | outside the sector |
 * | `UNKNOWN`| `null` aircraft |
 */
public enum Category {
    ALARM, SECTOR, NORMAL, UNKNOWN
}
