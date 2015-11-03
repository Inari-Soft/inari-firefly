/*******************************************************************************
 * Copyright (c) 2015, Andreas Hefti, inarisoft@yahoo.de 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/ 
package com.inari.firefly.entity;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.lang.aspect.AspectBitSet;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.animation.AnimationSystem;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.control.Controller;
import com.inari.firefly.control.EController;
import com.inari.firefly.entity.event.EntityActivationEvent;
import com.inari.firefly.entity.event.EntityActivationListener;
import com.inari.firefly.renderer.sprite.ESprite;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFTimer;


public abstract class EntityController extends Controller implements EntityActivationListener {
    
    protected final int COMPONENT_ID_ECONTROLLER = Indexer.getIndexForType( EController.class, EntityComponent.class );
    protected final int COMPONENT_ID_ETRANSFORM = Indexer.getIndexForType( ETransform.class, EntityComponent.class );
    protected final int COMPONENT_ID_ESPRITE = Indexer.getIndexForType( ESprite.class, EntityComponent.class );
    
    protected IEventDispatcher eventDispatcher;
    protected EntitySystem entitySystem;
    protected AnimationSystem animationSystem;
    
    protected EntityController( int id, FFContext context ) {
        super( id );
        eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        entitySystem = context.getComponent( EntitySystem.CONTEXT_KEY );
        animationSystem = context.getComponent( AnimationSystem.CONTEXT_KEY );
        
        eventDispatcher.register( EntityActivationEvent.class, this );
    }

    @Override
    public final void dispose( FFContext context ) {
        eventDispatcher.unregister( EntityActivationEvent.class, this );
    }
    
    @Override
    public final boolean match( AspectBitSet aspect ) {
        return aspect.contains( COMPONENT_ID_ECONTROLLER );
    }

    @Override
    public final void onEntityActivationEvent( EntityActivationEvent event ) {
        switch ( event.eventType ) {
            case ENTITY_ACTIVATED: {
                if ( hasControllerId( event.entityId ) ) {
                    componentIds.add( event.entityId );
                }
                break;
            } 
            case ENTITY_DEACTIVATED: {
                componentIds.remove( event.entityId );
                break;
            }
            default: {}
        }
    }

    @Override
    public final void update( final FFTimer timer ) {
        for ( int i = 0; i < componentIds.length(); i++ ) {
            int entityId = componentIds.get( i );
            if ( entityId >= 0 ) {
                update( timer, entityId );
            }
        }
    }
    
    public abstract AttributeKey<?>[] getControlledAttribute();
    
    protected abstract void update( final FFTimer timer, int entityId );
    
    private final boolean hasControllerId( int entityId ) {
        EController controllerComponent = entitySystem.getComponent( entityId, COMPONENT_ID_ECONTROLLER );
        if ( controllerComponent == null ) {
            return false;
        }
        
        return controllerComponent.controlledBy( indexedId );
    }

}
