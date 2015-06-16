package com.inari.firefly.sprite.tile;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.lang.aspect.Aspect;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.Disposable;
import com.inari.firefly.FFContext;
import com.inari.firefly.component.build.BaseComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilderFactory;
import com.inari.firefly.component.build.ComponentCreationException;
import com.inari.firefly.entity.IEntitySystem;
import com.inari.firefly.entity.event.AspectedEntityActivationListener;
import com.inari.firefly.entity.event.EntityActivationEvent;
import com.inari.firefly.sprite.tile.TileGrid.TileGridIterator;
import com.inari.firefly.system.ViewSystem;
import com.inari.firefly.system.event.ViewEvent;
import com.inari.firefly.system.event.ViewEvent.Type;
import com.inari.firefly.system.event.ViewEventListener;

public final class TileGridSystem 
    implements 
        ComponentBuilderFactory, 
        ViewEventListener,
        AspectedEntityActivationListener, 
        Disposable {
    
    public static final int VOID_ENTITY_ID = -1;
    
    private final IEventDispatcher eventDispatcher;
    private final IEntitySystem entityProvider;
    private final ViewSystem viewSystem;
    
    private final DynArray<DynArray<TileGrid>> tileGridOfViewsPerLayer = new DynArray<DynArray<TileGrid>>();
    private final DynArray<TileGrid> tileGridOfViews = new DynArray<TileGrid>();
    
    public TileGridSystem( FFContext context ) {
        eventDispatcher = context.get( FFContext.System.EVENT_DISPATCHER );
        entityProvider = context.get( FFContext.System.ENTITY_SYSTEM );
        viewSystem = context.get( FFContext.System.VIEW_SYSTEM );
        
        eventDispatcher.register( EntityActivationEvent.class, this );
        eventDispatcher.register( ViewEvent.class, this );
    }

    @Override
    public final void dispose( FFContext context ) {
        eventDispatcher.unregister( EntityActivationEvent.class, this );
        eventDispatcher.unregister( ViewEvent.class, this );
        
        tileGridOfViewsPerLayer.clear();
        tileGridOfViews.clear();
    }

    @Override
    public final void onEntityActivationEvent( EntityActivationEvent event ) {
        switch ( event.type ) {
            case ENTITY_ACTIVATED: {
                registerEntity( event.entityId, event.aspect );
                break;
            }
            case ENTITY_DEACTIVATED: {
                unregisterEntity( event.entityId, event.aspect );
                break;
            }
        }
    }
    
    @Override
    public final void onViewEvent( ViewEvent event ) {
        if ( event.type == Type.VIEW_DELETED ) {
            deleteAllTileGrid( event.view.indexedId() );
        }
    }

    @Override
    public final boolean match( Aspect aspect ) {
        return aspect.contains( ESingleTile.COMPONENT_TYPE ) || 
               aspect.contains( EMultiTile.COMPONENT_TYPE );
    }
    
    public final boolean hasTileGrid( int viewId, int layerId ) {
        return getTileGrid( viewId, layerId ) != null;
    }
    
    public final TileGrid getTileGrid( int viewId, int layerId ) {
        if ( layerId < 0 ) {
            return tileGridOfViews.get( viewId );
        }
        
        DynArray<TileGrid> tileGridsForView = tileGridOfViewsPerLayer.get( viewId );
        if ( tileGridsForView == null ) {
            return null;
        }
        return tileGridsForView.get( layerId );
    }
    
    public final void deleteAllTileGrid( int viewId ) {
        tileGridOfViews.remove( viewId );
        tileGridOfViewsPerLayer.remove( viewId );
    }
    
    public final void deleteTileGrid( int viewId, int layerId ) {
        if ( layerId < 0 ) {
            tileGridOfViews.remove( viewId );
            return;
        }
        
        DynArray<TileGrid> tileGridsForView = tileGridOfViewsPerLayer.get( viewId );
        if ( tileGridsForView == null ) {
            return;
        }
        tileGridsForView.remove( layerId );
    }
    
    public final TileGridIterator iterator( int viewId, int layerId, Rectangle clip ) {
        TileGrid tileGrid = getTileGrid( viewId, layerId );
        if ( tileGrid == null ) {
            return null;
        }
        return tileGrid.iterator( clip );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public final <C> ComponentBuilder<C> getComponentBuilder( Class<C> type ) {
        if ( type != TileGrid.class ) {
            throw new IllegalArgumentException( "Unsupported IComponent type for this IComponentBuilderFactory: " + type );
        }
        
        return (ComponentBuilder<C>) getTileGridBuilder();
    }

    
    public final BaseComponentBuilder<TileGrid> getTileGridBuilder() {
        return new BaseComponentBuilder<TileGrid>( this ) {
            @Override
            public TileGrid build( int componentId ) {
                TileGrid tileGrid = new TileGrid();
                tileGrid.fromAttributeMap( attributes );
                int viewId = tileGrid.getViewId();
                int layerId = tileGrid.getLayerId();
                if ( viewId < 0 ) {
                    throw new ComponentCreationException( "Missing viewId for TileGrid component. TileGrid must have a viewId" );
                } 
                
                if ( viewSystem.getView( viewId ) == null ) {
                    throw new ComponentCreationException( "The viewId: " + viewId + " doesn't exist as a View in ViewSystem. Create the view first" );
                }
                
                if ( layerId >= 0 ) {
                    if ( !viewSystem.isLayeringEnabled( viewId ) ) {
                        throw new ComponentCreationException( "Layering is not enabled for the View: " + viewId );
                    }
                    
                    if ( !viewSystem.hasLayer( viewId, layerId ) ) {
                        throw new ComponentCreationException( "The Layer with id: " + layerId + " doesn't exists. Create the Layer first." );
                    }
                    
                    DynArray<TileGrid> perLayer = tileGridOfViewsPerLayer.get( viewId );
                    if ( perLayer == null ) {
                        perLayer = new DynArray<TileGrid>();
                        tileGridOfViewsPerLayer.set( viewId, perLayer );
                    }
                    perLayer.set( layerId, tileGrid );
                    return tileGrid;
                } 
                
                tileGridOfViews.set( viewId, tileGrid );
                return tileGrid;
            }
        };
    }
    
    
    private final void registerEntity( int entityId, Aspect entityAspect ) {
        if ( entityAspect.contains( ESingleTile.COMPONENT_TYPE ) ) {
            ESingleTile tile = entityProvider.getComponent( entityId, ESingleTile.COMPONENT_TYPE );
            TileGrid tileGrid = getTileGrid( tile.getViewId(), tile.getLayerId() );
            tileGrid.set( entityId, tile.getGridXPosition(), tile.getGridYPosition() );
        } else {
            EMultiTile tile = entityProvider.getComponent( entityId, EMultiTile.COMPONENT_TYPE );
            TileGrid tileGrid = getTileGrid( tile.getViewId(), tile.getLayerId() );
            int[][] positions = tile.getGridPositions();
            for ( int i = 0; i < positions.length; i++ ) {
                tileGrid.set( entityId, positions[ i ][ 0 ], positions[ i ][ 1 ] );
            }
        }
    }
    
    private final void unregisterEntity( int entityId, Aspect entityAspect ) {
        if ( entityAspect.contains( ESingleTile.COMPONENT_TYPE ) ) {
            ESingleTile tile = entityProvider.getComponent( entityId, ESingleTile.COMPONENT_TYPE );
            TileGrid tileGrid = getTileGrid( tile.getViewId(), tile.getLayerId() );
            tileGrid.reset( tile.getGridXPosition(), tile.getGridYPosition() );
        } else {
            EMultiTile tile = entityProvider.getComponent( entityId, EMultiTile.COMPONENT_TYPE );
            TileGrid tileGrid = getTileGrid( tile.getViewId(), tile.getLayerId() );
            int[][] positions = tile.getGridPositions();
            for ( int i = 0; i < positions.length; i++ ) {
                tileGrid.reset( positions[ i ][ 0 ], positions[ i ][ 1 ] );
            }
        }
    }

    
}
