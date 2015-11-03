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
package com.inari.firefly.animation;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.event.IEventDispatcher;
import com.inari.commons.lang.TypedKey;
import com.inari.commons.lang.list.DynArray;
import com.inari.firefly.animation.event.AnimationEvent;
import com.inari.firefly.animation.event.AnimationEventListener;
import com.inari.firefly.component.Component;
import com.inari.firefly.component.ComponentBuilderHelper;
import com.inari.firefly.component.ComponentSystem;
import com.inari.firefly.component.attr.Attributes;
import com.inari.firefly.component.build.BaseComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilder;
import com.inari.firefly.component.build.ComponentBuilderFactory;
import com.inari.firefly.state.event.WorkflowEvent;
import com.inari.firefly.state.event.WorkflowEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.UpdateEvent;
import com.inari.firefly.system.UpdateEventListener;

public final class AnimationSystem 
    implements
        FFContextInitiable,
        ComponentSystem,
        ComponentBuilderFactory, 
        UpdateEventListener, 
        AnimationEventListener {
    
    public static final TypedKey<AnimationSystem> CONTEXT_KEY = TypedKey.create( "FF_ANIMATION_SYSTEM", AnimationSystem.class );
    
    private FFContext context;
    private IEventDispatcher eventDispatcher;
    
    private final DynArray<Animation> animations;

    AnimationSystem() {
        animations = new DynArray<Animation>();
    }
    
    @Override
    public void init( FFContext context ) {
        this.context = context;
        eventDispatcher = context.getComponent( FFContext.EVENT_DISPATCHER );
        
        eventDispatcher.register( UpdateEvent.class, this );
        eventDispatcher.register( AnimationEvent.class, this );
    }
    
    @Override
    public void dispose( FFContext context ) {
        clear();
        
        eventDispatcher.unregister( UpdateEvent.class, this );
        eventDispatcher.unregister( AnimationEvent.class, this );
    }
    
    public final void clear() {
        for ( Animation animation : animations ) {
            disposeAnimation( animation );
        }
        animations.clear();
    }

    @Override
    public void onAnimationEvent( AnimationEvent event ) {
        Animation animation = animations.get( event.animationId );
        if ( animation == null ) {
            return;
        }
        
        switch ( event.type ) {
            case START_ANIMATION: {
                animation.active = true;
                break;
            }
            case STOP_ANIMATION: {
                animation.active = false;
                animation.finished = true;
                break;
            }
        }
    }

    public final boolean exists( int animationId ) {
        if ( animationId < 0 ) {
            return false;
        }
        return animations.contains( animationId );
    }

    public final boolean isActive( int animationId ) {
        if ( !exists( animationId ) ) {
            return false;
        }

        Animation animation = animations.get( animationId );
        return animation.isActive();
    }
    
    public boolean isFinished( int animationId ) {
        if ( !exists( animationId ) ) {
            return true;
        }

        Animation animation = animations.get( animationId );
        return animation.isFinished();
    }

    @Override
    public final void update( UpdateEvent event ) {
        for ( int i = 0; i < animations.capacity(); i++ ) {
            Animation animation = animations.get( i );
            if ( animation != null ) {
                if ( animation.finished ) {
                    animations.remove( animation.index() );
                    animation.dispose();
                    continue;
                }

                animation.update( event.timer );
            }
        }
    }
    
    public final Animation getAnimation( int animationId ) {
        return animations.get( animationId );
    }
    
    public final int getAnimationId( String animationName ) {
        for ( int i = 0; i < animations.capacity(); i++ ) {
            if ( !animations.contains( i ) ) {
                continue;
            }
            
            Animation anim = animations.get( i );
            if ( animationName.equals( anim.getName() ) ) {
                return anim.getId();
            }
        }
        
        return -1;
    }
    
    public final <A extends Animation> A getAnimation( Class<A> type, int animationId ) {
        Animation animation = animations.get( animationId );
        if ( animation == null ) {
            return null;
        }
        return type.cast( animation );
    }

    public final float getValue( int animationId, int componentId, float currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }

        FloatAnimation animation = getAnimation( FloatAnimation.class, animationId );
        return animation.getValue( componentId, currentValue );
    }

    public final int getValue( int animationId, int componentId, int currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }

        IntAnimation animation = getAnimation( IntAnimation.class, animationId );
        return animation.getValue( componentId, currentValue );
    }

    public final <V> V getValue( int animationId, int componentId, V currentValue ) {
        if ( !isActive( animationId ) ) {
            return currentValue;
        }

        @SuppressWarnings( "unchecked" )
        ValueAnimation<V> animation = getAnimation( ValueAnimation.class, animationId );
        return animation.getValue( componentId, currentValue );
    }
    
    public final void deleteAnimation( int animationId ) {
        if ( !animations.contains( animationId ) ) {
            return;
        }
        
        disposeAnimation( animations.remove( animationId ) );
    }
    
    @Override
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public final <C> ComponentBuilder<C> getComponentBuilder( Class<C> type ) {
        if ( !Animation.class.isAssignableFrom( type ) ) {
            throw new IllegalArgumentException( "The IComponentType is not a subtype of Animation." + type );
        }
        
        return new AnimationBuilder( this, type );
    }
    
    public final <A extends Animation> AnimationBuilder<A> getAnimationBuilder( Class<A> animationType ) {
        return new AnimationBuilder<A>( this, animationType );
    }
    
    private static final Set<Class<?>> SUPPORTED_COMPONENT_TYPES = new HashSet<Class<?>>();
    @Override
    public final Set<Class<?>> supportedComponentTypes() {
        if ( SUPPORTED_COMPONENT_TYPES.isEmpty() ) {
            SUPPORTED_COMPONENT_TYPES.add( Animation.class );
        }
        return SUPPORTED_COMPONENT_TYPES;
    }

    @Override
    public final void fromAttributes( Attributes attributes ) {
        fromAttributes( attributes, BuildType.CLEAR_OLD ); 
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public final void fromAttributes( Attributes attributes, BuildType buildType ) {
        if ( buildType == BuildType.CLEAR_OLD ) {
            clear();
        }
        
        for ( Class<? extends Animation> animationSubType : attributes.getAllSubTypes( Animation.class ) ) {
            new ComponentBuilderHelper<Animation>() {
                @Override
                public Animation get( int id ) {
                    return getAnimation( id );
                }
                @Override
                public void delete( int id ) {
                    deleteAnimation( id );
                }
            }.buildComponents( Animation.class, buildType, (AnimationBuilder<Animation>) getAnimationBuilder( animationSubType ), attributes );
        }
        
    }

    @Override
    public final void toAttributes( Attributes attributes ) {
        for ( Animation animation : animations ) {
            ComponentBuilderHelper.toAttributes( attributes, animation.indexedObjectType(), animation );
        }
    }
    
    private final void disposeAnimation( Animation animation ) {
        if ( animation == null ) {
            return;
        }
        
        if ( animation instanceof WorkflowEventListener ) {
            eventDispatcher.unregister( WorkflowEvent.class, (WorkflowEventListener) animation );
        }
        
        animation.dispose();
    } 
    

    public final class AnimationBuilder<A extends Animation> extends BaseComponentBuilder<A> {
        
        private final Class<A> animationType;
        
        private AnimationBuilder( AnimationSystem system, Class<A> animationType ) {
            super( system );
            this.animationType = animationType;
        }
        
        @Override
        protected A createInstance( Constructor<A> constructor, Object... paramValues ) throws Exception {
            return constructor.newInstance( paramValues );
        }

        @Override
        public A build( int componentId ) {
            attributes.put( Component.INSTANCE_TYPE_NAME, animationType.getName() );
            A animation = getInstance( context, componentId );
            
            animation.fromAttributes( attributes );
            
            animations.set( animation.index(), animation );
            postInit( animation, context );
            
            return animation;
        }
    }

}
