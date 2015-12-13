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
package com.inari.firefly.entity;

import com.inari.commons.lang.indexed.IndexedType;
import com.inari.commons.lang.indexed.IndexedTypeKey;
import com.inari.commons.lang.indexed.Indexer;
import com.inari.firefly.component.Component;

public abstract class EntityComponent implements Component, IndexedType {
    
    public final EntityComponentTypeKey<?> indexedTypeKey;
    
    protected EntityComponent( EntityComponentTypeKey<?> indexedTypeKey ) {
        this.indexedTypeKey = indexedTypeKey;
    }

    public final IndexedTypeKey indexedTypeKey() {
        return indexedTypeKey;
    }
    
    public abstract void resetAttributes();

    
    public static final class EntityComponentTypeKey<C extends EntityComponent> extends IndexedTypeKey {
        
        public final Class<C> type;

        EntityComponentTypeKey( Class<C> indexedType ) {
            super( indexedType );
            type = indexedType;
        }

        @Override
        protected final Class<EntityComponent> baseIndexedType() {
            return EntityComponent.class;
        }
        
        @SuppressWarnings( "unchecked" )
        public static final <C extends EntityComponent> EntityComponentTypeKey<C> create( Class<C> type ) {
            return Indexer.getIndexedTypeKey( EntityComponentTypeKey.class, type );
        }
    }
    
}
