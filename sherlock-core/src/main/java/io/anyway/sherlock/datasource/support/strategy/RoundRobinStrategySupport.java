package io.anyway.sherlock.datasource.support.strategy;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import io.anyway.sherlock.datasource.DataSourceReadStrategy;
import io.anyway.sherlock.datasource.PartitionDataSource;
import io.anyway.sherlock.datasource.support.PartitionDataSourceSupport;

public class RoundRobinStrategySupport implements DataSourceReadStrategy{
	
	private Log logger = LogFactory.getLog(getClass());
	
	private ConcurrentHashMap<String,AtomicLong> hash= new ConcurrentHashMap<String,AtomicLong>();

	@Override
	public DataSource getSlaveDataSource(PartitionDataSource pds) {
		return getDataSourceByCycle(pds,0);
	}
	
	protected DataSource getDataSourceByCycle(PartitionDataSource pds,int w){
		List<DataSource> slaveDataSources= ((PartitionDataSourceSupport)pds).getSlaveDataSources();
		if(CollectionUtils.isEmpty(slaveDataSources)){
			if(logger.isInfoEnabled()){
				logger.info("SlaveDataSource array is empty, will use MasterDataSource");
			}
			return pds.getMasterDataSource();
		}
		AtomicLong next= hash.get(pds.getName());
		if(next== null){
			hash.putIfAbsent(pds.getName(), new AtomicLong(0));
			if((next= hash.get(pds.getName()))==null){
				return getDataSourceByCycle(pds,w);
			}
		}
    	int total= slaveDataSources.size()+ w,
        	idx= (int)(next.getAndIncrement() % total);
    	if(logger.isInfoEnabled()){
			logger.info("SlaveDataSource of PartitionDataSource ["+pds.getName()+"] idx: "+idx);
		}
        return idx< slaveDataSources.size()? slaveDataSources.get(idx): pds.getMasterDataSource();
    }
	
	@Override
	public String getStrategyName(){
		return "roundRobin";
	}

}
