package com.litesuits.orm.db.model;

import com.litesuits.orm.db.enums.Relation;

import java.lang.reflect.Field;

/**
 * 映射关系
 * @author MaTianyu
 * 2014-3-7下午11:16:19
 */
public class MapProperty extends Property {
	private static final long serialVersionUID = 1641409866866426637L;
	public static final String PRIMARYKEY = " PRIMARY KEY ";
	public static final String FIELD = "field";
	public Relation relation;
	public String mappingName;

	public MapProperty(Property p, Relation relation, String mappingName) {
		this(p.column, p.field, relation, mappingName);
	}

	private MapProperty(String column, Field field, Relation relation, String mappingName) {
		super(column, field);
		this.relation = relation;
		this.mappingName = mappingName;
	}

    public boolean isToMany(){
        return relation == Relation.ManyToMany || relation == Relation.OneToMany;
    }

    public boolean isToOne(){
        return relation == Relation.ManyToOne || relation == Relation.OneToOne;
    }

}
