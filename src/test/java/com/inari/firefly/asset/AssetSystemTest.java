package com.inari.firefly.asset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.inari.firefly.FFTest;
import com.inari.firefly.asset.AssetSystem.AssetBuilder;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.utils.Disposable;

public class AssetSystemTest extends FFTest {
    
    @Test
    public void testCreation() {
        ffContext.getSystem( AssetSystem.SYSTEM_KEY );
        
        Attributes attrs = new Attributes();
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        
        assertEquals( 
            "", 
            attrs.toString()
        );
    }

    @Test
    public void testBuildAsset() {
        AssetSystem service = ffContext.getSystem( AssetSystem.SYSTEM_KEY );
        
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        AssetBuilder assetBuilder = service.getAssetBuilder();
        assetBuilder
            .set( TestAsset.NAME, "asset1" )
            .build( 0, TestAsset.class );
        
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(0)::name:String=asset1", 
            attrs.toString() 
        );
        
        attrs.clear();
        assetBuilder
            .set( TestAsset.NAME, "asset2" )
            .build( 1, TestAsset.class );
        assetBuilder
            .set( TestAsset.NAME, "asset3" )
            .build( 2, TestAsset.class );
        assetBuilder
            .set( TestAsset.NAME, "asset4" )
            .build( 3, TestAsset.class );
        assetBuilder
            .set( TestAsset.NAME, "asset5" )
            .build( 4, TestAsset.class );
        
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(0)::name:String=asset1 "
            + "SystemComponent:Asset(1)::name:String=asset2 "
            + "SystemComponent:Asset(2)::name:String=asset3 "
            + "SystemComponent:Asset(3)::name:String=asset4 "
            + "SystemComponent:Asset(4)::name:String=asset5", 
            attrs.toString() 
        );
        attrs.clear();
    }
    
    @Test
    public void testCreateLoadDisposeAndDeleteSingleAsset() {
        AssetSystem service = ffContext.getSystem( AssetSystem.SYSTEM_KEY );
        
        service.init( ffContext );
        Attributes attrs = new Attributes();
        
        service
            .getAssetBuilder()
                .set( TestAsset.NAME, "asset1" )
            .build( TestAsset.class );
        
        assertEquals( 
            "EventLog [events=[AssetEvent [eventType=ASSET_CREATED, assetId=0]]]", 
            eventLog.toString() 
        );
        
        attrs.clear();
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(0)::name:String=asset1", 
            attrs.toString() 
        );
        
        service.loadAsset( "asset1" );
        assertTrue( service.isLoaded( "asset1" ) );
        attrs.clear();
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(0)::name:String=asset1", 
            attrs.toString() 
        );
        assertEquals( 
            "EventLog [events=["
            + "AssetEvent [eventType=ASSET_CREATED, assetId=0], "
            + "AssetEvent [eventType=ASSET_LOADED, assetId=0]]]", 
            eventLog.toString() 
        );
        
        service.disposeAsset( "asset1" );
        assertFalse( service.isLoaded( "asset1" ) );
        attrs.clear();
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "EventLog [events=["
            + "AssetEvent [eventType=ASSET_CREATED, assetId=0], "
            + "AssetEvent [eventType=ASSET_LOADED, assetId=0], "
            + "AssetEvent [eventType=ASSET_DISPOSED, assetId=0]]]", 
            eventLog.toString() 
        );
        
        service.deleteAsset( "asset1" );
        attrs.clear();
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "", 
            attrs.toString() 
        );
        assertEquals( 
            "EventLog [events=["
            + "AssetEvent [eventType=ASSET_CREATED, assetId=0], "
            + "AssetEvent [eventType=ASSET_LOADED, assetId=0], "
            + "AssetEvent [eventType=ASSET_DISPOSED, assetId=0], "
            + "AssetEvent [eventType=ASSET_DELETED, assetId=0]]]", 
            eventLog.toString() 
        );
    }
    
    @Test
    public void testPreventingOfUseOfIdTwice() {
        AssetSystem service = ffContext.getSystem( AssetSystem.SYSTEM_KEY );
        Attributes attrs = new Attributes();
        
        service.getAssetBuilder()
            .set( Asset.NAME, "asset2" )
            .buildAndNext( 1, TestAsset.class );
        
        try {
            service.getAssetBuilder()
                .set( Asset.NAME, "asset2" )
                .buildAndNext( 1, TestAsset.class );
            fail( "Exception expected here" );
        } catch ( Exception e ) {
            e.printStackTrace();
            assertEquals( 
                "Error while constructing: class com.inari.firefly.asset.AssetSystemTest$TestAsset", 
                e.getMessage() 
            );
            assertEquals( 
                "The Object: com.inari.firefly.asset.Asset index: 1 is already used by another Object!", 
                e.getCause().getMessage() 
           );
        }
        
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(1)::name:String=asset2", 
            attrs.toString() 
        );
    }
    
    @Test
    public void testDifferentTypes() {
        AssetSystem service = ffContext.getSystem( AssetSystem.SYSTEM_KEY );
        
        Attributes attrs = new Attributes();
        
        service.getAssetBuilder()
            .set( Asset.NAME, "asset2" )
            .buildAndNext( 1, TestAsset.class )
            .set( Asset.NAME, "asset3" )
            .buildAndNext( 2, TestAsset.class )
            .set( Asset.NAME, "asset4" )
            .buildAndNext( 3, TestAsset.class )
            .set( Asset.NAME, "asset5" )
            .build( 4, TestAsset.class );
        service.getAssetBuilder()
            .set( Asset.NAME, "asset22" )
            .buildAndNext( 21, TestAsset2.class )
            .set( Asset.NAME, "asset23" )
            .buildAndNext( 22, TestAsset2.class )
            .set( Asset.NAME, "asset24" )
            .buildAndNext( 23, TestAsset2.class )
            .set( Asset.NAME, "asset25" )
            .build( 24, TestAsset2.class );
        
        ffContext.toAttributes( attrs, Asset.TYPE_KEY );
        assertEquals( 
            "SystemComponent:Asset(1)::name:String=asset2 "
            + "SystemComponent:Asset(2)::name:String=asset3 "
            + "SystemComponent:Asset(3)::name:String=asset4 "
            + "SystemComponent:Asset(4)::name:String=asset5 "
            + "SystemComponent:Asset(21)::name:String=asset22 "
            + "SystemComponent:Asset(22)::name:String=asset23 "
            + "SystemComponent:Asset(23)::name:String=asset24 "
            + "SystemComponent:Asset(24)::name:String=asset25", 
            attrs.toString() 
        );
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
        public Disposable load( FFContext context ) {
            return this;
        }

        @Override
        public void dispose( FFContext context ) {

        }

        @Override
        public int getInstanceId( int index ) {
            return 0;
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
        public Disposable load( FFContext context ) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void dispose( FFContext context ) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public int getInstanceId( int index ) {
            // TODO Auto-generated method stub
            return 0;
        }

        
    }

}
