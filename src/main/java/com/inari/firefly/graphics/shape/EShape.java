package com.inari.firefly.graphics.shape;

import java.util.Set;

import com.inari.commons.JavaUtils;
import com.inari.commons.graphics.RGBColor;
import com.inari.commons.lang.list.DynArray;
import com.inari.commons.lang.list.DynArrayRO;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;
import com.inari.firefly.entity.EntityComponent;
import com.inari.firefly.graphics.BlendMode;
import com.inari.firefly.system.external.ShapeData;

public class EShape extends EntityComponent implements ShapeData {

    public static final EntityComponentTypeKey<EShape> TYPE_KEY = EntityComponentTypeKey.create( EShape.class );

    public static final AttributeKey<Type> SHAPE_TYPE = AttributeKey.create( "shapeType", Type.class, EShape.class );
    public static final AttributeKey<float[]> VERTICES = AttributeKey.create( "vertices", float[].class, EShape.class );
    public static final AttributeKey<DynArray<RGBColor>> COLORS = AttributeKey.createDynArray( "colors", EShape.class, RGBColor.class );
    public static final AttributeKey<Integer> SEGMENTS = AttributeKey.createInt( "segments", EShape.class );
    public static final AttributeKey<Boolean> FILL = AttributeKey.createBoolean( "fill", EShape.class );
    public static final AttributeKey<String> SHADER_ASSET_NAME = AttributeKey.createString( "shaderAssetName", EShape.class );
    public static final AttributeKey<Integer> SHADER_ID = AttributeKey.createInt( "shaderId", EShape.class );
    public static final AttributeKey<BlendMode> BLEND_MODE = AttributeKey.createBlendMode( "blendMode", EShape.class );
    public static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = JavaUtils.<AttributeKey<?>>unmodifiableSet(
        SHAPE_TYPE,
        VERTICES,
        COLORS,
        SEGMENTS,
        FILL,
        BLEND_MODE,
        SHADER_ID
    );
    
    private Type shapeType;
    private float[] vertices;
    private final DynArray<RGBColor> colors = DynArray.create( RGBColor.class, 4, 1 );
    private int segments;
    private boolean fill;
    private BlendMode blendMode;
    private int shaderId;

    public EShape() {
        super( TYPE_KEY );
        resetAttributes();
    }
    
    public final void resetAttributes() {
        shapeType = null;
        vertices = null;
        colors.clear();
        segments = 0;
        fill = false;
        blendMode = BlendMode.NONE;
        shaderId = -1;
    }

    public final Type getShapeType() {
        return shapeType;
    }

    public final void setShapeType( Type shapeType ) {
        this.shapeType = shapeType;
    }

    public final float[] getVertices() {
        return vertices;
    }

    public final void setVertices( float[] vertices ) {
        this.vertices = vertices;
    }

    public final DynArrayRO<RGBColor> getColors() {
        return colors;
    }

    public final int getSegments() {
        return segments;
    }

    public final void setSegments( int segments ) {
        this.segments = segments;
    }

    public final boolean isFill() {
        return fill;
    }

    public final void setFill( boolean fill ) {
        this.fill = fill;
    }

    public final BlendMode getBlendMode() {
        return blendMode;
    }

    public final void setBlendMode( BlendMode blendMode ) {
        this.blendMode = blendMode;
    }

    public final int getShaderId() {
        return shaderId;
    }

    public final void setShaderId( int shaderId ) {
        this.shaderId = shaderId;
    }

    public final Set<AttributeKey<?>> attributeKeys() {
        return ATTRIBUTE_KEYS;
    }

    public final void fromAttributes( AttributeMap attributes ) {
        shapeType = attributes.getValue( SHAPE_TYPE, shapeType );
        vertices = attributes.getValue( VERTICES, vertices );
        if ( attributes.contains( COLORS ) ) {
            colors.clear();
            colors.addAll( attributes.getValue( COLORS, colors ) );
        }
        segments = attributes.getValue( SEGMENTS, segments );
        fill = attributes.getValue( FILL, fill );
        blendMode = attributes.getValue( BLEND_MODE, blendMode );
        shaderId = attributes.getAssetInstanceId( SHADER_ASSET_NAME, SHADER_ID, shaderId );
    }

    public final void toAttributes( AttributeMap attributes ) {
        attributes.put( SHAPE_TYPE, shapeType );
        attributes.put( VERTICES, vertices );
        attributes.put( COLORS, colors );
        attributes.put( SEGMENTS, segments );
        attributes.put( FILL, fill );
        attributes.put( BLEND_MODE, blendMode );
        attributes.put( SHADER_ID, shaderId );
    }

}
