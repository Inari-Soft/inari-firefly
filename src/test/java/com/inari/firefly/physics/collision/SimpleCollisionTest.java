package com.inari.firefly.physics.collision;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.inari.commons.geom.Position;
import com.inari.commons.geom.PositionF;
import com.inari.commons.geom.Rectangle;
import com.inari.firefly.FFTest;
import com.inari.firefly.TestTimer;
import com.inari.firefly.entity.EntitySystem;
import com.inari.firefly.graphics.ETransform;
import com.inari.firefly.graphics.tile.ETile;
import com.inari.firefly.graphics.tile.TileGrid;
import com.inari.firefly.physics.movement.EMovement;
import com.inari.firefly.physics.movement.MovementSystem;

public class SimpleCollisionTest extends FFTest {

    private static final Rectangle WORLD_BOUNDS = new Rectangle( 0, 0, 100, 100 );

    @Test @Ignore
    public void testSimpleCollision() {

        TestTimer timer = (TestTimer) ffContext.getTimer();
        EntitySystem entitySystem = ffContext.getSystem( EntitySystem.SYSTEM_KEY );
        MovementSystem movementSystem = ffContext.getSystem( MovementSystem.SYSTEM_KEY );
        
        ffContext.getComponentBuilder( ContactPool.TYPE_KEY, CollisionQuadTree.class )
            .set( CollisionQuadTree.VIEW_ID, 0 )
            .set( CollisionQuadTree.LAYER_ID, 0 )
            .set( CollisionQuadTree.MAX_ENTRIES_OF_AREA, 10 )
            .set( CollisionQuadTree.MAX_LEVEL, 5 )
            .set( CollisionQuadTree.WORLD_AREA, WORLD_BOUNDS )
        .build();
            
        entitySystem.getEntityBuilder()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.POSITION, new PositionF( 10, 10 ) )
            .set( ECollision.COLLISION_BOUNDS, new Rectangle( 0, 0, 10, 10 ) )
            .set( EMovement.VELOCITY_X, 3f )
            .set( EMovement.ACTIVE, true )
        .activateAndNext()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.POSITION, new PositionF( 30, 10 ) )
            .set( ECollision.COLLISION_BOUNDS, new Rectangle( 0, 0, 10, 10 ) )
        .activate();
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( timer );
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( timer );
        timer.tick();
        movementSystem.update( timer );
        timer.tick();
        movementSystem.update( timer );
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "CollisionEvent [movedEntityId=0, collidingEntityId=1, collisionIntersectionBounds=[x=8,y=0,width=2,height=10], collisionIntersectionMask=null]]]", 
            eventLog.toString() 
        );
    }
    
    @Test @Ignore
    public void testSimpleTileCollision() {
        
        TestTimer timer = (TestTimer) ffContext.getTimer();
        ffContext.loadSystem( CollisionSystem.SYSTEM_KEY );
        EntitySystem entitySystem = ffContext.getSystem( EntitySystem.SYSTEM_KEY );
        MovementSystem movementSystem = ffContext.getSystem( MovementSystem.SYSTEM_KEY );
        
        ffContext.getComponentBuilder( TileGrid.TYPE_KEY )
            .set( TileGrid.CELL_WIDTH, 16 )
            .set( TileGrid.CELL_HEIGHT, 16 )
            .set( TileGrid.WORLD_XPOS, 0 )
            .set( TileGrid.WORLD_YPOS, 0 )
            .set( TileGrid.WIDTH, 10 )
            .set( TileGrid.HEIGHT, 10 )
            .set( TileGrid.VIEW_ID, 0 )
            .set( TileGrid.LAYER_ID, 0 )
        .build();

        entitySystem.getEntityBuilder()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .set( ETransform.POSITION, new PositionF( 10, 10 ) )
            .set( ECollision.COLLISION_BOUNDS, new Rectangle( 0, 0, 10, 10 ) )
            .set( EMovement.VELOCITY_X, 3f )
            .set( EMovement.ACTIVE, true )
        .activateAndNext()
            .set( ETransform.VIEW_ID, 0 )
            .set( ETransform.LAYER_ID, 0 )
            .add( ETile.GRID_POSITIONS, new Position( 2, 1) )
            .set( ECollision.COLLISION_BOUNDS, new Rectangle( 0, 0, 16, 16 ) )
        .activate();
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1]]]", 
            eventLog.toString() 
        );
        
        timer.tick();
        movementSystem.update( timer );
        timer.tick();
        movementSystem.update( timer );
        timer.tick();
        movementSystem.update( timer );
        timer.tick();
        movementSystem.update( timer );
        
        assertEquals( 
            "EventLog [events=["
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=0], "
            + "EntityActivationEvent [eventType=ENTITY_ACTIVATED, entityId=1], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "MoveEvent [entityIds=IntBag [nullValue=-1, expand=10, size=1, length=10, array=[0, -1, -1, -1, -1, -1, -1, -1, -1, -1]]], "
            + "CollisionEvent [movedEntityId=0, collidingEntityId=1, collisionIntersectionBounds=[x=10,y=6,width=0,height=4], collisionIntersectionMask=null]]]", 
            eventLog.toString()
        );
    }

}
