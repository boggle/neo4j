/*
 * Copyright (C) 2012 Neo Technology
 * All rights reserved
 */
package org.neo4j.kernel.api.operations;

import java.util.Iterator;
import java.util.Set;

import org.neo4j.kernel.api.ConstraintViolationKernelException;

public interface LabelImplicationSchemaOperations
{
//    CYPHER: ADD :label IMPLIES :other:Labels [| ANY -- in the future]
//    CYPHER: REMOVE :label IMPLIES :other:Labels [| NONE -- removeAll]
//
     void addLabelImplications( long labelId, Set<Long> impliedLabelIds ) throws ConstraintViolationKernelException;

//
//    void removeLabelImplications( long labelId, long... impliedLabelIds ) throws ConstraintViolationKernelException;
//
//    void removeAllLabelImplications( long labelId ) throws ConstraintViolationKernelException;
//
      Set<Long> getDirectLabelImplications( Iterator<Long> labelIds ) throws ConstraintViolationKernelException;

//
//    long[] getTransitiveLabelImplications( long... labelIds ) throws ConstraintViolationKernelException;
}
