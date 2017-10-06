package com.inari.firefly.physics.animation.timeline;

import java.util.Arrays;
import java.util.Set;

import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.ReadOnlyDynArray;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.physics.animation.IntAnimation;

public final class IntTimelineAnimation extends IntAnimation {
    
    public static final AttributeKey<DynArray<IntTimeTuple>> TIMELINE = AttributeKey.createDynArray( "timeline", IntTimelineAnimation.class, IntTimeTuple.class );
    private static final AttributeKey<?>[] ATTRIBUTE_KEYS = new AttributeKey[] {
        TIMELINE
    };
    
    private final DynArray<IntTimeTuple> timeline;
    
    private long lastUpdate;
    private int currentIndex;

    protected IntTimelineAnimation( int id ) {
        super( id );
        timeline = DynArray.create( IntTimeTuple.class, 10, 10 );
        reset();
    }

    @Override
    public final void reset() {
        super.reset();
        lastUpdate = -1;
        currentIndex = 0;
    }

    public final ReadOnlyDynArray<IntTimeTuple> getTimeline() {
        return timeline;
    }

    public final IntTimelineAnimation setTimeline( final ReadOnlyDynArray<IntTimeTuple> timeline ) {
        this.timeline.clear();
        this.timeline.addAll( timeline );
        this.timeline.trim();
        return this;
    }
    
    public final IntTimelineAnimation addFrame( int value, int time ) {
        return addFrame( new IntTimeTuple( value, time ) );
    }
    
    public final IntTimelineAnimation addFrame( final IntTimeTuple tuple ) {
        timeline.add( tuple );
        timeline.trim();
        return this;
    }
    
    public final void update() {
        long updateTime = context.getTime();
        
        if ( lastUpdate < 0 ) {
            lastUpdate = updateTime;
            return;
        }
        
        if ( updateTime - lastUpdate < timeline.get( currentIndex ).time ) {
            return;
        }
        
        lastUpdate = updateTime;
        currentIndex++;
        
        if ( currentIndex >= timeline.size() ) {
            currentIndex = 0;
            if ( !looping ) {
                finish();
            } 
        } 
    }
    
    public final int getInitValue() {
        if ( timeline == null || timeline.size() < 1 ) {
            return -1;
        }
        
        return getValue( -1, -1 );
    }

    public final int getValue( int component, int currentValue ) {
        return timeline.get( currentIndex ).value;
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( Arrays.asList( ATTRIBUTE_KEYS ) );
        return attributeKeys;
    }
    
    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        timeline.clear();
        if ( attributes.contains( TIMELINE ) ) {
            timeline.addAll( attributes.getValue( TIMELINE ) );
            timeline.trim();
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );

        attributes.put( TIMELINE, timeline );
    }

}
