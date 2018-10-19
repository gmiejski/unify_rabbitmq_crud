package org.miejski.simple.objects.events;

import org.miejski.simple.objects.ObjectState;

public interface ObjectModifier {
    ObjectState doSomething(ObjectState obj);

    String ID();
}
