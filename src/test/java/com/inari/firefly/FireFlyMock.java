package com.inari.firefly;

import com.inari.commons.event.IEventDispatcher;
import com.inari.firefly.system.FireFly;

public class FireFlyMock extends FireFly {

    public FireFlyMock() {
        super( new EventDispatcherMock(), new SystemInterfaceMock(), new TestTimer(), new InputMock() );
    }
    
    public FireFlyMock( IEventDispatcher eventDispatcher ) {
        super( eventDispatcher, new SystemInterfaceMock(), new TestTimer(), new InputMock() );
    }

}