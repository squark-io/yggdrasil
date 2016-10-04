package io.squark.dynamicjar.frameworkprovider.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * dynamicjar
 * <p>
 * Created by Erik Håkansson on 2016-05-28.
 * Copyright 2016
 */
@Entity
public class SampleEntity {
    @Id
    @GenericGenerator(name = "generator", strategy = "increment")
    @GeneratedValue(generator = "generator")
    long id;

    @Column
    String testColumn;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTestColumn() {
        return testColumn;
    }

    public void setTestColumn(String testColumn) {
        this.testColumn = testColumn;
    }
}
