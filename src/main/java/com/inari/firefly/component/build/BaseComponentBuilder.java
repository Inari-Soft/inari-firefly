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
package com.inari.firefly.component.build;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.inari.commons.StringUtils;
import com.inari.firefly.component.Component;
import com.inari.firefly.component.NamedComponent;
import com.inari.firefly.component.attr.Attribute;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.component.attr.ComponentAttributeMap;
import com.inari.firefly.state.event.WorkflowEvent;
import com.inari.firefly.state.event.WorkflowEventListener;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.FFContextInitiable;
import com.inari.firefly.system.FFInitException;

public abstract class BaseComponentBuilder implements ComponentBuilder {
    
    protected final AttributeMap attributes;
    
    protected BaseComponentBuilder() {
        this.attributes = new ComponentAttributeMap();
    }
    
    protected BaseComponentBuilder( AttributeMap attributes ) {
        this.attributes = attributes;
    }
    
    @Override
    public final ComponentBuilder clear() {
        attributes.clear();
        return this;
    }

    @Override
    public final BaseComponentBuilder setAttributes( AttributeMap attributes ) {
        attributes.putAll( attributes );
        return this;
    }

    @Override
    public final AttributeMap getAttributes() {
        return attributes;
    }

    @Override
    public final ComponentBuilder set( AttributeKey<?> key, Object value ) {
        attributes.putUntyped( key, value );
        return this;
    }
    
    @Override
    public final ComponentBuilder set( AttributeKey<Float> key, float value ) {
        attributes.put( key, value );
        return this;
    }
    
    @Override
    public final ComponentBuilder set( AttributeKey<Integer> key, int value ) {
        attributes.put( key, value );
        return this;
    }
    
    @Override
    public final ComponentBuilder set( AttributeKey<Long> key, long value ) {
        attributes.put( key, value );
        return this;
    }
    
    @Override
    public final ComponentBuilder set( AttributeKey<Double> key, double value ) {
        attributes.put( key, value );
        return this;
    }
    
    @Override
    public final ComponentBuilder set( Attribute... attributes ) {
        for ( Attribute attribute : attributes ) {
            this.attributes.putUntyped( attribute.getKey(), attribute.getValue() );
        }
        return this;
    }
    
    

    @Override
    public final int build( Class<?> componentType ) {
        int componentId = getId();
        return doBuild( componentId, componentType );
    }

    public final void build( int componentId, Class<?> componentType ) {
        doBuild( componentId, componentType );
    }
    
    protected abstract int doBuild( int componentId, Class<?> componentType );

    @Override
    public final ComponentBuilder buildAndNext() {
        build();
        return this;
    }

    @Override
    public final ComponentBuilder buildAndNext( int componentId ) {
        build( componentId );
        return this;
    }

    @Override
    public final ComponentBuilder buildAndNext( Class<?> componentType ) {
        build( getId(), componentType );
        attributes.clear();
        return this;
    }


    @Override
    public final ComponentBuilder buildAndNext( int componentId, Class<?> componentType ) {
        build( componentId, componentType );
        attributes.clear();
        return this;
    }
    
    protected <C extends Component> C getInstance( int componentId ) {
        return getInstance( null, componentId );
    }
    
    // TODO find better handling
    @SuppressWarnings( "unchecked" )
    protected <C extends Component> C getInstance( FFContext context, Integer componentId ) {
        String className = attributes.getValue( Component.INSTANCE_TYPE_NAME );
        if ( className == null ) {
            throw new ComponentCreationException( "Missing mandatory attribute " + Component.INSTANCE_TYPE_NAME + " for Component creation" );
        }
        
        Class<C> typeClass = null;
        try {
            typeClass = (Class<C>) Class.forName( className );
        } catch ( Exception e ) {
            throw new ComponentCreationException( "Failed to getComponent class for name: " + className );
        }
        
        if ( componentId == null ) {
            try {
                Constructor<C> constructor = typeClass.getDeclaredConstructor();
                boolean accessible = constructor.isAccessible();
                if ( !accessible ) {
                    constructor.setAccessible( true );
                }
                C instance = createInstance( constructor );
                constructor.setAccessible( accessible );
                return instance;
            } catch ( InvocationTargetException ite ) {
                throw new ComponentCreationException( "Error while constructing: " + typeClass, ite.getCause() );
            } catch ( Throwable t ) {
                if ( context == null ) {
                    throw new ComponentCreationException( "No Component: " + className + " with default constructor found", t );
                }
                try {
                    Constructor<C> constructor = typeClass.getDeclaredConstructor( FFContext.class );
                    boolean accessible = constructor.isAccessible();
                    if ( !accessible ) {
                        constructor.setAccessible( true );
                    }
                    C instance = createInstance( constructor, context );
                    constructor.setAccessible( accessible );
                    return instance;
                } catch ( InvocationTargetException ite ) {
                    throw new ComponentCreationException( "Error while constructing: " + typeClass, ite.getCause() );
                } catch ( Throwable tt ) { 
                    throw new ComponentCreationException( "Error:", tt );
                }
            }
        } else {
            try {
                Constructor<C> constructor = typeClass.getDeclaredConstructor( int.class );
                boolean accessible = constructor.isAccessible();
                if ( !accessible ) {
                    constructor.setAccessible( true );
                }
                C instance = createInstance( constructor, componentId );
                constructor.setAccessible( accessible );
                return instance;
            } catch ( InvocationTargetException ite ) {
                throw new ComponentCreationException( "Error while constructing: " + typeClass, ite.getCause() );
            } catch ( Throwable t ) {
                if ( context == null ) {
                    throw new ComponentCreationException( "No Component: " + className + " with default constructor found", t );
                }
                try {
                    Constructor<C> constructor = typeClass.getDeclaredConstructor( int.class, FFContext.class );
                    boolean accessible = constructor.isAccessible();
                    if ( !accessible ) {
                        constructor.setAccessible( true );
                    }
                    C instance = createInstance( constructor, componentId, context );
                    constructor.setAccessible( accessible );
                    return instance;
                } catch ( InvocationTargetException ite ) {
                    throw new ComponentCreationException( "Error while constructing: " + typeClass, ite.getCause() );
                } catch ( Throwable tt ) { 
                    throw new ComponentCreationException( "Error:", tt );
                }
            }
        }
    }
    
    protected <C extends Component> C createInstance( Constructor<C> constructor, Object... paramValues ) throws Exception {
        boolean hasAccess = constructor.isAccessible();
        if ( !hasAccess ) {
            constructor.setAccessible( true );
        }
        
        C newInstance = constructor.newInstance( paramValues );
        
        if ( !hasAccess ) {
            constructor.setAccessible( false );
        }
        return newInstance;
    }
    
    protected final void checkName( NamedComponent component ) {
        if ( StringUtils.isBlank( component.getName() ) ) {
            throw new FFInitException( "Name is mandatory for component: " + component );
        }
    }
    
    protected final void postInit( Component component, FFContext context ) {
        if ( component instanceof FFContextInitiable ) {
            ( (FFContextInitiable) component ).init( context );
        }
        if ( component instanceof WorkflowEventListener ) {
            context.registerListener( WorkflowEvent.class, (WorkflowEventListener) component );
        }
    }

    protected int getId() {
        int id = -1;
        if ( attributes.getComponentKey() != null && attributes.getComponentKey().getId() >= 0 ) {
            id = attributes.getComponentKey().getId(); 
        }
        return id;
    }

}
