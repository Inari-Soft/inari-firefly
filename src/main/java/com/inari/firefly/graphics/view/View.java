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
package com.inari.firefly.graphics.view;

import java.util.Set;

import com.inari.commons.JavaUtils;
import com.inari.commons.geom.PositionF;
import com.inari.commons.geom.Rectangle;
import com.inari.commons.graphics.RGBColor;
import com.inari.commons.lang.indexed.IndexedTypeKey;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.control.Controller;
import com.inari.firefly.graphics.BlendMode;
import com.inari.firefly.graphics.sprite.ESprite;
import com.inari.firefly.system.component.SystemComponent;

public class View extends SystemComponent {
    
    public static final SystemComponentKey<View> TYPE_KEY = SystemComponentKey.create( View.class );
    
    public static final AttributeKey<Rectangle> BOUNDS = AttributeKey.createRectangle( "bounds", View.class );
    public static final AttributeKey<PositionF> WORLD_POSITION = AttributeKey.createPositionF( "worldPosition", View.class );
    public static final AttributeKey<RGBColor> CLEAR_COLOR = AttributeKey.createColor( "clearColor", View.class );
    public static final AttributeKey<RGBColor> TINT_COLOR = AttributeKey.createColor( "tintColor", ESprite.class );
    public static final AttributeKey<BlendMode> BLEND_MODE = AttributeKey.createBlendMode( "blendMode", ESprite.class );
    public static final AttributeKey<Boolean> LAYERING_ENABLED = AttributeKey.createBoolean( "layeringEnabled", View.class );
    public static final AttributeKey<Float> ZOOM = AttributeKey.createFloat( "zoom", View.class );
    public static final AttributeKey<Float> FBO_SCALER = AttributeKey.createFloat( "fboScaler", View.class );
    public static final AttributeKey<String> CONTROLLER_NAME = AttributeKey.createString( "controllerName", View.class );
    public static final AttributeKey<Integer> CONTROLLER_ID = AttributeKey.createInt( "controllerId", View.class );
    public static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = JavaUtils.<AttributeKey<?>>unmodifiableSet(
        BOUNDS,
        WORLD_POSITION,
        CLEAR_COLOR,
        TINT_COLOR,
        BLEND_MODE,
        LAYERING_ENABLED,
        ZOOM,
        FBO_SCALER,
        CONTROLLER_ID
    );
    
    int order;
    boolean active = false;
    
    private boolean layeringEnabled;
    private final Rectangle bounds;
    private final PositionF worldPosition;
    private final RGBColor clearColor;
    private final RGBColor tintColor;
    private BlendMode blendMode;
    private float zoom;
    private float fboScaler;
    private int controllerId;
    
    View( int viewId ) {
        super( viewId );
        layeringEnabled = false;
        bounds = new Rectangle( 0, 0, 1, 1);
        worldPosition = new PositionF( 0, 0 );
        clearColor = new RGBColor( 0f, 0f, 0f, 1f );
        tintColor = new RGBColor( 1f, 1f, 1f, 1f );
        blendMode = BlendMode.NONE;
        zoom = 1.0f;
        fboScaler = 4.0f;
        controllerId = -1;
    }
    
    @Override
    public final IndexedTypeKey indexedTypeKey() {
        return TYPE_KEY;
    }
    
    public final int getOrder() {
        return order;
    }
    
    public final boolean isActive() {
        return active;
    }

    public final boolean isBase() {
        return ( index() == ViewSystem.BASE_VIEW_ID );
    }

    public final boolean isLayeringEnabled() {
        return layeringEnabled;
    }

    public final void setLayeringEnabled( boolean layeringEnabled ) {
        this.layeringEnabled = layeringEnabled;
    }

    public final void setBounds( Rectangle bounds ) {
        this.bounds.x = bounds.x;
        this.bounds.y = bounds.y;
        this.bounds.width = bounds.width;
        this.bounds.height = bounds.height;
    }

    public final Rectangle getBounds() {
        return bounds;
    }
    
    public final void setWorldPosition( PositionF worldPosition ) {
        this.worldPosition.x = worldPosition.x;
        this.worldPosition.y = worldPosition.y;
    }

    public final PositionF getWorldPosition() {
        return worldPosition;
    }
    
    public final void setClearColor( RGBColor clearColor ) {
        this.clearColor.r = clearColor.r;
        this.clearColor.g = clearColor.g;
        this.clearColor.b = clearColor.b;
        this.clearColor.a = clearColor.a;
    }

    public final RGBColor getClearColor() {
        return clearColor;
    }
    
    public final RGBColor getTintColor() {
        return tintColor;
    }

    public final void setTintColor( RGBColor tintColor ) {
        this.tintColor.r = tintColor.r;
        this.tintColor.g = tintColor.g;
        this.tintColor.b = tintColor.b;
        this.tintColor.a = tintColor.a;
    }

    public final BlendMode getBlendMode() {
        return blendMode;
    }

    public final void setBlendMode( BlendMode blendMode ) {
        this.blendMode = blendMode;
    }
    
    public float getZoom() {
        return zoom;
    }

    public final void setZoom( float zoom ) {
        this.zoom = zoom;
    }

    public final float getFboScaler() {
        return fboScaler;
    }

    public final void setFboScaler( float fboScaler ) {
        this.fboScaler = fboScaler;
    }

    public final int getControllerId() {
        return controllerId;
    }

    public final void setControllerId( int controllerId ) {
        this.controllerId = controllerId;
    }

    public final boolean controlledBy( int controllerId ) {
        return this.controllerId == controllerId;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return JavaUtils.unmodifiableSet( super.attributeKeys(), ATTRIBUTE_KEYS );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );

        setBounds( attributes.getValue( BOUNDS, bounds ) );
        setWorldPosition( attributes.getValue( WORLD_POSITION, worldPosition ) );
        setClearColor( attributes.getValue( CLEAR_COLOR, clearColor ) );
        setTintColor( attributes.getValue( TINT_COLOR, tintColor ) );
        blendMode = attributes.getValue( BLEND_MODE, blendMode );
        layeringEnabled = attributes.getValue( LAYERING_ENABLED, layeringEnabled );
        zoom = attributes.getValue( ZOOM, zoom );
        fboScaler = attributes.getValue( FBO_SCALER, fboScaler );
        controllerId = attributes.getIdForName( CONTROLLER_NAME, CONTROLLER_ID, Controller.TYPE_KEY, controllerId );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( BOUNDS, new Rectangle( bounds ) );
        attributes.put( WORLD_POSITION, new PositionF( worldPosition ) );
        attributes.put( CLEAR_COLOR, new RGBColor( clearColor ) );
        attributes.put( TINT_COLOR, new RGBColor( tintColor ) );
        attributes.put( BLEND_MODE, blendMode );
        attributes.put( LAYERING_ENABLED, layeringEnabled );
        attributes.put( ZOOM, zoom );
        attributes.put( FBO_SCALER, fboScaler );
        attributes.put( CONTROLLER_ID, controllerId );
    }

}
