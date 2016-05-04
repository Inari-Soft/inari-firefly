package com.inari.firefly.system.utils;

import com.inari.commons.config.StringConfigurable;
import com.inari.firefly.system.FFContext;

public abstract class Condition implements StringConfigurable {
    
    public abstract boolean check( FFContext context );

    @Override
    public void fromConfigString( String stringValue ) {
    }

    @Override
    public String toConfigString() {
        return "";
    }

}