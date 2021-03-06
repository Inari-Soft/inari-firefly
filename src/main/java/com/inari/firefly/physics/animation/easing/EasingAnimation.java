package com.inari.firefly.physics.animation.easing;

import java.util.Set;

import com.inari.commons.JavaUtils;
import com.inari.commons.geom.Easing;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.physics.animation.Animation;
import com.inari.firefly.physics.animation.FloatAnimation;
import com.inari.firefly.system.component.SystemComponentType;

public final class EasingAnimation extends FloatAnimation {
    
    public static final SystemComponentType COMPONENT_TYPE = new SystemComponentType( Animation.TYPE_KEY, EasingAnimation.class );
    public static final AttributeKey<EasingData> EASING_DATA = AttributeKey.createEasingData( "easingData", EasingAnimation.class );
    public static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = JavaUtils.<AttributeKey<?>>unmodifiableSet(
        EASING_DATA
    );
    
    private EasingData easingData;
    private boolean inverse;
    private float startValue;
    private float endValue;
    private float offset;

    EasingAnimation( int id ) {
        super( id );
    }

    public final Easing.Type getEasingType() {
        return easingData.easingType;
    }

    public final EasingData getEasingData() {
        return easingData;
    }

    public final void setEasingData( EasingData easingData ) {
        this.easingData = easingData;
    }

    @Override
    public final void activate() {
        super.activate();
        
        inverse = easingData.changeInValue - easingData.startValue < 0f;
        if ( easingData.startValue < 0f ) {
            offset = 0f - easingData.startValue;
        } 
        startValue = easingData.startValue + offset;
        endValue = easingData.changeInValue + offset;
    }

    public final void update() {
        if ( runningTime > easingData.duration ) {
            if ( looping ) {
                reset();
                activate();
            } else {
                stop();
            }
        }
    }
    
    public final float getInitValue() {
        return easingData.getStartValue();
    }

    public final float getValue( int componentId, float currentValue ) {
        if ( inverse ) {
            return ( startValue - easingData.easingType.calc( runningTime , endValue, startValue, easingData.duration ) ) - offset;
        } else {
            return easingData.easingType.calc( runningTime , startValue, endValue, easingData.duration ) - offset;
        }
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return JavaUtils.unmodifiableSet( super.attributeKeys(), ATTRIBUTE_KEYS );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        easingData = attributes.getValue( EASING_DATA, easingData );
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( EASING_DATA, easingData );
    }

}
