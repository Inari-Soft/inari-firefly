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
package com.inari.firefly.sprite;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.inari.commons.geom.Rectangle;
import com.inari.firefly.asset.Asset;
import com.inari.firefly.component.attr.AttributeKey;
import com.inari.firefly.component.attr.AttributeMap;

public final class SpriteAsset extends Asset {
    
    public static final AttributeKey<Integer> TEXTURE_ID = new AttributeKey<Integer>( "textureId", Integer.class, SpriteAsset.class );
    public static final AttributeKey<Rectangle> TEXTURE_REGION  = new AttributeKey<Rectangle>( "textureRegion", Rectangle.class, SpriteAsset.class );
    private static final Set<AttributeKey<?>> ATTRIBUTE_KEYS = new HashSet<AttributeKey<?>>( Arrays.<AttributeKey<?>>asList( new AttributeKey[] { 
        ASSET_GROUP,
        TEXTURE_ID,
        TEXTURE_REGION
    } ) );
    
    private int[] textureId = new int[ 1 ];
    private final Rectangle textureRegion;
    
    SpriteAsset( int assetId ) {
        super( assetId );
        textureRegion = new Rectangle();
    }
    
    @Override
    public final Class<SpriteAsset> getComponentType() {
        return SpriteAsset.class;
    }

    @Override
    protected final int[] dependsOn() {
        return textureId;
    }
    
    public final int getTextureId() {
        return textureId[ 0 ];
    }

    public final void setTextureId( int textureId ) {
        checkNotAlreadyLoaded();
        this.textureId[ 0 ] = textureId;
    }


    public final Rectangle getTextureRegion() {
        return textureRegion;
    }

    public final void setTextureRegion( Rectangle textureRegion ) {
        checkNotAlreadyLoaded();
        this.textureRegion.x = textureRegion.x;
        this.textureRegion.y = textureRegion.y;
        this.textureRegion.width = textureRegion.height;
        this.textureRegion.height = textureRegion.height;
    }

    @Override
    public final Set<AttributeKey<?>> attributeKeys() {
        Set<AttributeKey<?>> attributeKeys = super.attributeKeys();
        attributeKeys.addAll( ATTRIBUTE_KEYS );
        return super.attributeKeys( attributeKeys );
    }

    @Override
    public final void fromAttributes( AttributeMap attributes ) {
        checkNotAlreadyLoaded();
        super.fromAttributes( attributes );
        textureId[ 0 ] = attributes.getValue( TEXTURE_ID, textureId[ 0 ] );
        
        Rectangle textureRegion = attributes.getValue( TEXTURE_REGION );
        if ( textureRegion != null ) {
            setTextureRegion( textureRegion );
        }
    }

    @Override
    public final void toAttributes( AttributeMap attributes ) {
        super.toAttributes( attributes );
        attributes.put( TEXTURE_ID, textureId[ 0 ] );
        attributes.put( TEXTURE_REGION, new Rectangle( textureRegion ) );
    }

}
