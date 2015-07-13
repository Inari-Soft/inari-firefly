package com.inari.firefly.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.EventDispatcherMock;
import com.inari.firefly.FFContext;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.component.build.ComponentBuilder;
import com.inari.firefly.system.FFContextImpl;
import com.inari.firefly.system.FFContextImpl.InitMap;

public class AssetSystemTest {
    
    @Before
    public void clear() {
        Indexer.clear();
    }
    
    @Test
    public void testCreation() {
        FFContext ffContext = getTestFFContext();
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        
        Attributes attrs = new Attributes();
        service.toAttributes( attrs );
        
        assertEquals( 
            "", 
            attrs.toString()
        );
    }
    
    @Test
    public void testIllegalCalls() {
        FFContext ffContext = getTestFFContext();
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        
        try {
            service.loadAsset( "group", "name" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No Asset found for group: group name: name", iae.getMessage() );
        }
        
        try {
            service.loadAssets( "group" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No group found: group", iae.getMessage() );
        }
        
        try {
            service.disposeAsset( "group", "name" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No Asset found for group: group name: name", iae.getMessage() );
        }
        
        try {
            service.disposeAssets( "group" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No group found: group", iae.getMessage() );
        }
        
        try {
            service.deleteAsset( "group", "name" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No Asset found for group: group name: name", iae.getMessage() );
        }
        
        try {
            service.deleteAssets( "group" );
            fail( "Expect IllegalArgumentException here" );
        } catch ( IllegalArgumentException iae ) {
            assertEquals( "No group found: group", iae.getMessage() );
        }
    }

    
    
    @Test
    public void testBuildAsset() {
        FFContext ffContext = getTestFFContext();
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        ComponentBuilder<TestAsset> assetBuilder = service.getAssetBuilder( TestAsset.class );
        assetBuilder
            .setAttribute( TestAsset.NAME, "asset1" )
            .setAttribute( TestAsset.ASSET_GROUP,"group1" )
            .build( 0 );
        
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(0)::name:String=asset1, group:String=group1", 
            attrs.toString() 
        );
        
        attrs.clear();
        assetBuilder
            .setAttribute( TestAsset.NAME, "asset2" )
            .setAttribute( TestAsset.ASSET_GROUP,"group1" )
            .build( 1 );
        assetBuilder
            .setAttribute( TestAsset.NAME, "asset3" )
            .setAttribute( TestAsset.ASSET_GROUP,"group1" )
            .build( 2 );
        assetBuilder
            .setAttribute( TestAsset.NAME, "asset4" )
            .setAttribute( TestAsset.ASSET_GROUP,"group2" )
            .build( 3 );
        assetBuilder
            .setAttribute( TestAsset.NAME, "asset5" )
            .setAttribute( TestAsset.ASSET_GROUP,"group3" )
            .build( 4 );
        
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(0)::name:String=asset1, group:String=group1 " +
            "TestAsset(1)::name:String=asset2, group:String=group1 " +
            "TestAsset(2)::name:String=asset3, group:String=group1 " +
            "TestAsset(3)::name:String=asset4, group:String=group2 " +
            "TestAsset(4)::name:String=asset5, group:String=group3", 
            attrs.toString() 
        );
        attrs.clear();
    }
    
    @Test
    public void testCreateLoadDisposeAndDeleteSingleAsset() {
        FFContext ffContext = getTestFFContext();
        IEventDispatcher eventDispatcher = ffContext.get( FFContext.EVENT_DISPATCHER );
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        service
            .getAssetBuilder( TestAsset.class )
                .setAttribute( TestAsset.NAME, "asset1" )
                .setAttribute( TestAsset.ASSET_GROUP,"group1" )
            .build();
        
        assertEquals( 
            "TestEventDispatcher [events=[AssetEvent [eventType=ASSET_CREATED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset]]]", 
            eventDispatcher.toString() 
        );
        
        attrs.clear();
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(0)::name:String=asset1, group:String=group1", 
            attrs.toString() 
        );
        
        service.loadAsset( "group1", "asset1" );
        assertTrue( service.isAssetLoaded( "group1", "asset1" ) );
        attrs.clear();
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(0)::name:String=asset1, group:String=group1", 
            attrs.toString() 
        );
        assertEquals( 
            "TestEventDispatcher [events=[" +
            "AssetEvent [eventType=ASSET_CREATED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_LOADED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset]]]", 
            eventDispatcher.toString() 
        );
        
        service.disposeAsset( "group1", "asset1" );
        assertFalse( service.isAssetLoaded( "group1", "asset1" ) );
        attrs.clear();
        service.toAttributes( attrs );
        assertEquals( 
            "TestEventDispatcher [events=[" +
            "AssetEvent [eventType=ASSET_CREATED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_LOADED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_DISPOSED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset]]]", 
            eventDispatcher.toString() 
        );
        
        service.deleteAsset( "group1", "asset1" );
        attrs.clear();
        service.toAttributes( attrs );
        assertEquals( 
            "", 
            attrs.toString() 
        );
        assertEquals( 
            "TestEventDispatcher [events=[" +
            "AssetEvent [eventType=ASSET_CREATED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_LOADED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_DISPOSED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset], " +
            "AssetEvent [eventType=ASSET_DELETED, assetType=class com.inari.firefly.asset.AssetSystemTest$TestAsset]]]", 
            eventDispatcher.toString() 
        );
    }
    
    @Test
    public void testPreventingOfUseOfIdTwice() {
        FFContext ffContext = getTestFFContext();
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        service.getAssetBuilder( TestAsset.class )
            .setAttribute( Asset.NAME, "asset2" )
            .setAttribute( Asset.ASSET_GROUP,"group1" )
            .buildAndNext( 1 );
        
        try {
            service.getAssetBuilder( TestAsset.class )
                .setAttribute( Asset.NAME, "asset2" )
                .setAttribute( Asset.ASSET_GROUP,"group3" )
                .buildAndNext( 1 );
            fail( "Exception expected here" );
        } catch ( Exception e ) {
            e.printStackTrace();
            assertEquals( 
                "Error while constructing: class com.inari.firefly.asset.AssetSystemTest$TestAsset", 
                e.getMessage() 
            );
            assertEquals( 
                "The Object index: 1 is already used by another Object!", 
                e.getCause().getMessage() 
           );
        }
        
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(1)::name:String=asset2, group:String=group1", 
            attrs.toString() 
        );
    }
    
    @Test
    public void testDifferentTypesOnDifferentGroups() {
        FFContext ffContext = getTestFFContext();
        AssetSystem service = new AssetSystem();
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        service.getAssetBuilder( TestAsset.class )
            .setAttribute( Asset.NAME, "asset2" )
            .setAttribute( Asset.ASSET_GROUP,"group1" )
            .buildAndNext( 1 )
            .setAttribute( Asset.NAME, "asset3" )
            .setAttribute( Asset.ASSET_GROUP,"group1" )
            .buildAndNext( 2 )
            .setAttribute( Asset.NAME, "asset4" )
            .setAttribute( Asset.ASSET_GROUP,"group2" )
            .buildAndNext( 3 )
            .setAttribute( Asset.NAME, "asset5" )
            .setAttribute( Asset.ASSET_GROUP,"group3" )
            .build( 4 );
        service.getAssetBuilder( TestAsset2.class )
            .setAttribute( Asset.NAME, "asset22" )
            .setAttribute( Asset.ASSET_GROUP,"group1" )
            .buildAndNext( 1 )
            .setAttribute( Asset.NAME, "asset23" )
            .setAttribute( Asset.ASSET_GROUP,"group1" )
            .buildAndNext( 2 )
            .setAttribute( Asset.NAME, "asset24" )
            .setAttribute( Asset.ASSET_GROUP,"group2" )
            .buildAndNext( 3 )
            .setAttribute( Asset.NAME, "asset25" )
            .setAttribute( Asset.ASSET_GROUP,"group3" )
            .build( 4 );
        
        service.toAttributes( attrs );
        assertEquals( 
            "TestAsset(1)::name:String=asset2, group:String=group1 " +
            "TestAsset(2)::name:String=asset3, group:String=group1 " +
            "TestAsset2(1)::name:String=asset22, group:String=group1 " +
            "TestAsset2(2)::name:String=asset23, group:String=group1 " +
            "TestAsset(3)::name:String=asset4, group:String=group2 " +
            "TestAsset2(3)::name:String=asset24, group:String=group2 " +
            "TestAsset(4)::name:String=asset5, group:String=group3 " +
            "TestAsset2(4)::name:String=asset25, group:String=group3", 
            attrs.toString() 
        );
    }

    
    private FFContext getTestFFContext() {
        InitMap initMap = new InitMap();
        initMap.put( FFContext.EVENT_DISPATCHER, EventDispatcherMock.class );
        FFContext result = new FFContextImpl( initMap, true );
        return result;
    }
    
    public static class TestAsset extends Asset {
        
        TestAsset( int assetId ) {
            super( assetId );
        } 

        @Override
        public Set<AttributeKey<?>> attributeKeys() {
            return super.attributeKeys( new HashSet<AttributeKey<?>>() );
        }

        @Override
        public void fromAttributes( AttributeMap attributes ) {
            super.fromAttributes( attributes );
        }

        @Override
        public void toAttributes( AttributeMap attributes ) {
            super.toAttributes( attributes );
        }

        @Override
        public Class<TestAsset> getComponentType() {
            return TestAsset.class;
        }
    }
    
    public static class TestAsset2 extends Asset {
        
        TestAsset2( int assetId ) {
            super( assetId );
        } 

        @Override
        public Set<AttributeKey<?>> attributeKeys() {
            return super.attributeKeys( new HashSet<AttributeKey<?>>() );
        }

        @Override
        public void fromAttributes( AttributeMap attributes ) {
            super.fromAttributes( attributes );
        }

        @Override
        public void toAttributes( AttributeMap attributes ) {
            super.toAttributes( attributes );
        }

        @Override
        public Class<TestAsset2> getComponentType() {
            return TestAsset2.class;
        }
    }

}
