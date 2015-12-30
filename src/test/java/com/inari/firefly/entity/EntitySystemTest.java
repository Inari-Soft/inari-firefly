package com.inari.firefly.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.FireFlyMock;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.graphics.sprite.ESprite;
import com.inari.firefly.system.FFContext;

public class EntitySystemTest {
    
    @Before
    public void init() {
        Indexer.clear();
    }
    
    @Test
    public void testCreation() {
        FFContext testContext = new FireFlyMock().getContext();
        EntitySystem entitySystem = testContext.getSystem( EntitySystem.SYSTEM_KEY );
        
        Attributes attrs = new Attributes();
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        
        assertEquals( 
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=", 
            attrs.toString()
        );
        assertEquals( "1024", String.valueOf( entitySystem.activeEntities.size() ) );
        assertEquals( "0", String.valueOf( entitySystem.activeEntities.cardinality() ) );
        assertEquals( "1024", String.valueOf( entitySystem.inactiveEntities.size() ) );
        assertEquals( "0", String.valueOf( entitySystem.inactiveEntities.cardinality() ) );
        assertEquals( "1000", String.valueOf( entitySystem.components.capacity() ) );
        assertEquals( "0", String.valueOf( entitySystem.components.size() ) );
        
        
        entitySystem = new EntitySystem();
        entitySystem.init( testContext );
        
        attrs = new Attributes();
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        
        assertEquals( 
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=", 
            attrs.toString()
        );
    }
    
    @Test
    public void testCreationWithinContext() {
        FFContext testContext = new FireFlyMock().getContext();
        testContext.getSystem( EntitySystem.SYSTEM_KEY );
        
        Attributes attrs = new Attributes();
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        
        assertEquals( 
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=", 
            attrs.toString()
        );
    }
    
    @Test
    public void testCreateAndDelete() {
        FFContext testContext = new FireFlyMock().getContext();
        EntitySystem entitySystem = testContext.getSystem( EntitySystem.SYSTEM_KEY );
        
        Attributes attrs = new Attributes();
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        
        assertEquals( 
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=", 
            attrs.toString()
        );
        
        int entityId = entitySystem.getEntityBuilder()
            .set( ETransform.VIEW_ID, 1 )
            .set( ETransform.XPOSITION, 234 )
            .set( ETransform.YPOSITION, 134 )
            .set( ESprite.SPRITE_ID, 555 )
        .activate();
        
        assertEquals( "1", String.valueOf( entitySystem.activeEntities.cardinality() ) );
        assertEquals( "0", String.valueOf( entitySystem.inactiveEntities.cardinality() ) );
        assertEquals( "1", String.valueOf( entitySystem.components.size() ) );
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        assertEquals(
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=0 "
            + "SystemComponent:Entity(0)::viewId:Integer:ETransform=1, layerId:Integer:ETransform=0, xpos:Float:ETransform=234.0, ypos:Float:ETransform=134.0, pivotx:Float:ETransform=0.0, pivoty:Float:ETransform=0.0, scalex:Float:ETransform=1.0, scaley:Float:ETransform=1.0, rotation:Float:ETransform=0.0, parentId:Integer:ETransform=-1, spriteId:Integer:ESprite=555, ordering:Integer:ESprite=0, tintColor:RGBColor:ESprite=[r=1.0,g=1.0,b=1.0,a=1.0], blendMode:BlendMode:ESprite=NONE", 
            attrs.toString()
        );
        
        
        entitySystem.delete( entityId );
        
        assertEquals( "0", String.valueOf( entitySystem.activeEntities.cardinality() ) );
        assertEquals( "0", String.valueOf( entitySystem.inactiveEntities.cardinality() ) );
        assertEquals( "0", String.valueOf( entitySystem.components.size() ) );
        attrs = new Attributes();
        testContext.toAttributes( attrs, EntitySystem.Entity.ENTITY_TYPE_KEY );
        assertEquals( 
            "SystemComponent:Entity(-1)::ACTIVE_ENTITY_IDS:String=", 
            attrs.toString()
        );
    }
    
    

}
