package com.airta.action.engine.model.persistence;

/**
 * @author allenyin
 */
public interface IStorageClass {

    Object initiateStorageClassSession();

    boolean addRecord();

    boolean removeRecord();

    Object readRecords();

}
