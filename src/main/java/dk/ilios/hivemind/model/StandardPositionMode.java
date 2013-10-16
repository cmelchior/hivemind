package dk.ilios.hivemind.model;

public enum StandardPositionMode {
    // Fully enabled
    ENABLED,

    // Enabled, but only SP for starting positions are enabled, ie. start
    // position, start rotation and swap around axis for second pieces.
    LIMITED,

    // Standard Position are not calculated when placing/moving tokens.
    DISABLED
}
