package com.inari.firefly.physics.collision;

import com.inari.commons.lang.indexed.IndexedTypeKey;
import com.inari.firefly.physics.collision.Collisions.CollisionData;
import com.inari.firefly.physics.collision.Collisions.EntityData;
import com.inari.firefly.system.component.SystemComponent;

public abstract class CollisionConstraint extends SystemComponent {
    
    public static final SystemComponentKey<CollisionConstraint> TYPE_KEY = SystemComponentKey.create( CollisionConstraint.class );

    CollisionConstraint( int id ) {
        super( id );
    }
    
    @Override
    public final IndexedTypeKey indexedTypeKey() {
        return TYPE_KEY;
    }
    
    public abstract boolean check( EntityData entityData, CollisionData collisionData );

}