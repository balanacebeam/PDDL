package io.pddl.router.strategy.value;

import java.util.Arrays;
import java.util.List;

public class ShardingRangeValue<T extends Comparable<?>> extends ShardingValue<T>{

	
	@SuppressWarnings("unchecked")
	public ShardingRangeValue(String column,T lower,T upper){
		super(column);
		setValue(Arrays.asList(lower,upper));
	}
	
	public ShardingRangeValue(String column,List<T> value){
		super(column,value);
	}
	
	public T getLower(){
		return getValue().get(0);
	}
	
	public T getUpper(){
		return getValue().get(1);
	}
}