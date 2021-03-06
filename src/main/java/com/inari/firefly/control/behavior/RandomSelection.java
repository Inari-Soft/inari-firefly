package com.inari.firefly.control.behavior;

import java.util.Set;

import com.inari.commons.JavaUtils;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.IntBag;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.system.FFContext;
import com.inari.firefly.system.component.SystemComponentType;

public final class RandomSelection extends BehaviorNode {
    
    public static final SystemComponentType COMPONENT_TYPE = new SystemComponentType( BehaviorNode.TYPE_KEY, RandomSelection.class );
    public static final AttributeKey<IntBag> NODE_IDS = AttributeKey.createIntBag( "nodeIds", RandomSelection.class );
    public static final AttributeKey<DynArray<String>> NODE_NAMES = AttributeKey.createDynArray( "nodeNames", RandomSelection.class, String.class );
    public static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = JavaUtils.<AttributeKey<?>>unmodifiableSet(
        NODE_IDS
    );
    
    private final IntBag nodeIds = new IntBag( 10, -1 );

    protected RandomSelection( int index ) {
        super( index );
    }
    
    @Override
    final void nextAction( int entityId, EBehavoir behavior, FFContext context ) {
        int actualSubNodeId = behavior.getSubNodeMapping( index );
        if ( actualSubNodeId >= 0 ) {
            final BehaviorNode subNode = context.getSystemComponent( BehaviorNode.TYPE_KEY, actualSubNodeId );
            subNode.nextAction( entityId, behavior, context );
            if ( behavior.runningActionId >= 0 ) {
                return;
            }
        }
        
        behavior.removeNodeMapping( index );
        // find next action
        int nextIndex = FFContext.RANDOM.nextInt( nodeIds.size() );
        final BehaviorNode subNode = context.getSystemComponent( BehaviorNode.TYPE_KEY, nextIndex );
        subNode.nextAction( entityId, behavior, context );
        if ( behavior.runningActionId >= 0 ) {
            behavior.setNodeMapping( index, subNode.index() );
            return;
        }
    }
    
    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        return JavaUtils.unmodifiableSet( super.attributeKeys(), ATTRIBUTE_KEYS );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        super.fromAttributes( attributes );
        
        if ( attributes.contains( NODE_IDS ) ) {
            nodeIds.clear();
            nodeIds.addAll( attributes.getValue( NODE_IDS ) );
        } 
        if ( attributes.contains( NODE_NAMES ) ) {
            nodeIds.clear();
            nodeIds.addAll( attributes.getIdsForNames( NODE_NAMES, NODE_IDS, BehaviorNode.TYPE_KEY, nodeIds ) );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        
        attributes.put( NODE_IDS, nodeIds );
    }

}
