package com.chess.backend.interfaces;

import java.util.List;

/**
 * MultiTypeGetFace class
 *
 * @author alien_roger
 * @created at: 22.08.12 8:08
 */
public interface MultiTypeGetFace<E,T> extends TaskUpdateInterface<T> {

	void updateContacts(List<E> itemsList);
}
