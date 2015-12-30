package com.inari.firefly.graphics.tile;

import com.inari.firefly.entity.ETransform;
import com.inari.firefly.graphics.sprite.ESprite;
import com.inari.firefly.graphics.tile.TileGrid.TileIterator;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.RenderEvent;

public final class NormalFullTileGridRenderer extends TileGridRenderer {
    
    public static final String NAME = "NormalFullTileGridRenderer";
    
    NormalFullTileGridRenderer( int id, FFContext context ) {
        super( id, context );
    }

    @Override
    public final void render( RenderEvent event ) {
        int viewId = event.getViewId();
        int layerId = event.getLayerId();
        
        TileGrid tileGrid = tileGridSystem.getTileGrid( viewId, layerId );
        if ( tileGrid == null || ( tileGrid.getRendererId() >= 0 && tileGrid.getRendererId() != getId() ) ) {
            return;
        }
        
        TileIterator iterator = tileGrid.iterator( event.getClip() );
        if ( iterator == null ) {
            return;
        }

        while( iterator.hasNext() ) {
            int entityId = iterator.next();
            ESprite sprite = entitySystem.getComponent( entityId, ESprite.TYPE_KEY );
            ETransform transform = entitySystem.getComponent( entityId, ETransform.TYPE_KEY );
            
            transformCollector.set( transform );
            transformCollector.xpos += iterator.getWorldXPos();
            transformCollector.ypos += iterator.getWorldYPos();
            
            render( sprite, transform.getParentId() );
        }
    }

}
