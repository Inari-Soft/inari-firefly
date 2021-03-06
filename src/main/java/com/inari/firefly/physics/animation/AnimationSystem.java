/*******************************************************************************
 * Copyright (c) 2015 - 2016, Andreas Hefti, inarisoft@yahoo.de 
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
package com.inari.firefly.physics.animation;

import java.util.Set;

import com.inari.commons.JavaUtils;
import com.inari.commons.lang.aspect.IAspects;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.DynArrayRO;
import com.inari.firefly.entity.EntityActivationEvent;
import com.inari.firefly.entity.EntityActivationListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;
import com.inari.firefly.system.component.ComponentSystem;
import com.inari.firefly.system.component.SystemBuilderAdapter;
import com.inari.firefly.system.component.SystemComponent.SystemComponentKey;
import com.inari.firefly.system.component.SystemComponentMap;
import com.inari.firefly.system.component.SystemComponentMap.BuilderListenerAdapter;
import com.inari.firefly.system.external.FFTimer;

public final class AnimationSystem 
    extends 
        ComponentSystem<AnimationSystem>
    implements
        UpdateEventListener,
        EntityActivationListener {
    
    public static final FFSystemTypeKey<AnimationSystem> SYSTEM_KEY = FFSystemTypeKey.create( AnimationSystem.class );
    private static final Set<SystemComponentKey<?>> SUPPORTED_COMPONENT_TYPES = JavaUtils.<SystemComponentKey<?>>unmodifiableSet( 
        Animation.TYPE_KEY
    );

    final SystemComponentMap<Animation> animations;
    final DynArray<AnimationMapping> activeMappings;

    AnimationSystem() {
        super( SYSTEM_KEY );
        animations = new SystemComponentMap<>( 
            this, Animation.TYPE_KEY, 
            new BuilderListenerAdapter<Animation>() {
                public void notifyActivation( int id ) { animations.map.get( id ).activate(); }
                public void notifyDeactivation( int id ) { animations.map.get( id ).reset(); }
            },
            20, 10 
        ); 
        activeMappings = DynArray.create( AnimationMapping.class, 100, 100 );
    }
    
    @Override
    public void init( FFContext context ) {
        super.init( context );
        
        context
            .registerListener( UpdateEvent.TYPE_KEY, this )
            .registerListener( AnimationSystemEvent.TYPE_KEY, this )
            .registerListener( EntityActivationEvent.TYPE_KEY, this );
    }
    
    public final Set<SystemComponentKey<?>> supportedComponentTypes() {
        return SUPPORTED_COMPONENT_TYPES;
    }

    public final Set<SystemBuilderAdapter<?>> getSupportedBuilderAdapter() {
        return JavaUtils.<SystemBuilderAdapter<?>>unmodifiableSet( 
            animations.getBuilderAdapter()
        );
    }

    public final boolean match( IAspects aspects ) {
        return aspects.contains( EAnimation.TYPE_KEY );
    }

    public final void entityActivated( int entityId, IAspects aspects ) {
        DynArrayRO<AnimationMapping> animationMappings = context
            .getEntityComponent( entityId, EAnimation.TYPE_KEY )
            .getAnimationMappings();
        
        for ( int i = 0; i < animationMappings.capacity(); i++ ) {
            final AnimationMapping animationMapping = animationMappings.get( i );
            if ( animationMapping == null ) {
                continue;
            }
            
            animationMapping.entityId = entityId;
            if ( animationMapping.animationId < 0 ) {
                animationMapping.animationId = animations.getId( animationMapping.animationName );
            }
            
            activeMappings.add( animationMapping );
        }
    }

    public final void entityDeactivated( int entityId, IAspects aspects ) {
        for ( int i = 0; i < activeMappings.capacity(); i++ ) {
            AnimationMapping animationMapping = activeMappings.get( i );
            if ( animationMapping == null ) {
                continue;
            }
            
            if ( animationMapping.entityId == entityId ) {
                activeMappings.remove( i );
            }
        }
    }

    final void onAnimationEvent( AnimationSystemEvent event ) {
        if ( !animations.map.contains( event.animationId ) ) {
            return;
        }
        
        final Animation a = animations.get( event.animationId );
        if ( a == null ) {
            return;
        }

        switch ( event.type ) {
            case START_ANIMATION: {
                animations.activate( event.animationId );
            }
            case STOP_ANIMATION: {
                a.stop();
                break;
            }
            case RESUME_ANIMATION: {
                a.resume();
                break;
            }
            case FINISH_ANIMATION: {
                a.finish();
                break;
            }
        }
    }

    public final boolean isActive( int animationId ) {
        return animations.isActive( animationId );
    }

    public final void update( final FFTimer timer ) {
        for ( int i = 0; i < animations.map.capacity(); i++ ) {
            final Animation animation = animations.map.get( i );
            if ( animation == null ) {
                continue;
            }
            
            if ( animations.isActive( i ) ) {
                if ( animation.finished ) {
                    animations.delete( animation.index() );
                    continue;
                }
                
                animation.systemUpdate();
                applyValueAttribute( animation );
                continue;
            }

            if ( animation.startTime > 0 && timer.getTime() >= animation.startTime ) {
                animations.activate( i );
                continue;
            }
        }
    }
    
    private void applyValueAttribute( final Animation animation ) {
        for ( int i = 0; i < activeMappings.capacity(); i++ ) {
            AnimationMapping animationMapping = activeMappings.get( i );
            if ( animationMapping == null || animation.index() != animationMapping.animationId ) {
                continue;
            }
            
            animationMapping
                .adapterKey
                .getAdapterInstance()
                .apply( animationMapping.entityId, animation, context );
        }
    }

    public final float getValue( int animationId, int componentId, float currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }

        return animations
            .getAs( animationId, FloatAnimation.class )
            .getValue( componentId, currentValue );
    }

    public final int getValue( int animationId, int componentId, int currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }
        
        return animations
            .getAs( animationId, IntAnimation.class )
            .getValue( componentId, currentValue );
    }

    public final <V> V getValue( int animationId, int componentId, V currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }

        @SuppressWarnings( "unchecked" )
        ValueAnimation<V> animation = animations.getAs( animationId, ValueAnimation.class );
        return animation.getValue( componentId, currentValue );
    }
    
    public void dispose( FFContext context ) {
        clearSystem();
        
        context
            .disposeListener( UpdateEvent.TYPE_KEY, this )
            .disposeListener( AnimationSystemEvent.TYPE_KEY, this )
            .disposeListener( EntityActivationEvent.TYPE_KEY, this );
    }
    
    public final void clearSystem() {
        animations.clear();
        activeMappings.clear();
    }

}
