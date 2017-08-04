package com.inari.firefly.physics.movement;

import com.inari.firefly.graphics.ETransform;

public interface Integrator {
    
    void integrate( final EMovement movement, final ETransform transform, final long deltaTimeInSeconds );
    
    void step( final EMovement movement, final ETransform transform, final long deltaTimeInSeconds );

}
