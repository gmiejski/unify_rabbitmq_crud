package org.miejski.simple.objects.events;

import org.miejski.Modifier;
import org.miejski.simple.objects.ObjectState;

public interface ObjectModifier extends Modifier<ObjectState> {
    ObjectState doSomething(ObjectState obj);

    String ID();
}
